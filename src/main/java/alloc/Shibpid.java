package alloc;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name ="shibpid")
public class Shibpid {
	
	@Id
	private String persistentId;
	
	private String principalName;
	
	private String localEntity = "https://ent.netocentre.fr/idp/shibboleth";
	
	private String peerEntity = "https://ent.yeps.fr/shibboleth";
}
