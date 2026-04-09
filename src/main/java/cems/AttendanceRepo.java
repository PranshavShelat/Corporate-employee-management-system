package cems;

import cems.models.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepo extends JpaRepository<AttendanceRecord, Long> {
}