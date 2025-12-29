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

import java.util.Arrays;

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
        if(!openAiService.safe(requested.content())){
            throw new PrincipalNotFollowException("don't follow the principal");
        }
        Feedback feedback=new Feedback(encryption.encode(requested.content()));
        Confession confession=confessionRepo.findById(confessionId).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        confession.setFeedbacks(Arrays.asList(feedback));

        return mapper.toDTO(confession);
    }
}
