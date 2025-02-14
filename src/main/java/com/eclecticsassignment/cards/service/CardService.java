package com.eclecticsassignment.cards.service;

import org.springframework.stereotype.Service;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.repository.CardRepository;

import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }
    
    public Card getCardByName(String name) {
    	return cardRepository.getCardByName(name);
    }
    
    public void updateCardName(String oldCardName, String newCardName) {
    	cardRepository.updateCardName(oldCardName, newCardName);
    }
    
    public void deleteCard(String cardName) {
    	cardRepository.deleteCard(cardName);
    }

    public Optional<Card> updateCard(String name, Card cardDetails) {
        return cardRepository.findById(name).map(existingCard -> {
            existingCard.setDescription(cardDetails.getDescription());
            existingCard.setColor(cardDetails.getColor());
            existingCard.setStatus(cardDetails.getStatus());
            existingCard.setIsActive(cardDetails.getIsActive());
            return cardRepository.save(existingCard);
        });
    }
}
