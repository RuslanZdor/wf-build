package com.bmo.usaml.template;

import org.apache.maven.project.MavenProject;

public class PackageTemplate extends MasterPackageTemplate {

    public PackageTemplate() {
        super();
        setLibraryTemplate(new LibraryTemplate());
    }

    /**
     * File name for generated xml file with just code source inside
     * @param mavenProject maven project to build
     * @return combined name
     */
    public String buildFileName(MavenProject mavenProject) {
        return String.format("%s - configuration.xml", mavenProject.getName(), mavenProject.getVersion());
    }
}
