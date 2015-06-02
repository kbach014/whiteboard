package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;
import java.util.UUID;

import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public interface ShapeDAO {
	boolean insertShape(Shape shape);

    boolean updateShape(Shape shape);

    boolean deleteShape(Shape shape);

    Shape findShapeByUUID(UUID uuid);

    List<Shape> findAllShapesFromWB(Whiteboard whiteboard);
}
