package org.cfb.ungentry.census.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;	 
import javax.ws.rs.core.SecurityContext;

@Path("/security")
public class SecurityServices {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/name")
	public String getUserName( @Context SecurityContext sc ) {
		return sc.getUserPrincipal().getName();
	}
	
}
