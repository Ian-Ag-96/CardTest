package com.eclecticsassignment.cards.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCardNameModel {
	private String oldCardName;
	private String newCardName;
}
