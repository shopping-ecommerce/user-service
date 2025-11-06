package iuh.fit.se.batch;

import iuh.fit.se.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerMonthlyViolationResetJob {

    private final SellerService sellerService;

    /**
     * Chạy 00:00:00 ngày 1 mỗi tháng (giờ VN)
     * Cron: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        log.info("[JOB] SellerMonthlyViolationResetJob started");
        try {
            int updated = sellerService.resetMonthlyViolationCounters();
            log.info("[JOB] SellerMonthlyViolationResetJob completed, updated={}", updated);
        } catch (Exception e) {
            log.error("[JOB] SellerMonthlyViolationResetJob failed: {}", e.getMessage(), e);
        }
    }
}
