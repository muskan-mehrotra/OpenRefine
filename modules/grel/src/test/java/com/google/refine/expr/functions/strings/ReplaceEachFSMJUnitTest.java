package com.google.refine.expr.functions.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.refine.grel.GrelTestBase;

/**
 * FSM Coverage Tests for replaceEach()
 * 
 * FSM States:
 *   S0 - Start
 *   S1 - Arg Count Check (== 3?)
 *   S2 - Type Validation (String, Array, Array/String?)
 *   S3 - Convert to String Arrays
 *   S4 - Length Check (search.length >= replace.length?)
 *   S5 - Equality Check (lengths equal?)
 *   S6 - Pad Replace Array (fill with last element)
 *   S7 - Execute replaceEachRepeatedly
 *   SS - Success (return modified string)
 *   SF - Failure (return EvalError)
 * 
 * Test Coverage:
 *   1. Valid equal arrays         -> S0->S1->S2->S3->S4->S5->S7->SS
 *   2. Single replacement string  -> S0->S1->S2->S3->S4->S5->S6->S7->SS
 *   3. Replace shorter (padding)  -> S0->S1->S2->S3->S4->S5->S6->S7->SS
 *   4. Replace longer than search -> S0->S1->S2->S3->S4->SF
 *   5. Wrong arg count (too few)  -> S0->S1->SF
 *   6. Wrong arg count (too many) -> S0->S1->SF
 *   7. Wrong type (non-string)    -> S0->S1->S2->SF
 *   8. Wrong type (non-array)     -> S0->S1->S2->SF
 *   9. Empty arrays               -> S0->S1->S2->S3->S4->S5->S7->SS
 *  10. No matches found           -> S0->S1->S2->S3->S4->S5->S7->SS
 *  11. Chained replacement        -> S0->S1->S2->S3->S4->S5->S7->SS
 */
public class ReplaceEachFSMJUnitTest extends GrelTestBase {

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

    // =========================================================================
    // SUCCESS PATHS (SS)
    // =========================================================================

    /**
     * Test 1: Valid equal-length arrays
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S7 -> SS
     * 
     * Input:  "abc", ["a","b"], ["x","y"]
     * Result: "xyc" (a->x, b->y)
     */
    @Test
    public void validEqualArrays_returnsModifiedString() {
        Object result = eval("\"abc\".replaceEach([\"a\",\"b\"], [\"x\",\"y\"])");
        assertNotNull(result, "Result should not be null");
        assertEquals("xyc", result.toString(), "a->x and b->y should produce 'xyc'");
    }

    /**
     * Test 2: Single replacement string (triggers padding)
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S6 -> S7 -> SS
     * 
     * Input:  "abc", ["a","b"], "x"
     * Flow:   replace becomes ["x"], then padded to ["x","x"]
     * Result: "xxc" (both a and b replaced with x)
     */
    @Test
    public void singleReplacementString_paddedAndApplied() {
        Object result = eval("\"abc\".replaceEach([\"a\",\"b\"], \"x\")");
        assertNotNull(result, "Result should not be null");
        assertEquals("xxc", result.toString(), "Single replacement 'x' should replace both a and b");
    }

    /**
     * Test 3: Replace array shorter than search (padding with last element)
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S6 -> S7 -> SS
     * 
     * Input:  "abc", ["a","b","c"], ["x","y"]
     * Flow:   replace padded to ["y","y","y"] (ALL filled with last element)
     * Result: "yyy"
     */
    @Test
    public void replaceShorter_paddedWithLastElement() {
        Object result = eval("\"abc\".replaceEach([\"a\",\"b\",\"c\"], [\"x\",\"y\"])");
        assertNotNull(result, "Result should not be null");
        assertEquals("yyy", result.toString(), "Padding fills ALL positions with last element 'y'");
    }

    /**
     * Test 9: Empty arrays (edge case - no replacements)
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S7 -> SS
     * 
     * Input:  "abc", [], []
     * Result: "abc" (unchanged)
     */
    @Test
    public void emptyArrays_stringUnchanged() {
        Object result = eval("\"abc\".replaceEach([], [])");
        assertNotNull(result, "Result should not be null");
        assertEquals("abc", result.toString(), "Empty arrays should leave string unchanged");
    }

    /**
     * Test 10: No matches found in input string
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S7 -> SS
     * 
     * Input:  "abc", ["x","y"], ["1","2"]
     * Result: "abc" (no matches, unchanged)
     */
    @Test
    public void noMatchesFound_stringUnchanged() {
        Object result = eval("\"abc\".replaceEach([\"x\",\"y\"], [\"1\",\"2\"])");
        assertNotNull(result, "Result should not be null");
        assertEquals("abc", result.toString(), "No matches means string unchanged");
    }

    /**
     * Test 11: Chained replacement (replaceEachRepeatedly behavior)
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S7 -> SS
     * 
     * Input:  "abc", ["a","x"], ["x","y"]
     * Flow:   a->x first, then x->y (chained)
     * Result: "ybc"
     */
    @Test
    public void chainedReplacement_repeatedlyApplied() {
        Object result = eval("\"abc\".replaceEach([\"a\",\"x\"], [\"x\",\"y\"])");
        assertNotNull(result, "Result should not be null");
        assertEquals("ybc", result.toString(), "Chained: a->x then x->y produces 'ybc'");
    }

    // =========================================================================
    // FAILURE PATHS (SF)
    // =========================================================================

    /**
     * Test 4: Replace array longer than search array
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> SF
     * 
     * Input:  "abc", ["a"], ["x","y"]
     * Error:  replace.length (2) > search.length (1)
     */
    @Test
    public void replaceLongerThanSearch_returnsError() {
        Object result = eval("\"abc\".replaceEach([\"a\"], [\"x\",\"y\"])");
        assertTrue(isEvalError(result), 
            "Replace array longer than search should return EvalError, got: " + result);
    }

    /**
     * Test 5: Wrong argument count (too few - only 2 args)
     * FSM Path: S0 -> S1 -> SF
     * 
     * Input:  "abc", ["a"] (missing replace array)
     */
    @Test
    public void tooFewArguments_returnsError() {
        Object result = eval("\"abc\".replaceEach([\"a\"])");
        assertTrue(isEvalError(result), 
            "Too few arguments should return EvalError, got: " + result);
    }

    /**
     * Test 6: Wrong argument count (too many - 4 args)
     * FSM Path: S0 -> S1 -> SF
     * 
     * Input:  "abc", ["a"], ["x"], "extra"
     */
    @Test
    public void tooManyArguments_returnsError() {
        Object result = eval("\"abc\".replaceEach([\"a\"], [\"x\"], \"extra\")");
        assertTrue(isEvalError(result), 
            "Too many arguments should return EvalError, got: " + result);
    }

    /**
     * Test 7: Wrong type - first argument not a string
     * FSM Path: S0 -> S1 -> S2 -> SF
     * 
     * Input:  123, ["a"], ["x"]
     */
    @Test
    public void nonStringInput_returnsError() {
        // Using invoke directly since we can't call .replaceEach() on a number in GREL syntax
        Object result = invoke("replaceEach", 123, new String[]{"a"}, new String[]{"x"});
        assertTrue(isEvalError(result), 
            "Non-string input should return EvalError, got: " + result);
    }

    /**
     * Test 8: Wrong type - second argument not an array
     * FSM Path: S0 -> S1 -> S2 -> SF
     * 
     * Input:  "abc", "a", ["x"]
     */
    @Test
    public void nonArraySearch_returnsError() {
        Object result = invoke("replaceEach", "abc", "a", new String[]{"x"});
        assertTrue(isEvalError(result), 
            "Non-array search should return EvalError, got: " + result);
    }

    // =========================================================================
    // ADDITIONAL COVERAGE TESTS
    // =========================================================================

    /**
     * Additional: Multiple replacements with equal arrays
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S7 -> SS
     */
    @Test
    public void multipleReplacements_allApplied() {
        Object result = eval("\"The quick brown fox\".replaceEach([\"quick\",\"brown\",\"fox\"], [\"slow\",\"red\",\"dog\"])");
        assertNotNull(result, "Result should not be null");
        assertEquals("The slow red dog", result.toString());
    }

    /**
     * Additional: Vowel replacement with single replacement
     * FSM Path: S0 -> S1 -> S2 -> S3 -> S4 -> S5 -> S6 -> S7 -> SS
     */
    @Test
    public void vowelReplacement_singleChar() {
        Object result = eval("\"hello world\".replaceEach([\"a\",\"e\",\"i\",\"o\",\"u\"], \"*\")");
        assertNotNull(result, "Result should not be null");
        assertEquals("h*ll* w*rld", result.toString());
    }
}
