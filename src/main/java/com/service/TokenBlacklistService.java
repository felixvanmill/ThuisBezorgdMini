package com.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Add a token to the blacklist.
     * @param token the JWT to blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Check if a token is blacklisted.
     * @param token the JWT to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
