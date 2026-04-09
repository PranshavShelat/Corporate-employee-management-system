package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private float hoursWorked;

    @ManyToOne
    private Employee employee;

    public void calculateHours() {
        if (timeIn != null && timeOut != null) {
            this.hoursWorked = java.time.Duration.between(timeIn, timeOut).toMinutes() / 60.0f;
        }
    }
}