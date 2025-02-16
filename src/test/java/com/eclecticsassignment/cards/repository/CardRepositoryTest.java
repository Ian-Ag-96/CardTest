package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.Card;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void testGetCardByName() {
        Card card = new Card();
        card.setName("TestCard");
        card.setIsActive('Y');
        cardRepository.save(card);

        Card fetchedCard = cardRepository.getCardByName("TestCard");

        assertNotNull(fetchedCard);
        assertEquals("TestCard", fetchedCard.getName());
    }

    @Test
    void testGetAllCards() {
        Card card1 = new Card();
        card1.setName("Card1");
        card1.setIsActive('Y');

        Card card2 = new Card();
        card2.setName("Card2");
        card2.setIsActive('Y');

        cardRepository.save(card1);
        cardRepository.save(card2);

        List<Card> cards = cardRepository.getAllCards();

        assertEquals(2, cards.size());
    }
}
