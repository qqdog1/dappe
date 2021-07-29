package name.qd.dappe.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Address {
	@Id
    @GeneratedValue
    private int id;
	private String pkey;
	private String address;
	
	public int getId() {
		return id;
	}
	
	public String getPkey() {
		return pkey;
	}
	
	public void setPkey(String pkey) {
		this.pkey = pkey;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
}
