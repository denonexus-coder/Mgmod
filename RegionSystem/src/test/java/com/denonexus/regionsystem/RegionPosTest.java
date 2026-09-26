package com.denonexus.regionsystem;

import com.denonexus.regionsystem.region.RegionPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class RegionPosTest {

    @Test
    void regionCoordinates() {

        assertEquals(
                new RegionPos(0, 0),
                RegionPos.fromChunk(0, 0)
        );

        assertEquals(
                new RegionPos(0, 0),
                RegionPos.fromChunk(3, 3)
        );

        assertEquals(
                new RegionPos(1, 0),
                RegionPos.fromChunk(4, 0)
        );

        assertEquals(
                new RegionPos(-1, 0),
                RegionPos.fromChunk(-1, 0)
        );

        assertEquals(
                new RegionPos(-1, -1),
                RegionPos.fromChunk(-4, -4)
        );
    }
}
