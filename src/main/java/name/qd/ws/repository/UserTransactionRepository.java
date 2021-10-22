package name.qd.ws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import name.qd.ws.dto.UserTransaction;

public interface UserTransactionRepository extends CrudRepository<UserTransaction, Integer> {
	@EntityGraph(attributePaths = "fromAddress")
    List<UserTransaction> findByFromAddress(String fromAddress);
	
	@EntityGraph(attributePaths = "toAddress")
	List<UserTransaction> findByToAddress(String toAddress);
	
	@EntityGraph(attributePaths = "confirmCount")
	List<UserTransaction> findByConfirmCountLessThan(long confirmCount);
	
	boolean existsUserTransactionByHash(String hash);
	
	@EntityGraph(attributePaths = "hash")
	UserTransaction findByHash(String hash);
}
