package Todo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// pipe('|') 구분 텍스트를 읽어 todos 테이블에 저장하는 유틸리티.
// columns: user_id|title|content|startDate|endDate|completed|priority|dtype
public class TodoFile {

    public static Scanner openFile(String filename) {
        Scanner filein = null;
        try {
            filein = new Scanner(new File(filename), "UTF-8");
        } catch (Exception e) {
            System.out.println("CWD = " + new java.io.File(".").getAbsolutePath());
            System.out.printf("파일 오픈 실패: %s%n", filename);
            throw new RuntimeException(e);
        }
        return filein;
    }

    /** 한 줄 파싱 → Task로 변환 (dtype 컬럼 반영) */
    private static Task parseLineToTask(String line) {
        // 주석/빈줄 스킵
        if (line == null) return null;
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) return null;

        // 파이프 기준 분리
        String[] parts = line.split("\\|", -1); 
        
        // ★ [수정] 최소 컬럼 개수 확인 (dtype이 추가되어 8개여야 하지만, 구 버전 호환을 위해 7개 이상이면 통과)
        if (parts.length < 7) {
            System.out.println("경고: 컬럼 개수 부족 → " + line);
            return null;
        }

        String userId    = parts[0].trim();
        String title     = parts[1].trim();
        String content   = parts[2].trim();
        String startDate = parts[3].trim();
        String endDate   = parts[4].trim();
        String completedStr = parts[5].trim();
        String priorityStr  = parts[6].trim();
        
        // ★ [추가] 8번째 컬럼(dtype) 읽기 (없으면 빈 문자열)
        String dtypeStr = (parts.length >= 8) ? parts[7].trim() : "";

        if (userId.isEmpty() || title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            System.out.println("경고: 필수 필드 누락 → " + line);
            return null;
        }

        boolean completed = "1".equals(completedStr) || "true".equalsIgnoreCase(completedStr);
        
        // 우선순위 파싱 (숫자 -> int)
        int priorityInt = 2; // Default: 보통
        try {
            priorityInt = Integer.parseInt(priorityStr);
        } catch (NumberFormatException nfe) {
            // 예외 처리 (기본값 유지)
        }

        // ★ [핵심 수정] dtype을 확인하여 알맞은 자식 객체 생성
        Task t;
        
        if ("PERIOD".equalsIgnoreCase(dtypeStr)) {
            // 1. 파일에 "PERIOD"라고 적혀 있으면 기간 일정 생성
            t = new PeriodTask(title, content, startDate, endDate);
        } else if ("TIME".equalsIgnoreCase(dtypeStr)) {
            // 2. 파일에 "TIME"이라고 적혀 있으면 시간 일정 생성
            t = new TimeTask(title, content, startDate, endDate);
        } else {
            // 3. (안전장치) dtype이 없거나 모르는 값이면, 기존 로직(날짜 비교)으로 자동 판단
            if (isSameDay(startDate, endDate)) {
                t = new TimeTask(title, content, startDate, endDate);
            } else {
                t = new PeriodTask(title, content, startDate, endDate);
            }
        }

        // 공통 필드 설정
        t.setUserId(userId);
        t.setCompleted(completed);
        
        // ★ [중요] int 값을 Enum으로 변환하여 설정
        t.setPriority(Priority.fromDbValue(priorityInt));

        return t;
    }

    // 날짜 비교 헬퍼 메서드 (YYYY-MM-DD 부분만 비교)
    private static boolean isSameDay(String start, String end) {
        try {
            String sDate = start.length() >= 10 ? start.substring(0, 10) : start;
            String eDate = end.length() >= 10 ? end.substring(0, 10) : end;
            return sDate.equals(eDate);
        } catch (Exception e) {
            return true; 
        }
    }

    public static List<Integer> readAllAndInsert(String filename) {
        Scanner filein = openFile(filename);
        List<Integer> newIds = new ArrayList<>();
        try {
            while (filein.hasNextLine()) {
                String line = filein.nextLine();
                Task task = parseLineToTask(line);
                if (task == null) continue;

                // DB insert (TodoDao가 task의 실제 타입(Time/Period)을 확인해 DB dtype 컬럼에 저장함)
                int newId = TodoDao.insert(task); 
                task.setId(newId);
                newIds.add(newId);
            }
        } finally {
            filein.close();
        }
        return newIds;
    }
}