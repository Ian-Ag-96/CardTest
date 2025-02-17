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

import com.eclecticsassignment.cards.dbconn.DBConn;
import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.entity.User;
import com.eclecticsassignment.cards.model.CardModel;
import com.eclecticsassignment.cards.model.UpdateCardNameModel;
import com.eclecticsassignment.cards.model.UserModel;
import com.eclecticsassignment.cards.repository.UserRepository;
import com.eclecticsassignment.cards.service.CardService;
import com.eclecticsassignment.cards.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
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
    private final JwtUtil jwtUtil;

    public CardController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          HttpServletRequest request,
                          CardService cardService,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.request = request;
        this.cardService = cardService;
        this.jwtUtil = jwtUtil;
    }

    //Gets the current user details from the auth token
    @GetMapping("/getCurrentUser")
    public ResponseEntity<?> getCurrentUser(){
    	log.info("Running api getCurrentUser...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String username = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    		log.info("Fetching user...");
    		User user = userRepository.getUserByEmail(username);
    		
    		if(user == null) {
    			log.info("User not found. Token must be invalid.");
        		res.put("status", "1010");
                res.put("message", "User not found. Token must be invalid.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("User found.");
    		
    		HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
            
            res.put("status", "1001");
            res.put("message", "User found.");
            res.put("user", user);
            
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
    
    //Creates a user. Only users with role admin can perform this function
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody UserModel userModel) {
    	log.info("Running api createUser...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String email = userModel.getEmail();
    		if(email.isEmpty() || email == null) {
    			log.info("Email parameter cannot be empty or missing.");
        		res.put("status", "1010");
                res.put("message", "Email parameter cannot be empty or missing.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String password = userModel.getPassword();
    		if(password.isEmpty() || password == null) {
    			log.info("Password parameter cannot be empty or missing.");
        		res.put("status", "1010");
                res.put("message", "Password parameter cannot be empty or missing.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String role = userModel.getRole();
    		if(role.isEmpty() || role == null) {
    			log.info("Role parameter cannot be empty or missing.");
        		res.put("status", "1010");
                res.put("message", "Role parameter cannot be empty or missing.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(!User.isValidRole(role)) {
    			log.info("Role is invalid. Role can only be 'Member' or 'Admin'");
        		res.put("status", "1010");
                res.put("message", "Role is invalid. Role can only be 'Member' or 'Admin'");
                return ResponseEntity.ok().body(res);
    		}
    		
    		User existingUser = userRepository.getUserByEmail(userModel.getEmail());
    		if(existingUser != null) {
    			log.info("The user {} already exists.", userModel.getEmail());
        		res.put("status", "1010");
                res.put("message", "The user " + userModel.getEmail() + " already exists.");
                return ResponseEntity.ok().body(res);
    		}

        	String username = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
        	User currentUser = userRepository.getUserByEmail(username);
        	
        	if(!currentUser.getRole().equals("Admin")) {
        		log.info("The user {} is not an admin. Only admins can create users.", username);
        		res.put("status", "1010");
                res.put("message", "The user " + username + " is not an admin. Only admins can create users.");
                return ResponseEntity.ok().body(res);
        	}
        	
        	log.info("Creating user with details: email: {}, password: [Hidden], role: {}", email, role);
        	
    		User newUser = new User();
    		newUser.setEmail(userModel.getEmail());
    		newUser.setPassword(passwordEncoder.encode(userModel.getPassword()));
    		newUser.setRole(userModel.getRole());
    		
            userRepository.save(newUser);
            
            User createdUser = userRepository.getUserByEmail(username);
            
            HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			log.info("User created successfully.");
            
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

    //Authenticates a user and returns a JWT token for subsequent requests
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    	log.info("Running api login...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String email = credentials.get("email");
    		if(email.isEmpty() || email == null) {
    			log.info("Email parameter cannot be empty or missing.");
        		res.put("status", "1010");
                res.put("message", "Email parameter cannot be empty or missing.");
                return ResponseEntity.ok().body(res);
    		}
    		
            String password = credentials.get("password");
            if(password.isEmpty() || password == null) {
    			log.info("Password parameter cannot be empty or missing.");
        		res.put("status", "1010");
                res.put("message", "Password parameter cannot be empty or missing.");
                return ResponseEntity.ok().body(res);
    		}
            
            User existingUser = userRepository.getUserByEmail(email);
    		if(existingUser == null) {
    			log.info("The user {} does not exist.", email);
        		res.put("status", "1010");
                res.put("message", "The user " + email + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
            
            log.info("Beginning authentication for user {}", email);
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtUtil.generateToken(userDetails);
            
            log.info("User successfully authenticated.");
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
    
    //Tests whether the DBConnection is active or available
    @GetMapping("/testConn")
    public ResponseEntity<?> testConn() {
    	log.info("Running api testConn...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String connStatus = DBConn.testConn();
            if(connStatus.equals("Success")) {
            	log.info("Connection successful.");
            	res.put("status", "1001");
                res.put("message", "Db connection successful.");
                HttpHeaders headers = new HttpHeaders();
    			headers.setContentType(MediaType.APPLICATION_JSON);

    			return ResponseEntity.ok().headers(headers).body(res);
            } else {
            	log.info("Connection failed.");
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
    
    //Creates a card with the current authenticated user as the creator
    @PostMapping("/createCard")
    public ResponseEntity<?> createCard(@RequestBody CardModel cardModel) {
    	log.info("Running api createCard...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String cardName = cardModel.getName();
    		
    		if(cardName.isEmpty() || cardName == null) {
    			log.info("Card Name cannot be null or empty.");
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(cardService.getCardByName(cardName) != null) {
    			log.info("Card with name " + cardName + " already exists.");
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " already exists.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String cardColor = cardModel.getColor();
    		
    		if(cardColor != null && !cardColor.equals("")) {
    			if(!Card.isValidHexColor(cardColor)) {
    				log.info("Color has to be of the format #12FF56 (# followed by 6 digits or characters between a-f).");
    				res.put("status", "1010");
                    res.put("message", "Color has to be of the format #12FF56 (# followed by 6 digits or characters between a-f).");
                    return ResponseEntity.ok().body(res);
    			}
    		}
    		
    		String cardDescription = cardModel.getDescription();
    		
    		String username = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    		
    		log.info("Creating new card...");
    		Card newCard = new Card();
    		
    		newCard.setName(cardName);
    		newCard.setColor(cardColor);
    		newCard.setDescription(cardDescription);
    		newCard.setCreator(username);
    		
    		log.info("Card details: " + newCard.toString());
    		
    		cardService.insertCard(newCard);
    		
    		Card createdCard = cardService.getCardByName(cardName);
    		
    		HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
            
			log.info("Card created successfully.");
            res.put("status", "1001");
            res.put("message", "Card created successfully.");
            res.put("card", createdCard);
            
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
    
    //Updates the name of the card.
    @PostMapping("/updateCardName")
    public ResponseEntity<?> updateCardName(@RequestBody UpdateCardNameModel updateCardNameModel){
    	log.info("Running api updateCardName...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String currentCardName = updateCardNameModel.getOldCardName();
    		
    		if(currentCardName.isEmpty() || currentCardName == null) {
    			log.info("Old Card Name cannot be null or empty.");
    			res.put("status", "1010");
                res.put("message", "Old Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String newCardName = updateCardNameModel.getNewCardName();
    		
    		if(newCardName.isEmpty() || newCardName == null) {
    			log.info("New Card Name cannot be null or empty.");
    			res.put("status", "1010");
                res.put("message", "New Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(currentCardName.equals(newCardName)) {
    			log.info("Old Card Name is the same as New Card Name.");
    			res.put("status", "1010");
                res.put("message", "Old Card Name is the same as New Card Name.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("Fetching the current card...");
    		Card currentCard = cardService.getCardByName(currentCardName);
    		
    		if(currentCard != null) {
    			String creator = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    			User user = userRepository.getUserByEmail(creator);
    			
    			log.info("Checking whether user is authorized to modify the card...");
    			if(currentCard.getCreator().equals(creator) || user.getRole().equals("Admin")) {
    				int affectedRows = cardService.updateCardName(currentCardName, newCardName);
    				
    				if(affectedRows > 0) {
    					log.info("Card updated successfully.");
    					res.put("status", "1001");
                        res.put("message", "Card updated successfully.");
                        
                        Card newCard = cardService.getCardByName(newCardName);
                        res.put("card", newCard);
                        
                        HttpHeaders headers = new HttpHeaders();
            			headers.setContentType(MediaType.APPLICATION_JSON);

            			return ResponseEntity.ok().headers(headers).body(res);
    				} else {
    					log.info("Failed to update card.");
    					res.put("status", "1010");
                        res.put("message", "Failed to update card.");
                        return ResponseEntity.ok().body(res);
    				}
        			
    			} else {
    				log.info("The user is not an admin nor the card creator. They cannot update the card.");
    				res.put("status", "1010");
                    res.put("message", "The user is not an admin nor the card creator. They cannot update the card.");
                    return ResponseEntity.ok().body(res);
    			}
    		} else {
    			log.info("Card with name " + currentCardName + " does not exist.");
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
    
    //Updates the other card details excluding the name
    @PostMapping("/updateCardDetails")
    public ResponseEntity<?> updateCardDetails(@RequestBody Map<String, String> cardModel) {
    	log.info("Running api updateCardDetails...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		
    		if(!cardModel.containsKey("color") && !cardModel.containsKey("description") && !cardModel.containsKey("status")) {
    			log.info("Color, status, and description are absent. No updates can be made. Add at least one of these fields to update.");
    			res.put("status", "1010");
                res.put("message", "Color, status, and description are absent. No updates can be made. Add at least one of these fields to update.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String cardName = cardModel.get("name");
    		
    		if(cardName.isEmpty() || cardName == null) {
    			log.info("Card Name cannot be null or empty.");
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		Card cardForUpdate = cardService.getCardByName(cardName);
    		
    		if(cardService.getCardByName(cardName) == null) {
    			log.info("Card with name " + cardName + " does not exist.");
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String creator = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
			User user = userRepository.getUserByEmail(creator);
			if(!cardForUpdate.getCreator().equals(creator) && !user.getRole().equals("Admin")) {
				log.info("The user is not an admin nor the card creator. They cannot update the card.");
				res.put("status", "1010");
                res.put("message", "The user is not an admin nor the card creator. They cannot update the card.");
                return ResponseEntity.ok().body(res);
			}
    		
    		String color = cardModel.containsKey("color") ? cardModel.get("color") : null;
    		String description = cardModel.containsKey("description") ? cardModel.get("description") : null;
    		String status = cardModel.containsKey("status") ? cardModel.get("status") : null;
    		
    		if(color != null && (Card.isValidHexColor(color) || color.equals(""))) cardForUpdate.setColor(color);
    		if(description != null) cardForUpdate.setDescription(description);
    		if(status != null && Card.isValidStatus(status)) cardForUpdate.setStatus(status);
    		
    		log.info("Updating card...");
    		log.info("Updated card details {}", cardForUpdate.toString());
    		
    		cardService.saveCard(cardForUpdate);
    		
    		log.info("Card updated successfully.");
    		res.put("status", "1001");
            res.put("message", "Card updated successfully.");
            
            Card updatedCard = cardService.getCardByName(cardName);
            res.put("card", updatedCard);
            
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
    
    //Soft deletes a card
    @PostMapping("/deleteCard")
    public ResponseEntity<?> deleteCard(@RequestBody Map<String, String> data){
    	log.info("Running api deleteCard...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String cardName = data.get("cardName");
    		if(cardName.isEmpty() || cardName == null) {
    			log.info("Card Name cannot be null or empty.");
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		if(cardService.getCardByName(cardName) == null) {
    			log.info("Card with name " + cardName + " does not exist.");
    			res.put("status", "1010");
                res.put("message", "Card with name " + cardName + " does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String creator = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    		User user = userRepository.getUserByEmail(creator);
    		Card cardToBeDeleted = cardService.getCardByName(cardName);
    		
    		log.info("Card found. Details: {}", cardToBeDeleted.toString());
    		
    		if(!user.getRole().equals("Admin") && !cardToBeDeleted.getCreator().equals(creator)) {
    			log.info("User cannot delete the card. The user must be the creator of the card or an admin to delete it.");
    			res.put("status", "1010");
                res.put("message", "User cannot delete the card. The user must be the creator of the card or an admin to delete it.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("Deleting card...");
    		int affectedRows = cardService.deleteCard(cardName);
    		
    		if(affectedRows > 0) {
    			log.info("Card deleted successfully.");
    			res.put("status", "1001");
                res.put("message", "Card deleted successfully.");
                
                HttpHeaders headers = new HttpHeaders();
    			headers.setContentType(MediaType.APPLICATION_JSON);

    			return ResponseEntity.ok().headers(headers).body(res);
    		} else {
    			log.info("Failed to delete card.");
    			res.put("status", "1010");
                res.put("message", "Failed to delete card.");
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
    
    //Fetches all cards that are for the currently authenticated user. Limited to 10 records max.
    @GetMapping("/getAllCards")
    public ResponseEntity<?> getAllCards(@RequestBody Map<String, Boolean> sortFilters) {
    	log.info("Running api getAllCards...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		//implement fetch for user type admin or member
    		String username = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    		User user =  userRepository.getUserByEmail(username);
    		if(user == null) {
    			log.info("User does not exist. Invalid token.");
    			res.put("status", "1010");
                res.put("message", "User does not exist. Invalid token.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("Checking sort filters...");
    		log.info("Filters: {}", sortFilters);
    		
    		log.info("Fetching all cards for the current user...");
    		List<Card> allCards = user.getRole().equals("Admin") ?
    					cardService.getAllCards(sortFilters)
    				:	cardService.getAllMemeberCards(username, sortFilters);
    		
    		if(allCards ==  null) {
    			log.info("No cards found.");
    			res.put("status", "1010");
                res.put("message", "No cards found.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("Cards fetched successfully. Limited to 10 cards max.");
    		res.put("status", "1001");
            res.put("message", "Cards fetched successfully. Limited to 10 cards max.");
            res.put("Cards", allCards);
            
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
    
    //Fetches a single card data
    @GetMapping("/getSingleCard")
    public ResponseEntity<?> getSingleCard(@RequestBody Map<String, String> data) {
    	log.info("Running api getSingleCard...");
    	Map<String, Object> res = new HashMap<>();
    	try {
    		String username = jwtUtil.extractUsername(jwtUtil.getTokenFromRequest(request));
    		User user =  userRepository.getUserByEmail(username);
    		if(user == null) {
    			log.info("User does not exist. Invalid token.");
    			res.put("status", "1010");
                res.put("message", "User does not exist. Invalid token.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		String cardName = data.get("cardName");
    		
    		if(cardName.isEmpty() || cardName == null) {
    			res.put("status", "1010");
                res.put("message", "Card Name cannot be null or empty.");
                return ResponseEntity.ok().body(res);
    		}
    		log.info("Fetching card data...");
    		Card card = cardService.getCardByName(cardName);
    		
    		if(card == null) {
    			log.info("Card does not exist.");
    			res.put("status", "1010");
                res.put("message", "Card does not exist.");
                return ResponseEntity.ok().body(res);
    		}
    		
    		log.info("Card found. Details: {}", card.toString());
    		
    		if(!user.getRole().equals("Admin") && !card.getCreator().equals(username)) {
    			log.info("You must own the card or be the admin to view it.");
    			res.put("status", "1010");
                res.put("message", "You must own the card or be the admin to view it.");
                return ResponseEntity.ok().body(res);
    		}
    		log.info("Card fetched successfully.");
    		res.put("status", "1001");
            res.put("message", "Card fetched successfully.");
            res.put("Card", card);
            
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
}

