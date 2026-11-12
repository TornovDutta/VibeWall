package org.example.vibewall.service;

import org.example.vibewall.DTO.CreatePollRequest;
import org.example.vibewall.DTO.PollResponse;

import java.util.List;

public interface PollService {
    PollResponse createPoll(String userId, CreatePollRequest request);
    List<PollResponse> getFeedPolls(String currentUserId);
    PollResponse getPollById(String pollId, String currentUserId);
    PollResponse vote(String pollId, int optionId, String currentUserId);
    void deletePoll(String pollId, String currentUserId, boolean isAdmin);
}
