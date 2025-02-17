package com.eclecticsassignment.cards.service;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mocks
    }

    @Test
    @DisplayName("Test for inserting a card")
    void testInsertCard() throws SQLException, Exception {
        // Arrange
        Card card = new Card("Card1", "#FFFFFF", "Description", "Creator1");
        when(cardRepository.getCardByName("Card1")).thenReturn(card);

        // Act
        Card result = cardService.insertCard(card);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Card1");
        // Purpose: Verifies that a card is inserted successfully into the database.
    }

    @Test
    @DisplayName("Test for handling SQL exception while inserting card")
    void testInsertCardThrowsSQLException() throws SQLException, Exception {
        // Arrange
        Card card = new Card("Card1", "#FFFFFF", "Description", "Creator1");
        when(cardRepository.getCardByName("Card1")).thenThrow(new SQLException("SQL Error"));

        // Act & Assert
        Exception exception = null;
        try {
            cardService.insertCard(card);
        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
        // Purpose: Verifies that the exception is correctly thrown when there is an error while inserting the card.
    }

    @Test
    @DisplayName("Test for getting all cards with filters")
    void testGetAllCardsWithFilters() throws SQLException, Exception {
        // Arrange
        Map<String, Boolean> filters = new HashMap<>();
        filters.put("name", true);
        filters.put("color", false);
        when(cardRepository.getAllCards()).thenReturn(List.of(new Card("Card1", "#FFFFFF", "Description", "Creator1")));

        // Act
        List<Card> result = cardService.getAllCards(filters);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Card1");
        // Purpose: Verifies that the method returns the cards based on filters when using the repository method.
    }

    @Test
    @DisplayName("Test for getting member cards with filters")
    void testGetMemberCardsWithFilters() throws SQLException, Exception {
        // Arrange
        Map<String, Boolean> filters = new HashMap<>();
        filters.put("name", true);
        filters.put("color", false);
        String creator = "Creator1";
        when(cardRepository.getAllMemberCards(creator)).thenReturn(List.of(new Card("Card1", "#FFFFFF", "Description", "Creator1")));

        // Act
        List<Card> result = cardService.getAllMemeberCards(creator, filters);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Card1");
        // Purpose: Verifies that the method returns member cards based on the filters and creator.
    }

    @Test
    @DisplayName("Test for saving a card")
    void testSaveCard() {
        // Arrange
        Card card = new Card("Card1", "#FFFFFF", "Description", "Creator1");
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // Act
        Card result = cardService.saveCard(card);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Card1");
        // Purpose: Verifies that a card is saved correctly using the repository method.
    }

    @Test
    @DisplayName("Test for getting a card by name")
    void testGetCardByName() {
        // Arrange
        Card card = new Card("Card1", "#FFFFFF", "Description", "Creator1");
        when(cardRepository.getCardByName("Card1")).thenReturn(card);

        // Act
        Card result = cardService.getCardByName("Card1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Card1");
        // Purpose: Verifies that a card can be retrieved by its name from the repository.
    }

    @Test
    @DisplayName("Test for updating a card's name")
    void testUpdateCardName() {
        // Arrange
        when(cardRepository.updateCardName("Card1", "NewCard1")).thenReturn(1);

        // Act
        int result = cardService.updateCardName("Card1", "NewCard1");

        // Assert
        assertThat(result).isEqualTo(1);
        // Purpose: Verifies that a card's name can be successfully updated in the repository.
    }

    @Test
    @DisplayName("Test for deleting a card")
    void testDeleteCard() {
        // Arrange
        when(cardRepository.deleteCard("Card1", "Card1_deleted")).thenReturn(1);

        // Act
        int result = cardService.deleteCard("Card1");

        // Assert
        assertThat(result).isEqualTo(1);
        // Purpose: Verifies that a card can be deleted and the method returns the expected result.
    }

    @Test
    @DisplayName("Test for handling SQL exception while deleting a card")
    void testDeleteCardThrowsSQLException() {
        // Arrange
        when(cardRepository.deleteCard("Card1", "Card1_deleted")).thenThrow(new SQLException("SQL Error"));

        // Act & Assert
        Exception exception = null;
        try {
            cardService.deleteCard("Card1");
        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
        // Purpose: Verifies that the exception is thrown when an error occurs while deleting a card.
    }
}
