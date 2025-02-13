package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
	@Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    User getUserByEmail(@Param("email") String email);
}
