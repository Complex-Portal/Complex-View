/**
 *
 */
package uk.ac.ebi.intact.service.exporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import psidev.psi.mi.tab.converter.tab2xml.InteractorUniprotIdBuilder;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.binarysearch.wsclient.BinarySearchServiceClient;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.BinarySearch;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.SearchResult;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetworkFactory;
import uk.ac.ebi.intact.service.graph.binary.GraphException;





/**
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 *
 */
public class IntactExporter {

	public static final Log log = LogFactory.getLog(IntactExporter.class);

	private BinarySearchServiceClient client;


	public EntrySet exportToPsiMi25(Collection <BinaryInteraction> binaryInteractions) throws ExporterException{

		if (binaryInteractions.isEmpty()){
			log.warn("No BinaryInteractions given.");
		}

		EntrySet psimi25 = null;

		Tab2Xml t2x = new Tab2Xml();
		t2x.setInteractorNameBuilder(new InteractorUniprotIdBuilder());

		try {
			psimi25 = t2x.convert(binaryInteractions);
		} catch (Exception e) {
			throw new ExporterException(e.getMessage());
		}

		return psimi25;
	}

	public EntrySet exportToPsiMi10(Collection <BinaryInteraction> binaryInteractions) throws ExporterException{
		return null;
	}

	public Document  exportToGraphML(Collection <BinaryInteraction> binaryInteractions) throws GraphException {
		
		BinaryGraphNetwork network = BinaryGraphNetworkFactory.createBinaryGraphNetwork(binaryInteractions);
		Document doc = BinaryGraphNetworkFactory.export(network);

		return doc;
	}


	public EntrySet exportToPsiMi25(String [] acs) throws ExporterException, ConverterException{
		EntrySet psimi25 = null;

		if (acs != null){
			Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

			for (int i = 0; i < acs.length; i++){
				String ac = acs[i];
				Collection<BinaryInteraction> results = getBinaryInteraction(ac);
				binaryInteractions.addAll(results);
			}
			psimi25 = exportToPsiMi25(binaryInteractions);

		} else {
			throw new ExporterException("No Accession Numbers found.");
		}

		if (psimi25 != null) return psimi25;
		else throw new ExporterException("No Interactions found.");
	}

	public EntrySet exportToPsiMi10(String [] acs) {
		//TODO
		return null;
	}

	public Collection<BinaryInteraction> exportToPsiMiTab(String [] acs) throws ExporterException, ConverterException {

		Collection<BinaryInteraction> tabInteractions = null;

		if (acs != null){
			tabInteractions = new ArrayList<BinaryInteraction>();

			for (int i = 0; i < acs.length; i++){
				String ac = acs[i];
				Collection<BinaryInteraction> results = getBinaryInteraction(ac);

				if (!tabInteractions.addAll(results)){
					log.warn("Could not add result interaction.");
				}
			}
		} else {
			throw new ExporterException("No Accession Numbers found.");
		}

		if (!tabInteractions.isEmpty()) return tabInteractions;
		else throw new ExporterException("No Interactions found.");
	}

	public Document exportToGraphML(String [] acs) throws ConverterException, ExporterException, GraphException {
		Document graph = null;

		if (acs != null){
			Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

			for (int i = 0; i < acs.length; i++){
				String ac = acs[i];
				Collection<BinaryInteraction> results = getBinaryInteraction(ac);
				binaryInteractions.addAll(results);
			}
			graph = exportToGraphML(binaryInteractions);

		} else {
			throw new ExporterException("No Accession Numbers found.");
		}

		if (graph != null) return graph;
		else throw new ExporterException("No Interactions found.");
	}


	private Collection<BinaryInteraction> getBinaryInteraction(String ac) throws ConverterException{

		client = new BinarySearchServiceClient();
		BinarySearch port = client.getBinarySearchPort();

	    SearchResult sr = port.findBinaryInteractions(ac);
	    List<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction> webInteractions = sr.getInteractions();

		BinaryInteractionConverter biConverter = new BinaryInteractionConverter();
		Collection<BinaryInteraction> binaryInteractions = biConverter.convertWeb2Tab(webInteractions);


	    return binaryInteractions;
	}


}
