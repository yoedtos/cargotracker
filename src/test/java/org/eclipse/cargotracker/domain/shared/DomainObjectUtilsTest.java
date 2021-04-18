package org.eclipse.cargotracker.domain.shared;

import org.junit.Test;

import static org.junit.Assert.*;

public class DomainObjectUtilsTest {

    @Test
    public void testNullSafe() {
        String nullObject = null;
        String noneNullObject = new String("noneNull");

        var safeObject = DomainObjectUtils.nullSafe(nullObject, "safe");
        assertEquals("safe", safeObject);

        var safeObject2 = DomainObjectUtils.nullSafe(noneNullObject, "safe");
        assertEquals(noneNullObject, safeObject2);
    }
}
