package uk.ac.ebi.intact.service.graph.io;

import edu.uci.ics.jung.graph.Graph;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.io.impl.GraphMLTypeConverter;
import uk.ac.ebi.intact.service.graph.jaxb.GraphmlType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.Writer;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GraphIOUtils {

    private GraphIOUtils() {
    }

    public static void exportNetworkToGraphML(GraphNetwork graphNetwork, Writer writer) throws GraphIOException{
        GraphMLTypeConverter converter = new GraphMLTypeConverter();
        GraphmlType graphml = converter.from(graphNetwork);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GraphmlType.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(  new JAXBElement( new QName("http://graphml.graphdrawing.org/xmlns","graphml"), GraphmlType.class, graphml ), writer);

        } catch (JAXBException e) {
            throw new GraphIOException("Exception marshalling graphml type", e);
        }
    }

    public static Graph exportToJung(GraphNetwork graphNetwork) {
        return graphNetwork;
    }

    public static InputStream exportNetworkToCytoscape(GraphNetwork graphNetwork) throws GraphIOException{
        throw new UnsupportedOperationException();
    }


}
