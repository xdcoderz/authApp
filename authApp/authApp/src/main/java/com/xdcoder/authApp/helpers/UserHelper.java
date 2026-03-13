package com.xdcoder.authApp.helpers;

import java.util.UUID;

/*
 * UserHelper
 *
 * Utility/helper class related to user operations.
 * Helper classes usually contain small reusable methods that are used
 * in multiple parts of the application.
 *
 * In this case, the helper converts a String representation of a UUID
 * into a UUID object which is required when querying the database.
 */
public class UserHelper {

    /*
     * parseUUID
     *
     * Converts a String userId into a UUID object.
     *
     * Why this is needed:
     * In API requests, IDs usually come as Strings (from URL path variables),
     * but the database and entity use UUID type. So we convert the String
     * to UUID before performing repository operations.
     *
     * Example:
     * "/users/550e8400-e29b-41d4-a716-446655440000"
     * → converted into UUID object using this method.
     */
    public static UUID parseUUID(String uuid){
        return UUID.fromString(uuid);
    }
}