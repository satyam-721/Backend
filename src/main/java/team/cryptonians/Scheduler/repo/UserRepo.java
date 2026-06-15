package team.cryptonians.Scheduler.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.cryptonians.Scheduler.model.User;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    User findByUsername(String username);
}
