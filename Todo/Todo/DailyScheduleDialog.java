package Todo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class DailyScheduleDialog extends JDialog {

    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    
    private String currentUserId;
    private List<Task> todayTasks;
    
    private DefaultListModel<String> taskListModel; 
    private DefaultListModel<String> freeTimeListModel;
    private ClockPanel clockPanel; 

    public DailyScheduleDialog(Window parent, String userId) {
        super(parent, "오늘의 일정 상세", ModalityType.APPLICATION_MODAL);
        this.currentUserId = userId;
        this.todayTasks = new ArrayList<>();

        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.CARD_BG); // 테마 배경색

        taskListModel = new DefaultListModel<>();
        freeTimeListModel = new DefaultListModel<>();
        
        loadTodayTasks(); 
        updateFreeTimeList();

        // --- UI 구성 ---

        // 1. 상단 제목
        JLabel titleLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일의 하루")), SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_BOLD_24);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 2. 중앙 패널
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 간격 넓힘
        centerPanel.setBackground(Theme.CARD_BG);
        centerPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

        // [좌측] 시계 패널
        clockPanel = new ClockPanel();
        centerPanel.add(clockPanel);

        // [우측] 정보 패널
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setResizeWeight(0.5);
        rightSplitPane.setBorder(null);
        rightSplitPane.setDividerSize(8); // 구분선 조금 더 두껍게
        rightSplitPane.setBackground(Theme.BACKGROUND);

        // 리스트 패널 스타일링 헬퍼
        Border outerBorder = BorderFactory.createLineBorder(Theme.BORDER);
        
        // 2-1) 상단: 일정 목록
        JPanel taskListPanel = new JPanel(new BorderLayout());
        taskListPanel.setBackground(Theme.CARD_BG);
        
        TitledBorder taskTitle = BorderFactory.createTitledBorder(outerBorder, " 📅 오늘의 할 일 (더블클릭 수정) ");
        taskTitle.setTitleFont(Theme.FONT_BOLD_16);
        taskTitle.setTitleColor(Theme.PRIMARY);
        taskListPanel.setBorder(taskTitle);
        
        JList<String> taskList = new JList<>(taskListModel);
        taskList.setFont(Theme.FONT_REGULAR_14);
        taskList.setFixedCellHeight(35); // 높이 여유 있게
        taskList.setSelectionBackground(new Color(230, 240, 255));
        taskList.setSelectionForeground(Theme.TEXT_MAIN);
        
        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(0, 10, 0, 0));
                if (value.toString().startsWith("●")) {
                    l.setForeground(Theme.TEXT_MAIN);
                }
                return l;
            }
        });

        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < todayTasks.size()) {
                        openEditDialog(todayTasks.get(index));
                    }
                }
            }
        });
        taskListPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // 2-2) 하단: 빈 시간 목록
        JPanel freeListPanel = new JPanel(new BorderLayout());
        freeListPanel.setBackground(Theme.CARD_BG);
        
        TitledBorder freeTitle = BorderFactory.createTitledBorder(outerBorder, " 🌿 쉴 수 있는 빈 시간 ");
        freeTitle.setTitleFont(Theme.FONT_BOLD_16);
        freeTitle.setTitleColor(new Color(39, 174, 96)); // 초록색
        freeListPanel.setBorder(freeTitle);
        
        JList<String> freeTimeList = new JList<>(freeTimeListModel);
        freeTimeList.setFont(Theme.FONT_REGULAR_14);
        freeTimeList.setFixedCellHeight(35);
        freeTimeList.setSelectionBackground(new Color(235, 250, 235));
        
        freeTimeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(0, 10, 0, 0));
                if (value.toString().contains("[현재]")) {
                    l.setForeground(new Color(39, 174, 96));
                    l.setFont(Theme.FONT_BOLD_16);
                } else {
                    l.setForeground(Theme.TEXT_MAIN);
                }
                return l;
            }
        });
        freeListPanel.add(new JScrollPane(freeTimeList), BorderLayout.CENTER);

        rightSplitPane.setTopComponent(taskListPanel);
        rightSplitPane.setBottomComponent(freeListPanel);

        centerPanel.add(rightSplitPane);
        add(centerPanel, BorderLayout.CENTER);

        // 3. 하단 버튼
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Theme.CARD_BG);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton closeBtn = new JButton("닫기");
        Theme.styleButton(closeBtn); // 테마 버튼 스타일 적용
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openEditDialog(Task originalTask) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TaskDialog dialog = new TaskDialog(parentFrame, originalTask);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Task updatedTask = dialog.getTask();
        if (updatedTask != null) {
            updatedTask.setId(originalTask.getId());
            updatedTask.setUserId(originalTask.getUserId());
            TodoDao.update(updatedTask);
            loadTodayTasks();
            updateFreeTimeList();
            clockPanel.calculateLayout();
            clockPanel.repaint();
        }
    }

    private void loadTodayTasks() {
        LocalDate today = LocalDate.now();
        List<Task> allTasks = TodoDao.findByDate(currentUserId, today);
        taskListModel.clear();
        todayTasks.clear();

        for (Task t : allTasks) {
            try {
                String startStr = t.getStartDate();
                String endStr = t.getEndDate();
                if (startStr == null || endStr == null) continue;
                if (startStr.length() > 16) startStr = startStr.substring(0, 16);
                if (endStr.length() > 16) endStr = endStr.substring(0, 16);

                LocalDateTime startDT = LocalDateTime.parse(startStr, FULL_FMT);
                LocalDateTime endDT = LocalDateTime.parse(endStr, FULL_FMT);

                if (!startDT.toLocalDate().equals(endDT.toLocalDate())) continue;
                if (startDT.toLocalDate().equals(today)) todayTasks.add(t);
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        Collections.sort(todayTasks, Comparator.comparing(Task::getStartDate));

        if (todayTasks.isEmpty()) {
            taskListModel.addElement("오늘 예정된 일정이 없습니다.");
        } else {
            for (Task t : todayTasks) {
                try {
                    LocalDateTime st = LocalDateTime.parse(t.getStartDate().substring(0, 16), FULL_FMT);
                    LocalDateTime et = LocalDateTime.parse(t.getEndDate().substring(0, 16), FULL_FMT);
                    String timeStr = st.format(TIME_FMT) + " ~ " + et.format(TIME_FMT);
                    String status = t.isCompleted() ? "(완료)" : "";
                    taskListModel.addElement(String.format("●  [%s]  %s  %s", timeStr, t.getTitle(), status));
                } catch (Exception e) {}
            }
        }
    }

    private void updateFreeTimeList() {
        freeTimeListModel.clear();
        List<int[]> freeSlots = calculateFreeIntervals();
        
        if (freeSlots.isEmpty()) {
            freeTimeListModel.addElement("오늘은 꽉 찬 하루네요! 화이팅 🔥");
            return;
        }

        int currentMin = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        for (int[] slot : freeSlots) {
            int start = slot[0];
            int end = slot[1];
            int duration = end - start;
            String sTime = String.format("%02d:%02d", start / 60, start % 60);
            String eTime = String.format("%02d:%02d", end / 60, end % 60);
            
            String durationStr = "";
            if (duration >= 60) durationStr += (duration / 60) + "시간 ";
            if (duration % 60 > 0) durationStr += (duration % 60) + "분";
            
            String itemText = String.format("%s ~ %s   (%s)", sTime, eTime, durationStr);
            if (currentMin >= start && currentMin < end) itemText = "▶ " + itemText + "  [현재]";
            freeTimeListModel.addElement(itemText);
        }
    }

    private List<int[]> calculateFreeIntervals() {
        List<int[]> busyIntervals = new ArrayList<>();
        for (Task t : todayTasks) {
            try {
                String s = t.getStartDate().substring(0, 16);
                String e = t.getEndDate().substring(0, 16);
                LocalDateTime st = LocalDateTime.parse(s, FULL_FMT);
                LocalDateTime et = LocalDateTime.parse(e, FULL_FMT);
                int sMin = st.getHour() * 60 + st.getMinute();
                int eMin = et.getHour() * 60 + et.getMinute();
                if (eMin == 0 && et.toLocalDate().isAfter(st.toLocalDate())) eMin = 1440; 
                busyIntervals.add(new int[]{sMin, eMin});
            } catch (Exception ex) {}
        }
        busyIntervals.sort(Comparator.comparingInt(a -> a[0]));

        List<int[]> mergedBusy = new ArrayList<>();
        if (!busyIntervals.isEmpty()) {
            int[] current = busyIntervals.get(0);
            for (int i = 1; i < busyIntervals.size(); i++) {
                int[] next = busyIntervals.get(i);
                if (current[1] >= next[0]) current[1] = Math.max(current[1], next[1]);
                else { mergedBusy.add(current); current = next; }
            }
            mergedBusy.add(current);
        }

        List<int[]> freeIntervals = new ArrayList<>();
        int pointer = 0; 
        for (int[] busy : mergedBusy) {
            if (pointer < busy[0]) freeIntervals.add(new int[]{pointer, busy[0]});
            pointer = Math.max(pointer, busy[1]);
        }
        if (pointer < 1440) freeIntervals.add(new int[]{pointer, 1440});
        return freeIntervals;
    }

    private class ClockPanel extends JPanel {
        private class RenderArc {
            Task task;
            double startAngle, extentAngle;
            int startMin, endMin, layerIndex = 0; 
            public RenderArc(Task t, int sMin, int eMin) {
                this.task = t; this.startMin = sMin; this.endMin = eMin;
                this.startAngle = 90 - ((double)sMin / 1440.0 * 360.0);
                double duration = eMin - sMin;
                this.extentAngle = -((duration / 1440.0) * 360.0);
            }
        }
        private List<RenderArc> renderArcs = new ArrayList<>();
        private int maxLayerDepth = 1;

        public ClockPanel() { setBackground(Theme.CARD_BG); calculateLayout(); }

        public void calculateLayout() {
            renderArcs.clear();
            if (todayTasks.isEmpty()) return;
            for (Task t : todayTasks) {
                try {
                    String s = t.getStartDate().substring(0, 16);
                    String e = t.getEndDate().substring(0, 16);
                    LocalTime st = LocalDateTime.parse(s, FULL_FMT).toLocalTime();
                    LocalTime et = LocalDateTime.parse(e, FULL_FMT).toLocalTime();
                    int sMin = st.getHour() * 60 + st.getMinute();
                    int eMin = et.getHour() * 60 + et.getMinute();
                    if (eMin == 0 && et.isBefore(st)) eMin = 1440; 
                    renderArcs.add(new RenderArc(t, sMin, eMin));
                } catch(Exception ex) {}
            }
            List<Integer> layerEndTimes = new ArrayList<>();
            for (RenderArc arc : renderArcs) {
                boolean placed = false;
                for (int i = 0; i < layerEndTimes.size(); i++) {
                    if (layerEndTimes.get(i) <= arc.startMin) {
                        arc.layerIndex = i; layerEndTimes.set(i, arc.endMin); placed = true; break;
                    }
                }
                if (!placed) { arc.layerIndex = layerEndTimes.size(); layerEndTimes.add(arc.endMin); }
            }
            maxLayerDepth = layerEndTimes.size();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int outerDiameter = Math.min(w, h) - 40; 
            int centerX = w / 2, centerY = h / 2;
            int maxRadius = outerDiameter / 2;
            
            drawFreeTimeArcs(g2, centerX, centerY, maxRadius);
            drawClockFace(g2, centerX, centerY, maxRadius);

            int innerHoleRadius = maxRadius / 4; 
            int availableRadius = maxRadius - innerHoleRadius;
            int layerThickness = availableRadius / Math.max(1, maxLayerDepth);

            for (RenderArc arc : renderArcs) {
                int currentOuterR = maxRadius - (arc.layerIndex * layerThickness);
                Color color = getHashColor(arc.task.getTitle());
                g2.setColor(color);
                g2.setStroke(new BasicStroke(layerThickness - 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                g2.draw(new Arc2D.Double(centerX - (currentOuterR - layerThickness/2.0), centerY - (currentOuterR - layerThickness/2.0), (currentOuterR - layerThickness/2.0) * 2, (currentOuterR - layerThickness/2.0) * 2, arc.startAngle, arc.extentAngle, Arc2D.OPEN));
                drawText(g2, arc.task.getTitle(), arc.startAngle + arc.extentAngle/2, centerX, centerY, currentOuterR - layerThickness/2.0);
            }
            drawCurrentTimeHand(g2, centerX, centerY, maxRadius);
        }

        private void drawFreeTimeArcs(Graphics2D g2, int cx, int cy, int radius) {
            List<int[]> freeSlots = calculateFreeIntervals();
            g2.setColor(new Color(225, 247, 225)); 
            for (int[] slot : freeSlots) {
                int duration = slot[1] - slot[0];
                if (duration <= 0) continue;
                double startAngle = 90 - ((double)slot[0] / 1440.0 * 360.0);
                double extentAngle = -((double)duration / 1440.0 * 360.0);
                g2.fill(new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2, startAngle, extentAngle, Arc2D.PIE));
            }
        }

        private void drawClockFace(Graphics2D g2, int cx, int cy, int radius) {
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
            g2.setColor(Theme.TEXT_MAIN);
            for (int i = 0; i < 24; i++) {
                double angle = 90 - (i / 24.0 * 360);
                double rad = Math.toRadians(angle);
                int tickLen = (i % 6 == 0) ? 15 : 5;
                int x1 = (int) (cx + (radius - tickLen) * Math.cos(rad));
                int y1 = (int) (cy - (radius - tickLen) * Math.sin(rad));
                int x2 = (int) (cx + radius * Math.cos(rad));
                int y2 = (int) (cy - radius * Math.sin(rad));
                g2.drawLine(x1, y1, x2, y2);
                if (i % 6 == 0) {
                    String label = String.valueOf(i);
                    g2.setFont(Theme.FONT_BOLD_16);
                    int tx = (int) (cx + (radius + 15) * Math.cos(rad)); 
                    int ty = (int) (cy - (radius + 15) * Math.sin(rad));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(label, tx - fm.stringWidth(label)/2, ty + fm.getAscent()/2 - 2);
                }
            }
        }

        private void drawCurrentTimeHand(Graphics2D g2, int cx, int cy, int radius) {
            LocalTime now = LocalTime.now();
            double nowAngle = 90 - ((now.getHour() * 60 + now.getMinute()) / 1440.0 * 360.0);
            double rad = Math.toRadians(nowAngle);
            g2.setColor(new Color(231, 76, 60)); // ACCENT Color
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(cx, cy, (int)(cx + (radius - 10) * Math.cos(rad)), (int)(cy - (radius - 10) * Math.sin(rad)));
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        }

        private void drawText(Graphics2D g2, String text, double angleDeg, int cx, int cy, double dist) {
            double rad = Math.toRadians(angleDeg); 
            int tx = (int) (cx + dist * Math.cos(rad));
            int ty = (int) (cy - dist * Math.sin(rad));
            Font font = Theme.FONT_REGULAR_12;
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            if (textW > 80) { 
                text = text.substring(0, Math.min(text.length(), 8)) + "..";
                textW = fm.stringWidth(text);
            }
            g2.setColor(new Color(255, 255, 255, 220)); 
            g2.fillRect(tx - textW/2 - 2, ty - fm.getAscent()/2 - 2, textW + 4, fm.getHeight());
            g2.setColor(Color.BLACK);
            g2.drawString(text, tx - textW/2, ty + fm.getAscent()/2 - 2);
        }

        private Color getHashColor(String text) {
            int hash = text.hashCode();
            int r = ((hash & 0xFF0000) >> 16) % 127 + 128;
            int g = ((hash & 0x00FF00) >> 8) % 127 + 128;
            int b = (hash & 0x0000FF) % 127 + 128;
            return new Color(r, g, b);
        }
    }
}