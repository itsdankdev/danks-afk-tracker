package com.example.afktracker;

import com.google.inject.Provides;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name = "Dank's AFK Tracker", description = "Session-based click inactivity tracker")
public class AfkTrackerPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private OverlayManager overlayManager;
    @Inject private AfkTrackerOverlay overlay;
    @Inject private AfkTrackerConfig config;

    private long sessionStart;
    private long lastClick;
    private long lastSample;
    private long totalAfk;
    private long longestAfk;
    private boolean sessionActive;

    private final MouseAdapter clickListener = new MouseAdapter()
    {
        @Override public void mousePressed(MouseEvent e) { recordInteraction(); }
    };
    private final MouseMotionAdapter motionListener = new MouseMotionAdapter()
    {
        @Override public void mouseMoved(MouseEvent e) { if (config.trackMovement()) recordInteraction(); }
        @Override public void mouseDragged(MouseEvent e) { if (config.trackMovement()) recordInteraction(); }
    };

    @Provides AfkTrackerConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(AfkTrackerConfig.class);
    }

    @Override protected void startUp()
    {
        synchronized (this)
        {
            resetSession();
            if (client.getGameState() == GameState.LOGGED_IN) beginSession();
        }
        client.getCanvas().addMouseListener(clickListener);
        client.getCanvas().addMouseMotionListener(motionListener);
        overlayManager.add(overlay);
    }

    @Override protected void shutDown()
    {
        client.getCanvas().removeMouseListener(clickListener);
        client.getCanvas().removeMouseMotionListener(motionListener);
        overlayManager.remove(overlay);
        synchronized (this) { resetSession(); }
    }

    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        synchronized (this)
        {
            if (event.getGameState() == GameState.LOGGED_IN)
            {
                if (!sessionActive) beginSession();
            }
            else if (event.getGameState() == GameState.LOGIN_SCREEN
                || event.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR
                || event.getGameState() == GameState.CONNECTION_LOST)
            {
                resetSession();
            }
            // LOADING/HOPPING are transitional states; don't reset for them.
        }
    }

    private void beginSession()
    {
        resetSession();
        long now = System.nanoTime();
        sessionStart = lastClick = lastSample = now;
        sessionActive = true;
    }

    private void resetSession()
    {
        sessionStart = lastClick = lastSample = totalAfk = longestAfk = 0L;
        sessionActive = false;
    }

    private synchronized void recordInteraction()
    {
        if (!sessionActive) return;
        long now = System.nanoTime();
        sample(now);
        longestAfk = Math.max(longestAfk, Math.max(0L, now - lastClick - delayNanos()));
        lastClick = now;
    }

    private void sample(long now)
    {
        if (!sessionActive) return;
        long afkStart = Math.max(lastSample, lastClick + delayNanos());
        totalAfk += Math.max(0L, now - afkStart);
        lastSample = now;
    }

    private long delayNanos() { return config.idleDelayMinutes() * 60_000_000_000L; }
    private long elapsed(long now) { return sessionActive ? Math.max(0L, now - sessionStart) : 0L; }
    private long currentAfk(long now) { return sessionActive ? Math.max(0L, now - lastClick - delayNanos()) : 0L; }
    private long totalAfk(long now)
    {
        if (!sessionActive) return 0L;
        return totalAfk + Math.max(0L, now - Math.max(lastSample, lastClick + delayNanos()));
    }
    private static long ms(long nanos) { return nanos / 1_000_000L; }

    public synchronized boolean isSessionActive() { return sessionActive; }
    public synchronized boolean isIdle() { return sessionActive && currentAfk(System.nanoTime()) > 0L; }
    public synchronized long getSinceClickMs() { return sessionActive ? ms(Math.max(0L, System.nanoTime() - lastClick)) : 0L; }
    public synchronized long getCurrentAfkMs() { return ms(currentAfk(System.nanoTime())); }
    public synchronized long getTotalAfkMs() { return ms(totalAfk(System.nanoTime())); }
    public synchronized long getLongestAfkMs() { return ms(Math.max(longestAfk, currentAfk(System.nanoTime()))); }
    public synchronized long getSessionMs() { return ms(elapsed(System.nanoTime())); }
    public synchronized long getActiveMs()
    {
        long now = System.nanoTime();
        return ms(Math.max(0L, elapsed(now) - totalAfk(now)));
    }
}
