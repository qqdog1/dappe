package name.qd.ws.service.solana;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.utils.JsonUtils;

@Service
public class SolanaRPCService {
	private static Logger logger = LoggerFactory.getLogger(SolanaRPCService.class);
	
	@Autowired
	private ConfigManager configManager;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	private String url;
	
	// TODO 所有message的ID目前都是1
	
	@PostConstruct
	private void init() {
		url = configManager.getNodeUrl(SupportedChain.SOL.name());
		
		String version = getVersion();
		
		System.out.println(getBlockHeight());
		if(version.startsWith("1.9")) {
			
		}
	}
	
	public String getVersion() {
		String requestBody = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getVersion\"}";
		
		String body = rpcCall(requestBody);
		logger.debug("getVersion: {}", body);
		
		String version = "";
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(body);
			JsonNode resultNode = node.get("result");
			version = resultNode.get("solana-core").asText();
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		return version;
	}
	
	public boolean getHealth() {
		String requestBody = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getHealth\"}";
		
		String body = rpcCall(requestBody);
		logger.debug("getHealth: {}", body);
		
		boolean isSuccess = false;
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(body);
			String result = node.get("result").asText();
			isSuccess = "ok".equals(result);
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		return isSuccess;
	}
	
	public long getBlockHeight() {
		String requestBody = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getBlockHeight\"}";
	
		String body = rpcCall(requestBody);
		logger.debug("getBlockHeight: {}", body);
		
		long blockHeight = 0;
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(body);
			blockHeight = node.get("result").asLong();
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		
		return blockHeight;
	}
	
	public long getSlot() {
		String requestBody = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getSlot\"}";
		
		String body = rpcCall(requestBody);
		logger.debug("getSlot: {}", body);
		
		long slot = 0;
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(body);
			slot = node.get("result").asLong();
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		
		return slot;
	}
	
	public JsonNode getTransaction(String signature) {
		String requestBody = "{" + 
				"    \"jsonrpc\": \"2.0\"," + 
				"    \"id\": 1," + 
				"    \"method\": \"getTransaction\"," + 
				"    \"params\": [" + 
				"      \"" + signature + "\"," + 
				"      \"json\"" + 
				"    ]" + 
				"  }";
		
		String body = rpcCall(requestBody);
		logger.debug("getTransaction: {}", body);
		
		try {
			return JsonUtils.objectMapper.readTree(body);
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		
		return null;
	}
	
	public List<Long> getBlocks(long slot, int range) {
		long startSlot = slot + 1;
		long endSlot = slot + range;
		String requestBody = "{\"jsonrpc\": \"2.0\",\"id\":1,\"method\":\"getBlocks\",\"params\":[" + startSlot + "," + endSlot + "]}";
		
		String body = rpcCall(requestBody);
		logger.debug("getBlocks: {}", body);
		
		List<Long> lst = new ArrayList<>();
		try {
			JsonNode node = JsonUtils.objectMapper.readTree(body);
			JsonNode arrayNode = node.get("result");
			for(int i = 0 ; i < arrayNode.size() ; i++) {
				lst.add(arrayNode.get(i).asLong());
			}
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		
		return lst;
	}
	
	public JsonNode getBlock(long slot) {
		String requestBody = "{\"jsonrpc\": \"2.0\",\"id\":1,\"method\":\"getBlock\",\"params\":[" + slot + ", {\"encoding\": \"json\",\"transactionDetails\":\"full\",\"rewards\":false}]}";
	
		String body = rpcCall(requestBody);
		logger.debug("getBlock: {}", body);
		
		try {
			return JsonUtils.objectMapper.readTree(body);
		} catch (JsonProcessingException e) {
			logger.error("parse json string failed", e);
		}
		
		return null;
	}
	
	private String rpcCall(String requestBody) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		return response.getBody();
	}
}
