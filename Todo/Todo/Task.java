package Todo;

public class Task {
    private int id;          // todos.id
    private String userId;   // todos.user_id

    private String title;
    private String content;
    private String startDate; 
    private String endDate;
    private boolean completed;

    public Task() {}

    public Task(String title, String content, String startDate, String endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false;
    }

    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = completed;
    }

    //워킹카피(복사 편집)용 복사 생성자 
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

    // getters / setters
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
}