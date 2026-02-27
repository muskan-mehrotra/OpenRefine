package com.google.refine;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import com.google.refine.model.Project;

/**
 * Demonstrates stubbing by subclassing ProjectManagerStub
 * and overriding saveProject(Project) to avoid real disk writes,
 * while still verifying that the code path invokes saveProject().
 */
public class ProjectManagerSaveProjectStubbingTest extends RefineTest {

    /**
     * A stub which replaces the real saveProject(Project) implementation.
     * Instead of writing to disk, it just records that it was called.
     */
   static class TrackingProjectManagerStub extends ProjectManagerStub {
    final AtomicInteger saveCalls = new AtomicInteger(0);

    @Override
    public void saveProject(Project project) {
        // Stubbed behavior: no disk I/O, just track invocation.
        saveCalls.incrementAndGet();

        // Optional: emulate successful save
        project.setLastSave();
    }
}

    @Test
    public void saveProjectsInvokesStubbedSaveProject() {
        // Replace the default stub from RefineTest with our tracking stub
        TrackingProjectManagerStub pm = new TrackingProjectManagerStub();
        ProjectManager.singleton = pm;

        // Create + register a project (this updates metadata.modified)
        Project project = new Project();
        ProjectMetadata meta = new ProjectMetadata();
        meta.setName("stubbing-saveProject-test");
        ProjectManager.singleton.registerProject(project, meta);

        // Trigger code path which calls saveProject(project) internally
        // saveProjects(boolean) is made public by ProjectManagerStub
        pm.saveProjects(true);

        // Assert our stubbed saveProject() was used
        assertEquals(pm.saveCalls.get(), 1, "Expected saveProject() to be called once via the stub");
    }
}