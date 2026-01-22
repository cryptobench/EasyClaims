package com.easyclaims.data;

import java.util.UUID;

/**
 * Utility class for admin claim constants and helpers.
 */
public class AdminClaims {

    /**
     * Static UUID used for all admin claims.
     * This is distinct from the fake test claims UUID.
     */
    public static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Default display name for admin claims without a custom name.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Server";

    /**
     * Checks if the given owner ID represents an admin claim.
     *
     * @param ownerId The UUID to check
     * @return true if this is the admin claims UUID
     */
    public static boolean isAdminClaim(UUID ownerId) {
        return ADMIN_UUID.equals(ownerId);
    }
}
