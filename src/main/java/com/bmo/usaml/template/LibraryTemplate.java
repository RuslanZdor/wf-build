package com.bmo.usaml.template;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;

/**
 * This class generate source code from template wf-package-template.xml
 * name format include library name with version
 */
public class LibraryTemplate extends MasterLibraryTemplate {

    public LibraryTemplate() {
    }

    /**
     * File name for generated xml file with just code source inside
     * @param project maven project object
     * @return combined name
     */
    public String buildFileName(MavenProject project) {
        if (StringUtils.isBlank(project.getName())) {
            throw new IllegalArgumentException("Project name is required in pom.xml to library");
        }
        if (StringUtils.isBlank(project.getVersion())) {
            throw new IllegalArgumentException("Project version is required in pom.xml to library");
        }
        return String.format("%s-%s.xml", project.getName(), project.getVersion());
    }

}
