package de.h_brs.webeng.whiteboard.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.h_brs.webeng.whiteboard.backend.dao.WhiteboardDAO;
import de.h_brs.webeng.whiteboard.backend.dao.exception.UserNotFoundException;
import de.h_brs.webeng.whiteboard.backend.dao.exception.WhiteboardNotFoundException;
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
			dto.setAccessType(whiteboard.getAccessType());
			return Response.ok(dto).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(WhiteboardDto whiteboardDto, @PathParam("id") Long whiteboardId) {
		try {
			final WhiteboardDAO whiteboardDao = new RedisWhiteboardDAO();
			whiteboardDao.setAccessType(whiteboardId, whiteboardDto.getAccessType());

			return Response.noContent().build();
		} catch (WhiteboardNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
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
			final List<Whiteboard> createdWhiteboards = whiteboardDao.findCreatedWhiteboards(username);
			final List<Whiteboard> registeredWhiteboards = whiteboardDao.findRegisteredWhiteboards(username);

			// Fetch all Whiteboards which the user OWNS
			final List<WhiteboardDto> createdDtos = createdWhiteboards.stream().map(wb -> {
				WhiteboardDto dto = new WhiteboardDto();
				dto.setId(wb.getWbid());
				dto.setCreator(wb.getCreator());
				dto.setAccessType(wb.getAccessType());
				return dto;
			}).collect(Collectors.toList());

			// Fetch all Whiteboards which the user VISITS
			final List<WhiteboardDto> registeredDtos = registeredWhiteboards.stream().map(wb -> {
				WhiteboardDto dto = new WhiteboardDto();
				dto.setId(wb.getWbid());
				dto.setCreator(wb.getCreator());
				dto.setAccessType(wb.getAccessType());
				return dto;
			}).collect(Collectors.toList());

			// Merge OWN-List and VISIT-List
			final List<WhiteboardDto> allDtos = new ArrayList<WhiteboardDto>();
			allDtos.addAll(createdDtos);
			allDtos.addAll(registeredDtos);

			return Response.ok(allDtos).build();
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

		try {
			final WhiteboardDAO whiteboardDao = new RedisWhiteboardDAO();
			final List<Whiteboard> unregisteredWhiteboards = whiteboardDao.findRegisteredWhiteboards(username);

			final List<WhiteboardDto> unregisteredDtos = unregisteredWhiteboards.stream().map(wb -> {
				WhiteboardDto dto = new WhiteboardDto();
				dto.setId(wb.getWbid());
				dto.setCreator(wb.getCreator());
				dto.setAccessType(wb.getAccessType());
				return dto;
			}).collect(Collectors.toList());

			return Response.ok(unregisteredDtos).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

	}

}
