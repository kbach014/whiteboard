package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;

import de.h_brs.webeng.whiteboard.backend.domain.Shape;
import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public interface WhiteboardDAO {
	long insertWhiteboard(Whiteboard whiteboard);

    boolean updateWhiteboard(Whiteboard whiteboard);

    boolean deleteWhiteboard(Whiteboard whiteboard);

    Whiteboard findWhiteboardByID(long id);

    List<Whiteboard> findAllWhiteboards();

    boolean addShapeToWB(Shape shape, Whiteboard whiteboard);

    List<Shape> getAllShapesFromWB(Whiteboard whiteboard);

    boolean removeShapeFromWB(Shape shape, Whiteboard whiteboard);

    boolean addUserToWB(User user, Whiteboard whiteboard);

    boolean removeUserFromWB(User user, Whiteboard whiteboard, String p3);

    List<User> getAllUsersFromWB(Whiteboard whiteboard);
}
