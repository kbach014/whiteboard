package de.h_brs.webeng.whiteboard.backend.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.h_brs.webeng.whiteboard.backend.dao.UserDAO;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisUserDAO;

@WebFilter(filterName = "authFilter", urlPatterns = {"/drawings/*", "/rest/*"})
public final class AuthFilter implements Filter{
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		//String username = request.getSession().getAttribute("username").toString();
		String username = "sebi";
		UserDAO userDAO = new RedisUserDAO();
		
		if(userDAO.userExists(username)) {
			chain.doFilter(new AuthenticatedRequest(request, username), response);
		} 
		else {
			// TODO EXCEPTION
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	@Override
	public void destroy() {
		// noop
	}

}
