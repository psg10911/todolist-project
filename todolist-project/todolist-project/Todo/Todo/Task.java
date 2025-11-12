package Todo;
/**
 * 할 일(Task) 데이터를 담는 모델 클래스 (POJO).
 */
public class Task {
    private String title;
    private String content;
    private String startDate;
    private String endDate;
    private boolean completed;

    // TaskDialog에서 사용할 생성자
    public Task(String title, String content, String startDate, String endDate) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completed = false; // 새로 추가된 할 일은 항상 '미완료'
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isCompleted() { return completed; }

    /**
     * [수업 자료] JTable의 tableModel.addRow() 에 넣기 적합한
     * Object 배열 형태로 데이터를 변환합니다.
     */
    public Object[] toObjectArray() {
        return new Object[]{
            completed, // 0번: 완료 여부 (Boolean)
            title,     // 1번: 제목 (String)
            startDate, // 2번: 시작일 (String)
            endDate    // 3번: 종료일 (String)
        };
    }
}