package com.bmo.usaml.template;

import jdk.nashorn.api.scripting.URLReader;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract class for different template implementations
 */
abstract public class AbstractTemplate {

    private final String fileName;

    AbstractTemplate(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * Each template will provide unique pattern for combined file
     * @param mavenProject maven project object
     * @return combined name
     */
    public abstract String buildFileName(MavenProject mavenProject);

    /**
     * Each template has to provide implementation of logic how it should be builded
     * @param document SAX document to update
     * @param project Maven project object
     * @param code combined code to insert
     */
    public abstract void buildDocument(Document document, MavenProject project, String code) throws XPathExpressionException, MojoFailureException;

    /**
     * Open xml document and return SAX document
     *
     * @return parsed document object
     * @throws IOException                  in case of problem to read file
     * @throws SAXException                 in case when template is broken
     */
    public Document readDocument() throws IOException, SAXException {
        String source = readTemplate(fileName);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            return builder.parse( new ByteArrayInputStream(source.getBytes()));
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Builder configuration exception");
        }
    }

    /**
     * Save xml document to file
     *
     * @param document xml to save
     * @param file     destination to save file
     * @return saved File
     */
    public File saveDocument(Document document, File file) throws IOException, TransformerException {
        Objects.requireNonNull(document);
        Objects.requireNonNull(file);
        FileUtils.mkdir(file.getParent());
        if (file.exists()) {
            file.delete();
        }
        DOMSource source = new DOMSource(document);
        try (
                FileWriter writer = new FileWriter(file)
        ) {
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        }
        return file;
    }


    private String readTemplate(String template) throws IOException {
        Enumeration<URL> urlLoader = Thread.currentThread().getContextClassLoader().getResources(template);
        if (urlLoader.hasMoreElements()) {
            StringBuilder builder = new StringBuilder();
            try (
                    BufferedReader reader = new BufferedReader(new URLReader(urlLoader.nextElement()));
            ) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
            return builder.toString();
        } else {
            throw new IOException(String.format("Template %s is not exist in context", template));
        }
    }

    protected void putValue(Document document, String xmlPath, String value) throws XPathExpressionException {
        Objects.requireNonNull(document);
        Objects.requireNonNull(xmlPath);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(xmlPath).evaluate(document, XPathConstants.NODESET);
        if (nodeList.getLength() == 0) {
            throw new XPathExpressionException(String.format("there are no element for xpath :%s", xmlPath));
        } else {
            nodeList.item(0).setTextContent(value);
        }
    }
}
