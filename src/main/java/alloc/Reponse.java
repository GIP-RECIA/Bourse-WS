package alloc;

import lombok.Data;


@Data

public class Reponse {
	String id;
	
	boolean boursier = false;
	
	String uid;
	
	Reponse(Shibpid shib) {
		if (shib != null) {
			id = shib.persistentId;
			uid = shib.principalName;
			boursier = true;
		}
	}
	
}
