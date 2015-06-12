package de.h_brs.webeng.whiteboard.backend.auth;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class AuthenticatedRequest extends HttpServletRequestWrapper {
	
	private final String username;

	public AuthenticatedRequest(HttpServletRequest request, String username) {
		super(request);
		this.username = username;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return new Principal() {			
			@Override
			public String getName() {
				return username;
			}
		};
	}
	
}
