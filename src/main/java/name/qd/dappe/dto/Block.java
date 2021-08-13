package name.qd.dappe.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class Block {
	private String chain;
	@Column(name = "last_block")
	private long lastBlock;
	
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		this.chain = chain;
	}
	public long getLastBlock() {
		return lastBlock;
	}
	public void setLastBlock(long lastBlock) {
		this.lastBlock = lastBlock;
	}
}
