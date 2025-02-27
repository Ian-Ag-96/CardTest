package com.eclecticsassignment.cards.controller;

import com.eclecticsassignment.cards.entity.Card;
import com.eclecticsassignment.cards.entity.User;
import com.eclecticsassignment.cards.model.CardModel;
import com.eclecticsassignment.cards.model.UpdateCardNameModel;
import com.eclecticsassignment.cards.model.UserModel;
import com.eclecticsassignment.cards.repository.UserRepository;
import com.eclecticsassignment.cards.service.CardService;
import com.eclecticsassignment.cards.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class CardControllerTest {

    @Mock
    private UserRepository userRepository; // Mocking the user repository

    @Mock
    private PasswordEncoder passwordEncoder; // Mocking the password encoder

    @Mock
    private AuthenticationManager authenticationManager; // Mocking the authentication manager

    @Mock
    private UserDetailsService userDetailsService; // Mocking the user details service

    @Mock
    private HttpServletRequest request; // Mocking the HTTP request

    @Mock
    private CardService cardService; // Mocking the card service

    @Mock
    private JwtUtil jwtUtil; // Mocking the JWT utility class
    
    @Mock
    private User user;

    @Mock
    private Card card;

    @InjectMocks
    private CardController cardController; // Injecting mocks into the CardController
    
    private Map<String, String> requestData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializing the mocks
        // Simulate an HTTP request in the context
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        requestData = new HashMap<>();
        requestData.put("cardName", "testCard");
        
    }

    // 1. Tests for getting the currently signed in user
    @Test
    void testGetCurrentUser_Success() {
        // Arrange: Setting up the mock behavior for a successful request
        String token = "valid-token";
        String username = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(username);

        // Mocking the behavior of jwtUtil and userRepository
        when(jwtUtil.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.getUserByEmail(username)).thenReturn(mockUser);

        // Act: Calling the method under test
        ResponseEntity<?> response = cardController.getCurrentUser();

        // Assert: Verifying the result
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify status code
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody); // Check if response body is not null
        assertEquals("1001", responseBody.get("status")); // Verifying status message
        assertEquals("User found.", responseBody.get("message")); // Verifying success message
        assertNotNull(responseBody.get("user")); // Verifying that user data is included
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Arrange: Setting up the mock behavior for a case where the user is not found
        String token = "valid-token";
        String username = "test@example.com";

        // Mocking the behavior of jwtUtil and userRepository for a missing user
        when(jwtUtil.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.getUserByEmail(username)).thenReturn(null);

        // Act: Calling the method under test
        ResponseEntity<?> response = cardController.getCurrentUser();

        // Assert: Verifying the result
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify status code
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody); // Check if response body is not null
        assertEquals("1010", responseBody.get("status")); // Verifying status message for not found user
        assertEquals("User not found. Token must be invalid.", responseBody.get("message")); // Verifying error message
    }

    @Test
    void testGetCurrentUser_Exception() {
        // Arrange: Setting up the mock behavior for an exception scenario
        String token = "valid-token";
        String username = "test@example.com";

        // Mocking jwtUtil and userRepository to throw an exception
        when(jwtUtil.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.getUserByEmail(username)).thenThrow(new RuntimeException("Database error"));

        // Act: Calling the method under test
        ResponseEntity<?> response = cardController.getCurrentUser();

        // Assert: Verifying the result
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify status code
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody); // Check if response body is not null
        assertEquals("1010", responseBody.get("status")); // Verifying status message for exception
        assertEquals("An error ocurred.", responseBody.get("message")); // Verifying error message
        assertNotNull(responseBody.get("cause")); // Verifying that the error cause is included
    }
    
    // 2. Tests for creating a user
    @Test
    void testCreateUser_Success() {
        // Arrange: Valid user details and admin privileges
        UserModel userModel = new UserModel("test@example.com", "securePassword", "Member");
        String adminEmail = "admin@example.com";

        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setRole("Admin");

        when(jwtUtil.getTokenFromRequest(request)).thenReturn("valid-token");
        when(jwtUtil.extractUsername("valid-token")).thenReturn(adminEmail);
        when(userRepository.getUserByEmail(adminEmail)).thenReturn(adminUser);
        when(userRepository.getUserByEmail(userModel.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userModel.getPassword())).thenReturn("hashedPassword");

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify successful creation
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1001", responseBody.get("status"));
        assertEquals("User created successfully.", responseBody.get("message"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailMissing() {
        // Arrange: Missing email
        UserModel userModel = new UserModel("", "securePassword", "Member");

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify missing email error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Email parameter cannot be empty or missing.", responseBody.get("message"));
    }

    @Test
    void testCreateUser_PasswordMissing() {
        // Arrange: Missing password
        UserModel userModel = new UserModel("test@example.com", "", "Member");

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify missing password error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Password parameter cannot be empty or missing.", responseBody.get("message"));
    }

    @Test
    void testCreateUser_RoleMissing() {
        // Arrange: Missing role
        UserModel userModel = new UserModel("test@example.com", "securePassword", "");

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify missing role error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Role parameter cannot be empty or missing.", responseBody.get("message"));
    }

    @Test
    void testCreateUser_InvalidRole() {
        // Arrange: Invalid role
        UserModel userModel = new UserModel("test@example.com", "securePassword", "InvalidRole");

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify invalid role error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Role is invalid. Role can only be 'Member' or 'Admin'", responseBody.get("message"));
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        // Arrange: User already exists
        UserModel userModel = new UserModel("existing@example.com", "securePassword", "Member");

        User existingUser = new User();
        existingUser.setEmail(userModel.getEmail());

        when(userRepository.getUserByEmail(userModel.getEmail())).thenReturn(existingUser);

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify user already exists error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("The user existing@example.com already exists.", responseBody.get("message"));
    }

    @Test
    void testCreateUser_NotAdmin() {
        // Arrange: Current user is not an admin
        UserModel userModel = new UserModel("test@example.com", "securePassword", "Member");
        String nonAdminEmail = "user@example.com";

        User nonAdminUser = new User();
        nonAdminUser.setEmail(nonAdminEmail);
        nonAdminUser.setRole("Member");

        when(jwtUtil.getTokenFromRequest(request)).thenReturn("valid-token");
        when(jwtUtil.extractUsername("valid-token")).thenReturn(nonAdminEmail);
        when(userRepository.getUserByEmail(nonAdminEmail)).thenReturn(nonAdminUser);

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify only admins can create users error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("The user user@example.com is not an admin. Only admins can create users.", responseBody.get("message"));
    }

    @Test
    void testCreateUser_Exception() {
        // Arrange: Simulating an exception
        UserModel userModel = new UserModel("test@example.com", "securePassword", "Member");

        when(userRepository.getUserByEmail(anyString())).thenThrow(new RuntimeException("Database error"));

        // Act: Calling the method
        ResponseEntity<?> response = cardController.createUser(userModel);

        // Assert: Verify exception handling
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("An error ocurred.", responseBody.get("message"));
        assertEquals("Database error", responseBody.get("cause"));
    }
    
    //3. Tests for logging in
    @Test
    void testLogin_SuccessfulAuthentication() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "password123");

        User mockUser = new User();
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(mockUser);
        
        // Change this line: authenticate() method should be mocked appropriately
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mockToken");

        ResponseEntity<?> response = cardController.login(credentials);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals("1001", responseBody.get("status"));
        assertEquals("Authentication successful.", responseBody.get("message"));
        assertEquals("mockToken", responseBody.get("token"));
    }

    @Test
    void testLogin_MissingEmail() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("password", "password123");

        ResponseEntity<?> response = cardController.login(credentials);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals("1010", responseBody.get("status"));
        assertEquals("Email parameter cannot be empty or missing.", responseBody.get("message"));
    }

    @Test
    void testLogin_MissingPassword() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");

        ResponseEntity<?> response = cardController.login(credentials);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals("1010", responseBody.get("status"));
        assertEquals("Password parameter cannot be empty or missing.", responseBody.get("message"));
    }

    @Test
    void testLogin_UserDoesNotExist() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "password123");

        when(userRepository.getUserByEmail("test@example.com")).thenReturn(null);

        ResponseEntity<?> response = cardController.login(credentials);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals("1010", responseBody.get("status"));
        assertEquals("The user test@example.com does not exist.", responseBody.get("message"));
    }

    @Test
    void testLogin_AuthenticationFailure() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "password123");

        User mockUser = new User();
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(mockUser);
        doThrow(new RuntimeException("Authentication failed"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = cardController.login(credentials);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals("1010", responseBody.get("status"));
        assertEquals("An error occurred. Error: Authentication failed", responseBody.get("message"));
    }
    
    // 4. Card Creation Tests
    @Test
    void createCard_Success() throws SQLException, Exception {
        CardModel cardModel = new CardModel();
        cardModel.setName("Test Card");
        cardModel.setColor("#FFFFFF");
        cardModel.setDescription("Test Description");
        
        when(cardService.getCardByName("Test Card")).thenReturn(null);
        
        Card card = new Card();
        card.setName(cardModel.getName());
        card.setColor(cardModel.getColor());
        card.setDescription(cardModel.getDescription());


        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mockToken");
        when(jwtUtil.extractUsername("mockToken")).thenReturn("testUser");
        
        when(cardService.insertCard(any(Card.class))).thenReturn(card);

        ResponseEntity<?> response = cardController.createCard(cardModel);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertNotNull(responseBody);
        log.info("Response body: {}", responseBody.toString());
        assertEquals("1001", responseBody.get("status"));
        assertEquals("Card created successfully.", responseBody.get("message"));
        assertEquals(card, responseBody.get("card"));

        verify(cardService, times(1)).insertCard(any(Card.class));
    }

    @Test
    void createCard_CardNameMissing() throws SQLException, Exception {
        CardModel cardModel = new CardModel();
        cardModel.setName("");

        ResponseEntity<?> response = cardController.createCard(cardModel);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Card Name cannot be null or empty.", responseBody.get("message"));

        verify(cardService, never()).insertCard(any(Card.class));
    }

    @Test
    void createCard_CardAlreadyExists() throws SQLException, Exception {
        CardModel cardModel = new CardModel();
        cardModel.setName("Existing Card");

        when(cardService.getCardByName("Existing Card")).thenReturn(new Card());

        ResponseEntity<?> response = cardController.createCard(cardModel);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Card with name Existing Card already exists.", responseBody.get("message"));

        verify(cardService, never()).insertCard(any(Card.class));
    }

    @Test
    void createCard_InvalidColorFormat() throws SQLException, Exception {
        CardModel cardModel = new CardModel();
        cardModel.setName("Test Card");
        cardModel.setColor("InvalidColor");

        ResponseEntity<?> response = cardController.createCard(cardModel);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("Color has to be of the format #12FF56 (# followed by 6 digits or characters between a-f).", responseBody.get("message"));

        verify(cardService, never()).insertCard(any(Card.class));
    }

    @Test
    void createCard_ExceptionThrown() {
        CardModel cardModel = new CardModel();
        cardModel.setName("Test Card");
        cardModel.setColor("#FFFFFF");

        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mockToken");
        when(jwtUtil.extractUsername("mockToken")).thenReturn("testUser");
        when(cardService.getCardByName("Test Card")).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = cardController.createCard(cardModel);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("1010", responseBody.get("status"));
        assertEquals("An error ocurred.", responseBody.get("message"));
        assertEquals("Database error", responseBody.get("cause"));
    }
    
    //5. Update card name tests
    @Test
    void testUpdateCardName_OldCardNameIsEmpty() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("");
        model.setNewCardName("New Card Name");

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1010");
        expectedResponse.put("message", "Old Card Name cannot be null or empty.");

        assertEquals(ResponseEntity.ok(expectedResponse), response);
    }

    @Test
    void testUpdateCardName_NewCardNameIsEmpty() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("Old Card Name");
        model.setNewCardName("");

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1010");
        expectedResponse.put("message", "New Card Name cannot be null or empty.");

        assertEquals(ResponseEntity.ok(expectedResponse), response);
    }

    @Test
    void testUpdateCardName_OldAndNewCardNameAreSame() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("Same Card Name");
        model.setNewCardName("Same Card Name");

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1010");
        expectedResponse.put("message", "Old Card Name is the same as New Card Name.");

        assertEquals(ResponseEntity.ok(expectedResponse), response);
    }

    @Test
    void testUpdateCardName_CardDoesNotExist() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("Nonexistent Card");
        model.setNewCardName("New Card Name");

        when(cardService.getCardByName("Nonexistent Card")).thenReturn(null);

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1010");
        expectedResponse.put("message", "Card with name Nonexistent Card does not exist.");

        assertEquals(ResponseEntity.ok(expectedResponse), response);
    }

    @Test
    void testUpdateCardName_UserNotAuthorized() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("Old Card Name");
        model.setNewCardName("New Card Name");

        Card card = new Card();
        card.setCreator("otheruser@example.com");

        when(cardService.getCardByName("Old Card Name")).thenReturn(card);
        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUsername("mocked-token")).thenReturn("user@example.com");

        User user = new User();
        user.setRole("User");
        when(userRepository.getUserByEmail("user@example.com")).thenReturn(user);

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1010");
        expectedResponse.put("message", "The user is not an admin nor the card creator. They cannot update the card.");

        assertEquals(ResponseEntity.ok(expectedResponse), response);
    }

    @Test
    void testUpdateCardName_Success() {
        UpdateCardNameModel model = new UpdateCardNameModel();
        model.setOldCardName("Old Card Name");
        model.setNewCardName("New Card Name");

        Card card = new Card();
        card.setCreator("user@example.com");

        when(cardService.getCardByName("Old Card Name")).thenReturn(card);
        when(jwtUtil.getTokenFromRequest(request)).thenReturn("mocked-token");
        when(jwtUtil.extractUsername("mocked-token")).thenReturn("user@example.com");
        when(cardService.updateCardName("Old Card Name", "New Card Name")).thenReturn(1);

        Card updatedCard = new Card();
        updatedCard.setName("New Card Name");
        when(cardService.getCardByName("New Card Name")).thenReturn(updatedCard);

        ResponseEntity<?> response = cardController.updateCardName(model);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "1001");
        expectedResponse.put("message", "Card updated successfully.");
        expectedResponse.put("card", updatedCard);
        
        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

        assertEquals(ResponseEntity.ok().headers(headers).body(expectedResponse), response);
    }
    
    //6. Updating card details tests
    @Test
    void testUpdateCardDetails_NoFieldsProvided() {
        Map<String, String> cardModel = new HashMap<>();
        ResponseEntity<?> response = cardController.updateCardDetails(cardModel);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("No updates can be made"));
    }

    @Test
    void testUpdateCardDetails_CardNameIsEmpty() {
        Map<String, String> cardModel = new HashMap<>();
        cardModel.put("color", "#FFFFFF");

        ResponseEntity<?> response = cardController.updateCardDetails(cardModel);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Card Name cannot be null or empty"));
    }

    @Test
    void testUpdateCardDetails_CardNotFound() {
        Map<String, String> cardModel = new HashMap<>();
        cardModel.put("name", "TestCard");
        cardModel.put("color", "#FFFFFF");

        when(cardService.getCardByName("TestCard")).thenReturn(null);

        ResponseEntity<?> response = cardController.updateCardDetails(cardModel);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Card with name TestCard does not exist"));
    }

    @Test
    void testUpdateCardDetails_Exception() {
        Map<String, String> cardModel = new HashMap<>();
        cardModel.put("name", "TestCard");
        cardModel.put("color", "#FFFFFF");

        when(cardService.getCardByName("TestCard")).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = cardController.updateCardDetails(cardModel);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("An error occurred"));
    }
}
