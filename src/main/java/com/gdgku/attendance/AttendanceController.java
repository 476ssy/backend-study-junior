package com.gdgku.attendance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * [문제: 계층 분리(Layering) 부재]
 *
 * 이 컨트롤러는 데이터 저장(List)과 "지각 판정"이라는 핵심 비즈니스 로직까지 전부 떠안고 있다.
 * 문제는 그 하나의 규칙이 checkIn()과 updateCheckInTime() 두 곳에 각각 다시 구현되어 있고,
 * 그 과정에서 경계값 처리가 미묘하게 어긋났다는 점이다.
 * AttendanceControllerTest의 checkIn과_정정API는_같은_체크인_시각에_대해_같은_상태를_내려야한다()
 * 테스트가 이 불일치를 실제로 잡아낸다.
 *
 * 할 일: 지각 판정 로직을 AttendanceService(순수 자바 클래스)로 뽑아내 단일 진실 공급원으로 만들고,
 * Controller는 요청을 받아 Service를 호출하기만 하도록 리팩터링해서 테스트를 통과시키자.
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 10);
    private static final LocalTime ABSENT_CUTOFF = LocalTime.of(9, 30);

    private final List<Attendance> attendances = new ArrayList<>();
    private long nextId = 1L;

    public static class Attendance {
        private Long id;
        private String studentName;
        private LocalTime checkInTime;
        private String status;

        public Attendance() {
        }

        public Attendance(Long id, String studentName, LocalTime checkInTime, String status) {
            this.id = id;
            this.studentName = studentName;
            this.checkInTime = checkInTime;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public LocalTime getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(LocalTime checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @PostMapping("/check-in")
    public Attendance checkIn(@RequestBody Attendance request) {
        String status;
        if (!request.getCheckInTime().isAfter(LATE_CUTOFF)) {
            status = "ON_TIME";
        } else if (!request.getCheckInTime().isAfter(ABSENT_CUTOFF)) {
            status = "LATE";
        } else {
            status = "ABSENT";
        }

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

        String status;
        if (request.getCheckInTime().isBefore(LATE_CUTOFF)) {
            status = "ON_TIME";
        } else if (request.getCheckInTime().isBefore(ABSENT_CUTOFF)) {
            status = "LATE";
        } else {
            status = "ABSENT";
        }
        attendance.setStatus(status);

        return attendance;
    }
}
