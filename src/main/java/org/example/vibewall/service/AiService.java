package org.example.vibewall.service;

public interface AiService {
    String getResponse(String prompt);
    boolean unSafe(String str);
}
