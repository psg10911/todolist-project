package Todo;

/**
 * 할 일(Task) 데이터를 담는 모델 클래스 (POJO).
 */
public class Task {
    private int id;
    private String userId;

    private String title;
    private String content;
    private String startDate;
    private String endDate;
    private boolean completed;

    // TaskDialog 생성자 - insert용
    public Task(String title, String content, String startDate, String endDate, int priority) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false;     // 새로 추가된 할 일은 항상 '미완료'
    }

    // select용
    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed, int priority) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
    }
    
    /** 복사 생성자: 다이얼로그의 워킹카피 편집 패턴용 */
    public Task(Task src) {
        if (src == null) return;
        this.id = src.id;
        this.userId = src.userId;
        this.title = src.title;
        this.content = src.content;
        this.startDate = src.startDate;
        this.endDate = src.endDate;
        this.completed = src.completed;
    }
    
    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isCompleted() { return completed; }

    // Setters (UI에서 수정될 수 있는 값들)
    public void setId(int id) { this.id = id; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    
}

