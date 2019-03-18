package com.bmo.usaml.template;

import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;

public class MasterLibraryTemplate extends AbstractTemplate {

    private static final String TEMPLATE = "wf-library-template.xml";
    private static final String XML_PLACE = "config";

    public MasterLibraryTemplate() {
        super(TEMPLATE);
    }

    /**
     * File name for generated xml file with just code source inside
     * @param project maven project object
     * @return combined name
     */
    public String buildFileName(MavenProject project) {
        return String.format("%s.xml", project.getName());
    }

    /**
     * Library template contain only source code in default WF xml format.
     * @param document SAX document to update
     * @param code combined code to insert
     * @throws XPathExpressionException
     */
    @Override
    public void buildDocument(Document document, MavenProject project, String code) throws XPathExpressionException {
        putValue(document,XML_PLACE, code);
    }

}
