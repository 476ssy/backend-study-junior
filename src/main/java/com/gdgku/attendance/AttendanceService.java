package com.gdgku.attendance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service 
public class AttendanceService {
    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 10);
    private static final LocalTime ABSENT_CUTOFF = LocalTime.of(9, 30);

    private final List<Attendance> attendances = new ArrayList<>();
    private long nextId = 1L;

    public String determineStatus(LocalTime checktime) {
        String status;
        if (!checktime.isAfter(LATE_CUTOFF)) {
            status = "ON_TIME";
        } else if (!checktime.isAfter(ABSENT_CUTOFF)) {
            status = "LATE";
        } else {
            status = "ABSENT";
        }
        return status;
    }

    @PostMapping("/check-in")
    public Attendance checkIn(@RequestBody Attendance request) {
        String status = determineStatus(request.getCheckInTime());

        Attendance attendance = new Attendance(nextId++, request.getStudentName(), request.getCheckInTime(), status);
        attendances.add(attendance);
        return attendance;
    }

    @GetMapping
    public List<Attendance> getAttendances() {
        return attendances;
    }

    @GetMapping("/{id}")
    public Attendance getAttendance(@PathVariable Long id) {
        for (Attendance attendance : attendances) {
            if (attendance.getId().equals(id)) {
                return attendance;
            }
        }
        return null;
    }

    @GetMapping("/late-count")
    public long countLate() {
        long count = 0;
        for (Attendance attendance : attendances) {
            if ("LATE".equals(attendance.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // 관리자가 잘못 입력된 출석 시각을 정정하는 API.
    // 지각 판정 로직을 checkIn()과 별개로 다시 구현하다가 경계값 조건(<= vs <)이 미묘하게 달라졌다.
    @PutMapping("/{id}")
    public Attendance updateCheckInTime(@PathVariable Long id, @RequestBody Attendance request) {
        Attendance attendance = getAttendance(id);
        if (attendance == null) {
            return null;
        }

        attendance.setCheckInTime(request.getCheckInTime());

        String status = determineStatus(request.getCheckInTime());
        attendance.setStatus(status);

        return attendance;
    }
}
