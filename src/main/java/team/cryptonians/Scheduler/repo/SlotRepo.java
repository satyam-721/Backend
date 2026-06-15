package team.cryptonians.Scheduler.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.cryptonians.Scheduler.model.AvailabilitySlot;

@Repository
public interface SlotRepo extends JpaRepository<AvailabilitySlot,Integer> {
}
