package Todo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TodoController {

    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // 1. 날짜별 일정 조회
    public List<Task> getTasksByDate(String userId, LocalDate date) {
        // 비즈니스 로직이 필요하다면 여기에 추가 (예: 로그 남기기, 데이터 가공 등)
        return TodoDao.findByDate(userId, date);
    }

    // 2. 일정 추가
    public int addTask(Task task) {
        // 예: 데이터 유효성 검사 로직을 여기에 추가할 수 있음
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            System.out.println("제목이 없어 저장하지 않습니다."); 
            return -1;
        }
        return TodoDao.insert(task);
    }

    // 3. 일정 수정
    public void updateTask(Task task) {
        TodoDao.update(task);
    }

    // 4. 일정 삭제
    public void deleteTask(int taskId, String userId) {
        TodoDao.delete(taskId, userId);
    }

    public List<int[]> calculateFreeIntervals(List<Task> tasks) {
        List<int[]> busyIntervals = new ArrayList<>();
        
        for (Task t : tasks) {
            try {
                // 날짜 파싱 (Task 객체에 getStartDateTime() 같은 메서드를 만들면 더 좋음)
                String s = t.getStartDate().substring(0, 16);
                String e = t.getEndDate().substring(0, 16);
                LocalDateTime st = LocalDateTime.parse(s, FULL_FMT);
                LocalDateTime et = LocalDateTime.parse(e, FULL_FMT);
                
                // 시간을 분(0~1440)으로 변환
                int sMin = st.getHour() * 60 + st.getMinute();
                int eMin = et.getHour() * 60 + et.getMinute();
                
                // 자정을 넘어가거나 꽉 찬 경우 처리
                if (eMin == 0 && et.toLocalDate().isAfter(st.toLocalDate())) eMin = 1440; 
                
                busyIntervals.add(new int[]{sMin, eMin});
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        
        // 시작 시간 기준으로 정렬
        busyIntervals.sort(Comparator.comparingInt(a -> a[0]));

        // 겹치는 일정 병합 (Merge Intervals 알고리즘)
        List<int[]> mergedBusy = new ArrayList<>();
        if (!busyIntervals.isEmpty()) {
            int[] current = busyIntervals.get(0);
            for (int i = 1; i < busyIntervals.size(); i++) {
                int[] next = busyIntervals.get(i);
                if (current[1] >= next[0]) { // 겹치면 합침
                    current[1] = Math.max(current[1], next[1]);
                } else { 
                    mergedBusy.add(current); 
                    current = next; 
                }
            }
            mergedBusy.add(current);
        }

        // 빈 시간 찾기 (0분 ~ 1440분 사이의 빈 공간)
        List<int[]> freeIntervals = new ArrayList<>();
        int pointer = 0; 
        for (int[] busy : mergedBusy) {
            if (pointer < busy[0]) {
                freeIntervals.add(new int[]{pointer, busy[0]});
            }
            pointer = Math.max(pointer, busy[1]);
        }
        if (pointer < 1440) {
            freeIntervals.add(new int[]{pointer, 1440});
        }
        
        return freeIntervals;
    }
}