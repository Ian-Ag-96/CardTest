package com.eclecticsassignment.cards.controller;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.model.CardModel;
import com.eclecticsassignment.cards.service.CardService;
import com.eclecticsassignment.cards.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTest {

    @InjectMocks
    private CardController cardController;

    @Mock
    private CardService cardService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCard_ValidCard() {
        CardModel cardModel = new CardModel();
        cardModel.setName("TestCard");
        cardModel.setColor("#FFFFFF");
        cardModel.setDescription("This is a test card");

        Card newCard = new Card();
        newCard.setName(cardModel.getName());
        newCard.setColor(cardModel.getColor());
        newCard.setDescription(cardModel.getDescription());
        newCard.setCreator("test_user");

        when(jwtUtil.extractUsername(anyString())).thenReturn("test_user");
        when(cardService.getCardByName(cardModel.getName())).thenReturn(null);
        when(cardService.saveCard(any(Card.class))).thenReturn(newCard);

        ResponseEntity<?> response = cardController.createCard(cardModel);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1001", responseBody.get("status"));
        assertEquals("Card created successfully.", responseBody.get("message"));
    }

    @Test
    void testCreateCard_InvalidCardName() {
        CardModel cardModel = new CardModel();
        cardModel.setName("");

        ResponseEntity<?> response = cardController.createCard(cardModel);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Card Name cannot be null or empty.", responseBody.get("message"));
    }

    @Test
    void testCreateCard_DuplicateCardName() {
        CardModel cardModel = new CardModel();
        cardModel.setName("TestCard");

        when(cardService.getCardByName(cardModel.getName())).thenReturn(new Card());

        ResponseEntity<?> response = cardController.createCard(cardModel);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Card with name TestCard already exists.", responseBody.get("message"));
    }
}
