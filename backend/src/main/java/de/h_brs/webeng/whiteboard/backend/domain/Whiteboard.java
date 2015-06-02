package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Whiteboard {
	private long wbid;

    private User creator;

    private User[] artists;

    private Shape[] finishedShapes;

    public Whiteboard(long wbid, User creator) {
		this.wbid = wbid;
		this.creator = creator;
	}
}
