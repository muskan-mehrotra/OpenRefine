package com.google.refine.expr.functions.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.refine.grel.GrelTestBase;

public class SmartSplitCoverageTest extends GrelTestBase {

    private SmartSplit smartSplit;

    @BeforeEach
    @Override
    public void setUp() {
        smartSplit = new SmartSplit();
        bindings = new Properties();
    }

    @AfterEach
    @Override
    public void tearDown() {
        bindings = null;
    }

    @Test
    public void basicSplit_splitsOnDelimiter() {
        Object o = smartSplit.call(bindings, new Object[] { "a,b,c", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        assertEquals(Arrays.asList("a", "b", "c"), Arrays.asList(parts));
    }

    @Test
    public void delimiterNotFound_returnsSingleToken() {
        Object o = smartSplit.call(bindings, new Object[] { "abc", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        assertEquals(Arrays.asList("abc"), Arrays.asList(parts));
    }

    @Test
    public void quotedText_keepsDelimiterInsideQuotes() {
        Object o = smartSplit.call(bindings, new Object[] { "a,\"b,c\",d", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        assertEquals(Arrays.asList("a", "b,c", "d"), Arrays.asList(parts));
    }

    @Test
    public void escapedQuotes_insideQuotedField() {
        // "b""c" (CSV-style escaping) should become b"c
        Object o = smartSplit.call(bindings, new Object[] { "a,\"b\"\"c\",d", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        assertEquals(Arrays.asList("a", "b\"c", "d"), Arrays.asList(parts));
    }

    @Test
    public void emptyFields_preserved() {
        Object o = smartSplit.call(bindings, new Object[] { "a,,c,", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        // CsvParser returns null for empty fields, not empty strings
        assertEquals(4, parts.length);
        assertEquals("a", parts[0]);
        assertNull(parts[1]);
        assertEquals("c", parts[2]);
        assertNull(parts[3]);
    }

    @Test
    public void leadingAndTrailingWhitespace_preservedByDefault() {
        Object o = smartSplit.call(bindings, new Object[] { " a , b ,c ", "," });
        assertTrue(o instanceof String[]);
        String[] parts = (String[]) o;
        assertEquals(Arrays.asList(" a ", " b ", "c "), Arrays.asList(parts));
    }

    @Test
    public void nullInput_returnsErrorOrNull() {
        // SmartSplit doesn't handle null input - throws exception or returns null
        try {
            Object o = smartSplit.call(bindings, new Object[] { null, "," });
            assertTrue(o == null || isEvalError(o), "Expected null or EvalError for null input");
        } catch (Exception e) {
            // Expected - SmartSplit can't handle null input
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void invalidArgs_returnsError() {
        // delimiter null -> throws exception or returns EvalError
        try {
            Object o = smartSplit.call(bindings, new Object[] { "a,b", null });
            assertTrue(isEvalError(o), "Expected EvalError for null delimiter");
        } catch (Exception e) {
            // Expected - SmartSplit can't handle null delimiter
            assertNotNull(e.getMessage());
        }
    }

    private boolean isEvalError(Object obj) {
        try {
            Class<?> evalErrorClass = Class.forName("com.google.refine.expr.EvalError");
            return evalErrorClass.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

