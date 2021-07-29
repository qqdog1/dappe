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
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import name.qd.dappe.dto.Address;
import name.qd.dappe.repository.AddressRepository;

@Service
public class AddressService {
	private static Logger logger = LoggerFactory.getLogger(AddressService.class);
	
	@Autowired
	private AddressRepository addressRepository;
	
	public List<Address> getAllAddress() {
		return addressRepository.findAll();
	}
	
	public Address createAddress() {
		Address address = new Address();
		try {
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			String newAddress = Keys.getAddress(ecKeyPair);
			address.setAddress("0x" + newAddress);
			address.setPkey(ecKeyPair.getPrivateKey().toString(16));
			
			address = addressRepository.save(address);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.error("Create ec key pair failed.", e);
		}
		return address;
	}
	
	public Address getAddress(int id) {
		Optional<Address> optional = addressRepository.findById(id);
		if(optional.isEmpty()) return null;
		return optional.get();
	}
}
