package vn.edu.tdtu.lhqc.meowsic.managers;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for handling refresh notifications across the app
 */
public final class RefreshManager {
    
    public interface RefreshListener {
        void onDataChanged();
    }
    
    private static final List<RefreshListener> listeners = new ArrayList<>();
    
    private RefreshManager() {}
    
    /**
     * Add a refresh listener
     */
    public static void addListener(RefreshListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a refresh listener
     */
    public static void removeListener(RefreshListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Notify all listeners that data has changed
     */
    public static void notifyDataChanged() {
        for (RefreshListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onDataChanged();
            } catch (Exception e) {
                // Remove dead listeners
                listeners.remove(listener);
            }
        }
    }
    
    /**
     * Clear all listeners (useful for cleanup)
     */
    public static void clearListeners() {
        listeners.clear();
    }
}
