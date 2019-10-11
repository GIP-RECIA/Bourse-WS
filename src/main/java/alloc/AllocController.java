package alloc;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AllocController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private static final Logger log = LoggerFactory.getLogger(AllocController.class);
    
    @Autowired
    ShibRepository shibRepository;
    
    
    @RequestMapping("/id")
    public Shibpid control(@RequestParam(value="idshib" )String idShib) {
    	log.warn("idShib = {}", idShib);
    	idShib = "++6ZTfQxd5bZjAYge4xNW5/x7fY=";
    	int aux = 4;
    	for (Shibpid shib : shibRepository.findAll() ){
    		log.warn(" {} {} " , shib.persistentId, shib.principalName);
    		if (aux-- == 0) break;
    	}
    	
    	Optional<Shibpid> opt = shibRepository.findById(idShib);
    	if (opt.isEmpty()) {
    		log.warn("c'est vide {}" ,opt.toString());
    		return null;
    	}
    	
    	return opt.get();
    }
    
    @PostMapping(
			path="/alloc", 
			consumes = "application/json", 
			produces = "application/json"
		)
	public ResponseEntity<Object> post( @RequestBody Requete requete) {
		log.warn("post requete =  {}", requete);
			
		Optional<Shibpid> opt = shibRepository.findById(requete.id);
		if (opt.isEmpty()) {
    		log.warn("c'est vide {}" ,opt.toString());
    		return null;
    	}
		
		return new ResponseEntity<Object>(new Reponse(opt.get()) , HttpStatus.OK);
	}
}
