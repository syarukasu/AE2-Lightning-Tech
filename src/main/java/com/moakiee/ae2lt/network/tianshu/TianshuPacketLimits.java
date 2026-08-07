package com.moakiee.ae2lt.network.tianshu;

import io.netty.handler.codec.DecoderException;

public final class TianshuPacketLimits {
    public static final int MAX_LIST_ENTRIES = 2048;

    private TianshuPacketLimits() {
    }

    public static int requireListSize(String field, int size) {
        if (size < 0 || size > MAX_LIST_ENTRIES) {
            throw new IllegalArgumentException(
                    "Invalid " + field + " count " + size + " (maximum " + MAX_LIST_ENTRIES + ")");
        }
        return size;
    }

    public static int requireDecodedListSize(String field, int size) {
        if (size < 0 || size > MAX_LIST_ENTRIES) {
            throw new DecoderException(
                    "Invalid " + field + " count " + size + " (maximum " + MAX_LIST_ENTRIES + ")");
        }
        return size;
    }
}
