package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionRequested;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.exception.ConfessionNotFoundException;

public interface ConfessionService {
    ConfessionResponse create(String usersid, ConfessionRequested requested);

    ConfessionResponse update(String userId, ConfessionRequested requested, String id) throws ConfessionNotFoundException;

    void delete(String userId, String id) throws ConfessionNotFoundException;
}
