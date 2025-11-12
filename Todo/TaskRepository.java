package Todo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * [신규 클래스]
 * Task 데이터(allTasks)를 관리하는 저장소 클래스 (Model).
 * 데이터 추가, 삭제, 검색, 정렬, 필터링 로직을 담당합니다.
 */
public class TaskRepository {

    // 1. 데이터 원본 (TaskPanel에서 이동)
    private ArrayList<Task> allTasks = new ArrayList<>();

    public TaskRepository() {
        // 2. 더미 데이터 등록 (TaskPanel에서 이동)
        registerDummyTasks();
    }

    // 3. 데이터 조작 메서드
    public void addTask(Task task) {
        allTasks.add(task);
    }

    public void deleteTask(Task task) {
        allTasks.remove(task);
    }

    /**
     * 현재 선택된 날짜의 모든 Task 삭제 (TaskPanel의 '전체 삭제' 로직)
     * * @param currentDate (null이면 모든 Task 삭제)
     */
    public void deleteAllTasks(LocalDate currentDate) {
        if (currentDate == null) {
            allTasks.clear();
        } else {
            allTasks.removeIf(t -> {
                LocalDate start = LocalDate.parse(t.getStartDate());
                LocalDate end = LocalDate.parse(t.getEndDate());
                return (currentDate.isEqual(start) || currentDate.isAfter(start))
                        && (currentDate.isEqual(end) || currentDate.isBefore(end));
            });
        }
    }

    /**
     * Task 순서 위로 이동 (TaskPanel에서 이동)
     */
    public void moveTaskUp(Task task) {
        int index = allTasks.indexOf(task);
        if (index > 0) {
            allTasks.remove(index);
            allTasks.add(index - 1, task);
        }
    }

    /**
     * Task 순서 아래로 이동 (TaskPanel에서 이동)
     */
    public void moveTaskDown(Task task) {
        int index = allTasks.indexOf(task);
        if (index >= 0 && index < allTasks.size() - 1) {
            allTasks.remove(index);
            allTasks.add(index + 1, task);
        }
    }

    // 4. 데이터 조회 메서드
    /**
     * 필터링 및 정렬된 Task 리스트 반환 (TaskPanel의 refreshTaskList 로직)
     * * @param currentDate 기준 날짜
     * @param sortOption  정렬 옵션
     * @return 필터링/정렬된 리스트
     */
    public ArrayList<Task> getFilteredAndSortedTasks(LocalDate currentDate, String sortOption) {
        ArrayList<Task> filtered = new ArrayList<>();

        // 4-1. 날짜 필터링
        for (Task t : allTasks) {
            if (currentDate == null) {
                filtered.add(t);
                continue;
            }
            LocalDate start = LocalDate.parse(t.getStartDate());
            LocalDate end = LocalDate.parse(t.getEndDate());
            if ((currentDate.isEqual(start) || currentDate.isAfter(start))
                    && (currentDate.isEqual(end) || currentDate.isBefore(end))) {
                filtered.add(t);
            }
        }

        // 4-2. 정렬
        switch (sortOption) {
            case "중요도순" -> filtered.sort(Comparator.comparing(Task::getPriority).reversed());
            case "완료된순" -> filtered.sort(Comparator.comparing(Task::isCompleted).reversed());
            // "필터" (기본값)는 정렬하지 않음
        }
        return filtered;
    }

    /**
     * 키워드로 Task 검색 (TaskPanel의 openSearchDialog 로직)
     * * @param keyword 검색어
     * @return 검색된 리스트
     */
    public ArrayList<Task> searchTasks(String keyword) {
        return allTasks.stream()
                .filter(t -> t.getTitle().contains(keyword)
                        || t.getStartDate().contains(keyword)
                        || t.getEndDate().contains(keyword))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 오늘의 미완료 Task 개수 반환 (TaskPanel에서 이동)
     * * @return 미완료 개수
     */
    public int getIncompleteTaskCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Task t : allTasks) {
            LocalDate start = LocalDate.parse(t.getStartDate());
            LocalDate end = LocalDate.parse(t.getEndDate());
            if (!t.isCompleted()
                    && ((today.isEqual(start) || today.isAfter(start))
                            && (today.isEqual(end) || today.isBefore(end)))) {
                count++;
            }
        }
        return count;
    }

    /**
     * 더미 데이터 등록 (TaskPanel에서 이동)
     */
    private void registerDummyTasks() {
        allTasks.add(new Task("자바 Swing 스터디", "스터디 내용", "2025-11-12", "2025-11-12", 1));
        allTasks.add(new Task("프로젝트 디자인 구상", "회의", "2025-11-12", "2025-11-12", 2));
        allTasks.add(new Task("하이 하이 테스트", "테스트 일정", "2025-11-12", "2025-11-13", 3));
        allTasks.add(new Task("하이 분석", "분석 작업", "2025-11-12", "2025-11-12", 1));
        allTasks.add(new Task("ㄴㅇㄴㅇ", "ㅁㄴㅇㅁㄴㅇ", "2025-11-14", "2025-11-14", 1));
    }
}