package name.qd.ws.service.solana;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.TweetNaclFast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.Block;
import name.qd.ws.dto.UserAddress;
import name.qd.ws.dto.UserTransaction;
import name.qd.ws.repository.BlockRepository;
import name.qd.ws.repository.UserAddressRepository;
import name.qd.ws.repository.UserTransactionRepository;

@Service
public class SolanaService {
	private static Logger logger = LoggerFactory.getLogger(SolanaService.class);
	
	private String chain = SupportedChain.SOL.name();
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private SolanaRPCService solanaRPCService;
	
	@Autowired
	private UserAddressRepository userAddressRepository;
	
	@Autowired
	private UserTransactionRepository userTransactionRepository;
	
	@Autowired
	private BlockRepository blockRepository;
	
	private RpcClient rpcClient;
	
	private int confirmCount;
	
	@PostConstruct
	private void init() {
		String nodeUrl = configManager.getNodeUrl(chain);
		confirmCount = configManager.getConfirmCount(chain);
		
		// TODO 之後再改config格式
		for(Cluster cluster : Cluster.values()) {
			if(nodeUrl.toUpperCase().contains(cluster.name().toUpperCase())) {
				rpcClient = new RpcClient(cluster);
			}
		}
	}
	
	public UserAddress createAddress() {
		UserAddress userAddress = new UserAddress();
		
		TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair();
		
		String address = Base58.encode(keyPair.getPublicKey());
		String pkey = Base58.encode(keyPair.getSecretKey());
		
		userAddress.setChain(chain);
		userAddress.setAddress(address);
		userAddress.setPublicKey(address);
		userAddress.setPkey(pkey);
		
		userAddress = userAddressRepository.save(userAddress);

		return userAddress;
	}
	
	public String getBalance(String address, String currency) {
		PublicKey publicKey = new PublicKey(address);
		
		try {
			long balance = rpcClient.getApi().getBalance(publicKey);
			return new BigDecimal(balance).divide(configManager.getContractDecimal(chain, currency)).toPlainString();
		} catch (RpcException e) {
			logger.error("get balance from solana chain failed, address: {}, currency: {}", address, currency, e);
		}
		return "0";
	}
	
	public UserTransaction transferSOL(int id, String toAddress, String amount) {
		UserTransaction userTransaction = new UserTransaction();
		
		Optional<UserAddress> optional = userAddressRepository.findById(id);
		if(optional.isEmpty()) {
			return userTransaction;
		}
		UserAddress userAddress = optional.get();
				
		Account account = new Account(Base58.decode(userAddress.getPkey()));
		List<Account> lst = new ArrayList<>();
		lst.add(account);
		
		BigDecimal bigDecimalAmount = new BigDecimal(amount).multiply(configManager.getContractDecimal(chain, SupportedChain.SOL.name()));
		Transaction transaction = new Transaction();
		transaction.addInstruction(SystemProgram.transfer(new PublicKey(userAddress.getAddress()), new PublicKey(toAddress), bigDecimalAmount.longValue()));
		
		try {
//			rpcClient.getApi().sendAndConfirmTransaction(transaction, lst, solanaEventListener);
			String signature = rpcClient.getApi().sendTransaction(transaction, account);
			if(signature != null) {
				// 確認交易資訊 fee, blockHeight, is success
				JsonNode node = solanaRPCService.getTransaction(signature);
				
				userTransaction.setAmount(amount);
				userTransaction.setChain(chain);
				userTransaction.setCurrency(SupportedChain.SOL.name());
				userTransaction.setFromAddress(userAddress.getAddress());
				userTransaction.setToAddress(toAddress);
				userTransaction.setHash(signature);
				userTransaction.setGas(node.get("result").get("meta").get("fee").asText());
				userTransaction.setBlockNumber(node.get("result").get("slot").asLong());
				
				userTransaction = userTransactionRepository.save(userTransaction);
			}
		} catch (RpcException e) {
			logger.error("transfer failed", e);
		}		
		
		return userTransaction;		
	}
	
	@Scheduled(initialDelay = 1 * 1000, fixedDelay = 5 * 1000)
	private void syncBlock() {
		// TODO solana的confirm best practice 還要問
		// 現在全部用slot不用blockHeight
		// 但如果跳slot數會被當成confirm可能有風險?
		
		// DB拿最後一次sync的block
		Block block = blockRepository.findByChain(chain);
		long currentSlot = solanaRPCService.getSlot();
		
		if(block == null) {
			// DB沒有最後一次sync的紀錄
			saveProcessedBlock(currentSlot);
		} else {
			// DB有最後一次sync的紀錄
			if(block.getLastBlock() < currentSlot) {
				// 檢查每個block是否有新的deposit紀錄
				// 每次往後看5個block, 如果有回傳等於5個 繼續往後抓 如果回傳小於5 結束這次搜尋
				List<Long> lst = solanaRPCService.getBlocks(block.getLastBlock(), 5);
				
				for(Long slot : lst) {
					JsonNode node = solanaRPCService.getBlock(slot);
					// block裡面會有上百個transactions
					JsonNode nodeTransactions = node.get("result").get("transactions");
					for(JsonNode nodeTransaction : nodeTransactions) {
						String toAddress = nodeTransaction.get("transaction").get("message").get("accountKeys").get(1).asText();
						
						if(userAddressRepository.existsUserAddressByChainAndAddress(chain, toAddress)) {
							long preBalance = nodeTransaction.get("meta").get("preBalances").get(1).asLong();
							long postBalance = nodeTransaction.get("meta").get("postBalances").get(1).asLong();
							long diff = postBalance - preBalance;
							if(diff > 0) {
								// deposit
								UserTransaction userTransaction = new UserTransaction();
								userTransaction.setAmount(BigDecimal.valueOf(diff).divide(configManager.getContractDecimal(chain, SupportedChain.SOL.name())).toString());
								userTransaction.setBlockNumber(slot);
								userTransaction.setChain(chain);
								userTransaction.setCurrency(SupportedChain.SOL.name());
								userTransaction.setFromAddress(nodeTransaction.get("transaction").get("message").get("accountKeys").get(0).asText());
								userTransaction.setToAddress(toAddress);
								userTransaction.setGas(nodeTransaction.get("meta").get("fee").asText());
								userTransaction.setHash(nodeTransaction.get("transaction").get("signatures").get(0).asText());
								
								userTransactionRepository.save(userTransaction);
							}
						}
					}
				}
				// 已經有deposit紀錄的更新confirm count
				updateConfirmCount(currentSlot);
				
				if(lst.size() == 5) {
					syncBlock();
					logger.info("sync block again");
				}
			}
		}
	}
	
	private void saveProcessedBlock(long blockHeight) {
		Block block = new Block();
		block.setChain(chain);
		block.setLastBlock(blockHeight);
		blockRepository.save(block);
	}
	
	private void updateConfirmCount(long blockHeight) {
		List<UserTransaction> lst = userTransactionRepository.findByChainAndConfirmCountLessThan(chain, confirmCount);
		
		if(lst != null && lst.size() > 0) {
			for(UserTransaction userTransaction : lst) {
				userTransaction.setConfirmCount(blockHeight - userTransaction.getBlockNumber());
			}
			
			userTransactionRepository.saveAll(lst);
		}
	}
}
