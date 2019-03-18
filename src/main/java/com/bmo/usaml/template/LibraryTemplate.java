package com.bmo.usaml.template;

import org.apache.maven.project.MavenProject;

public class LibraryTemplate extends MasterLibraryTemplate {

    public LibraryTemplate() {
    }

    /**
     * File name for generated xml file with just code source inside
     * @param project maven project object
     * @return combined name
     */
    public String buildFileName(MavenProject project) {
        return String.format("%s-%s.xml", project.getName(), project.getVersion());
    }

}
