package com.bingzer.android.driven;

public abstract class AbsPermission implements Permission {

    /**
     * Returns the owner.
     */
    @Override
    public UserRole getOwner() {
        for(UserRole role : getRoles()){
            if(role.getRole() == PERMISSION_OWNER)
                return role;
        }

        return null;
    }

    /**
     * Checks to see if a user is an owner
     */
    @Override
    public boolean isOwner(UserInfo userInfo) {
        for(UserRole role : getRoles()){
            if(role.getUserInfo().equals(userInfo))
                return true;
        }

        return false;
    }
}
