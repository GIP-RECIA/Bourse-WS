package fr.recia.ws.bourse;

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
    private static final Logger logInfo =  LoggerFactory.getLogger("fr.recia.ws.bourse.infouid");
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
    	Integer nbDataLoaded[] = new Integer[2]; 
    	nbDataLoaded[0] = CsvReader.loadFileIne();
    	nbDataLoaded[1] = CsvReader.loadFileUid();
		return new ResponseEntity<Object>(nbDataLoaded, HttpStatus.OK);
	}
    
    
    @GetMapping(path = "/test")
	public  ResponseEntity<Object> testAll() {
    	CsvReader.loadFileIne();
    	List<ShibBean> allBoursier = new ArrayList<>();
    	
    	boolean bourse =false;
    	boolean autre = false;
    	boolean inconnue = false;
    	boolean incomplet = false;
    	boolean byUid = false;
    	
    	Iterable<ShibBean> all = shibRepository.findAll();
    	for (ShibBean shibBean : all) {
    		boolean add = false;
    		if (shibBean != null) {
    			evalNiveau(shibBean);
    			if (shibBean.error != null) {
					switch (shibBean.error) {
						case INCONNU:
							if (!inconnue) {
								add = inconnue = true;
								shibBean.comment = shibBean.oldId + " " + shibBean.uid;
							}
							break;
						case INCOMPLET:
							if (!incomplet) {
								add = incomplet = true;
								shibBean.comment = shibBean.uid;
							}
							break;
						default:
							add = true;
							break;
					}
    			} else if (shibBean.boursier) {
    				if (shibBean.ine == null) {
    					if (!byUid) {
    						add = byUid = true;
    						shibBean.comment = "Boursier by UID " + shibBean.uid;
    						log.debug("boursier by uid {}" , shibBean);
    					}
    				} else {
	    				if (!bourse) {
	    					bourse = add = true;
	    					shibBean.comment = "Boursier by INE " + shibBean.ine + " " + shibBean.uid;
	    					log.debug("boursier {}" , shibBean);
	    				}
    				}
    			} else {
    				if (!autre) {
    					add = autre = true;
    					shibBean.comment = shibBean.ine + " " + shibBean.uid;
    				}
    			}
    			if (add) {
    				allBoursier.add(shibBean);
    			} 
    			if (byUid && bourse && inconnue && incomplet && autre) {
    				break;
    			}
    		} 
    	}
    	return new ResponseEntity<Object>(allBoursier,  HttpStatus.OK);
    }
    
    @PostMapping(
			path="", 
			consumes = "application/json", 
			produces = "application/json"
		)
	public ResponseEntity<Object> post( @RequestBody Requete requete) {
    	
    	log.debug("post requete =  {}", requete);
    	
    	ShibBean shibpid = shibRepository.findByRealKey(requete.id, defaultLocal, defaultPeer);
    	
    	if (shibpid == null) {
    		shibpid = new ShibBean();
    		shibpid.error = EError.INVALIDE;
    		return new ResponseEntity<Object>(shibpid, HttpStatus.OK);
    	}
    	
    	evalNiveau(shibpid);
    	if (shibpid.uid != null) {
    		logInfo.info(shibpid.uid);
    	}
		return new ResponseEntity<Object>(shibpid , HttpStatus.OK);
	}


    private void evalNiveau(ShibBean shibBean) {
    	if (! CsvReader.niveauByUId(shibBean).isBoursier()) {
    		CsvReader.niveauByIne(ldapRepository.findIneByUid(shibBean));
    	}
    }
}
