package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;

import java.util.List;

public interface FeedService {
    List<ConfessionResponse> get();
}
