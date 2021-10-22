package name.qd.ws.repository;

import org.springframework.data.repository.CrudRepository;

import name.qd.ws.dto.Block;

public interface BlockRepository extends CrudRepository<Block, Void> {
	Block findByChain(String chain);
}
