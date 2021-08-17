package name.qd.dappe.service;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import name.qd.dappe.dto.UserAddress;
import name.qd.dappe.repository.UserAddressRepository;

@Service
public class AddressService {
	private static Logger logger = LoggerFactory.getLogger(AddressService.class);
	
	@Autowired
	private UserAddressRepository addressRepository;
	
	public List<UserAddress> getAllAddress() {
		return addressRepository.findAll();
	}
	
	public UserAddress createAddress() {
		UserAddress userAddress = new UserAddress();
		try {
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			String newAddress = Keys.getAddress(ecKeyPair);
			userAddress.setAddress("0x" + newAddress);
			userAddress.setPkey(ecKeyPair.getPrivateKey().toString(16));
			
			userAddress = addressRepository.save(userAddress);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.error("Create ec key pair failed.", e);
		}
		return userAddress;
	}
	
	public UserAddress getAddress(int id) {
		Optional<UserAddress> optional = addressRepository.findById(id);
		if(optional.isEmpty()) return null;
		return optional.get();
	}
	
	public UserAddress addAddress(String pkey) {
		if(pkey == null || pkey.length() != 64) return null;
		
		Credentials credentials = Credentials.create(pkey);
		
		UserAddress userAddress = new UserAddress();
		userAddress.setAddress(credentials.getAddress());
		userAddress.setPkey(pkey);
		
		if(!addressRepository.existsUserAddressByAddress(userAddress.getAddress())) {
			userAddress = addressRepository.save(userAddress);
			return userAddress;
		}
		
		return null;
	}
}
