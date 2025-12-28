package org.example.vibewall.service;

import org.example.vibewall.DTO.PostRequested;
import org.example.vibewall.DTO.PostResponse;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.springframework.http.HttpStatusCode;

public interface ConfessionService {
    PostResponse create(String usersid, PostRequested requested);

    PostResponse update(String userId, PostRequested requested, String id) throws ConfessionNotFoundException;

    void delete(String userId, String id) throws ConfessionNotFoundException;
}
