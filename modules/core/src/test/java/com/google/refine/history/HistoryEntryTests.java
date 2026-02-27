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

package com.google.refine.history;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Properties;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.RefineTest;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.util.TestUtils;

public class HistoryEntryTests extends RefineTest {

    public static final String fullJson = "{"
            + "\"id\":1533633623158,"
            + "\"description\":\"Create new column uri based on column country by filling 269 rows with grel:\\\"https://www.wikidata.org/wiki/\\\"+cell.recon.match.id\","
            + "\"time\":\"2018-08-07T09:06:37Z\","
            + "\"operation\":{\"op\":\"core/mock-operation\","
            + "   \"description\":\"some description\","
            + "   \"foo\":\"bar\"}"
            + "}";

    public static final String unknownOperationJson = "{"
            + "\"id\":1533633623158,"
            + "\"description\":\"some mysterious operation\","
            + "\"time\":\"2018-08-07T09:06:37Z\","
            + "\"operation\":{\"op\":\"someextension/unknown-operation\","
            + "   \"description\":\"some mysterious operation\","
            + "   \"some_parameter\":234\n"
            + "}\n"
            + "}";

    Project project;

    public static class MockOperation extends AbstractOperation {

        @JsonProperty("foo")
        public String someParameter = "bar";

        @Override
        public String getJsonDescription() {
            return "some description";
        }
    }

    @BeforeTest
    public void register() {
        OperationRegistry.registerOperation(getCoreModule(), "mock-operation", MockOperation.class);
    }

    @BeforeMethod
    public void setUp() {
        project = mock(Project.class);
    }

    @Test
    public void serializeHistoryEntry() throws Exception {
        String json = "{\"id\":1533651837506,"
                + "\"description\":\"Discard recon judgment for single cell on row 76, column organization_name, containing \\\"Catholic University Leuven\\\"\","
                + "\"time\":\"2018-08-07T14:18:29Z\"}";
        TestUtils.isSerializedTo(HistoryEntry.load(project, json), json);
    }

    @Test
    public void serializeHistoryEntryWithOperation() throws Exception {
        String jsonSimple = "{"
                + "\"id\":1533633623158,"
                + "\"description\":\"Create new column uri based on column country by filling 269 rows with grel:\\\"https://www.wikidata.org/wiki/\\\"+cell.recon.match.id\","
                + "\"time\":\"2018-08-07T09:06:37Z\","
                + "\"operation_id\":\"core/mock-operation\"}";

        HistoryEntry historyEntry = HistoryEntry.load(project, fullJson);
        TestUtils.isSerializedTo(historyEntry, jsonSimple, false);
        TestUtils.isSerializedTo(historyEntry, fullJson, true);
    }

    @Test
    public void deserializeUnknownOperation() throws IOException {
        // Unknown operations are serialized back as they were parsed
        HistoryEntry entry = HistoryEntry.load(project, unknownOperationJson);
        TestUtils.isSerializedTo(entry, unknownOperationJson, true);
    }

    /**
     * Tests the new createWithManager() factory, which allows injecting a mock HistoryEntryManager.
     * This test verifies that save() correctly delegates to the injected manager - functionality
     * that was previously untestable because the manager was hardcoded from ProjectManager.singleton.
     */
    @Test
    public void saveDelegatesToInjectedManager() throws Exception {
        Project testProject = createProject(new String[] { "col1" }, new Serializable[][] { { "val1" } });
        HistoryEntryManager mockManager = mock(HistoryEntryManager.class);

        Change noOpChange = new Change() {
            @Override
            public void apply(Project project) {
            }

            @Override
            public void revert(Project project) {
            }

            @Override
            public void save(java.io.Writer writer, Properties options) throws IOException {
            }
        };

        HistoryEntry entry = HistoryEntry.createWithManager(
                999L, testProject, "Test entry", null, noOpChange, mockManager);

        StringWriter writer = new StringWriter();
        Properties options = new Properties();
        options.setProperty("mode", "save");

        entry.save(writer, options);

        verify(mockManager).save(entry, writer, options);
    }

    /**
     * Tests time source injection for HistoryEntry using a mocked Supplier&lt;Instant&gt;.
     * Verifies that the injected time is used and that time-based serialization is deterministic.
     */
    @Test
    public void createWithManagerUsesMockedTimeSource() throws Exception {
        Project testProject = createProject(new String[] { "col1" }, new Serializable[][] { { "val1" } });
        HistoryEntryManager mockManager = mock(HistoryEntryManager.class);

        @SuppressWarnings("unchecked")
        Supplier<Instant> mockTimeSource = mock(Supplier.class);
        Instant fixedTime = Instant.parse("2018-08-07T09:06:37Z");
        when(mockTimeSource.get()).thenReturn(fixedTime);

        Change noOpChange = new Change() {
            @Override
            public void apply(Project project) {
            }

            @Override
            public void revert(Project project) {
            }

            @Override
            public void save(java.io.Writer writer, Properties options) throws IOException {
            }
        };

        HistoryEntry entry = HistoryEntry.createWithManager(
                999L, testProject, "Time test entry", null, noOpChange, mockManager, mockTimeSource);

        Assert.assertEquals(entry.time, fixedTime, "Entry should have the time from mocked time source");
        verify(mockTimeSource).get();

        String expectedJson = "{\"id\":999,\"description\":\"Time test entry\",\"time\":\"2018-08-07T09:06:37Z\"}";
        TestUtils.isSerializedTo(entry, expectedJson, false);
    }
}
