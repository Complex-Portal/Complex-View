/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.graph2MIF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import uk.ac.ebi.intact.application.dataConversion.PsiValidator;
import uk.ac.ebi.intact.application.dataConversion.PsiValidatorReport;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlI;
import uk.ac.ebi.intact.application.graph2MIF.exception.MIFSerializeException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;
import uk.ac.ebi.intact.util.simplegraph.Graph;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Graph2MIFWSService Implementation
 * <p/>
 * This is the implementation of The graph2MIF-WebService.
 * Interface defined in Graph2MIFWS.java
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06-Apr-2006</pre>
 */
public class Graph2MIFWSService implements Graph2MIFWS
{
    private static final Log logger = LogFactory.getLog(Graph2MIFWSService.class);

    public String getMIF(String ac, Integer depth, Boolean strictmif) throws IntactException,
                                                                             NoGraphRetrievedException,
                                                                             MIFSerializeException,
                                                                             NoInteractorFoundException
    {
        return getMIF(ac, depth, strictmif, PsiVersion.VERSION_1);
    }

    public String getMIF(String ac, Integer depth, Boolean strictmif, String psiVersion) throws IntactException,
                                                                                                NoGraphRetrievedException,
                                                                                                MIFSerializeException,
                                                                                                NoInteractorFoundException
    {
        return getMIF(ac, depth, strictmif, PsiVersion.valueOf(psiVersion));
    }

    /**
     *  Gets the MIF using a PsiVersion object
     */
    private String getMIF(String ac, Integer depth, Boolean strictmif, PsiVersion psiVersion) throws IntactException,
                                                                                                     NoGraphRetrievedException,
                                                                                                     MIFSerializeException,
                                                                                                     NoInteractorFoundException
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Generating PSI-MIF-XML for AC: '" + ac + "', using Depth: " + depth + ", scrict MIF: " + strictmif + ", version: " + psiVersion.getVersion());
        }

        Document mifDOM = getMIFDocument(ac, depth, psiVersion); // GraphNotConvertableException possible

        // serialize the DOMObject
        StringWriter writer = new StringWriter();
        OutputFormat of = new OutputFormat(mifDOM, "UTF-8", true); //(true|false) for (un)formated  output
        XMLSerializer xmls = new XMLSerializer(writer, of);
        try
        {
            xmls.serialize(mifDOM);
        }
        catch (IOException e)
        {
            logger.warn("IOException while serialize" + e.getMessage());
            throw new MIFSerializeException();
        }

        String xmlPsi = writer.toString();

        if (strictmif)
        {
            PsiValidatorReport report = PsiValidator.validate(xmlPsi);

            if (!report.isValid())
                 throw new MIFSerializeException("Output PSI xml is invalid for AC: "+ac);
        }

        //return the PSI-MIF-XML
        return xmlPsi;
    }

    /**
     * Generates the XML Document
     */
    private Document getMIFDocument(String ac, Integer depth, PsiVersion psiVersion)
            throws IntactException,
                   NoGraphRetrievedException,
                   NoInteractorFoundException
    {
        Graph graph = GraphFactory.getGraph(ac, depth); //NoGraphRetrievedExceptioni, IntactException and NoInteractorFoundException possible

        if (logger.isDebugEnabled())
        {
            logger.debug("\tgot graph: " + graph);
        }


        UserSessionDownload session = new UserSessionDownload(psiVersion);
        Interaction2xmlI interaction2xml = Interaction2xmlFactory.getInstance(session);

        // in order to have them in that order, experimentList, then interactorList, at last interactionList.
        session.getExperimentListElement();
        session.getInteractorListElement();

        Collection<Interaction> interactions = interactionsFromGraph(graph);

        for (Interaction interaction : interactions)
        {
            interaction2xml.create(session, session.getInteractionListElement(), interaction);
        }

        return session.getPsiDocument();
    }

    private static Collection<Interaction> interactionsFromGraph(Graph graph)
    {
        Collection<EdgeI> edges = graph.getEdges();
        Set<Interaction> interactions = new HashSet<Interaction>();

        for (EdgeI edge : edges)
        {
            Interaction compInter1 = edge.getComponent1().getInteraction();
            Interaction compInter2 = edge.getComponent2().getInteraction();

            // interaction for component1 and 2 should be the same
            if (!compInter1.equals(compInter2))
            {
                throw new RuntimeException("Interaction for component1 and 2 should be the same for edge: " + edge.getAc());
            }

            interactions.add(compInter1);
        }

        return interactions;
    }


    public static void main(String[] args)
    {
        Graph2MIFWSService ws = new Graph2MIFWSService();
        try
        {
            String s = ws.getMIF("EBI-104185", new Integer(1), Boolean.FALSE, "2.5");
            System.out.println(s);
        }
        catch (IntactException e)
        {
            e.printStackTrace();
        }
        catch (NoGraphRetrievedException e)
        {
            e.printStackTrace();
        }
        catch (MIFSerializeException e)
        {
            e.printStackTrace();
        }
        catch (NoInteractorFoundException e)
        {
            e.printStackTrace();
        }
    }
}
