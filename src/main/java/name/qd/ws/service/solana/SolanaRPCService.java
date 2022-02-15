package name.qd.ws.service.solana;

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

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.UserAddress;
import name.qd.ws.utils.JsonUtils;

@Service
public class SolanaRPCService {
	private static Logger logger = LoggerFactory.getLogger(SolanaRPCService.class);
	
	@Autowired
	private ConfigManager configManager;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	private String url;
	
	@PostConstruct
	private void init() {
		url = configManager.getNodeUrl(SupportedChain.SOL.name());
		
		System.out.println(getHealth());
	}
	
	public boolean getHealth() {
		String requestBody = "{\"jsonrpc\":\"2.0\",\"id\":1, \"method\":\"getHealth\"}";
		
		String body = rpcCall(requestBody);
		
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
	
	private String rpcCall(String requestBody) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		return response.getBody();
	}
}
