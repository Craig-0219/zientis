package com.zientis.display.data;

/**
 * Represents the current status of a display model.
 */
public enum DisplayStatus {
    /**
     * The display is currently active and visible.
     */
    ACTIVE,
    /**
     * The display is being created or updated.
     */
    UPDATING,
    /**
     * The display is loaded but not currently rendered or visible.
     */
    INACTIVE,
    /**
     * An error occurred with this display.
     */
    ERROR,
    /**
     * The display creation is pending.
     */
    PENDING,
    /**
     * The display is paused and not updating.
     */
    PAUSED,
    /**
     * The display is intentionally hidden.
     */
    HIDDEN,
    /**
     * The display is in the process of being created.
     */
    CREATING;
}
