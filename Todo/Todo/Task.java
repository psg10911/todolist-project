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
    private String priority; // 중요도: HIGH / MEDIUM / LOW


    // TaskDialog 생성자 -insert용
    public Task(String title, String content, String startDate, String endDate) {
        // this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false; // 새로 추가된 할 일은 항상 '미완료'
        this.priority = priority;
        
    }
    // select용
    public Task(int id, String userId, String title, String content,
            String startDate, String endDate, boolean completed) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.startDate = startDate;
    this.endDate = endDate;
    this.completed = completed;
    this.priority = priority;
}

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isCompleted() { return completed; }
    public String getPriority() { return priority; }

    // Setters(ui에서 수정될수있는 값들)
    public void setUserId(String userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setPriority(String priority) { this.priority = priority; }
    /**
     * [수업 자료] JTable의 tableModel.addRow() 에 넣기 적합한
     * Object 배열 형태로 데이터를 변환합니다.
     */
    public Object[] toObjectArray() {
        return new Object[]{
            // id,        // 0번: 고유 ID (Integer)
            // userId,    // 1번: 사용자 ID (String)
            completed, // 2번: 완료 여부 (Boolean)
            title,     // 3번: 제목 (String)
            startDate, // 4번: 시작일 (String)
            endDate,    // 5번: 종료일 (String)
            priority    // 6번: 중요도 (String) 
        };
    }
}
