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
import com.eclecticsassignment.cards.repository.CardRepository;
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
            res.put("cause", e.getMessage());
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
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            res.put("cause", e.getMessage());
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
            res.put("cause", e.getMessage());
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
    		
    		if(cardService.getCardByName(cardName) != null) {
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " already exists.");
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
    		
    		cardService.saveCard(newCard);
    		
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
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/updateCardName")
    public ResponseEntity<?> updateCardName(@RequestBody UpdateCardNameModel updateCardNameModel){
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String currentCardName = updateCardNameModel.getOldCardName();
    		
    		if(currentCardName.isEmpty() || currentCardName == null) {
    			res.put("status", "1010");
                res.put("message", "Old Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String newCardName = updateCardNameModel.getNewCardName();
    		
    		if(newCardName.isEmpty() || newCardName == null) {
    			res.put("status", "1010");
                res.put("message", "Old Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(currentCardName.equals(newCardName)) {
    			res.put("status", "1010");
                res.put("message", "Old Card Name is the same as New Card Name.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		Card currentCard = cardService.getCardByName(currentCardName);
    		
    		if(currentCard != null) {
    			String creator = JwtUtil.extractUsername(JwtUtil.getTokenFromRequest(request));
    			User user = userRepository.getUserByEmail(creator);
    			if(currentCard.getCreator().equals(creator) || user.getRole().equals("Admin")) {
    				cardService.updateCardName(currentCardName, newCardName);
        			res.put("status", "1001");
                    res.put("message", "Card updated successfully.");
                    
                    Card newCard = cardService.getCardByName(newCardName);
                    res.put("card", newCard);
                    
                    HttpHeaders headers = new HttpHeaders();
        			headers.setContentType(MediaType.APPLICATION_JSON);

        			return ResponseEntity.ok().headers(headers).body(res);
        			
    			} else {
    				res.put("status", "1010");
                    res.put("message", "The user is not an admin nor the card creator. They cannot update the card.");
                    return ResponseEntity.ok().body(res);
    			}
    		} else {
    			res.put("status", "1010");
                res.put("message", "Card with name " + currentCardName + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/updateCardDetails")
    public ResponseEntity<?> updateCardDetails(@RequestBody Map<String, String> cardModel) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String cardName = cardModel.get("name");
    		
    		if(cardName.isEmpty() || cardName == null) {
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		Card cardForUpdate = cardService.getCardByName(cardName);
    		
    		if(cardService.getCardByName(cardName) == null) {
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(!cardModel.containsKey("color") && !cardModel.containsKey("description") && !cardModel.containsKey("status")) {
    			res.put("status", "1010");
                res.put("message", "Both color, status, and description are absent. No updates can be made. Add at least one of these fields to update.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String creator = JwtUtil.extractUsername(JwtUtil.getTokenFromRequest(request));
			User user = userRepository.getUserByEmail(creator);
			if(!cardForUpdate.getCreator().equals(creator) && !user.getRole().equals("Admin")) {
				res.put("status", "1010");
                res.put("message", "The user is not an admin nor the card creator. They cannot update the card.");
                return ResponseEntity.ok().body(res);
			}
    		
    		String color = cardModel.containsKey("color") ? cardModel.get("color") : null;
    		String description = cardModel.containsKey("description") ? cardModel.get("description") : null;
    		String status = cardModel.containsKey("status") ? cardModel.get("status") : null;
    		
    		if(color != null) cardForUpdate.setColor(color);
    		if(description != null) cardForUpdate.setDescription(description);
    		if(status != null) cardForUpdate.setStatus(status);
    		
    		cardService.saveCard(cardForUpdate);
    		
    		res.put("status", "1001");
            res.put("message", "Card updated successfully.");
            res.put("card", cardForUpdate);
            
            HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			return ResponseEntity.ok().headers(headers).body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @PostMapping("/deleteCard")
    public ResponseEntity<?> deleteCard(@RequestBody String cardName){
    	Map<String, Object> res = new HashMap<>();
    	try {
    		if(cardName.isEmpty() || cardName == null) {
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		Card cardForUpdate = cardService.getCardByName(cardName);
    		
    		if(cardService.getCardByName(cardName) == null) {
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		cardService.deleteCard(cardName);
    		res.put("status", "1001");
            res.put("message", "Card deleted successfully.");
            res.put("card", cardForUpdate);
            
            HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			return ResponseEntity.ok().headers(headers).body(res);
    		
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
    }
    
    @GetMapping("/getAllCards")
    public ResponseEntity<?> getAllCards(@RequestBody String username) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		//implement fetch for user type admin or member
    		return ResponseEntity.ok().body(res);
    	} catch (Exception e) {
    		log.info("An error occurred. Error: {}", e.getMessage());
    		res.put("status", "1010");
            res.put("message", "An error ocurred.");
            res.put("cause", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok().body(res);
    	}
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

