package name.qd.dappe.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert.Unit;

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.Block;
import name.qd.dappe.dto.UserTransaction;
import name.qd.dappe.repository.BlockRepository;
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
	
	@Autowired
	private BlockRepository blockRepository;

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
//		subscribeTransaction();
	}
	
	private void logVersion() throws IOException {
		Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
		logger.info("Eth node version: {}", web3ClientVersion.getWeb3ClientVersion());
	}

	@Scheduled(initialDelay = 1 * 1000, fixedDelay = 5 * 60 * 1000)
	private void syncBlock() {
		Block block = blockRepository.findByChain("ETH");
		if(block == null) {
			insertCurrencyBlock();
		} else {
			BigInteger lastBlockNumber = getLastBlockNumber();
			if(lastBlockNumber != null) {
				updateConfirmCount(lastBlockNumber);
				updateTransaction(block.getLastBlock(), lastBlockNumber.longValue());
			}
		}
	}
	
	private void insertCurrencyBlock() {
		BigInteger lastBlockNumber = getLastBlockNumber();
		if(lastBlockNumber != null) {
			saveProcessedBlock(lastBlockNumber.longValue());
		}
	}
	
	private void saveProcessedBlock(long blockNumber) {
		Block block = new Block();
		block.setChain("ETH");
		block.setLastBlock(blockNumber);
		blockRepository.save(block);
	}
	
	private BigInteger getLastBlockNumber() {
		BigInteger lastBlockNumber = null;
		try {
			EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
			lastBlockNumber = ethBlock.getBlock().getNumber();
		} catch (IOException e) {
			logger.error("Get latest block failed.");
		}
		return lastBlockNumber;
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
	
	@SuppressWarnings("rawtypes")
	private void updateTransaction(long processedNumber, long lastBlockNumber) {
		while(processedNumber < lastBlockNumber) {
			try {
				processedNumber++;
				EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(processedNumber)), true).send();
			
				List<TransactionResult> lst = ethBlock.getBlock().getTransactions();
				for(TransactionResult transactionResult : lst) {
					TransactionObject transaction = (TransactionObject) transactionResult;
					
					String toAddress = transaction.getTo();
					// to address 如果是一般address 就是轉ETH, 是 contract address 就可能是轉幣
					// TODO 每個block 去scan db 未來應該是要改成Batch
					if(userAddressRepository.existsUserAddressByAddress(toAddress)) {
						transferETHRecord(transaction.getHash());
					} else if(configManager.isSupportedContractAddress(toAddress)) {
						String input = transaction.getInput();
						// transfer
						if(input.startsWith("0xa9059cbb")) {
							String address = getTransferAddressFromInput(input);
						}
					}
				}
			
				saveProcessedBlock(processedNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addDepositRecord(TransactionObject transaction) {
		UserTransaction userTransaction = new UserTransaction();

	}
	
	private String getTransferAddressFromInput(String input) {
		return input.substring(34, 74);
	}
	
	private void transferETHRecord(String hash) {
		try {
			EthTransaction transaction = web3j.ethGetTransactionByHash(hash).send();
			Transaction tx = transaction.getResult();
			
			UserTransaction userTransaction = new UserTransaction();
			userTransaction.setAmount(new BigDecimal(tx.getValue()).divide(Unit.ETHER.getWeiFactor()).toPlainString());
			userTransaction.setBlockNumber(tx.getBlockNumber().longValue());
			userTransaction.setCurrency("ETH");
			userTransaction.setFromAddress(tx.getFrom());
			userTransaction.setGas(tx.getGas().toString());
			userTransaction.setHash(hash);
			userTransaction.setToAddress(tx.getTo());
			
			userTransactionRepository.save(userTransaction);
		} catch (IOException e) {
			logger.error("get transaction from node failed. hash: {}", hash, e);
		}
	}
	
	// subscribe的方式如果geth node出問題
	// 會掉訊息
//	private void subscribeTransaction() {
//		web3j.transactionFlowable().subscribe(tx-> {
//			updateConfirmCount(tx.getBlockNumber());
//			checkDeposit(tx);
//		});
//	}
	
//	private void checkDeposit(Transaction transaction) {
//		String address = transaction.getTo();
//		
//		List<UserTransaction> lst = new ArrayList<>();
//		if(userAddressRepository.existsUserAddressByAddress(address)) {
//			UserTransaction userTransaction = new UserTransaction();
//			userTransaction.setFromAddress(transaction.getFrom());
//			userTransaction.setToAddress(address);
//			// TODO 還不知道怎麼抓currency
////			userTransaction.setCurrency(currency);
////			userTransaction.setAmount(amount);
//			userTransaction.setGas(transaction.getGas().toString());
//			userTransaction.setHash(transaction.getHash());
//			userTransaction.setBlockNumber(transaction.getBlockNumber().longValue());
//			
//			lst.add(userTransaction);
//		}
//		
//		if(lst.size() > 0) {
//			userTransactionRepository.saveAll(lst);
//		}
//	}
	
	public Web3j getWeb3j() {
		return web3j;
	}
}
