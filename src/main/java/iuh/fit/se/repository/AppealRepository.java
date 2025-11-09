package iuh.fit.se.repository;

import iuh.fit.se.entity.Appeal;
import iuh.fit.se.entity.enums.AppealStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, String> {

    List<Appeal> findBySellerIdOrderBySubmittedAtDesc(String sellerId);

    List<Appeal> findByStatusOrderBySubmittedAtAsc(AppealStatusEnum status);

    List<Appeal> findAllByOrderBySubmittedAtDesc();

    boolean existsBySellerIdAndViolationRecordId(String sellerId, String violationRecordId);

    int countBySellerId(String sellerId);

    int countBySellerIdAndIdNot(String sellerId, String excludeAppealId);
}