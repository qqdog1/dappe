package name.qd.ws.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.utils.Convert.Unit;

import name.qd.ws.config.ConfigManager;
import name.qd.ws.dto.UserTransaction;
import name.qd.ws.service.eth.ETHWalletService;
import name.qd.ws.service.flow.FlowService;

@RestController
@RequestMapping("/wallet")
public class WalletController {
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private ETHWalletService walletService;
	
	@Autowired
	private FlowService flowService;

	@RequestMapping(value = "/currencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getSupportCurrency() {
		return ResponseEntity.ok(configManager.getSupportedCurrencies());
	}
	
	@RequestMapping(value = "/balance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getBalance(@RequestParam String chain, @RequestParam String address, @RequestParam String currency) throws Exception {
		checkIsSupportedCurrency(currency);
		
		// TODO check address format
		
		String balance = "";
		if("ETH".equals(chain)) {
			// divide應該移到service 裡面
			if("ETH".equals(currency)) {
				balance = new BigDecimal(walletService.getEthBalance(address)).divide(Unit.ETHER.getWeiFactor()).toPlainString();
			} else {
				BigDecimal contractDecimal = configManager.getContractDecimal(chain, currency);
				if(contractDecimal == null) {
					throw new Exception(String.format("Get contract decimal failed. currency: {}", currency));
				}
				balance = new BigDecimal(walletService.getTokenBalance(chain, address, currency)).divide(contractDecimal).toPlainString();
			}
		} else if("FLOW".equals(chain)) {
			balance = flowService.getBalance(address);
		}
		
		return ResponseEntity.ok(balance);
	}
	
	@RequestMapping(value = "/withdraw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserTransaction> transfer(@RequestParam int id, @RequestParam String toAddress, @RequestParam String chain, @RequestParam String currency, @RequestParam String amount) throws Exception {
		checkIsSupportedCurrency(currency);
		
		UserTransaction userTransaction = null;
		if("ETH".equals(currency)) {
			userTransaction = walletService.transferEth(id, toAddress, new BigDecimal(amount));
		} else {
			userTransaction = walletService.transferToken(chain, currency, id, toAddress, new BigDecimal(amount));
		}
		return ResponseEntity.ok(userTransaction);
	}
	
	@RequestMapping(value = "/history/withdraw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserTransaction>> getWithdrawHistory(@RequestParam int id) throws Exception {
		return ResponseEntity.ok(walletService.getWithdrawHistory(id));
	}
	
	@RequestMapping(value = "/history/deposit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserTransaction>> getDepositHistory(@RequestParam int id) throws Exception {
		return ResponseEntity.ok(walletService.getDepositHistory(id));
	}
	
	private void checkIsSupportedCurrency(String currency) throws Exception {
		if(!configManager.getSupportedCurrencies().contains(currency)) {
			throw new Exception(String.format("Currency is not supported: {}", currency));
		}
	}
}
