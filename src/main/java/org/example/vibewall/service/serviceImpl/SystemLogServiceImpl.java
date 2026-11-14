package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.SystemLogResponse;
import org.example.vibewall.model.SystemLog;
import org.example.vibewall.repo.SystemLogRepository;
import org.example.vibewall.service.SystemLogService;
import org.example.vibewall.utility.AnonNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private static final Logger logger = LoggerFactory.getLogger(SystemLogServiceImpl.class);

    private final SystemLogRepository logRepo;

    @Override
    public void recordLog(String level, String source, String endpoint, Integer statusCode, String userId, String message, String details) {
        try {
            String alias = "Anonymous";
            if (userId != null && !userId.isBlank() && !"anonymous".equalsIgnoreCase(userId)) {
                try {
                    alias = AnonNameUtil.generate(userId);
                } catch (Exception ignored) {
                    alias = "User (" + userId.substring(0, Math.min(6, userId.length())) + ")";
                }
            }

            SystemLog log = SystemLog.builder()
                    .level(level != null ? level.toUpperCase() : "ERROR")
                    .source(source != null ? source : "API")
                    .endpoint(endpoint != null ? endpoint : "UNKNOWN")
                    .statusCode(statusCode)
                    .userId(userId)
                    .userAlias(alias)
                    .message(message != null ? message : "Error occurred")
                    .details(details)
                    .timestamp(new Date())
                    .build();

            logRepo.save(log);
            logger.info("SystemLog recorded: [{}] {} - {}", level, endpoint, message);
        } catch (Exception ex) {
            logger.error("Failed to persist SystemLog: {}", ex.getMessage());
        }
    }

    @Override
    public List<SystemLogResponse> getRecentLogs() {
        return logRepo.findTop100ByOrderByTimestampDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void clearLogs() {
        logRepo.deleteAll();
        logger.info("All system logs cleared by admin.");
    }

    @Override
    public long getErrorCount() {
        return logRepo.count();
    }

    private SystemLogResponse toDTO(SystemLog log) {
        return SystemLogResponse.builder()
                .id(log.getId())
                .level(log.getLevel())
                .source(log.getSource())
                .endpoint(log.getEndpoint())
                .statusCode(log.getStatusCode())
                .userId(log.getUserId())
                .userAlias(log.getUserAlias())
                .message(log.getMessage())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
