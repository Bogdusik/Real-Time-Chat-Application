package com.example.chat_backend;

/**
 * Test-only constants to avoid hardcoded credential-like strings in tests (Snyk/SAST).
 * These are not real credentials.
 */
public final class TestConstants {

    private TestConstants() {}

    public static final String TEST_USERNAME = "testUser";
    public static final String ANOTHER_USERNAME = "anotherUser";
    public static final String USER_WITHOUT_ID = "userWithoutId";
    public static final String FIND_BY_ID_TEST = "findByIdTest";
    public static final String ORIGINAL_USERNAME = "originalUsername";
    public static final String UPDATED_USERNAME = "updatedUsername";
    public static final String USER_TO_DELETE = "userToDelete";
    public static final String USER_1 = "user1";
    public static final String USER_2 = "user2";
    public static final String USER_3 = "user3";
    public static final String EXISTS_TEST = "existsTest";
    public static final String OLD_USERNAME = "oldUsername";
    public static final String NEW_USERNAME = "newUsername";
    public static final String TEST_MESSAGE_CONTENT = "Test message content";
    public static final String TEST_MESSAGE = "Test message";
    public static final String NEW_MESSAGE = "New message";
    public static final String SPECIAL_CHARS_USERNAME = "user@123_$%";
}
