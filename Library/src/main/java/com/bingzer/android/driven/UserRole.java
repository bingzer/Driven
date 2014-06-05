package com.bingzer.android.driven;


public final class UserRole {

    private UserInfo userInfo;
    private int role;

    public UserRole(UserInfo userInfo, int role){
        this.userInfo = userInfo;
        this.role = role;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
