package Todo;

public class Task {
    private int id;          // todos.id
    private String userId;   // todos.user_id

    private String title;
    private String content;
    private String startDate; 
    private String endDate;
    private boolean completed;

    private int priority;    // ★ 추가됨: 1=높음, 2=보통, 3=낮음

    public Task() {
        this.priority = 2; // 기본 보통
    }

    public Task(String title, String content, String startDate, String endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false;
        this.priority = 2; // 보통
    }

    /**
     * DB에서 값을 읽어올 때 사용하는 생성자 (priority 포함)
     */
    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed, int priority) { 
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
        this.priority = priority; // DB에서 읽어온 priority 값 설정
    }

    // DB에서 priority를 명시적으로 읽어오지 않았을 경우를 위한 오버로드 (호환성 유지)
    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
        this.priority = 2; // 기본값 설정
    }

    // 워킹카피
    public Task(Task src) {
        if (src == null) return;
        this.id = src.id;
        this.userId = src.userId;
        this.title = src.title;
        this.content = src.content;
        this.startDate = src.startDate;
        this.endDate = src.endDate;
        this.completed = src.completed;
        this.priority = src.priority;   // ★ 복사 추가
    }

    // GET/SET
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

    public int getPriority() { return priority; }        // ★ 추가
    public void setPriority(int priority) { this.priority = priority; } 
}
