package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Whiteboard {
	private long wbid;

    private String creator;

    private List<User> artists;

    private List<Shape> finishedShapes;

    public Whiteboard(long wbid, String creator) {
		this.wbid = wbid;
		this.creator = creator;
	}
    
    @Override
    public String toString() {
    	String whiteboardInfo = "WB-ID:"+getWbid()+"\n"
    					+ "Creator:"+getCreator()+"\n";
    	
		return whiteboardInfo;
    }
}
