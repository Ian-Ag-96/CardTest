package com.eclecticsassignment.cards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Card {
    
    @Id
    private String name;

    private String description;
    private String color;
    private String status;

    @Column(name = "date_created", updatable = false, insertable = false)
    private LocalDateTime dateCreated;

    private String creator;

    @Column(name = "is_active")
    private char isActive;
    
    public static boolean isValidHexColor(String input) {
        String regex = "#[a-fA-F0-9]{6}";
        return input != null && input.matches(regex);
    }
}

