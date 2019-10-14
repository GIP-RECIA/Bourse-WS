package alloc;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;


@Data

public class Reponse {
	
	String id;
	boolean boursier = false;
	
	@JsonIgnore
	String uid;
	
	@JsonIgnore
	String ine;
	
	Integer niveau;
	
	Reponse(){};
	
}
