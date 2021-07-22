package name.qd.dappe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import name.qd.dappe.config.ConfigManager;
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
	public ResponseEntity<Double> getBalance(@RequestParam String address,@RequestParam String currency) throws Exception {
		checkIsSupportedCurrency(currency);
		
		// TODO check address format
		
		Double balance;
		if("ETH".equals(currency)) {
			balance = walletService.getEthBalance(address);
		} else {
			balance = walletService.getTokenBalance(address, currency);
		}
		
		return ResponseEntity.ok(balance);
	}
	
	private void checkIsSupportedCurrency(String currency) throws Exception {
		if(!configManager.getSupportedCurrencies().contains(currency)) {
			throw new Exception(String.format("Currency is not supported: {}", currency));
		}
	}
}
