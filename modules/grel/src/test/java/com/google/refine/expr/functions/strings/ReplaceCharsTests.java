/*******************************************************************************
 * Copyright (C) 2026, OpenRefine contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.google.refine.expr.functions.strings;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.GrelTestBase;

public class ReplaceCharsTests extends GrelTestBase {

    @Test
    public void testReplaceChars() {
        assertEquals(invoke("replaceChars", "abc", "ac", "xy"), "xby");
        assertEquals(invoke("replaceChars", "hello", "l", ""), "heo");
    }

    @Test
    public void testReplaceCharsEvalErrors() {
        assertTrue(invoke("replaceChars") instanceof EvalError);
        assertTrue(invoke("replaceChars", "abc") instanceof EvalError);
        assertTrue(invoke("replaceChars", "abc", "a") instanceof EvalError);
        assertTrue(invoke("replaceChars", "abc", "a", null) instanceof EvalError);
    }
}

class IndexOfTests extends GrelTestBase {

    @Test
    public void testIndexOf() {
        assertEquals(invoke("indexOf", "banana", "na"), 2);
        assertEquals(invoke("indexOf", "banana", "zz"), -1);
    }

    @Test
    public void testIndexOfEvalErrors() {
        assertTrue(invoke("indexOf") instanceof EvalError);
        assertTrue(invoke("indexOf", "a") instanceof EvalError);
        assertTrue(invoke("indexOf", "a", 1) instanceof EvalError);
        assertTrue(invoke("indexOf", null, "a") instanceof EvalError);
    }
}

class LastIndexOfTests extends GrelTestBase {

    @Test
    public void testLastIndexOf() {
        assertEquals(invoke("lastIndexOf", "banana", "na"), 4);
        assertEquals(invoke("lastIndexOf", "banana", "zz"), -1);
    }

    @Test
    public void testLastIndexOfEvalErrors() {
        assertTrue(invoke("lastIndexOf") instanceof EvalError);
        assertTrue(invoke("lastIndexOf", "a") instanceof EvalError);
        assertTrue(invoke("lastIndexOf", "a", 1) instanceof EvalError);
        assertTrue(invoke("lastIndexOf", null, "a") instanceof EvalError);
    }
}

class SplitByCharTypeTests extends GrelTestBase {

    @Test
    public void testSplitByCharType() {
        assertArrayEquals((String[]) invoke("splitByCharType", "abc123XYZ"), new String[] { "abc", "123", "XYZ" });
        assertArrayEquals((String[]) invoke("splitByCharType", "ab-CD"), new String[] { "ab", "-", "CD" });
    }

    @Test
    public void testSplitByCharTypeEvalErrors() {
        assertTrue(invoke("splitByCharType") instanceof EvalError);
        assertTrue(invoke("splitByCharType", (Object) null) instanceof EvalError);
    }
}

class SplitByLengthsTests extends GrelTestBase {

    @Test
    public void testSplitByLengths() {
        assertArrayEquals((String[]) invoke("splitByLengths", "abcdef", 2, 2, 2), new String[] { "ab", "cd", "ef" });
        assertArrayEquals((String[]) invoke("splitByLengths", "abc", 2, 5), new String[] { "ab", "c" });
    }

    @Test
    public void testSplitByLengthsEdgeCases() {
        assertArrayEquals((String[]) invoke("splitByLengths", "abcd", 2, -1, 2), new String[] { "ab", "", "cd" });
        assertArrayEquals((String[]) invoke("splitByLengths", "abcd", 2, "x", 2), new String[] { "ab", "", "cd" });
    }

    @Test
    public void testSplitByLengthsEvalErrors() {
        assertTrue(invoke("splitByLengths") instanceof EvalError);
        assertTrue(invoke("splitByLengths", "abc") instanceof EvalError);
    }
}

class UnicodeTests extends GrelTestBase {

    @Test
    public void testUnicode() {
        assertArrayEquals((Integer[]) invoke("unicode", "AZ"), new Integer[] { 65, 90 });
        assertArrayEquals((Integer[]) invoke("unicode", 123), new Integer[] { 49, 50, 51 });
    }

    @Test
    public void testUnicodeNullInputs() {
        assertNull(invoke("unicode"));
        assertNull(invoke("unicode", (Object) null));
    }
}

class UnicodeTypeTests extends GrelTestBase {

    @Test
    public void testUnicodeType() {
        assertArrayEquals((String[]) invoke("unicodeType", "Az1 "),
                new String[] { "uppercase letter", "lowercase letter", "decimal digit number", "space separator" });
        assertArrayEquals((String[]) invoke("unicodeType", "A-"), new String[] { "uppercase letter", "dash punctuation" });
    }

    @Test
    public void testUnicodeTypeNullInputs() {
        assertNull(invoke("unicodeType"));
        assertNull(invoke("unicodeType", (Object) null));
    }
}
