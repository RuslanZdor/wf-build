package com.bmo.usaml.template;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class LibraryTemplateTest {

    private AbstractTemplate template;
    private MavenProject mavenProject;

    @Before
    public void init() {
        template = new LibraryTemplate();
        mavenProject = mock(MavenProject.class);
        when(mavenProject.getName()).thenReturn("name");
        when(mavenProject.getVersion()).thenReturn("1.0");
    }

    @Test
    public void buildFileName() {
        assertEquals("name-1.0.xml", template.buildFileName(mavenProject));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFileNameEmpty() {
        template.buildFileName(mavenProject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildFileVersionEmpty() {
        template.buildFileName(mavenProject);
    }
}