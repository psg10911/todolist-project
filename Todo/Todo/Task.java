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
    private int priority; // 중요도: 1(낮음), 2(보통), 3(높음)

    // TaskDialog 생성자 - insert용
    public Task(String title, String content, String startDate, String endDate, int priority) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;   // 중요도 값 (1, 2, 3)
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
    public int getPriority() { return priority; } // int형 반환

    // Setters (UI에서 수정될 수 있는 값들)
    public void setUserId(String userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setPriority(int priority) { this.priority = priority; } // int형 입력

    /**
     * [수업 자료] JTable의 tableModel.addRow() 에 넣기 적합한
     * Object 배열 형태로 데이터를 변환합니다.
     */
    public Object[] toObjectArray() {
        return new Object[]{
            completed,  // 완료 여부 (Boolean)
            title,      // 제목 (String)
            startDate,  // 시작일 (String)
            endDate,    // 종료일 (String)
            getPriorityLabel(priority) // 숫자를 사람이 보기 좋게 변환 (낮음/보통/높음)
        };
    }

    // 숫자 priority → 텍스트 변환 (UI 표시용)
    private String getPriorityLabel(int priority) {
        switch (priority) {
            case 3: return "높음";
            case 2: return "보통";
            case 1: return "낮음";
            default: return "-";
        }
    }
}
