package alloc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name ="shibpid")
public class Shibpid {
	
	@Id
	@Column(name="persistentId")
	String id;
	
	@JsonIgnore
	@Column(name="principalName")
	String uid;
	
	@JsonIgnore
	String localEntity ;
	
	@JsonIgnore
	String peerEntity ;
	
	@Transient
	boolean boursier = false;
	
	
	@Transient
	@JsonIgnore
	String ine;
	
	@Transient
	Integer niveau;
	
	
}
