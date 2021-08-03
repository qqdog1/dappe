package name.qd.dappe.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert.Unit;

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.UserAddress;
import name.qd.dappe.dto.UserTransaction;
import name.qd.dappe.repository.UserTransactionRepository;

@Service
public class WalletService {
	private static Logger logger = LoggerFactory.getLogger(WalletService.class);
	
	@Autowired
	private ConfigManager configManager;

	@Autowired
	private Web3jService web3jService;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private UserTransactionRepository userTransactionRepository;
	
	private Web3j web3j;
	
	@PostConstruct
	public void init() {
		web3j = web3jService.getWeb3j();
	}
	
	public BigInteger getEthBalance(String address) throws IOException {
		EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		return ethGetBalance.getBalance();
	}
	
	public BigInteger getTokenBalance(String address, String currency) throws Exception {
		String contractAddress = configManager.getContractAddress(currency);
		if(contractAddress == null) {
			throw new Exception(String.format("Can't find contract address, currency: {}", currency));
		}
		
		Function function = new Function("balanceOf", Arrays.asList(new Address(address)), Arrays.asList(new TypeReference<Uint256>() {}));
		String encodedFunction = FunctionEncoder.encode(function);

		EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(address, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST).send();
		
		return Numeric.toBigInt(response.getValue());
	}
	
	public UserTransaction transferEth(int id, String toAddress, BigDecimal amount) throws Exception {
		UserAddress userAddress = addressService.getAddress(id);
		if(userAddress == null) throw new Exception("id not exist.");
		
		Credentials credentials = Credentials.create(userAddress.getPkey());
		TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, toAddress, amount, Unit.ETHER).send();
		UserTransaction userTransaction = toUserTransaction(transactionReceipt, id, "ETH", amount);
		
		userTransaction = userTransactionRepository.save(userTransaction);
		return userTransaction;
	}
	
	public UserTransaction transferToken(String currency, int id, String toAddress, BigDecimal amount) throws Exception {
		UserAddress userAddress = addressService.getAddress(id);
		if(userAddress == null) throw new Exception("id not exist.");
		
		Credentials credentials = Credentials.create(userAddress.getPkey());
		BigDecimal decimalAmount = amount.multiply(configManager.getContractDecimal(currency));
		BigInteger gasPrice = getLastGasPrice();
		BigInteger gasLimit = BigInteger.valueOf(42000);
		String contractAddress = configManager.getContractAddress(currency);
		ERC20 erc20 = ERC20.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
		
		TransactionReceipt transactionReceipt = erc20.transfer(toAddress, decimalAmount.toBigInteger()).send();
		UserTransaction userTransaction = toUserTransaction(transactionReceipt, id, currency, amount);
		
		userTransaction = userTransactionRepository.save(userTransaction);
		return userTransaction;
	}
	
	private UserTransaction toUserTransaction(TransactionReceipt transactionReceipt, int id, String currency, BigDecimal amount) {
		UserTransaction userTransaction = new UserTransaction();
		userTransaction.setAmount(amount.toPlainString());
		userTransaction.setCurrency(currency);
		userTransaction.setFromAddress(transactionReceipt.getFrom());
		userTransaction.setGas(transactionReceipt.getGasUsed().toString());
		userTransaction.setHash(transactionReceipt.getTransactionHash());
		userTransaction.setToAddress(transactionReceipt.getTo());
		userTransaction.setUserId(id);
		return userTransaction;
	}
	
	private BigInteger getLastGasPrice() {
		try {
			EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
			return ethGasPrice.getGasPrice();
		} catch (IOException e) {
			logger.error("Get last gas price failed.", e);
		}
		return DefaultGasProvider.GAS_LIMIT;
	}
}
