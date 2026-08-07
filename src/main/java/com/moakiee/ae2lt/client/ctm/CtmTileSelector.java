package com.moakiee.ae2lt.client.ctm;

final class CtmTileSelector {
    private CtmTileSelector() {
    }

    static Quadrant quadrant(int sq, int tq) {
        if (tq == 0) {
            return sq == 0 ? Quadrant.TOP_LEFT : Quadrant.TOP_RIGHT;
        }
        return sq == 0 ? Quadrant.BOTTOM_LEFT : Quadrant.BOTTOM_RIGHT;
    }

    static Tile select(Quadrant quadrant, int edgeMask, int cornerMask) {
        return switch (quadrant) {
            case TOP_LEFT -> selectCorner(edgeMask, cornerMask, 0, 3, Quadrant.TOP_LEFT, 0, 0, 0, 0, 2, 0, 0, 2, 2, 2);
            case TOP_RIGHT -> selectCorner(edgeMask, cornerMask, 0, 1, Quadrant.TOP_RIGHT, 1, 0, 1, 0, 3, 0, 1, 2, 3, 2);
            case BOTTOM_LEFT -> selectCorner(edgeMask, cornerMask, 2, 3, Quadrant.BOTTOM_LEFT, 0, 1, 0, 1, 2, 1, 0, 3, 2, 3);
            case BOTTOM_RIGHT -> selectCorner(edgeMask, cornerMask, 2, 1, Quadrant.BOTTOM_RIGHT, 1, 1, 1, 1, 3, 1, 1, 3, 3, 3);
        };
    }

    private static Tile selectCorner(
            int edgeMask,
            int cornerMask,
            int firstEdge,
            int secondEdge,
            Quadrant diagonal,
            int baseX,
            int baseY,
            int interiorX,
            int interiorY,
            int firstOnlyX,
            int firstOnlyY,
            int secondOnlyX,
            int secondOnlyY,
            int missingDiagonalX,
            int missingDiagonalY) {
        boolean firstConnected = has(edgeMask, firstEdge);
        boolean secondConnected = has(edgeMask, secondEdge);
        if (firstConnected && secondConnected) {
            return has(cornerMask, diagonal.ordinal())
                    ? ctm(interiorX, interiorY)
                    : ctm(missingDiagonalX, missingDiagonalY);
        }
        if (firstConnected) {
            return ctm(firstOnlyX, firstOnlyY);
        }
        if (secondConnected) {
            return ctm(secondOnlyX, secondOnlyY);
        }
        return new Tile(Source.BASE, baseX, baseY);
    }

    private static Tile ctm(int x, int y) {
        return new Tile(Source.CTM, x, y);
    }

    private static boolean has(int mask, int bit) {
        return (mask & 1 << bit) != 0;
    }

    enum Quadrant {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    record Tile(Source source, int x, int y) {
    }

    enum Source {
        BASE(2),
        CTM(4);

        private final int gridSize;

        Source(int gridSize) {
            this.gridSize = gridSize;
        }

        int gridSize() {
            return this.gridSize;
        }
    }
}
