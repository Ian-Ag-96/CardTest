package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        // Insert initial data into the in-memory database for testing
        Card card1 = new Card("Card1", "Test card 1", "#FFFFFF", "To Do", null, "user1", 'Y');
        Card card2 = new Card("Card2", "Test card 2", "#000000", "In Progress", null, "user2", 'Y');
        Card card3 = new Card("Card3", "Test card 3", "#FF5733", "Done", null, "user1", 'N'); // Inactive card
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);
    }

    /**
     * Test the retrieval of a card by its name.
     * Verifies that a card with the given name exists and matches expected attributes.
     */
    @Test
    void testGetCardByName() {
        Card card = cardRepository.getCardByName("Card1");
        assertNotNull(card);
        assertEquals("Card1", card.getName());
    }

    /**
     * Test retrieving a card that doesn't exist in the database.
     * Verifies that the result is null when no matching card is found.
     */
    @Test
    void testGetCardByName_NotFound() {
        Card card = cardRepository.getCardByName("NonExistent");
        assertNull(card);
    }

    /**
     * Test updating the name of a card.
     * Verifies that the update affects exactly one row and that the card's name is updated in the database.
     */
    @Test
    void testUpdateCardName() {
        int rowsAffected = cardRepository.updateCardName("Card1", "UpdatedCard1");
        assertEquals(1, rowsAffected);

        Card updatedCard = cardRepository.getCardByName("UpdatedCard1");
        assertNotNull(updatedCard);
        assertEquals("UpdatedCard1", updatedCard.getName());
    }

    /**
     * Test soft-deleting a card by updating its name and active status.
     * Verifies that the card is marked inactive and renamed, and no longer accessible by its original name.
     */
    @Test
    void testDeleteCard() {
        int rowsAffected = cardRepository.deleteCard("Card2", "DeletedCard2");
        assertEquals(1, rowsAffected);

        // Original card should no longer exist
        Card deletedCard = cardRepository.getCardByName("Card2");
        assertNull(deletedCard);

        // Renamed card should exist with inactive status
        Card renamedCard = cardRepository.getCardByName("DeletedCard2");
        assertNotNull(renamedCard);
        assertEquals('N', renamedCard.getIsActive());
    }

    /**
     * Test retrieving all active cards.
     * Verifies that only active cards are returned and their count matches the expected value.
     */
    @Test
    void testGetAllCards() {
        List<Card> cards = cardRepository.getAllCards();
        assertNotNull(cards);
        assertEquals(2, cards.size()); // Only active cards
    }

    /**
     * Test retrieving all active cards created by a specific user.
     * Verifies that only the cards created by the given user are returned.
     */
    @Test
    void testGetAllMemberCards() {
        List<Card> cards = cardRepository.getAllMemberCards("user1");
        assertNotNull(cards);
        assertEquals(1, cards.size());
        assertEquals("Card1", cards.get(0).getName());
    }
}