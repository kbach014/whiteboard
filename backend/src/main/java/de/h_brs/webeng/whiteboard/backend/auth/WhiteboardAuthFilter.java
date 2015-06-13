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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.h_brs.webeng.whiteboard.backend.DrawingEndpoint;
import de.h_brs.webeng.whiteboard.backend.dao.UserDAO;
import de.h_brs.webeng.whiteboard.backend.dao.exception.UserNotFoundException;
import de.h_brs.webeng.whiteboard.backend.dao.exception.WhiteboardNotFoundException;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisUserDAO;

@WebFilter(filterName = "whiteboardAuthFilter", urlPatterns = { "/drawings/*" })
public class WhiteboardAuthFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);
	private final String URL_PREFIX = "/backend/drawings/";

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
		// TODO remove Hardcoding
		request.getSession().setAttribute("username", "sebi");

		if (request.getSession().getAttribute("username") != null) {
			String username = request.getSession().getAttribute("username").toString();
			String url = request.getRequestURI();
			Long wbID = Long.parseLong(url.substring(URL_PREFIX.length(), url.length()));

			if (userIsValid(username)) {
				try {
					if (userHasAccessToWhiteboard(username, wbID)) {
						// success
						chain.doFilter(new AuthenticatedRequest(request, username), response);
					} else {
						LOG.info("User " + username + " has no access to whiteboard#" + wbID);
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					}
				} catch (UserNotFoundException e) {
				} catch (WhiteboardNotFoundException e) {
					// TODO in diesem Fall baut er trotzdem eine Socketverbindung auf, da aber das WB nicht gefunden werden kann
					// muss dies unterbunden werden
					LOG.error("Whiteboard#" + wbID + " cannot be found!");
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}

			} else {
				LOG.error(username + " is NOT valid user!");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			LOG.error("username cannot be NULL!");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	@Override
	public void destroy() {
		// noop
	}

	private boolean userHasAccessToWhiteboard(String username, Long wbID) throws UserNotFoundException, WhiteboardNotFoundException {
		UserDAO userDAO = new RedisUserDAO();
		// Is session.getUserPrincipal() authorized to draw on this whiteboard?
		if (userDAO.userHasWhiteboard(username, wbID)) {
			LOG.info("User is authorized to draw on whiteboard#" + wbID);
			return true;
		} else {
			LOG.info("User is NOT authorized to draw on whiteboard#" + wbID);
			return false;
		}
	}

	private boolean userIsValid(String username) {
		UserDAO userDAO = new RedisUserDAO();

		if (username != null && userDAO.userExists(username)) {
			LOG.info("User " + username + " is a valid user!");
			return true;

		} else {
			LOG.info("User " + username + " is NOT a valid user!");
			return false;
		}
	}
}
