package name.qd.dappe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import name.qd.dappe.dto.UserTransaction;

public interface UserTransactionRepository extends CrudRepository<UserTransaction, Integer> {
	@EntityGraph(attributePaths = "fromAddress")
    List<UserTransaction> findByFromAddress(String fromAddress);
	
	@EntityGraph(attributePaths = "toAddress")
	List<UserTransaction> findByToAddress(String toAddress);
	
	@EntityGraph(attributePaths = "confirmCount")
	List<UserTransaction> findByConfirmCountLessThan(long confirmCount);
}
