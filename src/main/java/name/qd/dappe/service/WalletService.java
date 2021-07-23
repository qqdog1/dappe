package name.qd.dappe.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert.Unit;

import name.qd.dappe.config.ConfigManager;

@Service
public class WalletService {
	
	@Autowired
	private ConfigManager configManager;

	@Autowired
	private Web3jService web3jService;
	
	private Web3j web3j;
	
	@PostConstruct
	public void init() {
		web3j = web3jService.getWeb3j();
	}
	
	public Double getEthBalance(String address) throws IOException {
		EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		BigInteger bigInteger = ethGetBalance.getBalance();
		return new BigDecimal(bigInteger).divide(Unit.ETHER.getWeiFactor()).doubleValue();
	}
	
	public Double getTokenBalance(String address, String currency) throws Exception {
		String contractAddress = configManager.getContractAddress(currency);
		if(contractAddress == null) {
			throw new Exception(String.format("Can't find contract address, currency: {}", currency));
		}
		
		Function function = new Function("balanceOf", Arrays.asList(new Address(address)), Arrays.asList(new TypeReference<Uint256>() {}));
		String encodedFunction = FunctionEncoder.encode(function);

		EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(address, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST).send();
		
		BigDecimal balance = new BigDecimal(Numeric.toBigInt(response.getValue()));
		BigDecimal contractDecimal = configManager.getContractDecimal(currency);
		if(contractDecimal == null) {
			throw new Exception(String.format("Get contract decimal failed. currency: {}", currency));
		}
		return balance.divide(contractDecimal).doubleValue();
	}
}
