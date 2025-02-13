package com.eclecticsassignment.cards.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.eclecticsassignment.cards.config.JwtUtil;
import com.eclecticsassignment.cards.dbconn.DBConn;
import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.entity.User;
import com.eclecticsassignment.cards.model.CardModel;
import com.eclecticsassignment.cards.model.UpdateCardNameModel;
import com.eclecticsassignment.cards.repository.UserRepository;
import com.eclecticsassignment.cards.service.CardService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class CardController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final HttpServletRequest request;
    private final CardService cardService;

    public CardController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          HttpServletRequest request,
                          CardService cardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.request = request;
        this.cardService = cardService;
    }

    /**
     * Endpoint to create a new user with a BCrypt-hashed password
     */
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody User user) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		User existingUser = userRepository.getUserByEmail(user.getEmail());
    		if(existingUser != null) {
    			log.info("The user {} already exists.", user.getEmail());
        		res.put("status", "1010");
                res.put("message", "The user " + user.getEmail() + " already exists.");
                return ResponseEntity.ok().body(res);
    		}

        	String username = JwtUtil.extractUsername(JwtUtil.getTokenFromRequest(request));
        	User currentUser = userRepository.getUserByEmail(username);
        	
        	if(!currentUser.getRole().equals("Admin")) {
        		log.info("The user {} already exists.", username);
        		res.put("status", "1010");
                res.put("message", "The user " + username + " is not an admin. Only admins can create users.");
                return ResponseEntity.ok().body(res);
        	}
    		
    		user.setPassword(passwordEncoder.encode(user.getPassword()));
            User createdUser = userRepository.save(user);
            
            HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
            
            res.put("status", "1001");
            res.put("message", "User created successfully.");
            res.put("user", createdUser);
            
            return ResponseEntity.ok().headers(headers).body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }

    /**
     * Endpoint to authenticate a user and return a JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String email = credentials.get("email");
            String password = credentials.get("password");

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String token = JwtUtil.generateToken(userDetails);
            
            res.put("status", "1001");
            res.put("message", "Authentication successful.");
            res.put("token", token);
            
            HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			return ResponseEntity.ok().headers(headers).body(res);
            
    	} catch (AuthenticationException e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "Authentication failed.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @GetMapping("/testConn")
    public ResponseEntity<?> testConn() {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String connStatus = DBConn.testConn();
            if(connStatus.equals("Success")) {
            	res.put("status", "1001");
                res.put("message", "Db connection successful.");
                HttpHeaders headers = new HttpHeaders();
    			headers.setContentType(MediaType.APPLICATION_JSON);

    			return ResponseEntity.ok().headers(headers).body(res);
            } else {
            	res.put("status", "1010");
                res.put("message", "Db connection failed.");
                return ResponseEntity.ok().body(res);
            }
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/createCard")
    public ResponseEntity<?> createCard(@RequestBody CardModel cardModel) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String cardName = cardModel.getName();
    		
    		if(cardName.isEmpty() || cardName == null) {
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String cardColor = cardModel.getColor();
    		
    		if(cardColor != null && !cardColor.isEmpty()) {
    			if(!Card.isValidHexColor(cardColor)) {
    				res.put("status", "1010");
                    res.put("message", "Color has to be of the format #12FF56 (# followed by 6 digits or characters between a-f).");
                    return ResponseEntity.ok().body(res);
    			}
    		}
    		
    		String cardDescription = cardModel.getDescription();
    		
    		String username = JwtUtil.extractUsername(JwtUtil.getTokenFromRequest(request));
    		Card newCard = new Card();
    		
    		newCard.setName(cardName);
    		newCard.setColor(cardColor);
    		newCard.setDescription(cardDescription);
    		newCard.setCreator(username);
    		
    		HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
            
            res.put("status", "1001");
            res.put("message", "Card created successfully.");
            res.put("card", newCard);
            
            return ResponseEntity.ok().headers(headers).body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/updateCardName")
    public ResponseEntity<?> updateCardName(@RequestBody UpdateCardNameModel updateCardNameModel){
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String currentCardName = updateCardNameModel.getOldCardName();
    		Card existingCard = cardService.getCardByName(currentCardName);
    		
    		//implement logic for updating card name
    		return ResponseEntity.ok().body(res);
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/updateCardDetails")
    public Map<String, String> updateCard(@RequestBody CardModel cardModel) {
        Card card = new Card();
        return Map.of("message", "Card updated successfully.");
    }
    
    @GetMapping("/getAllCards")
    public ResponseEntity<?> getAllCards() {
//        String connStatus = DBConn.testConn();
//        if(connStatus.equals("Success")) {
//        	return Map.of("status", "Db connection successful.");
//        } else {
//        	return Map.of("status", "Db connection failed!");
//        }
    	return ResponseEntity.ok().body(null);
    }
    
    @GetMapping("/getCardsPerUser")
    public ResponseEntity<?> getCardsPerUser() {
//        String connStatus = DBConn.testConn();
//        if(connStatus.equals("Success")) {
//        	return Map.of("status", "Db connection successful.");
//        } else {
//        	return Map.of("status", "Db connection failed!");
//        }
    	return ResponseEntity.ok().body(null);
    }
    
    @GetMapping("/getSingleCard")
    public ResponseEntity<?> getSingleCard(@RequestParam("cardName") String cardName) {
//        String connStatus = DBConn.testConn();
//        if(connStatus.equals("Success")) {
//        	return Map.of("status", "Db connection successful.");
//        } else {
//        	return Map.of("status", "Db connection failed!");
//        }
    	return ResponseEntity.ok().body(null);
    }
}

