package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.DTO.FeedbackRequest;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PlatformMisuseException;

public interface FeedbackService {
    ConfessionResponse giveFeedback(String confessionId, FeedbackRequest requested) throws ConfessionNotFoundException, PlatformMisuseException;

    ConfessionResponse deleteFeedback(String confessionId, int feedbackId) throws ConfessionNotFoundException;

    ConfessionResponse updateFeedback(String confessionId, int feedbackId, FeedbackRequest requested) throws ConfessionNotFoundException, PlatformMisuseException;
}
