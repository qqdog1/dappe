package name.qd.ws.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class UserAddress {
	@Id
    @GeneratedValue
    private int id;
	private String chain;
	private String pkey;
	private String publicKey;
	private String address;
	
	public int getId() {
		return id;
	}
	
	public String getChain() {
		return chain;
	}
	
	public void setChain(String chain) {
		this.chain = chain;
	}
	
	public String getPkey() {
		return pkey;
	}
	
	public void setPkey(String pkey) {
		this.pkey = pkey;
	}
	
	public String getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
}
