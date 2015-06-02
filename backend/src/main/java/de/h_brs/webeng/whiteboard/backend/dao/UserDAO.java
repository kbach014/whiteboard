package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;

import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public interface UserDAO {
	boolean updateUser(User user);

	boolean deleteUser(User user);

	User findUserByUsername(String username);

	boolean register(User user);

	boolean login(User user);

	List<Whiteboard> findRegisteredWhiteboards(User user);

	boolean registerToWhiteboard(User user, Whiteboard whiteboard);
}
