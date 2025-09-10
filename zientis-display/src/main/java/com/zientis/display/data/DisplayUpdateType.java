package com.zientis.display.data;

/**
 * Represents the type of update to be performed on an island display.
 */
public enum DisplayUpdateType {
    /**
     * A complete rebuild of the display, re-scanning the original island.
     */
    FULL_REBUILD,

    /**
     * An incremental update, usually for minor block changes.
     */
    INCREMENTAL,

    /**
     * An update affecting only the hologram.
     */
    HOLOGRAM_ONLY,

    /**
     * An update affecting only the particle effects.
     */
    PARTICLE_ONLY,

    /**
     * A special update for when an island's display tier is upgraded.
     */
    TIER_UPGRADE,

    /**
     * An update to the physical position of the display model.
     */
    POSITION_UPDATE,

    /**
     * Forces a refresh of the display, often re-rendering from cached data.
     */
    FORCE_REFRESH;
}
