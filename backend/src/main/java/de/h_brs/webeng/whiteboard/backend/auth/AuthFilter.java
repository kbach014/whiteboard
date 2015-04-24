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
		// TODO get real user from request.getSession(true).getAttribute("username");
		// TODO or else response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		chain.doFilter(new AuthenticatedRequest(request, "anonymous"), response);
	}

	@Override
	public void destroy() {
		// noop
	}

}
