package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;

import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.*;

public interface UserDAO {
	boolean updateUser(User user) throws UserNotFoundException;

	boolean deleteUser(User user);

	User findUserByUsername(String username) throws UserNotFoundException;

	void register(User user) throws UserAlreadyRegisteredException;

	User login(String username, String password) throws UserNotFoundException, PasswordIncorrectException;

	void registerToWhiteboard(String username, Long whiteboardId) throws UserNotFoundException, WhiteboardNotFoundException, UserWhiteboardException;
	
    List<User> findAllUsersFromWB(Whiteboard whiteboard) throws WhiteboardNotFoundException;
    
    public boolean userHasWhiteboard(User user, Whiteboard whiteboard) throws UserNotFoundException, WhiteboardNotFoundException;
	
    public boolean userHasWhiteboard(String username, Long wbID) throws UserNotFoundException, WhiteboardNotFoundException; 
			
	boolean userExists(String username);
	
	boolean userExists(User user);
}

