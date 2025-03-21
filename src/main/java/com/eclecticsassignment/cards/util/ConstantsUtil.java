package com.eclecticsassignment.cards.util;

public class ConstantsUtil {
	
	public static final String COLOR_FORMAT_ERROR = "Color has to be of the format #12FF56 (# followed by 6 digits or characters between a-f).";
	public static final String RESPONSE_KEY_MESSAGE = "message";
	public static final String RESPONSE_KEY_STATUS = "status";
	public static final String RESPONSE_GENERIC_ERROR = "An error occurred. Error: {}";
	public static final String GENERIC_ERROR_MESSAGE = "An error ocurred.";
	public static final String KEY_COLOR = "color";
	public static final String KEY_CAUSE = "cause";
	public static final String KEY_ADMIN = "Admin";
	public static final String KEY_DESCRIPTION = "description";
	public static final String ERROR_RESPONSE_CODE = "1010";
	public static final String SUCCESS_RESPONSE_CODE = "1001";
	public static final String MISSING_PASSWORD_ERROR = "Password parameter cannot be empty or missing.";
	public static final String MISSING_CARD_NAME_ERROR = "Card Name cannot be null or empty.";
	public static final String MISSING_EMAIL_ERROR = "Email parameter cannot be empty or missing.";
	public static final String INVALID_TOKEN_ERROR = "User does not exist. Invalid token.";
	public static final String UNAUTHORIZED_UPDATE_ERROR = "The user is not an admin nor the card creator. They cannot update the card.";
	
	private ConstantsUtil() {}

}
