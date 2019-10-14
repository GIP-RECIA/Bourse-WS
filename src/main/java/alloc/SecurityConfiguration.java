package alloc;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	
	
	 @Override
	    protected void configure(HttpSecurity http) throws Exception {

	        http.authorizeRequests()
	        	.antMatchers(HttpMethod.GET, "/alloc").hasIpAddress("192.168.45.196")
	        	.antMatchers(HttpMethod.POST, "/alloc").hasIpAddress("192.168.57.117")
	        	.anyRequest().denyAll()
	        	.and().csrf().disable();
	    }

}
