package de.h_brs.webeng.whiteboard.backend.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CredentialDto {
	
	private String username;
	private String password;

}
