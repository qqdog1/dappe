package name.qd.ws.service.solana;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.TweetNaclFast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert.Unit;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.UserAddress;
import name.qd.ws.repository.UserAddressRepository;
import name.qd.ws.service.eth.ETHService;

@Service
public class SolanaService {
	private static Logger logger = LoggerFactory.getLogger(SolanaService.class);
	
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private UserAddressRepository userAddressRepository;
	
	private RpcClient rpcClient;
	
	@PostConstruct
	private void init() {
		String nodeUrl = configManager.getNodeUrl(SupportedChain.SOL.name());
		
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
		
		userAddress.setChain(SupportedChain.SOL.name());
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
			return new BigDecimal(balance).divide(configManager.getContractDecimal(SupportedChain.SOL.name(), currency)).toPlainString();
		} catch (RpcException e) {
			logger.error("get balance from solana chain failed, address: {}, currency: {}", address, currency, e);
		}
		return "0";
	}
}
