package Todo;

// ★ abstract 키워드 추가
public abstract class Task {
    // 필드는 자식들도 쓰기 편하게 protected로 변경하거나 그대로 둡니다.
    // (여기서는 기존 private 유지하고 getter/setter 사용하겠습니다)
    private int id;
    private String userId;
    private String title;
    private String content;
    protected String startDate; // 자식에서 접근하기 위해 protected 권장
    protected String endDate;   // 자식에서 접근하기 위해 protected 권장
    private boolean completed;
    private Priority priority;

    public Task() { this.priority = Priority.MEDIUM; }

    public Task(String title, String content, String startDate, String endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false;
        this.priority = Priority.MEDIUM;
    }

    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed, Priority priority) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
        this.priority = priority;
    }
    
    // 복사 생성자
    public Task(Task src) {
        this(src.id, src.userId, src.title, src.content, src.startDate, src.endDate, src.completed, src.priority);
    }

    // ★★★ [추가] 자식들이 구현해야 할 "포맷팅된 시작/종료 시간" 메서드
    public abstract String getFormattedStart();
    public abstract String getFormattedEnd();

    public String getScheduleString() {
        return getFormattedStart() + " ~ " + getFormattedEnd();
    }

    public abstract Task copy();

    // (Getter/Setter는 기존 코드 그대로 유지 - 생략)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}