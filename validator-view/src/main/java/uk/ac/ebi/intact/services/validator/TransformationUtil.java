/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * This util class deals with XSLT transformations
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: TransformationUtil.java 5053 2006-06-14 13:04:18Z baranda $
 * @since <pre>13-Jun-2006</pre>
 */
public class TransformationUtil {

    private static final Log log = LogFactory.getLog(TransformationUtil.class);

    /**
     * Transforms a PSI XML file to HTML
     *
     * @param is the stream to stransform
     * @return the outputStream with the HTML
     * @throws TransformerException things may fail
     */
    public static OutputStream transformToHtml(InputStream is) throws TransformerException {
        // we use and xslt file to transform to HTML, provided in the jar
        InputStream xslt = TransformationUtil.class.getResourceAsStream( "/validator/xslt/MIF25_view.xsl" );
        OutputStream outputStream = null;

        try{
            outputStream = transform(is, xslt);
        }
        finally {
            try {
                xslt.close();
            } catch (IOException e) {
                log.error("Impossible to close the Xslt inputstream", e);
            }
        }
        return outputStream;
    }

    /**
     * Transform a PSI XML file to the "expanded" version
     *
     * @param is the stream to stransform
     * @return the outputStream with the expanded PSI XML
     * @throws TransformerException things may fail
     */
    public static OutputStream transformToExpanded(InputStream is) throws TransformerException {
        // we use and xslt file to transform to the expanded version, provided in the jar
        InputStream xslt = TransformationUtil.class.getResourceAsStream( "/validator/xslt/MIF25_expand.xsl" );

        OutputStream outputStream = null;

        try{
            outputStream = transform(is, xslt);
        }
        finally {
            try {
                xslt.close();
            } catch (IOException e) {
                log.error("Impossible to close the Xslt inputstream", e);
            }
        }
        return outputStream;
    }

    /**
     * The actual method that does the transformation
     *
     * @param isToTransform The stream to transform
     * @param xslt          The stream with the XSLT rules
     * @return The transformed stream
     * @throws TransformerException thrown if something has been wrong with the transformation
     */
    private static OutputStream transform(InputStream isToTransform, InputStream xslt) throws TransformerException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(isToTransform);
        Source xsltSource = new StreamSource(xslt);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(outputStream));

        return outputStream;
    }
}