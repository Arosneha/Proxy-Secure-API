
package com.example.proxy_service.Dto;

public class UsageResponse {

    private String userEmail;
    private Long requestCount;

    public UsageResponse(String userEmail, Long requestCount) {
        this.userEmail = userEmail;
        this.requestCount = requestCount;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getRequestCount() {
        return requestCount;
    }
}
