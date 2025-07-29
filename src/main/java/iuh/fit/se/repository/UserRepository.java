package iuh.fit.se.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iuh.fit.se.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    //    Optional<User> findByEmail(String email);
    User findByAccountId(String accountId);
    //    List<User> findByRole(UserRoleEnum role);
}
