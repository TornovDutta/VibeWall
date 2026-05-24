package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionRequest;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.exception.ConfessionNotFoundException;

public interface ConfessionService {
    ConfessionResponse create(String usersid, ConfessionRequest requested);

    ConfessionResponse update(String userId, ConfessionRequest requested, String id) throws ConfessionNotFoundException;

    void delete(String userId, String id) throws ConfessionNotFoundException;
}
