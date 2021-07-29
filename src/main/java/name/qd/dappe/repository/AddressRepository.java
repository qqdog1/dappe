package name.qd.dappe.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import name.qd.dappe.dto.Address;

public interface AddressRepository extends CrudRepository<Address, Integer> {
	List<Address> findAll();
}
