/*******************************************************************************
 * Copyright (C) 2018, OpenRefine contributors
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

package com.google.refine.expr.functions.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.GrelTestBase;

public class IncTests extends GrelTestBase {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSSSSSX");

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

    @Test
    public void testInc() {
        OffsetDateTime source = OffsetDateTime.parse("20180510-23:55:44.000789000Z",
                formatter);

        // add hours
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "hours"));
        assertEquals(invoke("inc", source, 2, "hours"), source.plus(2, ChronoUnit.HOURS));
        assertEquals(invoke("inc", source, 2, "hour"), source.plus(2, ChronoUnit.HOURS));
        assertEquals(invoke("inc", source, 2, "h"), source.plus(2, ChronoUnit.HOURS));

        // add years
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "year"));
        assertEquals(invoke("inc", source, 2, "years"), source.plus(2, ChronoUnit.YEARS));
        assertEquals(invoke("inc", source, 2, "year"), source.plus(2, ChronoUnit.YEARS));

        // add months
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "months"));
        assertEquals(invoke("inc", source, 2, "months"), source.plus(2, ChronoUnit.MONTHS));
        assertEquals(invoke("inc", source, 2, "month"), source.plus(2, ChronoUnit.MONTHS));

        // add minutes
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "minutes"));
        assertEquals(invoke("inc", source, 2, "minutes"), source.plus(2, ChronoUnit.MINUTES));
        assertEquals(invoke("inc", source, 2, "minute"), source.plus(2, ChronoUnit.MINUTES));
        assertEquals(invoke("inc", source, 2, "min"), source.plus(2, ChronoUnit.MINUTES));

        // add weeks
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "weeks"));
        assertEquals(invoke("inc", source, 2, "weeks"), source.plus(2, ChronoUnit.WEEKS));
        assertEquals(invoke("inc", source, 2, "week"), source.plus(2, ChronoUnit.WEEKS));
        assertEquals(invoke("inc", source, 2, "w"), source.plus(2, ChronoUnit.WEEKS));

        // add seconds
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "seconds"));
        assertEquals(invoke("inc", source, 2, "seconds"), source.plus(2, ChronoUnit.SECONDS));
        assertEquals(invoke("inc", source, 2, "sec"), source.plus(2, ChronoUnit.SECONDS));
        assertEquals(invoke("inc", source, 2, "s"), source.plus(2, ChronoUnit.SECONDS));

        // add milliseconds
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "milliseconds"));
        assertEquals(invoke("inc", source, 2, "milliseconds"), source.plus(2, ChronoUnit.MILLIS));
        assertEquals(invoke("inc", source, 2, "ms"), source.plus(2, ChronoUnit.MILLIS));
        assertEquals(invoke("inc", source, 2, "S"), source.plus(2, ChronoUnit.MILLIS));

        // add nanos
        assertInstanceOf(OffsetDateTime.class, invoke("inc", source, 2, "nanos"));
        assertEquals(invoke("inc", source, 2, "nanos"), source.plus(2, ChronoUnit.NANOS));
        assertEquals(invoke("inc", source, 2, "nano"), source.plus(2, ChronoUnit.NANOS));
        assertEquals(invoke("inc", source, 2, "n"), source.plus(2, ChronoUnit.NANOS));

        // exception
        assertInstanceOf(EvalError.class, invoke("inc", source, 99));
        assertInstanceOf(EvalError.class, invoke("inc", source.toInstant().toEpochMilli(), 99, "h"));
    }

    @Test
    public void testIncNegativeValues() {
        OffsetDateTime source = OffsetDateTime.parse("20180510-23:55:44.000789000Z", formatter);

        // subtract hours
        assertEquals(invoke("inc", source, -2, "hours"), source.plus(-2, ChronoUnit.HOURS));
        assertEquals(invoke("inc", source, -2, "hour"), source.plus(-2, ChronoUnit.HOURS));
        assertEquals(invoke("inc", source, -2, "h"), source.plus(-2, ChronoUnit.HOURS));

        // subtract years
        assertEquals(invoke("inc", source, -2, "years"), source.plus(-2, ChronoUnit.YEARS));
        assertEquals(invoke("inc", source, -2, "year"), source.plus(-2, ChronoUnit.YEARS));

        // subtract months
        assertEquals(invoke("inc", source, -2, "months"), source.plus(-2, ChronoUnit.MONTHS));
        assertEquals(invoke("inc", source, -2, "month"), source.plus(-2, ChronoUnit.MONTHS));

        // subtract minutes
        assertEquals(invoke("inc", source, -2, "minutes"), source.plus(-2, ChronoUnit.MINUTES));
        assertEquals(invoke("inc", source, -2, "minute"), source.plus(-2, ChronoUnit.MINUTES));
        assertEquals(invoke("inc", source, -2, "min"), source.plus(-2, ChronoUnit.MINUTES));

        // subtract weeks
        assertEquals(invoke("inc", source, -2, "weeks"), source.plus(-2, ChronoUnit.WEEKS));
        assertEquals(invoke("inc", source, -2, "week"), source.plus(-2, ChronoUnit.WEEKS));
        assertEquals(invoke("inc", source, -2, "w"), source.plus(-2, ChronoUnit.WEEKS));

        // subtract seconds
        assertEquals(invoke("inc", source, -2, "seconds"), source.plus(-2, ChronoUnit.SECONDS));
        assertEquals(invoke("inc", source, -2, "sec"), source.plus(-2, ChronoUnit.SECONDS));
        assertEquals(invoke("inc", source, -2, "s"), source.plus(-2, ChronoUnit.SECONDS));

        // subtract milliseconds
        assertEquals(invoke("inc", source, -2, "milliseconds"), source.plus(-2, ChronoUnit.MILLIS));
        assertEquals(invoke("inc", source, -2, "ms"), source.plus(-2, ChronoUnit.MILLIS));
        assertEquals(invoke("inc", source, -2, "S"), source.plus(-2, ChronoUnit.MILLIS));

        // subtract nanos
        assertEquals(invoke("inc", source, -2, "nanos"), source.plus(-2, ChronoUnit.NANOS));
        assertEquals(invoke("inc", source, -2, "nano"), source.plus(-2, ChronoUnit.NANOS));
        assertEquals(invoke("inc", source, -2, "n"), source.plus(-2, ChronoUnit.NANOS));
    }

    @Test
    public void testIncErrorCases() {
        OffsetDateTime source = OffsetDateTime.parse("20180510-23:55:44.000789000Z", formatter);

        // Test null first argument
        assertInstanceOf(EvalError.class, invoke("inc", null, 1, "hours"));

        // Test null second argument
        assertInstanceOf(EvalError.class, invoke("inc", source, null, "hours"));

        // Test null third argument
        assertInstanceOf(EvalError.class, invoke("inc", source, 1, null));

        // Test wrong number of arguments
        assertInstanceOf(EvalError.class, invoke("inc"));
        assertInstanceOf(EvalError.class, invoke("inc", source));
        assertInstanceOf(EvalError.class, invoke("inc", source, 1));
        assertInstanceOf(EvalError.class, invoke("inc", source, 1, "hours", "extra"));

        // Test wrong type for first argument
        assertInstanceOf(EvalError.class, invoke("inc", "not a date", 1, "hours"));
        assertInstanceOf(EvalError.class, invoke("inc", 123, 1, "hours"));

        // Test wrong type for second argument
        assertInstanceOf(EvalError.class, invoke("inc", source, "not a number", "hours"));

        // Test wrong type for third argument
        assertInstanceOf(EvalError.class, invoke("inc", source, 1, 123));

        // Test invalid unit (this will throw RuntimeException in getField)
        // Note: The current implementation throws RuntimeException for invalid units
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> invoke("inc", source, 1, "invalidunit"));
        assertTrue(exception.getMessage().contains("not recognized"));
    }

    @Test
    public void testIncWithDifferentNumberTypes() {
        OffsetDateTime source = OffsetDateTime.parse("20180510-23:55:44.000789000Z", formatter);

        // Test with Integer
        assertEquals(invoke("inc", source, Integer.valueOf(2), "hours"), source.plus(2, ChronoUnit.HOURS));

        // Test with Long
        assertEquals(invoke("inc", source, Long.valueOf(2), "hours"), source.plus(2, ChronoUnit.HOURS));

        // Test with Double
        assertEquals(invoke("inc", source, Double.valueOf(2.5), "hours"), source.plus(2, ChronoUnit.HOURS));

        // Test with Float
        assertEquals(invoke("inc", source, Float.valueOf(2.5f), "hours"), source.plus(2, ChronoUnit.HOURS));
    }

}
