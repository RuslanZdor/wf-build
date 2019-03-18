package com.bmo.usaml.template;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MasterPackageTemplate extends AbstractTemplate {

    private static final String TEMPLATE = "wf-package-template.xml";
    private static final String XML_CONTENT = "objectListWrapper/objects/objectContainer/machineConfig/content";
    private static final String XML_INCLUDE = "objectListWrapper/objects/objectContainer/machineConfig/inclusionCode";
    private static final String XML_NAME = "objectListWrapper/objects/objectContainer/machineConfig/name";
    private static final String XML_UUID = "objectListWrapper/objects/objectContainer/machineConfig/uuid";
    private static final String XML_HASHCODE = "objectListWrapper/objects/objectContainer/hashCode";
    private static final String XML_DESCRIPTION = "objectListWrapper/objects/objectContainer/machineConfig/description";

    private static final String XML_VERSION_CONPONENT_UUID = "objectListWrapper/objects/objectContainer/machineConfig/version/componentUuid";
    private static final String XML_VERSION_INCREMENTAL = "objectListWrapper/objects/objectContainer/machineConfig/version/incrementalVersion";
    private static final String XML_VERSION_DATE = "objectListWrapper/objects/objectContainer/machineConfig/version/lastModified";
    private static final String XML_VERSION_VERSION_UUID = "objectListWrapper/objects/objectContainer/machineConfig/version/versionUuid";

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private AbstractTemplate libraryTemplate;

    public MasterPackageTemplate() {
        super(TEMPLATE);
        libraryTemplate = new MasterLibraryTemplate();
    }

    /**
     * File name for generated xml file with just code source inside
     *
     * @param project maven project object
     * @return combined name
     */
    public String buildFileName(MavenProject project) {
        return String.format("%s-%s - configuration.xml", project.getName(), project.getVersion());
    }

    /**
     * Library template contain only source code in default WF xml format.
     *
     * @param document SAX document to update
     * @param project  maven project object
     * @param code     combined code to insert
     * @throws XPathExpressionException
     */
    @Override
    public void buildDocument(Document document, MavenProject project, String code) throws MojoFailureException, XPathExpressionException {
        Document libraryDocument = null;
        String templateCode = "";
        try {
            libraryDocument = libraryTemplate.readDocument();
            libraryTemplate.buildDocument(libraryDocument, project, code);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(libraryDocument), result);
            templateCode = writer.toString();
        } catch (IOException | SAXException | TransformerException e1) {
            e1.printStackTrace();
        }

        putValue(document, XML_CONTENT, templateCode);
        putValue(document, XML_VERSION_DATE, LocalDateTime.now().format(dateFormat));
        putValue(document, XML_UUID, UUID.randomUUID().toString());

        putValue(document, XML_NAME, libraryTemplate.buildFileName(project));
        putValue(document, XML_INCLUDE, libraryTemplate.buildFileName(project));

        putValue(document, XML_HASHCODE, Integer.toString(code.hashCode()));
        putValue(document, XML_DESCRIPTION, project.getDescription());
        putValue(document, XML_VERSION_INCREMENTAL, project.getVersion());
    }

    public void setLibraryTemplate(AbstractTemplate template) {
        this.libraryTemplate = template;
    }
}
