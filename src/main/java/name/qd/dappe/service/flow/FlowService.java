package name.qd.dappe.service.flow;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.bouncycastle.util.encoders.Hex;
import org.onflow.protobuf.entities.AccountOuterClass.AccountKey;
import org.onflow.sdk.Flow;
import org.onflow.sdk.FlowAccessApi;
import org.onflow.sdk.FlowAccount;
import org.onflow.sdk.FlowAccountKey;
import org.onflow.sdk.FlowAddress;
import org.onflow.sdk.FlowArgument;
import org.onflow.sdk.FlowId;
import org.onflow.sdk.FlowPublicKey;
import org.onflow.sdk.FlowScript;
import org.onflow.sdk.FlowTransaction;
import org.onflow.sdk.FlowTransactionProposalKey;
import org.onflow.sdk.FlowTransactionResult;
import org.onflow.sdk.FlowTransactionStatus;
import org.onflow.sdk.HashAlgorithm;
import org.onflow.sdk.SignatureAlgorithm;
import org.onflow.sdk.Signer;
import org.onflow.sdk.cadence.StringField;
import org.onflow.sdk.crypto.Crypto;
import org.onflow.sdk.crypto.KeyPair;
import org.onflow.sdk.crypto.PrivateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.UserAddress;
import name.qd.dappe.repository.UserAddressRepository;

@Service
public class FlowService {
	private static Logger logger = LoggerFactory.getLogger(FlowService.class);
	
	public static String CHAIN = "FLOW";
	
	private String SERVICE_PRIVATE_KEY = "c547883651c7e1a866372d2e9d8ffc3204080676136866b4a852101d08f92c04";
	private String SERVICE_ADDRESS = "0xc12c21f4c961c434";
	private FlowAddress flowAddress = new FlowAddress(SERVICE_ADDRESS);
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private UserAddressRepository userAddressRepository;
	
	private FlowAccessApi flowAccessApi;
	
	private int confirmCount;

	@PostConstruct
	private void init() {
		String nodeUrl = configManager.getNodeUrl(CHAIN);
		String[] url = nodeUrl.split(":");
		flowAccessApi = Flow.newAccessApi(url[0], Integer.parseInt(url[1]));
	}
	
	public UserAddress createAddress() {
		FlowAccountKey payerAccountKey = getAccountKey(flowAddress);
		
		KeyPair keyPair = Crypto.generateKeyPair();
		
		FlowAccountKey newAccountPublicKey = new FlowAccountKey(
                0,
                new FlowPublicKey(keyPair.getPublic().getHex()),
                SignatureAlgorithm.ECDSA_P256,
                HashAlgorithm.SHA3_256,
                1,
                0,
                false);
		
		FlowTransaction flowTransaction = new FlowTransaction(
                new FlowScript(loadScript("create_account.cdc")),
                Arrays.asList(new FlowArgument(new StringField(Hex.toHexString(newAccountPublicKey.getEncoded())))),
                getLatestBlockID(),
                100L,
                new FlowTransactionProposalKey(
                		flowAddress,
                        payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()),
                flowAddress,
                Arrays.asList(flowAddress),
                new ArrayList<>(),
                new ArrayList<>());

		PrivateKey privateKey = Crypto.decodePrivateKey(SERVICE_PRIVATE_KEY);
        Signer signer = Crypto.getSigner(privateKey, payerAccountKey.getHashAlgo());
        flowTransaction = flowTransaction.addPayloadSignature(flowAddress, 0, signer);
        flowTransaction = flowTransaction.addEnvelopeSignature(flowAddress, 1, signer);

        FlowId txID = flowAccessApi.sendTransaction(flowTransaction);
        System.out.println(txID.getBase16Value());
        FlowTransactionResult txResult = waitForSeal(txID);

        FlowAddress newFlowAddress = getAccountCreatedAddress(txResult);
        
        UserAddress userAddress = new UserAddress();
        userAddress.setAddress(newFlowAddress.getStringValue());
        userAddress.setChain(CHAIN);
        userAddress.setPkey(keyPair.getPrivate().getHex());
        userAddress.setPublicKey(keyPair.getPublic().getHex());
        
        userAddressRepository.save(userAddress);
        
        return userAddress;
	}
	
	public String getBalance(String address) {
		FlowAddress flowAddress = new FlowAddress(address);
		FlowAccount flowAccount = flowAccessApi.getAccountAtLatestBlock(flowAddress);
		return flowAccount.getBalance().toPlainString();
	}
	
	private FlowAccountKey getAccountKey(FlowAddress address) {
        FlowAccount flowAccount = getAccount(address);
        AccountKey accountKey = flowAccount.builder().addKeysBuilder().build();
        flowAccount.builder().addKeys(accountKey);
        return flowAccount.getKeys().get(flowAccount.getKeys().size() - 1);
    }
	
	private FlowAccount getAccount(FlowAddress address) {
        return flowAccessApi.getAccountAtLatestBlock(address);
    }
	
	private byte[] loadScript(String name) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);) {
            return is.readAllBytes();
        } catch (IOException e) {
            logger.error("load script failed", e);
        }
        return null;
    }
	
	private FlowId getLatestBlockID() {
        return flowAccessApi.getLatestBlockHeader().getId();
    }
	
	private FlowTransactionResult waitForSeal(FlowId txID) {
        FlowTransactionResult txResult;

        while(true) {
            txResult = getTransactionResult(txID);
            if (txResult.getStatus().equals(FlowTransactionStatus.SEALED)) {
                return txResult;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
	
	private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        if (!txResult.getStatus().equals(FlowTransactionStatus.SEALED)
            || txResult.getErrorMessage().length() > 0) {
            return null;
        }

        String rez = txResult
                .getEvents()
                .get(0)
                .getEvent()
                .getValue()
                .getFields()[0]
                .getValue()
                .getValue().toString();
        return new FlowAddress(rez.substring(2));
    }
	
	private FlowTransactionResult getTransactionResult(FlowId txID) {
		return flowAccessApi.getTransactionResultById(txID);
    }
}
