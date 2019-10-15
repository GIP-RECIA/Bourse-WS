package alloc;

import java.util.ArrayList;
import java.util.List;

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
@RequestMapping(path = "/")
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
    
    
    @GetMapping(path = "/test")
	public  ResponseEntity<Object> testAll() {
    	CsvReader.loadFile();
    	List<ShibBean> allBoursier = new ArrayList<>();
    	
    	int nbBourse =0;
    	int nbAutre = 0;
    	int nbIconnue = 0;
    	int nbIncomplet = 0;
    	
    	Iterable<ShibBean> all = shibRepository.findAll();
    	for (ShibBean shib : all) {
    		boolean add = false;
    		if (shib != null) {
    			ShibBean shibBean = CsvReader.niveau(ldapRepository.findIneByUid(shib));
    			if (shibBean.error != null) {
					switch (shibBean.error) {
						case INCONNU:
								add = nbIconnue++ < 10;
							break;
						case INCOMPLET:
								add = nbIncomplet++ < 10;
						default:
							add = true;
							break;
					}
    			} else if (shibBean.boursier) {
    				add = nbBourse++ < 10;
    				log.debug("boursier {}" , shibBean);
    				if (!add) {
    					break;
    				} 
    			} else {
					add = nbAutre++ < 10;
    			}
    			if (add) {
    				allBoursier.add(shibBean);
    			}
    		} 
    	}
    	log.debug("total boursier={} inconnue ={} incomplet={} nonBoursier= {}" , nbBourse, nbIconnue, nbIncomplet , nbAutre);
    	return new ResponseEntity<Object>(allBoursier,  HttpStatus.OK);
    }
    @PostMapping(
			path="", 
			consumes = "application/json", 
			produces = "application/json"
		)
	public ResponseEntity<Object> post( @RequestBody Requete requete) {
    	CsvReader.loadFile();
    	log.debug("post requete =  {}", requete);
    	
    	ShibBean shibpid = shibRepository.findByRealKey(requete.id, defaultLocal, defaultPeer);
    	
    	if (shibpid == null) {
    		shibpid = new ShibBean();
    		shibpid.error = EError.INVALIDE;
    		return new ResponseEntity<Object>(shibpid, HttpStatus.OK);
    	}
		
		
		return new ResponseEntity<Object>(CsvReader.niveau(ldapRepository.findIneByUid(shibpid)) , HttpStatus.OK);
	}
}
