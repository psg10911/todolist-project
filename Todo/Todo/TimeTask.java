package Todo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTask extends Task {
    
    public TimeTask(Task src) { super(src); }
    public TimeTask() { super(); }
    public TimeTask(String title, String content, String s, String e) { super(title, content, s, e); }
    public TimeTask(int id, String uid, String t, String c, String s, String e, boolean comp, Priority p) {
        super(id, uid, t, c, s, e, comp, p);
    }

    @Override
    public Task copy() { return new TimeTask(this); }

    // ★ [수정] 시간 포맷 (HH:mm) 반환
    @Override
    public String getFormattedStart() {
        return formatTime(getStartDate());
    }

    // ★ [수정] 시간 포맷 (HH:mm) 반환
    @Override
    public String getFormattedEnd() {
        return formatTime(getEndDate());
    }

    private String formatTime(String dtStr) {
        try {
            // DB 문자열(yyyy-MM-dd HH:mm:ss)을 파싱해서 시간만 추출
            if (dtStr.length() > 16) dtStr = dtStr.substring(0, 16);
            LocalDateTime dt = LocalDateTime.parse(dtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "";
        }
    }
}