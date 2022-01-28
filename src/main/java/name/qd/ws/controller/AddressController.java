package name.qd.ws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import name.qd.ws.constant.SupportedChain;
import name.qd.ws.dto.UserAddress;
import name.qd.ws.service.AddressService;
import name.qd.ws.service.eth.ETHService;
import name.qd.ws.service.eth.ETHWalletService;
import name.qd.ws.service.flow.FlowService;
import name.qd.ws.service.solana.SolanaService;

@RestController
@RequestMapping("/v1/address")
public class AddressController {
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private ETHWalletService ethWalletService;
	
	@Autowired
	private FlowService flowService;

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserAddress>> getAllAddress() {
		return ResponseEntity.ok(addressService.getAllAddress());
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserAddress> createAddress(@RequestParam String chain) throws Exception {
		// TODO 可改成interface 或其他方法
		if(SupportedChain.ETH.name().equals(chain)) {
			return ResponseEntity.ok(ethWalletService.createAddress());
		} else if(SupportedChain.SOL.name().equals(chain)) {
			
		} else if(SupportedChain.FLOW.name().equals(chain)) {
//			return ResponseEntity.ok(flowService.createAddress());
		}
		throw new Exception(String.format("Chain is not supported, {}", chain));
	}
	
//	@RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<UserAddress> addAddress(@RequestBody String pkey) {
//		return ResponseEntity.ok(addressService.addAddress(pkey));
//	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserAddress> getAddress(@PathVariable int id) {
		return ResponseEntity.ok(addressService.getAddress(id));
	}
}
