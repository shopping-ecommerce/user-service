package iuh.fit.se.repository;

import iuh.fit.se.entity.ViolationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViolationRecordRepository extends JpaRepository<ViolationRecord,String> {
}
