package com.bingzer.android.driven;

import java.util.List;

/**
 * Represents a permission.
 * For each {@link com.bingzer.android.driven.RemoteFile} there has to be a permission
 * associated with it. For each permission, there has to be an owner.
 */
public interface Permission {

    /**
     * Default permission.
     * Owner can do read/write
     */
    int PERMISSION_OWNER = 0;

    /**
     * Full permission.
     * Can do read/write but do not own it.
     */
    int PERMISSION_FULL = 1;

    /**
     * Read-only permission
     */
    int PERMISSION_READ = 2;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns all available user roles
     */
    List<UserRole> getRoles();

    /**
     * Returns the owner.
     */
    UserRole getOwner();

    /**
     * Checks to see if a user is an owner
     */
    boolean isOwner(UserInfo userInfo);

}
