package name.qd.dappe.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.UserTransaction;
import name.qd.dappe.repository.UserAddressRepository;
import name.qd.dappe.repository.UserTransactionRepository;

@Service
public class Web3jService {
	private static Logger logger = LoggerFactory.getLogger(Web3jService.class);

	@Autowired
	private Environment env;
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private UserTransactionRepository userTransactionRepository;
	
	@Autowired
	private UserAddressRepository userAddressRepository;

	private Web3j web3j;
	
	private int confirmCount;
	
	// 上次關機最後處裡到的block
	private int lastBlockNumber;
	// 本次啟動查到最新的block
	private int startBlockNumber;

	@PostConstruct
	public void init() {
		String nodeUrl = env.getProperty("eth.node.url");
		web3j = Web3j.build(new HttpService(nodeUrl));

		try {
			logVersion();
		} catch (IOException e) {
			logger.error("Get eth node version failed, url:{}", nodeUrl, e);
		}
		
		confirmCount = configManager.getConfirmCount();
		subscribeTransaction();
		// 補關機的時候 中間沒有收到的transaction
		
	}
	
	private void logVersion() throws IOException {
		Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
		logger.info("Eth node version: {}", web3ClientVersion.getWeb3ClientVersion());
	}
	
	private void subscribeTransaction() {
		web3j.transactionFlowable().subscribe(tx-> {
			updateConfirmCount(tx.getBlockNumber());
			checkDeposit(tx);
		});
	}
	
	private void updateConfirmCount(BigInteger blockNumber) {
		List<UserTransaction> lst = userTransactionRepository.findByConfirmCountLessThan(confirmCount);
		
		if(lst != null && lst.size() > 0) {
			for(UserTransaction userTransaction : lst) {
				userTransaction.setConfirmCount(blockNumber.longValue() - userTransaction.getBlockNumber());
			}
			
			userTransactionRepository.saveAll(lst);
		}
	}
	
	private void checkDeposit(Transaction transaction) {
		String address = transaction.getTo();
		
		List<UserTransaction> lst = new ArrayList<>();
		if(userAddressRepository.existsUserAddressByAddress(address)) {
			UserTransaction userTransaction = new UserTransaction();
			userTransaction.setFromAddress(transaction.getFrom());
			userTransaction.setToAddress(address);
			// TODO 還不知道怎麼抓currency
//			userTransaction.setCurrency(currency);
//			userTransaction.setAmount(amount);
			userTransaction.setGas(transaction.getGas().toString());
			userTransaction.setHash(transaction.getHash());
			userTransaction.setBlockNumber(transaction.getBlockNumber().longValue());
			
			lst.add(userTransaction);
		}
		
		if(lst.size() > 0) {
			userTransactionRepository.saveAll(lst);
		}
	}
	
	public Web3j getWeb3j() {
		return web3j;
	}
}
