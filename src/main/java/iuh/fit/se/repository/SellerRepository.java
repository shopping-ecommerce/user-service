package iuh.fit.se.repository;

import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.enums.SellerStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
    boolean existsByUserId(String userId);
    Optional<Seller> findByUserId(String userId);
    List<Seller> findAllByStatus(SellerStatusEnum status);
    @Query("SELECT s.email FROM Seller s WHERE s.status = 'SUSPENDED' OR s.status = 'APPROVED'")
    List<String> findAllEmailsByStatus();
    List<Seller> findByStatusAndSuspensionEndDateBefore(
            SellerStatusEnum status, LocalDateTime date);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
    UPDATE Seller s
       SET s.status = 'SUSPENDED',
           s.suspendedAt = CURRENT_TIMESTAMP,
           s.suspensionReason = :reason,
           s.modifiedTime = CURRENT_TIMESTAMP
     WHERE s.status = 'APPROVED'
""")
    int bulkSuspendAllApprovedJpql(@Param("reason") String reason);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Seller s
           SET s.violationCount = 0,
               s.modifiedTime = CURRENT_TIMESTAMP
         WHERE s.violationCount IS NULL OR s.violationCount <> 0
    """)
    int resetAllViolationCountToZero();
}
