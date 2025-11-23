package Todo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


 //pipe('|') 구분 텍스트를 읽어 todos 테이블에 저장하는 유틸리티.
 //컬럼: user_id|title|content|startDate|endDate|completed|priority
 
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

    /** 한 줄 파싱 → Task로 변환 (유효성/트리밍 포함) */
    private static Task parseLineToTask(String line) {
        // 주석/빈줄 스킵
        if (line == null) return null;
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) return null;

        // 파이프 기준 분리
        String[] parts = line.split("\\|", -1); // 빈 필드 허용
        if (parts.length < 7) {
            System.out.println("경고: 컬럼 개수 부족 → " + line);
            return null;
        }

        String userId    = parts[0].trim();
        String title     = parts[1].trim();
        String content   = parts[2].trim();
        String startDate = parts[3].trim(); // "YYYY-MM-DD HH:MM:SS"
        String endDate   = parts[4].trim();
        String completedStr = parts[5].trim();
        String priorityStr  = parts[6].trim();

        if (userId.isEmpty() || title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            System.out.println("경고: 필수 필드 누락 → " + line);
            return null;
        }

        boolean completed = "1".equals(completedStr) || "true".equalsIgnoreCase(completedStr);
        int priority = 0; // default
        try {
            priority = Integer.parseInt(priorityStr);
        } catch (NumberFormatException nfe) {
            // 혹시 "HIGH/MEDIUM/LOW" 같은 텍스트가 오면 매핑
            String p = priorityStr.toUpperCase();
            if (p.equals("HIGH"))   priority = 3;
            else if (p.equals("MEDIUM")) priority = 2;
            else if (p.equals("LOW"))    priority = 1;
            else priority = 0;
        }

        Task t = new Task(title, content, startDate, endDate);
        t.setUserId(userId);
        t.setCompleted(completed);

        // Task에 priority 필드가 이미 있다면 세터 사용
        try {
            // 반영되어 있다면:
            t.setPriority(priority);
        } catch (NoSuchMethodError | Exception ignore) {
            // 아직 Task에 priority가 없다면 무시 (DB insert에서 values에만 사용 가능)
        }

        return t;
    }

    /**
     * 파일 전체 읽기 → DB 저장.
     * @return 성공 삽입된 레코드의 id 목록
     */
    public static List<Integer> readAllAndInsert(String filename) {
        Scanner filein = openFile(filename);
        List<Integer> newIds = new ArrayList<>();
        try {
            while (filein.hasNextLine()) {
                String line = filein.nextLine();
                Task task = parseLineToTask(line);
                if (task == null) continue;

                // DB insert
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
