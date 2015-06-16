package de.h_brs.webeng.whiteboard.backend.dao;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;

public interface ShapeDAO {
	void insertRect(Rectangle rect) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException;
	
	void insertPath(Path path) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException;
	
	void insertCircle(Circle cirlce) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException;

    boolean updateShape(Shape shape);

    boolean deleteShape(Shape shape);

    Shape findShapeByUUID(UUID uuid);

    List<Shape> findAllShapesFromWB(Whiteboard whiteboard) throws WhiteboardNotFoundException, JsonParseException, JsonMappingException, IOException;
}

