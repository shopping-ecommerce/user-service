package iuh.fit.se.batch;

import iuh.fit.se.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerAutoUnsuspendJob {

    private final SellerService sellerService;

    /**
     * Chạy mỗi ngày lúc 00:00 theo múi giờ Việt Nam
     * Cron: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        log.info("[JOB] SellerAutoUnsuspendJob started");
        try {
            sellerService.autoUnsuspendExpiredSellers();
            log.info("[JOB] SellerAutoUnsuspendJob completed");
        } catch (Exception e) {
            log.error("[JOB] SellerAutoUnsuspendJob failed: {}", e.getMessage(), e);
        }
    }
}
