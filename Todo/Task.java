package Todo;

/**
 * 할 일(Task) 데이터를 담는 모델 클래스 (POJO).
 * 중요도(priority), 완료여부(completed) 포함 버전
 */
public class Task {
    private String title; // 일정 제목
    private String content; // 일정 내용
    private String startDate; // 시작일 (yyyy-MM-dd)
    private String endDate; // 종료일 (yyyy-MM-dd)
    private int priority; // 중요도 (1=낮음, 2=보통, 3=높음)
    private boolean completed; // 완료 여부

    // 🔹 기본 생성자 (필요 시)
    public Task() {
    }

    // 🔹 중요도 포함 생성자 (TaskDialog → TaskPanel로 전달)
    public Task(String title, String content, String startDate, String endDate, int priority) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.completed = false; // 새로 추가된 할 일은 기본적으로 미완료 상태
    }

    // 🔹 중요도 생략 가능 (기존 코드 호환용)
    public Task(String title, String content, String startDate, String endDate) {
        this(title, content, startDate, endDate, 2); // 기본 중요도: 2 (보통)
    }

    // ✅ Getter / Setter
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * JTable 등에 데이터를 전달할 때 사용하기 위한 Object 배열 변환 메서드.
     * (TaskPanel에서 tableModel.addRow(...) 등과 호환 가능)
     */
    public Object[] toObjectArray() {
        return new Object[] {
                completed, // 0: 완료 여부
                title, // 1: 제목
                startDate, // 2: 시작일
                endDate, // 3: 종료일
                priority // 4: 중요도
        };
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s ~ %s) %s",
                priority, title, startDate, endDate, completed ? "(완료)" : "(미완료)");
    }
}