package edu.oakland.cse480;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.logging.Logger;

@Api(name = "myendpoint")
public class MyEndpoint {
	private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

	@ApiMethod(name = "compute")
	public MyResult compute(MyRequest req) {
		log.severe("API CALLED");
		return new MyResult("HELLO " + req.getMessage());
	}
}
