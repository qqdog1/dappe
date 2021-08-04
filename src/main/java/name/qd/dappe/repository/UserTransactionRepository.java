package name.qd.dappe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import name.qd.dappe.dto.UserTransaction;

public interface UserTransactionRepository extends CrudRepository<UserTransaction, Integer> {
	@EntityGraph(attributePaths = "from_address")
    List<UserTransaction> findByFromAddress(String fromAddress);
	
	@EntityGraph(attributePaths = "to_address")
	List<UserTransaction> findByToAddress(String toAddress);
	
	@EntityGraph(attributePaths = "confirm_count")
	List<UserTransaction> findByConfirmCount(int confirm_count);
}
