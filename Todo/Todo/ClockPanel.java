package Todo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 하루 일정을 원형 시계(Pie Chart) 형태로 그려주는 UI 컴포넌트
 * (기존 DailyScheduleDialog 내부 클래스에서 분리됨)
 */
public class ClockPanel extends JPanel {
    
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private List<Task> tasks = new ArrayList<>();
    private List<int[]> freeSlots = new ArrayList<>(); // 빈 시간 배경용

    private class RenderArc {
        Task task;
        double startAngle, extentAngle;
        public RenderArc(Task t, int sMin, int eMin) {
            this.task = t;
            // 시계 방향 12시(90도) 기준 각도 계산
            this.startAngle = 90 - ((double)sMin / 1440.0 * 360.0);
            double duration = eMin - sMin;
            this.extentAngle = -((duration / 1440.0) * 360.0);
        }
    }
    private List<RenderArc> renderArcs = new ArrayList<>();

    public ClockPanel() {
        setBackground(Theme.CARD_BG);
    }

    // ★ 외부에서 데이터를 주입받는 메서드 (Setter)
    public void setTasks(List<Task> tasks, List<int[]> freeSlots) {
        this.tasks = tasks;
        this.freeSlots = freeSlots;
        calculateLayout();
        repaint(); // 다시 그리기 요청
    }

    private void calculateLayout() {
        renderArcs.clear();
        if (tasks == null || tasks.isEmpty()) return;
        
        for (Task t : tasks) {
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

        for (RenderArc arc : renderArcs) {
            Color color = getHashColor(arc.task.getTitle());
            Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 180);
            
            g2.setColor(transparentColor);
            g2.fill(new Arc2D.Double(
                    centerX - maxRadius, centerY - maxRadius, 
                    maxRadius * 2, maxRadius * 2, 
                    arc.startAngle, arc.extentAngle, Arc2D.PIE));
            
            drawText(g2, arc.task.getTitle(), arc.startAngle + arc.extentAngle/2, centerX, centerY, maxRadius * 0.7);
        }
        drawCurrentTimeHand(g2, centerX, centerY, maxRadius);
    }

    private void drawFreeTimeArcs(Graphics2D g2, int cx, int cy, int radius) {
        if(freeSlots == null) return;
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
        g2.setColor(new Color(231, 76, 60)); 
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