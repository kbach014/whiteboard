package de.h_brs.webeng.whiteboard.backend.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.h_brs.webeng.whiteboard.backend.dao.WhiteboardDAO;
import de.h_brs.webeng.whiteboard.backend.dao.exception.UserNotFoundException;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisWhiteboardDAO;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;
import de.h_brs.webeng.whiteboard.backend.dto.WhiteboardDto;

// http://localhost:8080/backend/rest/whiteboards
@Path("whiteboards")
public class WhiteboardService {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@Context HttpServletRequest req) {
		final String username = (String) req.getSession().getAttribute("username");
		if (username == null) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		try {
			final WhiteboardDAO whiteboardDao = new RedisWhiteboardDAO();
			final Whiteboard whiteboard = whiteboardDao.insertWhiteboard(username);
			final WhiteboardDto dto = new WhiteboardDto();
			dto.setId(whiteboard.getWbid());
			dto.setCreator(whiteboard.getCreator());
			return Response.ok(dto).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Path("/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findRegisteredWhiteboards(@Context HttpServletRequest req) {
		final String username = (String) req.getSession().getAttribute("username");
		if (username == null) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			final WhiteboardDAO whiteboardDao = new RedisWhiteboardDAO();
			final List<Whiteboard> whiteboards = whiteboardDao.findRegisteredWhiteboards(username);
			final List<WhiteboardDto> dtos = whiteboards.stream().map(wb -> {
				WhiteboardDto dto = new WhiteboardDto();
				dto.setId(wb.getWbid());
				dto.setCreator(wb.getCreator());
				return dto;
			}).collect(Collectors.toList());
			
			for(int x=0; x<dtos.size(); ++x) {
				System.out.println(dtos.get(x).getId());
			}
			
			return Response.ok(dtos).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}
	
	@GET
	@Path("/unregistered")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findUnregisteredWhiteboards(@Context HttpServletRequest req) {
		final String username = (String) req.getSession().getAttribute("username");
		if (username == null) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		// TODO
		final List<WhiteboardDto> dtos = Collections.emptyList();
		return Response.ok(dtos).build();
	}

}
