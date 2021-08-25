package name.qd.dappe.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
//	@Value("classpath:contract.conf")
//	private Resource contractConfig;
	
	@Autowired
	private Environment env;
	
	@Value("${currencies}")
	private List<String> supportedCurrencies;
	private Map<String, String> mapContractAddress = new HashMap<>();
	private Map<String, String> mapContractAddressToCurrency = new HashMap<>();
	private Map<String, BigDecimal> mapContractDecimals = new HashMap<>();
	
	@Value("${eth.node.confirm.count}")
	private int confirmCount;
	
	@PostConstruct
	public void init() {
//		try {
//			lst = new ObjectMapper().readValue(contractConfig.getInputStream(), List.class);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		for(String currency : supportedCurrencies) {
			String contractAddress = env.getProperty("currency." + currency + ".contract.address");
			if(contractAddress != null) {
				contractAddress = contractAddress.toLowerCase();
				mapContractAddress.put(currency, contractAddress);
				mapContractAddressToCurrency.put(contractAddress, currency);
			}
			
			String contractDecimals = env.getProperty("currency." + currency + ".contract.decimals");
			if(contractDecimals != null) {
				mapContractDecimals.put(currency, BigDecimal.TEN.pow(Integer.valueOf(contractDecimals)));
			}
		}
	}
	
	public boolean isSupportedContractAddress(String contractAddress) {
		return mapContractAddressToCurrency.containsKey(contractAddress);
	}
	
	public String getCurrencyByContractAddress(String contractAddress) {	return mapContractAddressToCurrency.get(contractAddress);
	}
	
	public List<String> getSupportedCurrencies() {
		return supportedCurrencies;
	}
	
	public String getContractAddress(String currency) {
		return mapContractAddress.get(currency);
	}
	
	public BigDecimal getContractDecimal(String currency) {
		return mapContractDecimals.get(currency);
	}
	
	public int getConfirmCount() {
		return confirmCount;
	}
}
