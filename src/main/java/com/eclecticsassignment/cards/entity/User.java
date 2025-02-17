package com.eclecticsassignment.cards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User {

    @Id
    private String email;

    @JsonIgnore
    private String password;
    private String role;

    @Column(name = "date_created", updatable = false, insertable = false)
    private LocalDateTime dateCreated;
    
    public static boolean isValidRole(String role) {
    	return role.equals("Admin") || role.equals("Member");
    }
    
    public User (String email, String password, String role) {
    	this.email = email;
    	this.password = password;
    	this.role = role;
    }
}

