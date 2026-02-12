package com.paike.admin.dto;

public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static LoginResponse of(String token, Long userId, String username, String realName, String role) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(userId);
        response.setUsername(username);
        response.setRealName(realName);
        response.setRole(role);
        return response;
    }
}
