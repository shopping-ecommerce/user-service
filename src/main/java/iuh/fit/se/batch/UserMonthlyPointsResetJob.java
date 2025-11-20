package iuh.fit.se.batch;

import iuh.fit.se.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMonthlyPointsResetJob {

    private final UserService userService;

    /**
     * Reset penalty points for all users at 00:00:00 on the 1st of every month (Vietnam time)
     * Cron: second minute hour day-of-month month day-of-week
     * Format: 0 0 0 1 * * means "at 00:30:00 on day 1 of every month"
     */
    @Scheduled(cron = "0 30 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        log.info("[JOB] UserMonthlyPointsResetJob started at {}", java.time.LocalDateTime.now());
        try {
            int updatedCount = userService.resetMonthlyPenaltyPoints();
            log.info("[JOB] UserMonthlyPointsResetJob completed successfully. Reset penalty points for {} users", updatedCount);
        } catch (Exception e) {
            log.error("[JOB] UserMonthlyPointsResetJob failed with error: {}", e.getMessage(), e);
            // Có thể thêm notification hoặc alert ở đây nếu cần
        }
    }
}