package name.qd.dappe.repository;

import org.springframework.data.repository.CrudRepository;

import name.qd.dappe.dto.UserTransaction;

public interface UserTransactionRepository extends CrudRepository<UserTransaction, Integer> {
}
