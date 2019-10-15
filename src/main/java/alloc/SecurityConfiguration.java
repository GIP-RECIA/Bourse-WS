package alloc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.util.StringUtils;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

	@Value("${allowed.ip.list.extern}")
	List<String> ipListExtern;
	
	@Value("${allowed.ip.list.intern}")
	List<String> ipListIntern;
	
	
	private String buildIpListe(List<String> ips, StringBuilder sb) {
		for (String ip : ips) {
			if (! StringUtils.isEmpty(ip)) {
				if (sb.length() > 0) {
					sb.append(" or ");
				}
				sb.append("hasIpAddress('");
				sb.append(ip);
				sb.append("')");
			}
		}
		return sb.toString();
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		String ipIntern = buildIpListe(ipListIntern, sb);
		String ipExtern = buildIpListe(ipListExtern, sb);
		
		log.info("SecurityConfiguration configure: ip extern:{}; ip intern:{}" , ipExtern, ipIntern);
	  
		http.authorizeRequests()
	    	.antMatchers(HttpMethod.GET, "/*").access(ipIntern)
	    	.antMatchers(HttpMethod.POST, "/").access(ipExtern)
	    	.anyRequest().denyAll()
	    	.and().csrf().disable();
	 }

}
