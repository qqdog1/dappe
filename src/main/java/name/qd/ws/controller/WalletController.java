package name.qd.ws.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.utils.Convert.Unit;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.UserTransaction;
import name.qd.ws.service.eth.ETHWalletService;
import name.qd.ws.service.flow.FlowService;
import name.qd.ws.service.solana.SolanaService;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private ETHWalletService ethWalletService;
	
	@Autowired
	private FlowService flowService;
	
	@Autowired
	private SolanaService solanaService;

	@RequestMapping(value = "/currencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<String>>> getSupportCurrency() {
		Map<String, List<String>> map = new HashMap<>();
		for(SupportedChain supportedChain : SupportedChain.values()) {
			map.put(supportedChain.name(), configManager.getSupportedCurrencies(supportedChain.name()));
		}
		return ResponseEntity.ok(map);
	}
	
	@RequestMapping(value = "/balance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getBalance(@RequestParam String chain, @RequestParam String address, @RequestParam String currency) throws Exception {
		checkIsSupportedCurrency(chain, currency);
		
		// TODO check address format
		
		String balance = "";
		if(SupportedChain.ETH.name().equals(chain)) {
			// divide應該移到service 裡面
			if(SupportedChain.ETH.name().equals(currency)) {
				balance = new BigDecimal(ethWalletService.getEthBalance(address)).divide(Unit.ETHER.getWeiFactor()).toPlainString();
			} else {
				BigDecimal contractDecimal = configManager.getContractDecimal(chain, currency);
				if(contractDecimal == null) {
					throw new Exception(String.format("Get contract decimal failed. currency: {}", currency));
				}
				balance = new BigDecimal(ethWalletService.getTokenBalance(address, currency)).divide(contractDecimal).toPlainString();
			}
		} else if(SupportedChain.SOL.name().equals(chain)) {
			balance = solanaService.getBalance(address, currency);
		} else if(SupportedChain.FLOW.name().equals(chain)) {
//			balance = flowService.getBalance(address);
		}
		
		return ResponseEntity.ok(balance);
	}
	
	@RequestMapping(value = "/withdraw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserTransaction> transfer(@RequestParam int id, @RequestParam String toAddress, @RequestParam String chain, @RequestParam String currency, @RequestParam String amount) throws Exception {
		checkIsSupportedCurrency(chain, currency);
		
		UserTransaction userTransaction = null;
		if(SupportedChain.ETH.name().equals(chain)) {
			if(SupportedChain.ETH.name().equals(currency)) {
				userTransaction = ethWalletService.transferEth(id, toAddress, new BigDecimal(amount));
			} else {
				userTransaction = ethWalletService.transferToken(currency, id, toAddress, new BigDecimal(amount));
			}
		} else if(SupportedChain.SOL.name().equals(chain)) {
			if(SupportedChain.SOL.name().equals(currency)) {
				userTransaction = solanaService.transferSOL(id, toAddress, amount);
			} else {
//				userTransaction = solanaService.transferToken(currency, id, toAddress, amount);
			}
		}
		
		return ResponseEntity.ok(userTransaction);
	}
	
	@RequestMapping(value = "/history/withdraw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserTransaction>> getWithdrawHistory(@RequestParam int id) throws Exception {
		return ResponseEntity.ok(ethWalletService.getWithdrawHistory(id));
	}
	
	@RequestMapping(value = "/history/deposit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserTransaction>> getDepositHistory(@RequestParam int id) throws Exception {
		return ResponseEntity.ok(ethWalletService.getDepositHistory(id));
	}
	
	private void checkIsSupportedCurrency(String chain, String currency) throws Exception {
		if(!configManager.getSupportedCurrencies(chain).contains(currency)) {
			throw new Exception(String.format("Currency is not supported: {}", currency));
		}
	}
}
