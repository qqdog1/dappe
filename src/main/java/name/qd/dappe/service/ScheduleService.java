package name.qd.dappe.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.UserTransaction;
import name.qd.dappe.repository.UserTransactionRepository;

@Service
public class ScheduleService {
	private static Logger logger = LoggerFactory.getLogger(AddressService.class);
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private UserTransactionRepository userTransactionRepository;
	
	@Autowired
	private Web3jService web3jService;
	
	private Web3j web3j;
	
	@PostConstruct
	private void init() {
		web3j = web3jService.getWeb3j();
	}

	// 如果有轉入紀錄寫到DB
	// 尚未完成都會在這邊被計算
	// 轉出也掃
	@Scheduled(initialDelay = 5 * 1000, fixedDelay = 5 * 1000)
	private void checkConfirmCount() {
		int confirmCount = configManager.getConfirmCount();
		List<UserTransaction> lst = userTransactionRepository.findByConfirmCountLessThan(confirmCount);
		
		if(lst != null && lst.size() > 0) {
			EthBlockNumber ethBlockNumber = null;
			long blockNumber = 0;
			try {
				ethBlockNumber = web3j.ethBlockNumber().send();
				blockNumber = ethBlockNumber.getBlockNumber().longValue();
			} catch (IOException e) {
				logger.error("Get current eth block number failed.", e);
				return;
			}
			
			for(UserTransaction userTransaction : lst) {
				userTransaction.setConfirmCount(blockNumber - userTransaction.getBlockNumber());
			}
			
			userTransactionRepository.saveAll(lst);
		}
	}
	
	// scan deposit
	@Scheduled(initialDelay = 30 * 1000, fixedDelay = 30 * 1000)
	private void scanAddress() {
		
	}
}
