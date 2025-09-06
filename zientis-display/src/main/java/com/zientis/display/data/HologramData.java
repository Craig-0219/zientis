package com.zientis.display.data;

import org.bukkit.Location;
import java.util.List;

/**
 * Holds the data required to display an information hologram.
 */
public class HologramData {

    private final Location location;
    private final List<String> lines;

    public HologramData(Location location, List<String> lines) {
        this.location = location;
        this.lines = lines;
    }

    public Location getLocation() {
        return location;
    }

    public List<String> getLines() {
        return lines;
    }

    public boolean isEmpty() {
        return lines == null || lines.isEmpty();
    }
}
