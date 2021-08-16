package name.qd.dappe;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.event.ListSelectionEvent;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Dapp {
//	Generate new private key: 1209bd9dc9ebfdb082da97da87bd1cef82d4783d636d88f3f55b87cf40041914
//	public key: d19554ca05ce7f31427632e05111d7bc15ebdc2bd346dbed264a30d08a57c079088c105719379bc2baa46caf0cd19b5af67719ca4d2b6b3ddb545f02ff50c08
//	Get address by public key: 0xde2a1b877eba26a4f18822d27522d8eedbac76b7
	
	private static final String TEST_PKEY = "ee057a93a5327dbca303b745994de5246b36757c646c734e107d973de2908f33";
	private static final String TEST_PKEY2 = "fcf4e9da729d2d36bfb609402902ec33008c2fbbfd1b661338d13278ee3cce1f";
	private static final String CONTRACT_ADDRESS = "0xeCfab06fe2420EB0Dda8dA01093511963e72772c";
	private static final String CONTRACT_ABI = "[ { \"constant\": true, \"inputs\": [], \"name\": \"name\", \"outputs\": [ { \"name\": \"\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_spender\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"approve\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"totalSupply\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_from\", \"type\": \"address\" }, { \"name\": \"_to\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"transferFrom\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"decimals\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint8\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"burn\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [ { \"name\": \"\", \"type\": \"address\" } ], \"name\": \"balanceOf\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_from\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"burnFrom\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"symbol\", \"outputs\": [ { \"name\": \"\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_to\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"transfer\", \"outputs\": [ { \"name\": \"\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_spender\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" }, { \"name\": \"_extraData\", \"type\": \"bytes\" } ], \"name\": \"approveAndCall\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [ { \"name\": \"\", \"type\": \"address\" }, { \"name\": \"\", \"type\": \"address\" } ], \"name\": \"allowance\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"inputs\": [ { \"name\": \"initialSupply\", \"type\": \"uint256\" }, { \"name\": \"tokenName\", \"type\": \"string\" }, { \"name\": \"tokenSymbol\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"constructor\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"from\", \"type\": \"address\" }, { \"indexed\": true, \"name\": \"to\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Transfer\", \"type\": \"event\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"owner\", \"type\": \"address\" }, { \"indexed\": true, \"name\": \"spender\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Approval\", \"type\": \"event\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"from\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Burn\", \"type\": \"event\" } ]";
	private static final String TEST_ADDRESS = "0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5";
	private static final String TEST_ADDRESS2 = "0x3e694925f348e0887D4a79082FAceF9f95165Fa0";
	private Web3j web3j;
	private Admin admin;
	private Credentials credentials;
	private ObjectMapper objectMapper = new ObjectMapper();

	private Dapp() {
		web3j = Web3j.build(new HttpService());
		admin = Admin.build(new HttpService());
		credentials = getCredentialsFromPrivateKey();
		
		try {
//			getVersion();
//			getPersonalListAccounts();
//			getPersonalAccount();
//			getNetVersion();
//			getChainId();
//			getBalance(TEST_ADDRESS);
//			getBalance(TEST_ADDRESS2);
//			getBalance(CONTRACT_ADDRESS);
//			getTokenBalance(TEST_ADDRESS, CONTRACT_ADDRESS);
//			getTokenBalance(TEST_ADDRESS2, CONTRACT_ADDRESS);
//			getDecimal(CONTRACT_ADDRESS);
//			getAddress();
//			getAddress2();
//			getAddressTxnHash(TEST_ADDRESS);
//			createAddress();
//			transEth(credentials, TEST_ADDRESS2, 0.11);
//			transToken(credentials, TEST_ADDRESS2, CONTRACT_ADDRESS, "123");
//			transToken2(credentials, TEST_ADDRESS2, CONTRACT_ADDRESS, 123);
//			getSpecificBlock(10778049);
			// trans eth record 10777555
			getSpecificBlock(10777555);
			// trans SC record 10846160
			getSpecificBlock(10846160);
//			subscribeEvent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getVersion() throws IOException, InterruptedException, ExecutionException {
		Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
		String clientVersion = web3ClientVersion.getWeb3ClientVersion();
		System.out.println("version:" + clientVersion);
	}

	private void getPersonalListAccounts() throws InterruptedException, ExecutionException {
		PersonalListAccounts personalListAccounts = admin.personalListAccounts().sendAsync().get();
		System.out.println("personalListAccounts:" + personalListAccounts.getAccountIds());
	}

	private void getPersonalAccount() throws IOException {
		PersonalUnlockAccount personalUnlockAccount = admin
				.personalUnlockAccount("0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5", "").send();
		System.out.println("getPersonalAccount:" + personalUnlockAccount.accountUnlocked());
	}
	
	private void getNetVersion() throws IOException {
		System.out.println("getNetVersion:" + web3j.netVersion().send().getNetVersion());
	}
	
	private void getChainId() throws IOException {
		System.out.println("getChainId:" + web3j.ethChainId().send().getChainId());
	}

	private Credentials getCredentialsFromPrivateKey() {
		return Credentials.create(TEST_PKEY);
	}
	
	private void getBalance(String address) throws IOException {
		EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		System.out.println("Balance:" + ethGetBalance.getBalance());
		System.out.println("RawResponse:" + ethGetBalance.getRawResponse());
		System.out.println("Result:" + ethGetBalance.getResult());
	}
	
	private void getLatestBlock() throws IOException {
		EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
		System.out.println(ethBlock.getBlock().getNumber());
	}
	
	private void getSpecificBlock(long blockNumber) throws IOException {
		EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), true).send();
		List<TransactionResult> lst = ethBlock.getBlock().getTransactions();
		for(TransactionResult transactionResult : lst) {
			TransactionObject transaction = (TransactionObject) transactionResult;
			
			if("0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5".equalsIgnoreCase(transaction.getFrom())) {
				if("0x3e694925f348e0887d4a79082facef9f95165fa0".equalsIgnoreCase(transaction.getTo()) ) {
					System.out.println("Transfer eth:");
					
					EthTransaction tran = web3j.ethGetTransactionByHash(transaction.getHash()).send();
					System.out.println(objectMapper.writeValueAsString(tran));
					System.out.println(new BigDecimal(tran.getResult().getValue()).divide(Unit.ETHER.getWeiFactor()));
				}
				EthGetTransactionReceipt tx = web3j.ethGetTransactionReceipt(transaction.getHash()).send();
				TransactionReceipt transactionReceipt = tx.getResult();
				System.out.println("tx -------------");
				System.out.println(objectMapper.writeValueAsString(tx));
				System.out.println(objectMapper.writeValueAsString(transactionReceipt));
				String input = transaction.getInput();
				System.out.println("input:" + input);
			} else if("0x3e694925f348e0887d4a79082facef9f95165fa0".equalsIgnoreCase(transaction.getFrom())) {
				System.out.println("input: " + transaction.getInput());
				String addr = "0x" + transaction.getInput().substring(34, 74);
				if(addr.equalsIgnoreCase("0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5")) {
					String hex = transaction.getInput().substring(74);
					System.out.println(hex);
					System.out.println(new BigInteger(hex.replaceFirst("^0+(?!$)", ""), 16));
				}
			}
		}
	}
	
	public String hexStringToString(String hex) {
	    int l = hex.length();
	    byte[] data = new byte[l / 2];
	    for (int i = 0; i < l; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	                + Character.digit(hex.charAt(i + 1), 16));
	    }
	    return new String(data);
	}
	
	private void getEthAccount() {
//		web3j.ethAccounts().send();
	}
	
	private void getDecimal(String contractAddress) {
		Function function = new Function("decimals", Collections.emptyList(), Arrays.asList(new TypeReference<Uint256>() {}));
		String encodedFunction = FunctionEncoder.encode(function);
		
		try {
			EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(contractAddress, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST).send();
			System.out.println("Decimals: " + Numeric.toBigInt(response.getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private BigInteger getLastGas() {
		try {
			EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
			System.out.println("Gas:" + ethGasPrice.getGasPrice());
			return ethGasPrice.getGasPrice();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return DefaultGasProvider.GAS_LIMIT;
	}
	
	private BigInteger getNonce(String fromAddress) throws IOException {
		BigInteger nonce = null;
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send();
        if (ethGetTransactionCount != null) {
        	nonce = ethGetTransactionCount.getTransactionCount();
        }
        return nonce;
	}
	
	private void getTokenBalance(String address, String contractAddress) {
		Function function = new Function("balanceOf", Arrays.asList(new Address(address)), Arrays.asList(new TypeReference<Uint256>() {}));
		String encodedFunction = FunctionEncoder.encode(function);

		try {
			EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(address, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST).send();
			System.out.println("Token balance: " + Numeric.toBigInt(response.getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void transToken2(Credentials credentialsFrom, String addressTo, String contractAddress, long amount) {
		BigInteger gas = getLastGas();
		BigInteger gasLimit = BigInteger.valueOf(42000);
		ERC20 erc20 = ERC20.load(contractAddress, web3j, credentialsFrom, new StaticGasProvider(gas, gasLimit));
		System.out.println("==================================================");
		try {
			TransactionReceipt transactionReceipt = erc20.transfer(addressTo, BigInteger.valueOf(amount)).send();
			System.out.println("==================================================");
			System.out.println(objectMapper.writeValueAsString(transactionReceipt));
			System.out.println("==================================================");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getAddress() {
		System.out.println("Address: " + credentials.getAddress());
	}
	
	private void getAddress2() {
		ECKeyPair ecKeyPair = credentials.getEcKeyPair();
		
		BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
		String privatekeyInHex = privateKeyInDec.toString(16);
		System.out.println("pKey: " + privatekeyInHex);
		
		BigInteger publicKey = ecKeyPair.getPublicKey();
		System.out.println("public key: " + publicKey.toString(16));
		System.out.println("Get address by public key: 0x" + Keys.getAddress(publicKey));
		
		try {
			WalletFile walletFile = Wallet.createLight("abc", ecKeyPair);
			System.out.println("New address: 0x" + walletFile.getAddress());
		} catch (CipherException e) {
			e.printStackTrace();
		}
		
		BigInteger publicKey2 = Sign.publicKeyFromPrivate(privateKeyInDec);
		System.out.println("public key 2: " + publicKey2.toString(16));
	}
	
	private void getAddressTxnHash(String address) {
		Function function = new Function("transactionHash", Arrays.asList(new Address(address)), Collections.singletonList(new TypeReference<>() {}));
		String encodedFunction = FunctionEncoder.encode(function);
		
		try {
			EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(address, address, encodedFunction), DefaultBlockParameterName.LATEST).send();
			System.out.println("txn hash: " + response.getValue());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void subscribeEvent() {
		// 給一個block內的完整資料
//		web3j.transactionFlowable().subscribe(tx -> {
//			System.out.println("subscribe tx: " + objectMapper.writeValueAsString(tx));
//		});
		
		// ?
//		web3j.blockFlowable(false).subscribe(block -> {
//			System.out.println("subscribe block: " + block.getResult());
//		});
		
		// 只給block hash
//		web3j.ethBlockHashFlowable().subscribe(block -> {
//			System.out.println("subscribe block hash:" + objectMapper.writeValueAsString(block));
//		});

//		web3j.ethPendingTransactionHashFlowable().subscribe(tx -> {
//			System.out.println("subscribe eth pending tx: " + objectMapper.writeValueAsString(tx));
//		});
		
//		web3j.pendingTransactionFlowable().subscribe(tx -> {
//			System.out.println("subscribe pending tx: " + objectMapper.writeValueAsString(tx));
//		});
	}
	
	private void createAddress() {
		try {
			System.out.println("==================================");
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			System.out.println("Generate new private key: " + ecKeyPair.getPrivateKey().toString(16));
			
			BigInteger publicKey = ecKeyPair.getPublicKey();
			System.out.println("public key: " + publicKey.toString(16));
			System.out.println("Get address by public key: 0x" + Keys.getAddress(publicKey));
			
			System.out.println("==================================");
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}
	
	private void transToken(Credentials credentialsFrom, String addressTo, String contractAddress, String amount) {
		BigDecimal bigDecimalAmount = new BigDecimal(amount);
		Function function = new Function("transfer", Arrays.asList(new Address(addressTo), new Uint256(bigDecimalAmount.toBigInteger())), Collections.singletonList(new TypeReference<>() {}));
		String encodedFunction = FunctionEncoder.encode(function);

		BigInteger gas = getLastGas();
		BigInteger gasLimit = BigInteger.valueOf(42000);
		BigInteger nonce;
		try {
			nonce = getNonce(credentialsFrom.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gas, gasLimit, contractAddress, encodedFunction);
		
		byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		String hexValue = Numeric.toHexString(signMessage);
		try {
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			System.out.println("==================================================");
			System.out.println("Gas * price = " + gas.multiply(gasLimit));
			System.out.println(objectMapper.writeValueAsString(ethSendTransaction));
			System.out.println("==================================================");
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void transEth(Credentials credentialsFrom, String addressTo, double amount) {
		try {
			TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentialsFrom, addressTo, BigDecimal.valueOf(amount), Unit.ETHER).send();
			System.out.println(transactionReceipt.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Dapp dapp = new Dapp();
	}
}
