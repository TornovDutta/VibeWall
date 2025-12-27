package org.example.vibewall.service;

public interface OpenAiService {
    String getResponse(String prompt);
    boolean safe(String str);
}
