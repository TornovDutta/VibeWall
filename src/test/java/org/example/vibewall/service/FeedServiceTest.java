package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.service.serviceImple.FeedServiceImplement;
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
    private ConfessionRepo repo;

    @Mock
    private ConfessionMapper mapper;

    @InjectMocks
    private FeedServiceImplement feedService;

    @Test
    void get_shouldReturnConfessionResponses() {
        // Arrange
        Confession confession1 = new Confession();
        Confession confession2 = new Confession();

        List<Confession> confessions = List.of(confession1, confession2);

        ConfessionResponse response1 = new ConfessionResponse();
        ConfessionResponse response2 = new ConfessionResponse();

        List<ConfessionResponse> responses = List.of(response1, response2);

        when(repo.findAll()).thenReturn(confessions);
        when(mapper.toDTO(confessions)).thenReturn(responses);

        // Act
        List<ConfessionResponse> result = feedService.get();

        // Assert
        assertEquals(2, result.size());
        assertEquals(responses, result);

        verify(repo, times(1)).findAll();
        verify(mapper, times(1)).toDTO(confessions);
        verifyNoMoreInteractions(repo, mapper);
    }
}