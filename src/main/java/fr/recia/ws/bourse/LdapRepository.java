package fr.recia.ws.bourse;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;


@Service
public class LdapRepository {
	private static final Integer THREE_SECONDS = 3000;
	
	private static class LdapBean {
		String ine;
		boolean boursier = false;
	}
	
	@Autowired
    private LdapTemplate ldapTemplate;
	
	private static AttributesMapper<LdapBean> ineAttributesMapper =  new AttributesMapper<LdapBean>() {
		
		@Override
		public LdapBean mapFromAttributes(Attributes attributes) throws NamingException {
			LdapBean bean = new LdapBean();
			Attribute attr = attributes.get("ENTEleveIne");
			if (attr != null)  {
				bean.ine = (String) attr.get();
			}
			attr = attributes.get("ENTEleveBoursier");
			if (attr != null) {
				String b = (String)attributes.get("ENTEleveBoursier").get();
				bean.boursier = (b != null && b.equals("O")) ;
			}
			return bean;
		}
	};
	
	
	public ShibBean findIneByUid(ShibBean rep) {
		SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        sc.setTimeLimit(THREE_SECONDS);
        sc.setCountLimit(10);
        sc.setReturningAttributes(new String[]{"ENTEleveINE", "ENTEleveBoursier"});

        String filter = "(&(objectclass=ENTEleve)(uid=" + rep.uid + "))";
        List<LdapBean> l = ldapTemplate.search("ou=people", filter, sc, ineAttributesMapper);
        
        if (l == null || l.isEmpty()) {
        	rep.id = "";
        	rep.error = EError.INCONNU;
        } else {
        	LdapBean bean = l.get(0);
        	if (bean.ine == null) {
        		rep.error = EError.INCOMPLET;
        	} else {
        		rep.ine = bean.ine;
        	}
        	rep.boursier = bean.boursier;
        	
        }
        
        return rep;
	}
	
}
