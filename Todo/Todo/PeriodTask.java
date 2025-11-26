package Todo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PeriodTask extends Task {

    public PeriodTask(Task src) { super(src); }
    public PeriodTask() { super(); }
    public PeriodTask(String title, String content, String s, String e) { super(title, content, s, e); }
    public PeriodTask(int id, String uid, String t, String c, String s, String e, boolean comp, Priority p) {
        super(id, uid, t, c, s, e, comp, p);
    }

    @Override
    public Task copy() { return new PeriodTask(this); }

    // ★ [수정] 날짜 포맷 (MM-dd) 반환
    @Override
    public String getFormattedStart() {
        return formatDate(getStartDate());
    }

    // ★ [수정] 날짜 포맷 (MM-dd) 반환
    @Override
    public String getFormattedEnd() {
        return formatDate(getEndDate());
    }

    private String formatDate(String dtStr) {
        try {
            // DB 문자열을 파싱해서 날짜만 추출
            if (dtStr.length() > 16) dtStr = dtStr.substring(0, 16);
            LocalDateTime dt = LocalDateTime.parse(dtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return dt.format(DateTimeFormatter.ofPattern("MM-dd"));
        } catch (Exception e) {
            return "";
        }
    }
}