package com.moakiee.ae2lt.logic;

import com.moakiee.ae2lt.network.tianshu.TianshuPacketLimits;
import io.netty.handler.codec.DecoderException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NetworkIdentityPrimitivesTest {
    @Test
    void targetPatternKeyUsesTargetEqualityAndPatternIdentity() {
        TargetPatternKey<String> first = new TargetPatternKey<>(new String("target"), null);
        TargetPatternKey<String> second = new TargetPatternKey<>(new String("target"), null);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals("target", first.target());
        assertNull(first.pattern());
    }

    @Test
    void targetPatternKeyRejectsNullTarget() {
        assertThrows(NullPointerException.class, () -> new TargetPatternKey<>(null, null));
    }

    @Test
    void weakIdentityMapsCreateUsableMaps() {
        Map<Object, String> weakKeys = WeakIdentityMaps.weakKeys();
        Object key = new Object();
        weakKeys.put(key, "value");
        assertEquals("value", weakKeys.get(key));

        Map<Object, Object> weakBoth = WeakIdentityMaps.weakKeysAndValues();
        Object value = new Object();
        weakBoth.put(key, value);
        assertSame(value, weakBoth.get(key));
    }

    @Test
    void tianshuPacketLimitAcceptsBoundaryAndRejectsOversize() {
        assertEquals(0, TianshuPacketLimits.requireListSize("rules", 0));
        assertEquals(2048, TianshuPacketLimits.requireListSize("rules", 2048));
        assertThrows(IllegalArgumentException.class,
                () -> TianshuPacketLimits.requireListSize("rules", -1));
        assertThrows(IllegalArgumentException.class,
                () -> TianshuPacketLimits.requireListSize("rules", 2049));
    }

    @Test
    void decodedPacketLimitUsesDecoderException() {
        assertEquals(2048, TianshuPacketLimits.requireDecodedListSize("patterns", 2048));
        assertThrows(DecoderException.class,
                () -> TianshuPacketLimits.requireDecodedListSize("patterns", 2049));
    }
}
