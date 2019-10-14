package alloc;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;


@Service
public class LdapRepository {
	private static final Integer THREE_SECONDS = 3000;
	
	
	@Autowired
    private LdapTemplate ldapTemplate;
	
	private static AttributesMapper<String> ineAttributesMapper =  new AttributesMapper<String>() {
		@Override
		public String mapFromAttributes(Attributes attributes) throws NamingException {
			return (String)attributes.get("ENTEleveIne").get();
		}
	};
	
	public Reponse findIneByUid(Reponse rep) {
		SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setTimeLimit(THREE_SECONDS);
        sc.setCountLimit(10);
        sc.setReturningAttributes(new String[]{"ENTEleveINE"});

        String filter = "(&(objectclass=ENTEleve)(uid=" + rep.uid + "))";
        List<String> l = ldapTemplate.search("ou=people", filter, sc, ineAttributesMapper);
        
        if (l == null || l.isEmpty()) {
        	rep.id = "";
        } else rep.ine = l.get(0);
        
        return rep;
	}
	
	
}