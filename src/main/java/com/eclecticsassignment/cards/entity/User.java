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
}

