package com.moakiee.ae2lt.client.ctm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CtmTileSelectorTest {
    @Test
    void quadrantMappingMatchesTextureQuadrants() {
        assertEquals(CtmTileSelector.Quadrant.TOP_LEFT, CtmTileSelector.quadrant(0, 0));
        assertEquals(CtmTileSelector.Quadrant.TOP_RIGHT, CtmTileSelector.quadrant(1, 0));
        assertEquals(CtmTileSelector.Quadrant.BOTTOM_LEFT, CtmTileSelector.quadrant(0, 1));
        assertEquals(CtmTileSelector.Quadrant.BOTTOM_RIGHT, CtmTileSelector.quadrant(1, 1));
    }

    @Test
    void disconnectedTopLeftUsesBaseTile() {
        CtmTileSelector.Tile tile = CtmTileSelector.select(CtmTileSelector.Quadrant.TOP_LEFT, 0, 0);
        assertEquals(CtmTileSelector.Source.BASE, tile.source());
        assertEquals(0, tile.x());
        assertEquals(0, tile.y());
    }

    @Test
    void connectedTopLeftWithDiagonalUsesInteriorCtmTile() {
        int topAndLeftEdges = (1 << 0) | (1 << 3);
        int topLeftCorner = 1 << CtmTileSelector.Quadrant.TOP_LEFT.ordinal();
        CtmTileSelector.Tile tile = CtmTileSelector.select(
                CtmTileSelector.Quadrant.TOP_LEFT,
                topAndLeftEdges,
                topLeftCorner);

        assertEquals(CtmTileSelector.Source.CTM, tile.source());
        assertEquals(0, tile.x());
        assertEquals(0, tile.y());
    }

    @Test
    void connectedTopLeftWithoutDiagonalUsesMissingCornerTile() {
        int topAndLeftEdges = (1 << 0) | (1 << 3);
        CtmTileSelector.Tile tile = CtmTileSelector.select(
                CtmTileSelector.Quadrant.TOP_LEFT,
                topAndLeftEdges,
                0);

        assertEquals(CtmTileSelector.Source.CTM, tile.source());
        assertEquals(2, tile.x());
        assertEquals(2, tile.y());
    }
}
