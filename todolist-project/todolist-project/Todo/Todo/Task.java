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
        this.title = title;// 제목
        this.content = content;// 내용
        this.startDate = startDate;// 시작일
        this.endDate = endDate;// 종료일
        this.completed = false; // 새로 추가된 할 일은 항상 '미완료'
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isCompleted() { return completed; }

    // Setters
    public Object[] toObjectArray() {
        return new Object[]{
            completed, // 0번: 완료 여부 (Boolean)
            title,     // 1번: 제목 (String)
            startDate, // 2번: 시작일 (String)
            endDate    // 3번: 종료일 (String)
        };
    }
}
