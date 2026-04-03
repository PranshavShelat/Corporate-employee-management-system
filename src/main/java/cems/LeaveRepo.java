package cems;

import cems.models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepo extends JpaRepository<LeaveRequest, Long> {
}