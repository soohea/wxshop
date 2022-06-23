package com.soohea.wxshop.entity;

import com.soohea.wxshop.generate.User;

public class LoginResponse {
    private boolean login;
    private User user;

    public LoginResponse() {
    }

    public static LoginResponse notLogin() {
        return new LoginResponse(false, null);
    }

    public static LoginResponse login(User user) {
        return new LoginResponse(true, user);
    }

    public LoginResponse(boolean login, User user) {
        this.login = login;
        this.user = user;
    }

    public boolean isLogin() {
        return login;
    }

    public User getUser() {
        return user;
    }
}

