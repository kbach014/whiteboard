package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;
import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;

public interface ShapeDAO {
	void insertRect(Rectangle rect) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException;
	
	void insertCircle(Circle cirlce) throws UserNotFoundException, WhiteboardNotFoundException, IllegalShapeException;

    boolean updateShape(Shape shape);

    boolean deleteShape(Shape shape);

    Shape findShapeByUUID(UUID uuid);

    HashMap<String, List<Shape>> findAllShapesFromWB(Whiteboard whiteboard) throws WhiteboardNotFoundException;
}

