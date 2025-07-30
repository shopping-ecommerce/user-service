package iuh.fit.se.repository;

import iuh.fit.se.entity.Seller;
import iuh.fit.se.enums.SellerStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
    boolean existsByUserId(String userId);
    Optional<Seller> findByUserId(String userId);
    List<Seller> findAllByStatus(SellerStatusEnum status);
}
