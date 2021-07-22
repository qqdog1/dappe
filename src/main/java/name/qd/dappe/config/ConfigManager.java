package name.qd.dappe.config;

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
				mapContractAddress.put(currency, contractAddress);
			}
		}
	}
	
	public List<String> getSupportedCurrencies() {
		return supportedCurrencies;
	}
	
	public String getContractAddress(String currency) {
		return mapContractAddress.get(currency);
	}
}
