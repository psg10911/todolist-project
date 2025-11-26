package Todo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 주간 시간표를 그리는 전용 패널 (UI/Rendering 담당)
 * WeeklyTimeTableDialog에서 분리됨
 */
public class TimeTablePanel extends JPanel {
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd");

    private final int ROW_HEIGHT = 50; 
    private final int HEADER_HEIGHT = 45; 
    private final int TIME_COL_WIDTH = 60; 
    
    private LocalDate startOfWeek;
    private Map<Integer, List<RenderBlock>> dayBlocks = new HashMap<>();
    
    // ★ 더블 클릭 시 실행할 행동(콜백)을 저장할 변수
    private Consumer<Task> onTaskDoubleClicked;

    public TimeTablePanel() {
        setBackground(Theme.CARD_BG);
        setPreferredSize(new Dimension(800, 24 * ROW_HEIGHT + HEADER_HEIGHT + 20)); 
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    checkClick(e.getPoint());
                }
            }
        });
    }

    // ★ 외부에서 "더블 클릭되면 뭐 할지"를 설정하는 메서드
    public void setOnTaskDoubleClicked(Consumer<Task> action) {
        this.onTaskDoubleClicked = action;
    }

    private void checkClick(Point p) {
        for (List<RenderBlock> blocks : dayBlocks.values()) {
            for (RenderBlock block : blocks) {
                if (block.bounds != null && block.bounds.contains(p)) {
                    // 설정된 행동이 있으면 실행 (Task 전달)
                    if (onTaskDoubleClicked != null) {
                        onTaskDoubleClicked.accept(block.task);
                    }
                    return; 
                }
            }
        }
    }

    public void setData(LocalDate startOfWeek, List<Task> tasks) {
        this.startOfWeek = startOfWeek;
        this.dayBlocks.clear();
        distributeTasks(tasks);
        for (int i = 0; i < 7; i++) {
            if (dayBlocks.containsKey(i)) {
                calculateLayout(dayBlocks.get(i));
            }
        }
        repaint();
    }

    private void distributeTasks(List<Task> tasks) {
        if (tasks == null) return;
        for (Task t : tasks) {
            try {
                String sStr = t.getStartDate(); String eStr = t.getEndDate();
                if (sStr == null || eStr == null) continue;
                if (sStr.length() > 16) sStr = sStr.substring(0, 16);
                if (eStr.length() > 16) eStr = eStr.substring(0, 16);

                LocalDateTime startDT = LocalDateTime.parse(sStr, FULL_FMT);
                LocalDateTime endDT = LocalDateTime.parse(eStr, FULL_FMT);

                if (!startDT.toLocalDate().equals(endDT.toLocalDate())) continue; 

                for (int i = 0; i < 7; i++) {
                    LocalDate currentDay = startOfWeek.plusDays(i);
                    LocalDateTime dayStart = currentDay.atStartOfDay();
                    LocalDateTime dayEnd = currentDay.atTime(LocalTime.MAX);

                    if (startDT.isBefore(dayEnd) && endDT.isAfter(dayStart)) {
                        int startMin = startDT.getHour() * 60 + startDT.getMinute();
                        int endMin = endDT.getHour() * 60 + endDT.getMinute();
                        if (endDT.isAfter(dayEnd)) endMin = 24 * 60;

                        RenderBlock block = new RenderBlock(t, startMin, endMin);
                        dayBlocks.computeIfAbsent(i, k -> new ArrayList<>()).add(block);
                    }
                }
            } catch (Exception e) {}
        }
    }

    private void calculateLayout(List<RenderBlock> blocks) {
        if (blocks.isEmpty()) return;
        Collections.sort(blocks, Comparator.comparingInt(b -> b.startMin));
        List<List<RenderBlock>> columns = new ArrayList<>();
        for (RenderBlock block : blocks) {
            boolean placed = false;
            for (List<RenderBlock> col : columns) {
                RenderBlock last = col.get(col.size() - 1);
                if (last.endMin <= block.startMin) {
                    col.add(block);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                List<RenderBlock> newCol = new ArrayList<>();
                newCol.add(block);
                columns.add(newCol);
            }
        }
        int totalCols = columns.size();
        for (int i = 0; i < totalCols; i++) {
            for (RenderBlock b : columns.get(i)) {
                b.colIndex = i;
                b.totalCols = totalCols;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth(); int height = getHeight();
        int colWidth = (width - TIME_COL_WIDTH) / 7;

        g2.setFont(Theme.FONT_REGULAR_12);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= 24; i++) {
            int y = HEADER_HEIGHT + (i * ROW_HEIGHT);
            g2.setColor(new Color(240, 240, 240)); 
            g2.drawLine(TIME_COL_WIDTH, y, width, y);
            
            if (i < 24) {
                String timeStr = String.format("%02d:00", i);
                g2.setColor(Theme.TEXT_SUB);
                g2.drawString(timeStr, TIME_COL_WIDTH - fm.stringWidth(timeStr) - 8, y + fm.getAscent() / 2);
            }
        }

        g2.setColor(Theme.BORDER);
        g2.drawLine(TIME_COL_WIDTH, 0, TIME_COL_WIDTH, height);
        for (int i = 1; i <= 7; i++) {
            int x = TIME_COL_WIDTH + (i * colWidth);
            g2.drawLine(x, 0, x, height);
        }

        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        for (int i = 0; i < 7; i++) {
            int x = TIME_COL_WIDTH + (i * colWidth);
            LocalDate day = startOfWeek.plusDays(i);
            
            g2.setColor(Theme.BACKGROUND); 
            g2.fillRect(x, 0, colWidth, HEADER_HEIGHT);
            g2.setColor(Theme.BORDER);
            g2.drawRect(x, 0, colWidth, HEADER_HEIGHT);

            String dayText = dayNames[i] + " (" + day.format(DATE_FMT) + ")";
            if (day.equals(LocalDate.now())) {
                g2.setColor(Theme.PRIMARY); 
                g2.setFont(Theme.FONT_BOLD_16);
            } else {
                g2.setColor(Theme.TEXT_MAIN);
                g2.setFont(Theme.FONT_BOLD_16);
            }
            int textX = x + (colWidth - fm.stringWidth(dayText)) / 2;
            int textY = (HEADER_HEIGHT + fm.getAscent()) / 2 - 2;
            g2.drawString(dayText, textX, textY);
        }

        for (int i = 0; i < 7; i++) {
            if (!dayBlocks.containsKey(i)) continue;
            int dayX = TIME_COL_WIDTH + (i * colWidth);
            
            for (RenderBlock b : dayBlocks.get(i)) {
                int blockWidth = (colWidth - 4) / b.totalCols;
                int x = dayX + 2 + (b.colIndex * blockWidth);
                
                double pixelsPerMin = (double) ROW_HEIGHT / 60.0;
                int y = HEADER_HEIGHT + (int)(b.startMin * pixelsPerMin);
                int h = (int)((b.endMin - b.startMin) * pixelsPerMin);
                h = Math.max(h, 25); 

                b.bounds = new Rectangle(x, y, blockWidth - 1, h);

                Color color = getHashColor(b.task.getTitle());
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(x, y, blockWidth - 1, h, 8, 8));
                g2.setColor(color.darker());
                g2.fill(new RoundRectangle2D.Float(x, y, 5, h, 8, 8)); 
                g2.fillRect(x+3, y, 3, h);

                g2.setColor(getContrastColor(color));
                g2.setFont(Theme.FONT_BOLD_16.deriveFont(11f)); 
                
                Shape clip = g2.getClip();
                g2.clipRect(x + 8, y, blockWidth - 10, h);
                
                g2.drawString(b.task.getTitle(), x + 8, y + 14);
                if (h > 30) {
                    String timeStr = String.format("%02d:%02d", b.startMin/60, b.startMin%60);
                    g2.setFont(Theme.FONT_REGULAR_12.deriveFont(10f));
                    g2.drawString(timeStr, x + 8, y + 26);
                }
                g2.setClip(clip);
            }
        }
    }

    private Color getHashColor(String text) {
        int hash = text.hashCode();
        int r = ((hash & 0xFF0000) >> 16) % 127 + 128;
        int g = ((hash & 0x00FF00) >> 8) % 127 + 128;
        int b = (hash & 0x0000FF) % 127 + 128;
        return new Color(r, g, b);
    }
    
    private Color getContrastColor(Color bg) {
        double y = (299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue()) / 1000;
        return y >= 128 ? Color.DARK_GRAY : Color.WHITE;
    }

    private static class RenderBlock {
        Task task;
        int startMin, endMin;
        int colIndex = 0, totalCols = 1; 
        Rectangle bounds;
        public RenderBlock(Task t, int s, int e) {
            this.task = t; this.startMin = s; this.endMin = e;
        }
    }
}