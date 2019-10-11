package alloc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="shibpid")
public class Shibpid {
	
	@Id
	@Column(name = "persistentId")
	String persistentId;
	
	@Column(name = "principalName")
	String principalName;
	
}
