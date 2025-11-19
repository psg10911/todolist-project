package Todo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class TaskPanel extends JPanel {

    private JLabel selectedDateLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private LocalDate currentDate;

    // 모든 날짜의 일정 저장 (검색, 표시 공용)
    private final ArrayList<Task> allTasks = new ArrayList<>();

    private final String[] columnNames = { "완료", "일정 제목", "시작일", "종료일" };

    public TaskPanel() {
        setLayout(new BorderLayout(5, 10));// 패널 간격 설정
        setPreferredSize(new Dimension(400, 0));// 고정 너비 설정

      
        // 상단 영역 (날짜 + 정렬 + 검색)

        JPanel topPanel = new JPanel(new BorderLayout());// 상단 패널 (날짜 + 정렬 + 검색)
        selectedDateLabel = new JLabel(" ", SwingConstants.CENTER);// 선택된 날짜 라벨
        selectedDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));// 폰트 설정
        topPanel.add(selectedDateLabel, BorderLayout.CENTER);// 가운데에 날짜 라벨 추가

         // 정렬 + 검색 패널 (오른쪽)

        JPanel sortSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));// 정렬 + 검색 패널
        JComboBox<String> sortComboBox = new JComboBox<>(new String[] { "최신순", "중요도순", "완료된순" });// 정렬 콤보박스
        JButton searchBtn = new JButton("🔍 검색");// 검색 버튼
        sortSearchPanel.add(sortComboBox);// 정렬 콤보박스
        sortSearchPanel.add(searchBtn);// 검색 버튼
        topPanel.add(sortSearchPanel, BorderLayout.EAST);// 오른쪽에 정렬 + 검색 패널 추가
        add(topPanel, BorderLayout.NORTH);// 상단 패널 추가


        // 중앙 JTable 영역

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;// 첫 번째 열은 Boolean (체크박스), 나머지는 String
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 체크박스만 수정 가능
            }
        };

        table = new JTable(tableModel);// 일정 테이블
        table.setRowHeight(28);// 행 높이 설정
        table.getColumnModel().getColumn(0).setPreferredWidth(40);// 완료 체크박스 열 너비 설정
        table.getColumnModel().getColumn(1).setPreferredWidth(180);// 제목 열 너비 설정

        // 드래그로 순서 변경 가능
        table.setDragEnabled(true);// 드래그 활성화
        table.setDropMode(DropMode.INSERT_ROWS);// 행 삽입 모드
        table.setTransferHandler(new TableRowTransferHandler(table));// 전송 핸들러 설정

        add(new JScrollPane(table), BorderLayout.CENTER);


        // 하단 버튼

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));// 버튼 패널
        JButton addTaskBtn = new JButton("할 일 추가");// 할 일 추가 버튼
        JButton delTaskBtn = new JButton("할 일 삭제");// 할 일 삭제 버튼
        buttonPanel.add(addTaskBtn);// 추가 버튼
        buttonPanel.add(delTaskBtn);// 삭제 버튼
        add(buttonPanel, BorderLayout.SOUTH);// 하단 버튼 패널 추가


        // 더미 일정 미리 등록 (검색 가능)

        registerDummyTasks();


        // 버튼 이벤트 처리

        addTaskBtn.addActionListener(e -> {
            TaskDialog dialog = new TaskDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);// 모달창 생성
            dialog.setVisible(true);// 모달창 표시
            if (dialog.getTask() != null) {
                Task newTask = dialog.getTask();// 새로 추가된 일정
                addIfNotExists(newTask);// 중복 방지 후 allTasks에 추가

                // 현재 날짜면 바로 화면에 표시
                if (currentDate != null && newTask.getStartDate().equals(currentDate.toString())) {
                    tableModel.addRow(newTask.toObjectArray());// 테이블에 새 일정 추가
                }
            }
        });

        delTaskBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();// 선택된 행 인덱스
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "이 할 일을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);// 삭제 확인 대화상자
                if (confirm == JOptionPane.YES_OPTION) {
                    String title = tableModel.getValueAt(selectedRow, 1).toString();// 선택된 일정 제목
                    String start = tableModel.getValueAt(selectedRow, 2).toString();// 선택된 일정 시작일

                     // 테이블과 allTasks에서 일정 제거
                    tableModel.removeRow(selectedRow);// 테이블에서 행 제거
                    allTasks.removeIf(t -> t.getTitle().equals(title) && t.getStartDate().equals(start));// allTasks에서 일정 제거
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택해주세요.");
            }
        });

        searchBtn.addActionListener(e -> openSearchDialog());
    }

    // 더미 일정 초기 등록 (검색 시 항상 포함됨)
    private void registerDummyTasks() {
        Task t1 = new Task("자바 Swing 스터디", "스터디 내용", "2025-11-10", "2025-11-10");
        Task t2 = new Task("프로젝트 디자인 구상", "프로젝트 회의", "2025-11-10", "2025-11-10");
        Task t3 = new Task("하이 하이 테스트", "테스트 일정", "2025-08-10", "2025-08-12");
        Task t4 = new Task("하이 분석", "분석 작업", "2025-08-10", "2025-08-15");

        addIfNotExists(t1);
        addIfNotExists(t2);
        addIfNotExists(t3);
        addIfNotExists(t4);
    }

    // 전체 일정 검색 모달창
    private void openSearchDialog() {
        JDialog searchDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "전체 일정 검색", true);// 모달 대화상자
        searchDialog.setLayout(new BorderLayout(10, 10));// 레이아웃 설정
        searchDialog.setSize(420, 350);// 크기 설정
        searchDialog.setLocationRelativeTo(this);// 위치 설정

         // 검색 상단 패널

        JPanel searchTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));// 검색 상단 패널
        JLabel searchLabel = new JLabel("키워드:");// 검색 라벨
        JTextField searchField = new JTextField(15);// 검색 입력 필드
        JButton execBtn = new JButton("검색");// 실행 버튼
        searchTop.add(searchLabel);// 검색 라벨
        searchTop.add(searchField);// 검색 입력 필드
        searchTop.add(execBtn);// 실행 버튼
        searchDialog.add(searchTop, BorderLayout.NORTH);// 상단 패널 추가

         // 검색 결과 테이블

        DefaultTableModel resultModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable resultTable = new JTable(resultModel);// 검색 결과 테이블
        resultTable.setRowHeight(28);// 행 높이 설정
        searchDialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);// 중앙에 결과 테이블 추가

         // 하단 닫기 버튼

        JButton closeBtn = new JButton("닫기");// 닫기 버튼
        closeBtn.addActionListener(e -> searchDialog.dispose());// 닫기 버튼 이벤트
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));// 하단 패널
        bottom.add(closeBtn);// 닫기 버튼 추가
        searchDialog.add(bottom, BorderLayout.SOUTH);// 하단 패널 추가

        execBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();// 검색 키워드
             // 유효성 검사
            if (keyword.length() < 2 || keyword.contains(" ")) {// 2글자 미만 또는 공백 포함
                JOptionPane.showMessageDialog(searchDialog, "키워드는 2글자 이상이며 공백을 포함할 수 없습니다.");// 경고창 표시
                return;// 종료
            }

            resultModel.setRowCount(0);// 기존 결과 초기화

             // 검색 수행
            for (Task t : allTasks) {
                if (t.getTitle().contains(keyword)
                        || t.getStartDate().contains(keyword)
                        || t.getEndDate().contains(keyword)) {// 제목, 시작일, 종료일에 키워드 포함 시
                    resultModel
                            .addRow(new Object[] { t.isCompleted(), t.getTitle(), t.getStartDate(), t.getEndDate() });// 결과 테이블에 추가
                }
            }

            if (resultModel.getRowCount() == 0)
                JOptionPane.showMessageDialog(searchDialog, "검색 결과가 없습니다.");// 결과 없음 알림
        });

        searchDialog.setVisible(true);// 모달 대화상자 표시
    }

    // 날짜 변경 시 호출
    public void loadTasksForDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일(E)");// 날짜 포맷터
        selectedDateLabel.setText(date.format(formatter));// 선택된 날짜 레이블 업데이트
        tableModel.setRowCount(0);  // 기존 행 초기화

         // 해당 날짜의 일정만 테이블에 추가

        for (Task t : allTasks) {
            if (t.getStartDate().equals(date.toString())) {
                tableModel.addRow(t.toObjectArray());
            }
        }
    }

    // 중복 방지 후 allTasks 추가
    private void addIfNotExists(Task task) {
        boolean exists = allTasks.stream()
                .anyMatch(t -> t.getTitle().equals(task.getTitle())// 제목과 시작일이 동일한지 확인
                        && t.getStartDate().equals(task.getStartDate()));// 중복 여부 확인
        if (!exists)// 중복이 아니면 추가
            allTasks.add(task);// 일정 추가
    }

    // JTable 행 드래그 순서 변경용 TransferHandler
    static class TableRowTransferHandler extends TransferHandler {
        private final JTable table;

        public TableRowTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int row = table.getSelectedRow();
            return new StringSelection(String.valueOf(row));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop();
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!support.isDrop())
                return false;
            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dl.getRow();
            int dragRow;
            try {
                dragRow = Integer.parseInt((String) support.getTransferable()
                        .getTransferData(DataFlavor.stringFlavor));
            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }

            if (dragRow == dropRow)
                return false;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Object[] rowData = new Object[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++)
                rowData[i] = model.getValueAt(dragRow, i);
            model.removeRow(dragRow);
            if (dropRow > dragRow)
                dropRow--;
            model.insertRow(dropRow, rowData);
            table.setRowSelectionInterval(dropRow, dropRow);
            return true;
        }
    }
}
