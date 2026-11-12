package org.example.vibewall.service;

import org.example.vibewall.DTO.NotificationResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Notification;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.repo.NotificationRepository;
import org.example.vibewall.service.serviceImpl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepo;

    @Mock
    private ConfessionRepository confessionRepo;

    @Mock
    private Encryption encryption;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Confession testConfession;

    @BeforeEach
    void setUp() {
        testConfession = new Confession();
        testConfession.setId("conf123");
        testConfession.setUserId("user456");
        testConfession.setContent("encrypted_content");
    }

    @Test
    void notifyConfessionCreated_shouldSaveNotification() {
        notificationService.notifyConfessionCreated("user456", "conf123", "My secret confession");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("user456", saved.getUserId());
        assertEquals("conf123", saved.getConfessionId());
        assertEquals("POST_CREATED", saved.getType());
        assertFalse(saved.isRead());
    }

    @Test
    void notifyReaction_shouldNotifyConfessionOwner() {
        when(confessionRepo.findById("conf123")).thenReturn(Optional.of(testConfession));
        when(encryption.decode("encrypted_content")).thenReturn("I feel anxious today");

        notificationService.notifyReaction("conf123", "heart");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("user456", saved.getUserId());
        assertEquals("conf123", saved.getConfessionId());
        assertEquals("REACTION", saved.getType());
        assertTrue(saved.getMessage().contains("heart"));
        assertTrue(saved.getMessage().contains("I feel anxious today"));
    }

    @Test
    void notifyFeedback_shouldNotifyConfessionOwner() {
        when(confessionRepo.findById("conf123")).thenReturn(Optional.of(testConfession));

        notificationService.notifyFeedback("conf123", "You are not alone, hang in there!");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("user456", saved.getUserId());
        assertEquals("conf123", saved.getConfessionId());
        assertEquals("COMMENT", saved.getType());
        assertTrue(saved.getMessage().contains("You are not alone"));
    }

    @Test
    void getUserNotifications_shouldReturnFormattedDTOs() {
        Notification n = Notification.builder()
                .id("notif1")
                .userId("user456")
                .confessionId("conf123")
                .type("COMMENT")
                .title("New Comment")
                .message("Test message")
                .read(false)
                .createdAt(new Date())
                .build();

        when(notificationRepo.findByUserIdOrderByCreatedAtDesc("user456")).thenReturn(List.of(n));

        List<NotificationResponse> list = notificationService.getUserNotifications("user456");
        assertEquals(1, list.size());
        assertEquals("notif1", list.get(0).getId());
        assertEquals("COMMENT", list.get(0).getType());
    }

    @Test
    void markAsRead_shouldUpdateStatus() {
        Notification n = Notification.builder()
                .id("notif1")
                .userId("user456")
                .read(false)
                .build();

        when(notificationRepo.findById("notif1")).thenReturn(Optional.of(n));

        NotificationResponse res = notificationService.markAsRead("user456", "notif1");
        assertNotNull(res);
        assertTrue(res.isRead());
        verify(notificationRepo).save(n);
    }
}
