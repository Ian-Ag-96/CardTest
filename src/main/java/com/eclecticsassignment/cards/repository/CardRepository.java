package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.Card;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, String> {
	@Query(value = "SELECT * FROM cards WHERE name = :name AND is_active = 'Y' LIMIT 1", nativeQuery = true)
    Card getCardByName(@Param("name") String name);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE cards SET name = :newCardName WHERE name = :oldCardName", nativeQuery = true)
    int updateCardName(@Param("oldCardName") String oldCardName, @Param("newCardName") String newCardName);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE cards SET is_active = 'N', name =:deletedCardName WHERE name = :cardName", nativeQuery = true)
    int deleteCard(@Param("cardName") String cardName, @Param("deletedCardName") String deletedCardName);
	
	//Add allowance for sorting by name, color, status, date of creation
	@Query(value = "SELECT * FROM cards WHERE is_active = 'Y' LIMIT 10", nativeQuery = true)
    List<Card> getAllCards();
	
	@Query(value = "SELECT * FROM cards WHERE is_active = 'Y' AND creator = :creator LIMIT 10", nativeQuery = true)
    List<Card> getAllMemberCards(@Param("creator") String creator);
}

