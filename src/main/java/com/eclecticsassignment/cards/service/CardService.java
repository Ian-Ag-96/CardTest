package com.eclecticsassignment.cards.service;

import org.springframework.stereotype.Service;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.repository.CardRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private DataSource dataSource;

    public CardService(CardRepository cardRepository, DataSource dataSource) {
        this.cardRepository = cardRepository;
        this.dataSource = dataSource;
    }

    public Card insertCard(Card card) throws SQLException, Exception {
    	String insertSql = "insert into cards (name, color, description, creator) values (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
        	 PreparedStatement stmt = conn.prepareStatement(insertSql)){
        	stmt.setString(1, card.getName());
        	stmt.setString(2, card.getColor());
        	stmt.setString(3, card.getDescription());
        	stmt.setString(4, card.getCreator());
        	int affectedRows = stmt.executeUpdate();
        	if(affectedRows > 0) {
        		return cardRepository.getCardByName(card.getName());
        	} else {
        		throw new Exception("Failed to save card.");
        	}
        } catch (SQLException e) {
        	throw new SQLException("Error: " + e.getMessage());
        } catch (Exception e) {
        	throw new Exception("Error: " + e.getMessage());
        }
    }
    
    public Card saveCard(Card card) {
    	return cardRepository.save(card);
    }
    
    public Card getCardByName(String name) {
    	return cardRepository.getCardByName(name);
    }
    
    public int updateCardName(String oldCardName, String newCardName) {
    	return cardRepository.updateCardName(oldCardName, newCardName);
    }
    
    public int deleteCard(String cardName) {
    	String deletedCardName =  cardName + "_deleted_" + LocalDateTime.now().toString();
    	return cardRepository.deleteCard(cardName, deletedCardName);
    }
    
    public List<Card> getAllCards(){
    	return cardRepository.getAllCards();
    }
    
    public List<Card> getAllMemeberCards(String creator){
    	return cardRepository.getAllMemberCards(creator);
    }
}
