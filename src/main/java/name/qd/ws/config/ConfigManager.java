package name.qd.ws.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
	@Autowired
	private Environment env;
	
	private List<String> supportedChains;
	
	private Map<String, ChainConfig> mapChainConfig = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		supportedChains = env.getProperty("chains", List.class);
		
		for(String chain : supportedChains) {
			mapChainConfig.put(chain, new ChainConfig(chain));
		}
	}
	
	public String getNodeUrl(String chain) {
		return mapChainConfig.get(chain).getNodeUrl();
	}
	
	public boolean isSupportedContractAddress(String chain, String contractAddress) {
		if(mapChainConfig.containsKey(chain)) {
			return mapChainConfig.get(chain).getCurrencyByContractAddress(contractAddress) != null;
		}
		return false;
	}
	
	public String getCurrencyByContractAddress(String chain, String contractAddress) {	
		return mapChainConfig.get(chain).getCurrencyByContractAddress(contractAddress);
	}
	
	public List<String> getSupportedCurrencies() {
		List<String> lst = new ArrayList<>();
		for(String chain : supportedChains) {
			List<String> lstSupportedCurrencies = getSupportedCurrencies(chain);
			lst.addAll(lstSupportedCurrencies);
		}
		return lst;
	}
	
	public List<String> getSupportedCurrencies(String chain) {
		return mapChainConfig.get(chain).getSupportedCurrencies();
	}
	
	public String getContractAddress(String chain, String currency) {
		return mapChainConfig.get(chain).getContractAddress(currency);
	}
	
	public BigDecimal getContractDecimal(String chain, String currency) {
		return mapChainConfig.get(chain).getContractDecimal(currency);
	}
	
	public int getConfirmCount(String chain) {
		return mapChainConfig.get(chain).getConfirmCount();
	}
}
