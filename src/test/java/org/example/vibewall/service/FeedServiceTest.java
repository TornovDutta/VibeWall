package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.service.serviceImpl.FeedServiceImpl;
import org.example.vibewall.utility.ConfessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private ConfessionRepository repo;

    @Mock
    private ConfessionMapper mapper;

    @InjectMocks
    private FeedServiceImpl feedService;

    @Test
    void get_shouldReturnConfessionResponses() {
        Confession confession1 = new Confession();
        Confession confession2 = new Confession();

        List<Confession> confessions = List.of(confession1, confession2);

        ConfessionResponse response1 = new ConfessionResponse();
        ConfessionResponse response2 = new ConfessionResponse();

        List<ConfessionResponse> responses = List.of(response1, response2);

        when(repo.findAll()).thenReturn(confessions);
        when(mapper.toDTO(confessions)).thenReturn(responses);

        List<ConfessionResponse> result = feedService.get();

        assertEquals(2, result.size());
        assertEquals(responses, result);

        verify(repo, times(1)).findAll();
        verify(mapper, times(1)).toDTO(confessions);
        verifyNoMoreInteractions(repo, mapper);
    }
}
