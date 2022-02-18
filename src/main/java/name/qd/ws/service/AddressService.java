package name.qd.ws.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

import name.qd.ws.dto.UserAddress;
import name.qd.ws.repository.UserAddressRepository;

@Service
public class AddressService {
	private static Logger logger = LoggerFactory.getLogger(AddressService.class);
	
	@Autowired
	private UserAddressRepository addressRepository;
	
	public List<UserAddress> getAllAddress() {
		return addressRepository.findAll();
	}
	
	public UserAddress getAddress(int id) {
		Optional<UserAddress> optional = addressRepository.findById(id);
		if(optional.isEmpty()) return null;
		return optional.get();
	}
	
	public UserAddress addAddress(String chain, String pkey) {
		// TODO FLOW chain 要怎麼add address
		
		if(pkey == null || pkey.length() != 64) return null;
		
		Credentials credentials = Credentials.create(pkey);
		
		UserAddress userAddress = new UserAddress();
		userAddress.setAddress(credentials.getAddress());
		userAddress.setPkey(pkey);
		
		if(!addressRepository.existsUserAddressByChainAndAddress(chain, userAddress.getAddress())) {
			userAddress = addressRepository.save(userAddress);
			return userAddress;
		}
		
		return null;
	}
}
