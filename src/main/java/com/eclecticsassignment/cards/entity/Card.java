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
    
    public static boolean isValidHexColor(String color) {
        String regex = "#[a-fA-F0-9]{6}";
        return color != null && color.matches(regex);
    }
    
    public static boolean isValidStatus(String status) {
    	return status.equals("To Do") || status.equals("In Progress") || status.equals("Done");
    }
    
    @Override
    public String toString() {
        return "Card{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", color='" + color + '\'' +
               ", status='" + status + '\'' +
               ", dateCreated=" + dateCreated +
               ", creator='" + creator + '\'' +
               ", isActive=" + isActive +
               '}';
    }

	public Card(String name, String description, String color, String creator) {
		super();
		this.name = name;
		this.description = description;
		this.color = color;
		this.creator = creator;
	}
}

