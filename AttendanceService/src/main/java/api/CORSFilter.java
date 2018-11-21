package main.java.api;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

//Used to allow cross-origin requests from the domain of the Ionic app
public class CORSFilter implements ContainerResponseFilter{
	@Context ServletContext context;
	private static Logger LOGGER = LogManager.getLogger("CORSFilter");

	@Override
	public ContainerResponse filter(ContainerRequest arg0, ContainerResponse arg1) {
		String allowedOrigin = "";
		//try to retrieve allowed origin from config file
		//if config file is not found, no cross-origin request is allowed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
			ConfigManager config = mapper.readValue(new File(context.getRealPath("WEB-INF/classes/config.yaml")), ConfigManager.class);
			allowedOrigin = config.getAllowedOrigin();
		} catch (IOException e) {
			LOGGER.error("Could not read config file", e);
		}
        
        //Get origin of request and compare it to allowed origins
        String origin = arg0.getHeaderValue("Origin");
        if(origin != null) {
        	String[] allowedOrigins = allowedOrigin.split(",");
            for(int i = 0; i < allowedOrigins.length; i++) {
            	Pattern p = Pattern.compile(allowedOrigins[i]);
            	Matcher m = p.matcher(origin);
            	if (m.matches()) {
            		arg1.getHttpHeaders().putSingle("Access-Control-Allow-Origin", origin);
                    arg1.getHttpHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    arg1.getHttpHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type");
                    arg1.getHttpHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            		break;
            	}
            }
        }
        return arg1;
	}

}
