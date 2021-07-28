package name.qd.dappe;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.event.ListSelectionEvent;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
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

public class Dapp {
	private static final String TEST_PKEY = "ee057a93a5327dbca303b745994de5246b36757c646c734e107d973de2908f33";
	private static final String TEST_PKEY2 = "fcf4e9da729d2d36bfb609402902ec33008c2fbbfd1b661338d13278ee3cce1f";
	private static final String CONTRACT_ADDRESS = "0xeCfab06fe2420EB0Dda8dA01093511963e72772c";
	private static final String CONTRACT_ABI = "[ { \"constant\": true, \"inputs\": [], \"name\": \"name\", \"outputs\": [ { \"name\": \"\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_spender\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"approve\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"totalSupply\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_from\", \"type\": \"address\" }, { \"name\": \"_to\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"transferFrom\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"decimals\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint8\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"burn\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [ { \"name\": \"\", \"type\": \"address\" } ], \"name\": \"balanceOf\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_from\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"burnFrom\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [], \"name\": \"symbol\", \"outputs\": [ { \"name\": \"\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_to\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" } ], \"name\": \"transfer\", \"outputs\": [ { \"name\": \"\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": false, \"inputs\": [ { \"name\": \"_spender\", \"type\": \"address\" }, { \"name\": \"_value\", \"type\": \"uint256\" }, { \"name\": \"_extraData\", \"type\": \"bytes\" } ], \"name\": \"approveAndCall\", \"outputs\": [ { \"name\": \"success\", \"type\": \"bool\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"function\" }, { \"constant\": true, \"inputs\": [ { \"name\": \"\", \"type\": \"address\" }, { \"name\": \"\", \"type\": \"address\" } ], \"name\": \"allowance\", \"outputs\": [ { \"name\": \"\", \"type\": \"uint256\" } ], \"payable\": false, \"stateMutability\": \"view\", \"type\": \"function\" }, { \"inputs\": [ { \"name\": \"initialSupply\", \"type\": \"uint256\" }, { \"name\": \"tokenName\", \"type\": \"string\" }, { \"name\": \"tokenSymbol\", \"type\": \"string\" } ], \"payable\": false, \"stateMutability\": \"nonpayable\", \"type\": \"constructor\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"from\", \"type\": \"address\" }, { \"indexed\": true, \"name\": \"to\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Transfer\", \"type\": \"event\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"owner\", \"type\": \"address\" }, { \"indexed\": true, \"name\": \"spender\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Approval\", \"type\": \"event\" }, { \"anonymous\": false, \"inputs\": [ { \"indexed\": true, \"name\": \"from\", \"type\": \"address\" }, { \"indexed\": false, \"name\": \"value\", \"type\": \"uint256\" } ], \"name\": \"Burn\", \"type\": \"event\" } ]";
	private static final String TEST_ADDRESS = "0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5";
	private static final String TEST_ADDRESS2 = "0x3e694925f348e0887D4a79082FAceF9f95165Fa0";
	private Web3j web3j;
	private Admin admin;
	private Credentials credentials;

	private Dapp() {
		web3j = Web3j.build(new HttpService());
		admin = Admin.build(new HttpService());
		credentials = getCredentialsFromPrivateKey();
		
		try {
			getVersion();
			getPersonalListAccounts();
			getPersonalAccount();
			getNetVersion();
			getChainId();
			getBalance(TEST_ADDRESS);
			getBalance(TEST_ADDRESS2);
			getBalance(CONTRACT_ADDRESS);
			getTokenBalance(TEST_ADDRESS, CONTRACT_ADDRESS);
			getTokenBalance(TEST_ADDRESS2, CONTRACT_ADDRESS);
			getDecimal(CONTRACT_ADDRESS);
			getAddress();
			createAddress();
//			transEth(credentials, TEST_ADDRESS2, 0.11);
			transToken2(credentials, TEST_ADDRESS2, CONTRACT_ADDRESS, 123);
		} catch (InterruptedException | ExecutionException | IOException e) {
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
		ERC20 erc20 = ERC20.load(contractAddress, web3j, credentialsFrom, new StaticGasProvider(gas, gas));
		try {
			TransactionReceipt transactionReceipt = erc20.transfer(addressTo, BigInteger.valueOf(amount)).send();
			System.out.println(transactionReceipt.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getAddress() {
		System.out.println("Address: " + credentials.getAddress());
	}
	
	private void createAddress() {
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
	
	private void transToken(Credentials credentialsFrom, String addressTo, String contractAddress, long amount) {
		Function function = new Function("transfer", Arrays.asList(new Address(addressTo), new Uint256(amount)), Collections.emptyList());
		String encodedFunction = FunctionEncoder.encode(function);

		TransactionManager transactionManager = new FastRawTransactionManager(web3j, credentialsFrom);
		try {
			BigInteger gas = getLastGas();
			String transactionHash = transactionManager.sendTransaction(gas, gas, contractAddress, encodedFunction, BigInteger.valueOf(amount)).getTransactionHash();
			Optional<TransactionReceipt> transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt();
			if(!transactionReceipt.isEmpty()) {
				TransactionReceipt receipt = transactionReceipt.get();
				//
				System.out.println("================= Do transfer ================");
				System.out.println("getBlockNumberRaw:" + receipt.getBlockNumberRaw());
				System.out.println("getCumulativeGasUsedRaw:" + receipt.getCumulativeGasUsedRaw());
				System.out.println("getFrom:" + receipt.getFrom());
				System.out.println("getGasUsedRaw:" + receipt.getGasUsedRaw());
				System.out.println("getLogsBloom:" + receipt.getLogsBloom());
				System.out.println("getRevertReason:" + receipt.getRevertReason());
				System.out.println("getRoot:" + receipt.getRoot());
				System.out.println("getStatus:" + receipt.getStatus());
				System.out.println("getTo:" + receipt.getTo());
				System.out.println("getTransactionHash:" + receipt.getTransactionHash());
				System.out.println("getTransactionIndexRaw:" + receipt.getTransactionIndexRaw());
				System.out.println("getCumulativeGasUsed:" + receipt.getCumulativeGasUsed());
				System.out.println("getGasUsed:" + receipt.getGasUsed());
				System.out.println("getLogs:" + receipt.getLogs());
				System.out.println("getTransactionIndex:" + receipt.getTransactionIndex());
				System.out.println("================= Transfer end ===============");
			} else {
				System.out.println("do transfer failed");
			}
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
