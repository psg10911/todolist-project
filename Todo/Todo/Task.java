package Todo;

public class Task {
    private int id;          // todos.id
    private String userId;   // todos.user_id

    // private 필드, 각 필드는 todos 테이블의 컬럼과 매핑
    private String title; // todos.title
    private String content; // todos.content
    private String startDate; // todos.start_date
    private String endDate; // todos.end_date
    private boolean completed; // todos.completed
    private int priority;    // ★ 추가됨: 1=높음, 2=보통, 3=낮음

    public Task() {
        this.priority = 2; // 중요도 기본 -> 보통
    }
    // 새 할 일 생성 시 사용하는 생성자
    public Task(String title, String content, String startDate, String endDate) {
        this.title = title;// todos.title
        this.content = content; // todos.content
        this.startDate = startDate; // todos.start_date
        this.endDate = endDate; // todos.end_date
        this.completed = false; // todos.completed
        this.priority = 2; // 중요도 기본 -> 보통
    }

    //DB에서 값을 읽어올 때 사용하는 생성자 (priority 포함)
     
    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed, int priority) { 
        this.id = id;// todos.id
        this.userId = userId;// todos.user_id
        this.title = title;// todos.title
        this.content = content; // todos.content
        this.startDate = startDate; // todos.start_date
        this.endDate = endDate; // todos.end_date
        this.completed = completed; // todos.completed
        this.priority = priority; // DB에서 읽어온 priority 값 설정
    }

    // DB에서 priority를 명시적으로 읽어오지 않았을 경우를 위한 오버로드 (호환성 유지)
    public Task(int id, String userId, String title, String content,
                String startDate, String endDate, boolean completed) {
        this.id = id;// todos.id
        this.userId = userId;// todos.user_id
        this.title = title;// todos.title
        this.content = content; // todos.content
        this.startDate = startDate; // todos.start_date
        this.endDate = endDate; // todos.end_date
        this.completed = completed; // todos.completed
        this.priority = 2; // 기본값 설정
    }

    // 워킹카피
    public Task(Task src) {
        if (src == null) return;
        this.id = src.id;// todos.id
        this.userId = src.userId;// todos.user_id
        this.title = src.title;// todos.title
        this.content = src.content; // todos.content
        this.startDate = src.startDate; // todos.start_date
        this.endDate = src.endDate; // todos.end_date
        this.completed = src.completed; // todos.completed
        this.priority = src.priority;   // ★ 복사 추가
    }

    // GET/SET
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }// todos.id

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }// todos.user_id

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }// todos.title

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }// todos.content

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }// todos.start_date

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }// todos.end_date

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }// todos.completed

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; } // todos.priority
}
