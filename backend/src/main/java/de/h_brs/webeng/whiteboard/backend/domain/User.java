package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class User {
    private String firstname;

    private String lastname;

    private String username;

    private String password;

    private List<Whiteboard> registeredWhiteboards;
    
    
    public User(String username) {
    	this.username = username;
    }
    
    public User(String username, String firstname, String lastname) {
    	this(username);
    	this.firstname = firstname;
    	this.lastname = lastname;
    }
    
    public User(String username, String firstname, String lastname, String password) {
    	this(username, firstname, lastname);
    	this.password = password;
    }

}
