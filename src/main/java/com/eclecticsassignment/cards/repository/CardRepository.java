package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, String> {
	@Query(value = "SELECT * FROM cards WHERE name = :name LIMIT 1", nativeQuery = true)
    Card getCardByName(@Param("name") String name);
}

