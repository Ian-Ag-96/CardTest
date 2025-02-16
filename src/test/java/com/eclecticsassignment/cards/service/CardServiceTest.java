package com.eclecticsassignment.cards.service;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCard() {
        Card card = new Card();
        card.setName("TestCard");

        when(cardRepository.save(card)).thenReturn(card);

        Card savedCard = cardService.saveCard(card);

        assertNotNull(savedCard);
        assertEquals("TestCard", savedCard.getName());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testGetCardByName() {
        String cardName = "TestCard";
        Card card = new Card();
        card.setName(cardName);

        when(cardRepository.getCardByName(cardName)).thenReturn(card);

        Card fetchedCard = cardService.getCardByName(cardName);

        assertNotNull(fetchedCard);
        assertEquals(cardName, fetchedCard.getName());
        verify(cardRepository, times(1)).getCardByName(cardName);
    }

    @Test
    void testDeleteCard() {
        String cardName = "TestCard";

        doNothing().when(cardRepository).deleteCard(eq(cardName), anyString());

        cardService.deleteCard(cardName);

        verify(cardRepository, times(1)).deleteCard(eq(cardName), anyString());
    }
}

