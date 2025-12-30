package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.DTO.FeedbackRequested;
import org.example.vibewall.DTO.FeedbackResponse;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;

public interface FeedbackService  {
    ConfessionResponse giveFeedback(String confessionId, FeedbackRequested requested) throws ConfessionNotFoundException, PrincipalNotFollowException;

    ConfessionResponse deleteFeedback(String confessionId, int feedbackId) throws ConfessionNotFoundException;

    ConfessionResponse updateFeedback(String confessionId, int feedbackId, FeedbackRequested requested) throws ConfessionNotFoundException, PrincipalNotFollowException;
}
