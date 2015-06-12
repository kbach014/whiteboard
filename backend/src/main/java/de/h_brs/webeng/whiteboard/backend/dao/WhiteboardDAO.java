package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;

import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public interface WhiteboardDAO {
	Whiteboard insertWhiteboard(User creator) throws UserNotFoundException;

    boolean updateWhiteboard(Whiteboard whiteboard);

    boolean deleteWhiteboard(Whiteboard whiteboard);

    Whiteboard findWhiteboardByID(long id) throws WhiteboardNotFoundException;

    List<Whiteboard> findWhiteboards(int start, int count);

	List<Whiteboard> findRegisteredWhiteboards(User user) throws UserNotFoundException;
	
	List<Whiteboard> findUnregisteredWhiteboards(User user, int start, int count) throws UserNotFoundException;
	
	public void setPublicity(Whiteboard wb, int mode);
    
    boolean whiteboardExists(String wbid);
    
    boolean whiteboardExists(Whiteboard whiteboard);
}
