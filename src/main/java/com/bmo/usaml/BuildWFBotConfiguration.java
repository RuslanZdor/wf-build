package com.bmo.usaml;

import com.bmo.usaml.template.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Mojo(name = "buildBotConfiguration")
public class BuildWFBotConfiguration extends AbstractMojo {

    @Parameter(defaultValue = "${project.groupId}", readonly = true)
    private String groupId;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private String buildDirectory;

    @Parameter(defaultValue = "${project.artifactId}", readonly = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String version;

    @Parameter(defaultValue = "${pathForArtifact}", readonly = true)
    private String pathForArtifact;

    @Parameter(defaultValue = "${skipMasterBuild}", readonly = true)
    private boolean skipMasterBuild;

    @Parameter(defaultValue = "${skipVersionBuild}", readonly = true)
    private boolean skipVersionBuild;

    private List<AbstractTemplate> templates = new ArrayList<>();

    public void init() {
        if (!skipMasterBuild) {
            templates.add(new MasterLibraryTemplate());
            templates.add(new MasterPackageTemplate());
        }
        if (!skipVersionBuild) {
            templates.add(new LibraryTemplate());
            templates.add(new PackageTemplate());
        }
    }

    public void execute() throws MojoFailureException {
        init();
        MavenProject mavenProject = (MavenProject) getPluginContext().get("project");
        try {
            String code = comnineCodeSources(mavenProject.getCompileSourceRoots());
            for (AbstractTemplate template : templates) {
                Document document = template.readDocument();
                template.buildDocument(document, mavenProject, code);
                File savedSource = template.saveDocument(document, new File(buildDirectory, template.buildFileName(mavenProject)));
                if (StringUtils.isNotBlank(pathForArtifact)) {
                    FileUtils.forceMkdir(new File(pathForArtifact));
                    File destination =  new File(pathForArtifact, savedSource.getName());
                    if(destination.exists()) {
                        destination.delete();
                    }
                    FileUtils.copyFile(savedSource, destination);
                }
            }
        } catch (SAXException | IOException | TransformerException | XPathExpressionException e) {
            throw new MojoFailureException("Fail to compile project ", e);
        }
    }

    private String comnineCodeSources(List<String> sourceFolders) throws MojoFailureException {
        Queue<File> files = new LinkedList<>();
        for (String path : sourceFolders) {
            files.add(new File(path));
        }

        StringBuilder codeSource = new StringBuilder();
        while (!files.isEmpty()) {
            File file = files.poll();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            }
            if ("java".equals(FilenameUtils.getExtension(file.getName()))) {
                try (
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                ) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        codeSource.append(line).append("\n");
                    }
                } catch (IOException e) {
                    throw new MojoFailureException("Fail to read source files", e);
                }
            }
        }
        return codeSource.toString();
    }
}