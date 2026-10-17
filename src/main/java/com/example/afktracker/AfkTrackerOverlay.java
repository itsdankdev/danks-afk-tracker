package com.example.afktracker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Visual-only replacement for the V3 overlay. Timer logic stays in AfkTrackerPlugin. */
public class AfkTrackerOverlay extends OverlayPanel
{
    private static final int W = 248;
    private static final int H = 228;
    private static final Color BACK = new Color(13, 20, 19, 128);
    private static final Color CARD = new Color(27, 40, 35, 166);
    private static final Color LIME = new Color(164, 246, 107);
    private static final Color WHITE = new Color(241, 246, 240);
    private static final Color MUTED = new Color(161, 176, 166);
    private static final Color BORDER = new Color(55, 80, 65);
    private final AfkTrackerPlugin plugin;
    private final AfkTrackerConfig config;

    @Inject
    public AfkTrackerOverlay(AfkTrackerPlugin plugin, AfkTrackerConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setMovable(true);
        panelComponent.setPreferredSize(new Dimension(W, H));
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (!config.showOverlay()) return null;
        Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try
        {
            g.setColor(BACK);
            g.fillRoundRect(0, 0, W, H, 12, 12);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(0, 0, W - 1, H - 1, 12, 12);

            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            draw(g, "DANK'S AFK TRACKER", 12, 21, LIME);
            boolean logged = plugin.isSessionActive();
            boolean afk = logged && plugin.isIdle();
            String status = !logged ? "OFFLINE" : afk ? "AFK" : "ACTIVE";
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            int badgeWidth = g.getFontMetrics().stringWidth(status) + 14;
            int badgeX = W - badgeWidth - 10;
            g.setColor(new Color(42, 68, 45, 185));
            g.fillRoundRect(badgeX, 9, badgeWidth, 18, 9, 9);
            draw(g, status, badgeX + 7, 22, LIME);

            g.setColor(BORDER);
            g.drawLine(12, 34, W - 12, 34);
            center(g, "CURRENT AFK", 54, MUTED, new Font("SansSerif", Font.PLAIN, 10));
            center(g, format(plugin.getCurrentAfkMs()), 91, LIME, new Font("SansSerif", Font.BOLD, 30));
            center(g, "Starts after " + config.idleDelayMinutes() + " min without a click", 109,
                MUTED, new Font("SansSerif", Font.PLAIN, 10));

            tile(g, 10, 122, "SESSION", format(plugin.getSessionMs()));
            tile(g, 126, 122, "ACTIVE", format(plugin.getActiveMs()));
            tile(g, 10, 168, "TOTAL AFK", format(plugin.getTotalAfkMs()));
            tile(g, 126, 168, "LONGEST AFK", format(plugin.getLongestAfkMs()));
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            draw(g, "No click", 12, 221, MUTED);
            right(g, format(plugin.getSinceClickMs()), W - 12, 221, WHITE);
            return new Dimension(W, H);
        }
        finally
        {
            g.setFont(oldFont);
            g.setColor(oldColor);
            if (oldAA != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

    private void tile(Graphics2D g, int x, int y, String title, String value)
    {
        g.setColor(CARD);
        g.fillRoundRect(x, y, 112, 41, 8, 8);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        draw(g, title, x + 9, y + 15, MUTED);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        draw(g, value, x + 9, y + 33, WHITE);
    }

    private static void draw(Graphics2D g, String s, int x, int baseline, Color c)
    {
        g.setColor(c);
        g.drawString(s, x, baseline);
    }
    private static void center(Graphics2D g, String s, int baseline, Color c, Font f)
    {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        draw(g, s, (W - fm.stringWidth(s)) / 2, baseline, c);
    }
    private static void right(Graphics2D g, String s, int right, int baseline, Color c)
    {
        draw(g, s, right - g.getFontMetrics().stringWidth(s), baseline, c);
    }
    private static String format(long ms)
    {
        long seconds = Math.max(0, ms) / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds / 60) % 60;
        long remainder = seconds % 60;
        return hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, remainder)
            : String.format("%d:%02d", seconds / 60, remainder);
    }
}
