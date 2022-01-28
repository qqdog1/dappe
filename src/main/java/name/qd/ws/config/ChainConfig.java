package name.qd.ws.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import name.qd.ws.utils.JsonUtils;

public class ChainConfig {
	private static Logger logger = LoggerFactory.getLogger(ChainConfig.class);
	private Resource configRecource;
	private String nodeUrl;
	private int confirmCount;
	private List<String> supportedCurrencies = new ArrayList<>();
	
	private Map<String, String> mapContractAddress = new HashMap<>();
	private Map<String, String> mapContractAddressToCurrency = new HashMap<>();
	private Map<String, BigDecimal> mapContractDecimals = new HashMap<>();

	public ChainConfig(String chain) {
		String configFile = chain + ".conf";
		configRecource = new ClassPathResource(configFile);
		
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(configRecource.getInputStream());
			
			nodeUrl = node.get("nodeUrl").asText();
			confirmCount = node.get("confirmCount").asInt();
			
			// 如果規則不一樣 這段也可以讓各chain自己實作
			JsonNode nodeCurrencies = node.get("currencies");
			for(JsonNode nodeCurrency : nodeCurrencies) {
				String currency = nodeCurrency.get("currency").asText();
				supportedCurrencies.add(currency);
				if(nodeCurrency.has("contractAddress")) {
					String contractAddress = nodeCurrency.get("contractAddress").asText();
					mapContractAddress.put(currency, contractAddress);
					mapContractAddressToCurrency.put(contractAddress, currency);
				}
				BigDecimal contractDecimal = BigDecimal.TEN.pow(Integer.valueOf(nodeCurrency.get("decimal").asText()));
				mapContractDecimals.put(currency, contractDecimal);
			}
			
			logger.info("{} config loaded.", configFile);
		} catch (IOException e) {
			logger.error("Load config failed. {}", configFile);
		}
	}
	
	public String getNodeUrl() {
		return nodeUrl;
	}
	
	public int getConfirmCount() {
		return confirmCount;
	}
	
	public List<String> getSupportedCurrencies() {
		return supportedCurrencies;
	}
	
	public String getContractAddress(String currency) {
		return mapContractAddress.get(currency);
	}
	
	public String getCurrencyByContractAddress(String contractAddress) {
		return mapContractAddressToCurrency.get(contractAddress);
	}
	
	public BigDecimal getContractDecimal(String currency) {
		return mapContractDecimals.get(currency);
	}
}
