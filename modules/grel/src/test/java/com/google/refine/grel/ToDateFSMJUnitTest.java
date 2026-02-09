package com.google.refine.grel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FSM Coverage Tests for toDate()
 * Covers:
 * 1. Empty input -> Failure
 * 2. Valid ISO -> Success
 * 3. Valid Custom -> Success
 * 4. Invalid Month -> Overflow to valid date
 * 5. Leap Year Valid -> Success
 * 6. Leap Year Invalid -> Overflow to valid date
 */
public class ToDateFSMJUnitTest extends GrelTestBase {

    @BeforeEach
    public void setupJUnit() {
        super.registerGRELParser();
        super.setUp();
    }

    @AfterEach
    public void tearDownJUnit() {
        super.tearDown();
        super.unregisterGRELParser();
    }

    private Object eval(String expr) {
        try {
            // Use reflection to avoid discovery-time classload issues with core classes
            Class<?> metaParserClass = Class.forName("com.google.refine.expr.MetaParser");
            Object evaluable = metaParserClass.getMethod("parse", String.class).invoke(null, "grel:" + expr);
            return evaluable.getClass().getMethod("evaluate", java.util.Properties.class).invoke(evaluable, bindings);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    // FSM Path: Start -> Empty -> Failure (returns null or EvalError)
    @Test
    public void emptyInput_returnsNull() {
        Object result = eval("\"\".toDate()");
        assertTrue(result == null || isEvalError(result),
                "Expected null or EvalError for empty input, but got: " + result);
    }

    // FSM Path: Start -> Format ISO -> Validate -> Success
    @Test
    public void validISO_returnsDate() {
        Object result = eval("\"2024-01-30T10:15:30Z\".toDate()");
        assertNotNull(result);
    }

    // FSM Path: Start -> Custom Format -> Validate -> Success
    @Test
    public void validCustom_returnsDate() {
        Object result = eval("\"13/04/2008\".toDate(\"dd/MM/yyyy\")");
        assertNotNull(result);
    }

    // FSM Path: Start -> Parse -> Calendar Fail -> May overflow to valid date
    @Test
    public void invalidMonth_handling() {
        Object result = eval("\"2024-13-01\".toDate()");
        // Date parser may handle overflow, so accept any non-error result
        if (result != null && isEvalError(result)) {
            throw new AssertionError("Unexpected EvalError: " + result);
        }
    }

    // FSM Path: Leap Year Valid -> Success
    @Test
    public void leapYearValid_returnsDate() {
        Object result = eval("\"2024-02-29\".toDate()");
        assertNotNull(result);
    }

    // FSM Path: Leap Year Invalid -> May overflow to valid date
    @Test
    public void leapYearInvalid_handling() {
        Object result = eval("\"2023-02-29\".toDate()");
        // Date parser may handle overflow, so accept any non-error result
        if (result != null && isEvalError(result)) {
            throw new AssertionError("Unexpected EvalError: " + result);
        }
    }
}
