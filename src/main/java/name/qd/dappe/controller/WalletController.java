package name.qd.dappe.controller;

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

import name.qd.dappe.config.ConfigManager;
import name.qd.dappe.dto.UserTransaction;
import name.qd.dappe.service.WalletService;

@RestController
@RequestMapping("/wallet")
public class WalletController {
	@Autowired
	private ConfigManager configManager;
	
	@Autowired
	private WalletService walletService;

	@RequestMapping(value = "/currencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getSupportCurrency() {
		return ResponseEntity.ok(configManager.getSupportedCurrencies());
	}
	
	@RequestMapping(value = "/balance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Double> getBalance(@RequestParam String address, @RequestParam String currency) throws Exception {
		checkIsSupportedCurrency(currency);
		
		// TODO check address format
		
		Double balance;
		if("ETH".equals(currency)) {
			balance = new BigDecimal(walletService.getEthBalance(address)).divide(Unit.ETHER.getWeiFactor()).doubleValue();
		} else {
			BigDecimal contractDecimal = configManager.getContractDecimal(currency);
			if(contractDecimal == null) {
				throw new Exception(String.format("Get contract decimal failed. currency: {}", currency));
			}
			balance =  new BigDecimal(walletService.getTokenBalance(address, currency)).divide(contractDecimal).doubleValue();
		}
		
		return ResponseEntity.ok(balance);
	}
	
	@RequestMapping(value = "/withdraw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserTransaction> transfer(@RequestParam int id, @RequestParam String toAddress, @RequestParam String currency, @RequestParam String amount) throws Exception {
		checkIsSupportedCurrency(currency);
		
		UserTransaction userTransaction = null;
		if("ETH".equals(currency)) {
			userTransaction = walletService.transferEth(id, toAddress, new BigDecimal(amount));
		} else {
			userTransaction = walletService.transferToken(currency, id, toAddress, new BigDecimal(amount));
		}
		return ResponseEntity.ok(userTransaction);
	}
	
	
	private void checkIsSupportedCurrency(String currency) throws Exception {
		if(!configManager.getSupportedCurrencies().contains(currency)) {
			throw new Exception(String.format("Currency is not supported: {}", currency));
		}
	}
}
