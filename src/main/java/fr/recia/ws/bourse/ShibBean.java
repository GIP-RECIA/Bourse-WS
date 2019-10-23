package fr.recia.ws.bourse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@Entity
@Table(name ="shibpid")
public class ShibBean {
	
	@Id
	@Column(name="persistentId")
	String id = "";
	
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
	
	@JsonInclude(Include.NON_NULL)
	@Transient
	Integer niveau;
	
	@JsonInclude(Include.NON_NULL)
	@Transient
	EError error;
	
	@JsonInclude(Include.NON_NULL)
	@Transient
	String comment;
	
	@Transient
	@JsonIgnore
	String oldId;
	
}
