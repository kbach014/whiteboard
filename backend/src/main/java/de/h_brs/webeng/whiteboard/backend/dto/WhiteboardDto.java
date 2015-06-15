package de.h_brs.webeng.whiteboard.backend.dto;

import de.h_brs.webeng.whiteboard.backend.domain.AccessType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WhiteboardDto {
	
	private Long id;
	private String creator;
	private AccessType accessType;

}
