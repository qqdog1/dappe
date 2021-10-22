package name.qd.ws.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import name.qd.ws.dto.UserAddress;

public interface UserAddressRepository extends CrudRepository<UserAddress, Integer> {
	List<UserAddress> findAll();
	
	boolean existsUserAddressByAddress(String address);
}
