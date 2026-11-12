package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.NotificationResponse;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification feed and status")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get current user notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificationService.getUserNotifications(details.getId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        long count = notificationService.getUnreadCount(details.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal CustomUserDetails details,
            @PathVariable String id
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificationService.markAsRead(details.getId(), id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAllAsRead(details.getId());
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails details,
            @PathVariable String id
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.deleteNotification(details.getId(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @DeleteMapping
    @Operation(summary = "Clear all notifications")
    public ResponseEntity<Map<String, String>> clearAllNotifications(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (details == null || details.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.clearAllNotifications(details.getId());
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }
}
