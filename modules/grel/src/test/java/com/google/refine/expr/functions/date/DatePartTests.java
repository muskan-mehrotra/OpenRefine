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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.refine.grel.GrelTestBase;

public class DatePartTests extends GrelTestBase {

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

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSSSSSX");

    @Test
    public void testOffsetDateTimeDatePart() {
        // 2018-4-30 23:55:44
        OffsetDateTime source = OffsetDateTime.parse("20180430-23:55:44.000789000Z",
                formatter);

        // hours
        assertEquals(invoke("datePart", source, "hours"), 23);
        assertEquals(invoke("datePart", source, "hour"), 23);
        assertEquals(invoke("datePart", source, "h"), 23);

        // minutes
        assertEquals(invoke("datePart", source, "minutes"), 55);
        assertEquals(invoke("datePart", source, "minute"), 55);
        assertEquals(invoke("datePart", source, "min"), 55);

        // seconds
        assertEquals(invoke("datePart", source, "seconds"), 44);
        assertEquals(invoke("datePart", source, "sec"), 44);
        assertEquals(invoke("datePart", source, "s"), 44);

        // milliseconds
        assertEquals(invoke("datePart", source, "milliseconds"), 789);
        assertEquals(invoke("datePart", source, "ms"), 789);
        assertEquals(invoke("datePart", source, "S"), 789);

        // nanos
        assertEquals(invoke("datePart", source, "nanos"), 789000);
        assertEquals(invoke("datePart", source, "nano"), 789000);
        assertEquals(invoke("datePart", source, "n"), 789000);

        // years
        assertEquals(invoke("datePart", source, "years"), 2018);
        assertEquals(invoke("datePart", source, "year"), 2018);

        // months
        assertEquals(invoke("datePart", source, "months"), 4);
        assertEquals(invoke("datePart", source, "month"), 4);

        // weeks
        assertEquals(invoke("datePart", source, "weeks"), 5);
        assertEquals(invoke("datePart", source, "week"), 5);
        assertEquals(invoke("datePart", source, "w"), 5);

        // days, day, d
        assertEquals(invoke("datePart", source, "days"), 30);
        assertEquals(invoke("datePart", source, "day"), 30);
        assertEquals(invoke("datePart", source, "d"), 30);

        // weekday
        assertEquals(invoke("datePart", source, "weekday"), "MONDAY");

        // time
        assertEquals(invoke("datePart", source, "time"), 1525132544000l);
    }

    @Test
    public void testCalendarDatePart() {
        // 2018-4-30 23:55:44.789
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2018, Calendar.APRIL, 30, 23, 55, 44);
        calendar.set(Calendar.MILLISECOND, 789);

        // hours
        assertEquals(invoke("datePart", calendar, "hours"), 23);
        assertEquals(invoke("datePart", calendar, "hour"), 23);
        assertEquals(invoke("datePart", calendar, "h"), 23);

        // minutes
        assertEquals(invoke("datePart", calendar, "minutes"), 55);
        assertEquals(invoke("datePart", calendar, "minute"), 55);
        assertEquals(invoke("datePart", calendar, "min"), 55);

        // seconds
        assertEquals(invoke("datePart", calendar, "seconds"), 44);
        assertEquals(invoke("datePart", calendar, "sec"), 44);
        assertEquals(invoke("datePart", calendar, "s"), 44);

        // milliseconds
        assertEquals(invoke("datePart", calendar, "milliseconds"), 789);
        assertEquals(invoke("datePart", calendar, "ms"), 789);
        assertEquals(invoke("datePart", calendar, "S"), 789);

        // years
        assertEquals(invoke("datePart", calendar, "years"), 2018);
        assertEquals(invoke("datePart", calendar, "year"), 2018);

        // months (Calendar.MONTH is 0-based, but DatePart adds 1)
        assertEquals(invoke("datePart", calendar, "months"), 4);
        assertEquals(invoke("datePart", calendar, "month"), 4);

        // weeks
        assertEquals(invoke("datePart", calendar, "weeks"), calendar.get(Calendar.WEEK_OF_MONTH));
        assertEquals(invoke("datePart", calendar, "week"), calendar.get(Calendar.WEEK_OF_MONTH));
        assertEquals(invoke("datePart", calendar, "w"), calendar.get(Calendar.WEEK_OF_MONTH));

        // days
        assertEquals(invoke("datePart", calendar, "days"), 30);
        assertEquals(invoke("datePart", calendar, "day"), 30);
        assertEquals(invoke("datePart", calendar, "d"), 30);

        // weekday
        String weekday = (String) invoke("datePart", calendar, "weekday");
        assertNotNull(weekday);
        assertTrue(weekday.equals("Sunday") || weekday.equals("Monday") || weekday.equals("Tuesday") 
                || weekday.equals("Wednesday") || weekday.equals("Thursday") || weekday.equals("Friday") 
                || weekday.equals("Saturday"));

        // time
        assertEquals(invoke("datePart", calendar, "time"), calendar.getTimeInMillis());
    }

    @Test
    public void testDateDatePart() {
        // 2018-4-30 23:55:44.789
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2018, Calendar.APRIL, 30, 23, 55, 44);
        calendar.set(Calendar.MILLISECOND, 789);
        Date date = calendar.getTime();

        // hours
        assertEquals(invoke("datePart", date, "hours"), 23);
        assertEquals(invoke("datePart", date, "hour"), 23);
        assertEquals(invoke("datePart", date, "h"), 23);

        // minutes
        assertEquals(invoke("datePart", date, "minutes"), 55);
        assertEquals(invoke("datePart", date, "minute"), 55);
        assertEquals(invoke("datePart", date, "min"), 55);

        // seconds
        assertEquals(invoke("datePart", date, "seconds"), 44);
        assertEquals(invoke("datePart", date, "sec"), 44);
        assertEquals(invoke("datePart", date, "s"), 44);

        // milliseconds
        assertEquals(invoke("datePart", date, "milliseconds"), 789);
        assertEquals(invoke("datePart", date, "ms"), 789);
        assertEquals(invoke("datePart", date, "S"), 789);

        // years
        assertEquals(invoke("datePart", date, "years"), 2018);
        assertEquals(invoke("datePart", date, "year"), 2018);

        // months
        assertEquals(invoke("datePart", date, "months"), 4);
        assertEquals(invoke("datePart", date, "month"), 4);

        // days
        assertEquals(invoke("datePart", date, "days"), 30);
        assertEquals(invoke("datePart", date, "day"), 30);
        assertEquals(invoke("datePart", date, "d"), 30);

        // time
        assertEquals(invoke("datePart", date, "time"), date.getTime());
    }

    @Test
    public void testDatePartErrorCases() {
        OffsetDateTime source = OffsetDateTime.parse("20180430-23:55:44.000789000Z", formatter);

        // Test null first argument
        assertTrue(invoke("datePart", null, "hours") instanceof com.google.refine.expr.EvalError);

        // Test null second argument
        assertTrue(invoke("datePart", source, null) instanceof com.google.refine.expr.EvalError);

        // Test wrong number of arguments
        assertTrue(invoke("datePart") instanceof com.google.refine.expr.EvalError);
        assertTrue(invoke("datePart", source) instanceof com.google.refine.expr.EvalError);
        assertTrue(invoke("datePart", source, "hours", "extra") instanceof com.google.refine.expr.EvalError);

        // Test wrong type for first argument
        assertTrue(invoke("datePart", "not a date", "hours") instanceof com.google.refine.expr.EvalError);
        assertTrue(invoke("datePart", 123, "hours") instanceof com.google.refine.expr.EvalError);

        // Test wrong type for second argument
        assertTrue(invoke("datePart", source, 123) instanceof com.google.refine.expr.EvalError);

        // Test unrecognized date part
        assertTrue(invoke("datePart", source, "invalidpart") instanceof com.google.refine.expr.EvalError);
        assertTrue(invoke("datePart", source, "") instanceof com.google.refine.expr.EvalError);
    }

}
