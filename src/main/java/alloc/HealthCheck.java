package alloc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
public class HealthCheck {
	
	private static final Logger log = LoggerFactory.getLogger(HealthCheck.class);	
	
	@RequestMapping(value = "/health-check", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void healthCheck(HttpServletRequest request, HttpServletResponse response) {
        // Do nothing, just return HTTP 200, OK
        log.debug("Doing a health check. Returning HTTP 200, OK");
    }
}
