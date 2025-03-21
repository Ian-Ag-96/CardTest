package com.eclecticsassignment.cards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.repository.CardRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final DataSource dataSource;

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
    
    public List<Card> getAllCardsWithFilters(Map<String, Boolean> sortFilters) throws SQLException, Exception{
    	StringBuilder filterBuilder = new StringBuilder();
    	filterBuilder.append(sortFilters.getOrDefault("name", false) ? "name": "");
    	filterBuilder.append(sortFilters.getOrDefault("color", false) ? filterBuilder.length() == 0 ? "color": ", color" : "");
    	filterBuilder.append(sortFilters.getOrDefault("status", false) ? filterBuilder.length() == 0 ? "status": ", status" : "");
    	filterBuilder.append(sortFilters.getOrDefault("dateCreated", false) ? filterBuilder.length() == 0 ? "date_created": ", date_created" : "");

        boolean ascending = sortFilters.getOrDefault("ascending", false) && filterBuilder.length() != 0;

        boolean descending = sortFilters.getOrDefault("descending", false) && filterBuilder.length() != 0;
    	
    	String filters = filterBuilder.toString();
        log.info("Filters: " + filters + " Ascending: " + ascending + " Descending: " + descending);
    	String getSql = "SELECT * FROM cards WHERE is_active = 'Y'" +
                (filters.isEmpty() ? "" : " ORDER BY " + filters) +
                (ascending ? " ASC" : descending ? " DESC" : "") +
                " LIMIT 10";
    	log.info("SQL: " + getSql);
        List<Card> cards = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
           	 PreparedStatement stmt = conn.prepareStatement(getSql);
        	 ResultSet res = stmt.executeQuery()){
           	 
           	while (res.next()) {
                Card card = new Card();
                card.setName(res.getString("name"));
                card.setDescription(res.getString("description"));
                card.setColor(res.getString("color"));
                card.setStatus(res.getString("status"));
                card.setDateCreated(res.getTimestamp("date_created").toLocalDateTime());
                card.setCreator(res.getString("creator"));
                card.setIsActive(res.getString("is_active").charAt(0));

                cards.add(card);
            }
           	return cards;
       } catch (SQLException e) {
       		throw new SQLException("Error: " + e.getMessage());
       } catch (Exception e) {
       		throw new Exception("Error: " + e.getMessage());
       }
    }
    
    public List<Card> getMemberCardsWithFilters(Map<String, Boolean> sortFilters, String creator) throws SQLException, Exception{
    	StringBuilder filterBuilder = new StringBuilder();
    	filterBuilder.append(sortFilters.getOrDefault("name", false) ? "name": "");
    	filterBuilder.append(sortFilters.getOrDefault("color", false) ? filterBuilder.length() == 0 ? "color": ", color" : "");
    	filterBuilder.append(sortFilters.getOrDefault("status", false) ? filterBuilder.length() == 0 ? "status": ", status" : "");
    	filterBuilder.append(sortFilters.getOrDefault("dateCreated", false) ? filterBuilder.length() == 0 ? "date_created": ", date_created" : "");

        boolean ascending = sortFilters.getOrDefault("ascending", false) && filterBuilder.length() != 0;

        boolean descending = sortFilters.getOrDefault("descending", false) && filterBuilder.length() != 0;
    	
    	String filters = filterBuilder.toString();
    	log.info("Filters: " + filters + " Ascending: " + ascending + " Descending: " + descending);
        String getSql = "SELECT * FROM cards WHERE is_active = 'Y' AND creator = ?" +
                (filters.isEmpty() ? "" : " ORDER BY " + filters) +
                (ascending ? " ASC" : descending ? " DESC" : "") +
                " LIMIT 10";
        log.info("SQL: " + getSql);
    	List<Card> cards = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
           	 PreparedStatement stmt = conn.prepareStatement(getSql)){
           	 
        	stmt.setString(1, creator);
        	ResultSet res = stmt.executeQuery();
        	
           	while (res.next()) {
                Card card = new Card();
                card.setName(res.getString("name"));
                card.setDescription(res.getString("description"));
                card.setColor(res.getString("color"));
                card.setStatus(res.getString("status"));
                card.setDateCreated(res.getTimestamp("date_created").toLocalDateTime());
                card.setCreator(res.getString("creator"));
                card.setIsActive(res.getString("is_active").charAt(0));

                cards.add(card);
            }
           	return cards;
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
    
    public List<Card> getAllCards(Map<String, Boolean> sortFilters) throws SQLException, Exception{
    	if(sortFilters.containsKey("name") || sortFilters.containsKey("color") || sortFilters.containsKey("status") || sortFilters.containsKey("dateCreated")) {
    		return getAllCardsWithFilters(sortFilters);
    	}
    	return cardRepository.getAllCards();
    }
    
    public List<Card> getAllMemberCards(String creator, Map<String, Boolean> sortFilters) throws SQLException, Exception{
    	if(sortFilters.containsKey("name") || sortFilters.containsKey("color") || sortFilters.containsKey("status") || sortFilters.containsKey("dateCreated")) {
    		return getMemberCardsWithFilters(sortFilters, creator);
    	}
    	return cardRepository.getAllMemberCards(creator);
    }
}
