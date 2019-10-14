package alloc;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    	
    	Shibpid shibpid = new Shibpid();
    	
    	shibpid.setPersistentId(requete.id);
    	
    //	Collection<Shibpid> col =  shibRepository.findByModel(shibpid.getPersistentId(), shibpid.getLocalEntity(), shibpid.getPeerEntity());
    //	if (col == null || col.isEmpty()) {
    	
    //		return null;
    //	}
    //	shibpid = col.iterator().next();
    	shibpid = shibRepository.findByModel(shibpid.getPersistentId(), shibpid.getLocalEntity(), shibpid.getPeerEntity());
    	
	/*	Optional<Shibpid> opt = shibRepository.findById(requete.id);
		if (opt.isEmpty()) {
    		log.warn("c'est vide {}" ,opt.toString());
    		return null;
    	}
    */
		Reponse reponse = new Reponse();
		
	//	reponse.id = opt.get().persistentId;
	//	reponse.uid = opt.get().principalName; // "F17400f4";
		reponse.id = shibpid.getPersistentId();
		reponse.uid = shibpid.getPrincipalName();
		
		log.info("reponse.uid {}", reponse);
		
		return new ResponseEntity<Object>(CsvReader.niveau(ldapRepository.findIneByUid(reponse)) , HttpStatus.OK);
	}
}
