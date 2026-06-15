package team.cryptonians.Scheduler.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.cryptonians.Scheduler.model.AvailabilitySlot;
import team.cryptonians.Scheduler.model.Booking;

@Repository
public interface BookingRepo extends JpaRepository<Booking,Integer> {

    Booking findBySlotId(Integer id);
}
