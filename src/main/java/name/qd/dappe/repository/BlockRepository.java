package name.qd.dappe.repository;

import org.springframework.data.repository.CrudRepository;

import name.qd.dappe.dto.Block;

public interface BlockRepository extends CrudRepository<Block, Void> {
	Block findByChain(String chain);
}
