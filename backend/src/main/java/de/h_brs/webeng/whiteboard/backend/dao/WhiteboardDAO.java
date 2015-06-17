package de.h_brs.webeng.whiteboard.backend.dao;

import java.util.List;

import de.h_brs.webeng.whiteboard.backend.dao.exception.*;
import de.h_brs.webeng.whiteboard.backend.domain.AccessType;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;

public interface WhiteboardDAO {
	Whiteboard insertWhiteboard(String creatorName) throws UserNotFoundException;

    boolean updateWhiteboard(Whiteboard whiteboard);

    boolean deleteWhiteboard(Whiteboard whiteboard);

    Whiteboard findWhiteboardByID(long id) throws WhiteboardNotFoundException;

    List<Whiteboard> findWhiteboards(int start, int count);

	List<Whiteboard> findRegisteredWhiteboards(String username) throws UserNotFoundException;
	
	List<Whiteboard> findUnregisteredWhiteboards(String username, int start, int count) throws UserNotFoundException;
	
	List<Whiteboard> findCreatedWhiteboards(String username) throws UserNotFoundException;
	
	void setAccessType(Long wbid, AccessType accessType) throws WhiteboardNotFoundException;
    
    boolean whiteboardExists(Long wbid);
    
    boolean whiteboardExists(Whiteboard whiteboard);
}
