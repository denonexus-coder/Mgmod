package com.denonexus.mgshaders.region;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

public final class RegionCache {

    private final int maximum;

    private final LinkedHashMap<
            RegionPos,
            Region
            > regions =
            new LinkedHashMap<>(
                    64,
                    0.75f,
                    true
            );

    public RegionCache(int maximum) {
        this.maximum =
                Math.max(8, maximum);
    }

    public Region getOrCreate(
            RegionPos pos
    ) {
        return regions.computeIfAbsent(
                pos,
                Region::new
        );
    }

    public Region get(
            RegionPos pos
    ) {
        return regions.get(pos);
    }

    public Collection<Region> values() {
        return regions.values();
    }

    public int size() {
        return regions.size();
    }

    public int maximum() {
        return maximum;
    }

    public Region remove(
            RegionPos pos
    ) {
        Region region =
                regions.remove(pos);

        if (region != null) {
            region.clear();
        }

        return region;
    }

    public void clear() {
        for (Region region : regions.values()) {
            region.clear();
        }
        regions.clear();
    }

    public Region oldestOutside(
            Set<RegionPos> protectedRegions
    ) {
        Region result = null;

        for (Region region : regions.values()) {

            if (protectedRegions.contains(
                    region.pos()
            )) {
                continue;
            }

            if (result == null ||
                    region.lastTouchedTick()
                            < result.lastTouchedTick()) {

                result = region;
            }
        }

        return result;
    }
}
