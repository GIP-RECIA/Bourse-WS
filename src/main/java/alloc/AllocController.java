package alloc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/alloc")
public class AllocController {

    private static final Logger log = LoggerFactory.getLogger(AllocController.class);
    
    @Autowired
    ShibRepository shibRepository;
    
    @Autowired
    LdapRepository ldapRepository; 
    
    @Autowired
    CsvReader CsvReader;
    
    @Value("${shibbolet.entity.local}")
    String defaultLocal;
    
    @Value("${shibbolet.entity.peer}")
    String defaultPeer;
    
    
    @GetMapping(path = "/loaddata")
	public  ResponseEntity<Object> get() {
		
    	Integer nbDataLoaded = CsvReader.loadFile();
		return new ResponseEntity<Object>(nbDataLoaded, HttpStatus.OK);
	}
    
    @PostMapping(
			path="", 
			consumes = "application/json", 
			produces = "application/json"
		)
	public ResponseEntity<Object> post( @RequestBody Requete requete) {
    	CsvReader.loadFile();
    	log.warn("post requete =  {}", requete);
    	
    	
    	
    	log.info("Call indByRealKey {} {} {} ", requete.id, defaultLocal, defaultPeer);
    	Shibpid shibpid = shibRepository.findByRealKey(requete.id, defaultLocal, defaultPeer);
    	
    	if (shibpid == null) {
    		return null;
    	}
		Reponse reponse = new Reponse();
		
		reponse.id = shibpid.persistentId;
		reponse.uid = shibpid.principalName;
		
		log.info("reponse.uid {}", reponse);
		
		return new ResponseEntity<Object>(CsvReader.niveau(ldapRepository.findIneByUid(reponse)) , HttpStatus.OK);
	}
}
