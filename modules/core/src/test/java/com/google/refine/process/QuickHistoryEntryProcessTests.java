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

package com.google.refine.process;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.refine.ProjectManager;
import com.google.refine.ProjectMetadata;
import com.google.refine.RefineTest;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.util.TestUtils;

public class QuickHistoryEntryProcessTests extends RefineTest {

    public static class QuickHistoryEntryProcessStub extends QuickHistoryEntryProcess {

        private boolean createHistoryEntryCalled = false;

        public QuickHistoryEntryProcessStub(Project project, String briefDescription) {
            super(project, briefDescription);

        }

        @Override
        protected HistoryEntry createHistoryEntry(long historyEntryID)
                throws Exception {
            createHistoryEntryCalled = true;

            Change change = new Change() {
                @Override
                public void apply(Project project) {
                    // no-op
                }

                @Override
                public void revert(Project project) {
                    // no-op
                }

                @Override
                public void save(Writer writer, Properties options) throws IOException {
                    // no-op
                }
            };

            AbstractOperation operation = mock(AbstractOperation.class);

            return new HistoryEntry(historyEntryID, _project, "stub history entry", operation, change);
        }

        public boolean wasCreateHistoryEntryCalled() {
            return createHistoryEntryCalled;
        }
    }

    @Test
    public void serializeQuickHistoryEntryProcess() {
        Project project = mock(Project.class);
        Process process = new QuickHistoryEntryProcessStub(project, "quick description");
        int hashCode = process.hashCode();
        TestUtils.isSerializedTo(process, "{"
                + "\"id\":" + hashCode + ","
                + "\"description\":"
                + "\"quick description\","
                + "\"immediate\":true,"
                + "\"status\":\"pending\"}");
    }

    @Test
    public void performImmediateUsesStubbedCreateHistoryEntry() throws Exception {
        Project project = new Project();
        ProjectMetadata projectMetadata = new ProjectMetadata();
        ProjectManager.singleton.registerProject(project, projectMetadata);

        QuickHistoryEntryProcessStub process = new QuickHistoryEntryProcessStub(project, "quick description");

        process.performImmediate();

        Assert.assertTrue(process.wasCreateHistoryEntryCalled(), "createHistoryEntry should have been called");
        Assert.assertEquals(process.getStatus(), "done");
        Assert.assertEquals(process.getDescription(), "stub history entry");
    }
}
