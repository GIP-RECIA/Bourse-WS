package alloc;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="shibpid")
public class Shibpid {
	
	@Id
	String persistentId;
	
	String principalName;
	
}
