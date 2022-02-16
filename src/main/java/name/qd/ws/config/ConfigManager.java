package name.qd.ws.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import name.qd.ws.constant.SupportedChain;

@Component
public class ConfigManager {
	private Map<String, ChainConfig> mapChainConfig = new HashMap<>();
	
	@PostConstruct
	public void init() {
		for(SupportedChain supportedChain : SupportedChain.values()) {
			mapChainConfig.put(supportedChain.name(), new ChainConfig(supportedChain.name()));
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
