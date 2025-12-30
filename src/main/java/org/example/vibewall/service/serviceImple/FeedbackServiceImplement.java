package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.DTO.FeedbackRequested;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.service.FeedbackService;
import org.example.vibewall.service.OpenAiService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImplement implements FeedbackService {
    private final ConfessionRepo confessionRepo;
    private final OpenAiService openAiService;
    private final Encryption encryption;
    private final ConfessionMapper mapper;
    @Override
    public ConfessionResponse giveFeedback(String confessionId, FeedbackRequested requested) throws ConfessionNotFoundException,
            PrincipalNotFollowException {
        if(openAiService.unSafe(requested.content())){
            throw new PrincipalNotFollowException("don't follow the principal");
        }
        Confession confession=confessionRepo.findById(confessionId).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        int feedbackId = confession.getFeedbacks().size();

        Feedback feedback=new Feedback(feedbackId,encryption.encode(requested.content()));

        confession.getFeedbacks().add(feedback);
        confessionRepo.save(confession);

        return mapper.toDTO(confession);
    }
    @Override
    public ConfessionResponse updateFeedback(
            String confessionId,
            int feedbackId,
            FeedbackRequested requested)
            throws ConfessionNotFoundException, PrincipalNotFollowException {

        if (openAiService.unSafe(requested.content())) {
            throw new PrincipalNotFollowException("don't follow the principal");
        }

        Confession confession = confessionRepo.findById(confessionId)
                .orElseThrow(() -> new ConfessionNotFoundException("wrong id"));

        if (feedbackId < 0 || feedbackId >= confession.getFeedbacks().size()) {
            throw new ConfessionNotFoundException("feedback not found");
        }

        Feedback feedback = confession.getFeedbacks().get(feedbackId);
        feedback.setFeedback(encryption.encode(requested.content()));

        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }
    @Override
    public ConfessionResponse deleteFeedback(
            String confessionId,
            int feedbackId)
            throws ConfessionNotFoundException {

        Confession confession = confessionRepo.findById(confessionId)
                .orElseThrow(() -> new ConfessionNotFoundException("wrong id"));

        if (feedbackId < 0 || feedbackId >= confession.getFeedbacks().size()) {
            throw new ConfessionNotFoundException("feedback not found");
        }


        confession.getFeedbacks().remove(feedbackId);


        for (int i = 0; i < confession.getFeedbacks().size(); i++) {
            confession.getFeedbacks().get(i).setId(i);
        }

        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }


}
