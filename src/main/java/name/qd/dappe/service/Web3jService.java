package name.qd.dappe.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

@Service
public class Web3jService {
	private static Logger logger = LoggerFactory.getLogger(Web3jService.class);

	@Autowired
	private Environment env;

	private Web3j web3j;

	@PostConstruct
	public void init() {
		String nodeUrl = env.getProperty("eth.node.url");
		web3j = Web3j.build(new HttpService(nodeUrl));

		try {
			logVersion();
		} catch (IOException e) {
			logger.error("Get eth node version failed, url:{}", nodeUrl, e);
		}
	}

	private void logVersion() throws IOException {
		Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
		logger.info("Eth node version: {}", web3ClientVersion.getWeb3ClientVersion());
	}
	
	public Web3j getWeb3j() {
		return web3j;
	}
}
