package name.qd.ws.service.solana;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.TweetNaclFast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert.Unit;

import com.google.common.collect.Lists;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.UserAddress;
import name.qd.ws.dto.UserTransaction;
import name.qd.ws.repository.UserAddressRepository;
import name.qd.ws.service.eth.ETHService;

@Service
public class SolanaService {
	private static Logger logger = LoggerFactory.getLogger(SolanaService.class);
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private UserAddressRepository userAddressRepository;
	
	private RpcClient rpcClient;
	
	private SolanaEventListener solanaEventListener;
	
	@PostConstruct
	private void init() {
		String nodeUrl = configManager.getNodeUrl(SupportedChain.SOL.name());
		
		// TODO 之後再改config格式
		for(Cluster cluster : Cluster.values()) {
			if(nodeUrl.toUpperCase().contains(cluster.name().toUpperCase())) {
				rpcClient = new RpcClient(cluster);
			}
		}
		
		solanaEventListener = new SolanaEventListener();
	}
	
	public UserAddress createAddress() {
		UserAddress userAddress = new UserAddress();
		
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair();
		
		String address = Base58.encode(keyPair.getPublicKey());
		String pkey = Base58.encode(keyPair.getSecretKey());
		
		userAddress.setChain(SupportedChain.SOL.name());
		userAddress.setAddress(address);
		userAddress.setPublicKey(address);
		userAddress.setPkey(pkey);
		
		userAddress = userAddressRepository.save(userAddress);

		return userAddress;
	}
	
	public String getBalance(String address, String currency) {
		PublicKey publicKey = new PublicKey(address);
		
		try {
			long balance = rpcClient.getApi().getBalance(publicKey);
			return new BigDecimal(balance).divide(configManager.getContractDecimal(SupportedChain.SOL.name(), currency)).toPlainString();
		} catch (RpcException e) {
			logger.error("get balance from solana chain failed, address: {}, currency: {}", address, currency, e);
		}
		return "0";
	}
	
	public UserTransaction transferSOL(int id, String toAddress, long amount) {
		UserTransaction userTransaction = new UserTransaction();
		
		Optional<UserAddress> optional = userAddressRepository.findById(id);
		if(optional.isEmpty()) {
			return userTransaction;
		}
		UserAddress userAddress = optional.get();
				
		Account account = new Account(Base58.decode(userAddress.getPkey()));
		List<Account> lst = new ArrayList<>();
		lst.add(account);
		
		Transaction transaction = new Transaction();
		transaction.addInstruction(SystemProgram.transfer(new PublicKey(userAddress.getAddress()), new PublicKey(toAddress), amount));
		
		try {
			rpcClient.getApi().sendAndConfirmTransaction(transaction, lst, solanaEventListener);
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return userTransaction;		
	}
}
