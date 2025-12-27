package org.example.vibewall.service;

import org.example.vibewall.DTO.PostRequested;
import org.example.vibewall.DTO.PostResponse;
import org.springframework.http.HttpStatusCode;

public interface ConfessionService {
    PostResponse create(String usersid, PostRequested requested);
}
