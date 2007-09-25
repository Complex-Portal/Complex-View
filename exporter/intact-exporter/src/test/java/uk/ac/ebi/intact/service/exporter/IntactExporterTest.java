/**
 * 
 */
package uk.ac.ebi.intact.service.exporter;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.service.graph.binary.GraphException;

/**
 * @author Nadin
 *
 */
public class IntactExporterTest extends TestCase {
	
	public IntactExporterTest(String name){
		super(name);
	}
	
	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public static Test suite(){
		return new TestSuite( IntactExporterTest.class);
	}

	public void testExportToPsiMi25StringArray() {
		IntactExporter exporter = new IntactExporter();
		String [] acs = new String[2];
		acs[0]="P12345";
		acs[1]="P97929";
		try {
			EntrySet output = exporter.exportToPsiMi25(acs);
		} catch (ExporterException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
	}
	
	public void testExportToPsiMiTABStringArray() {
		IntactExporter exporter = new IntactExporter();
		String [] acs = new String[2];
		acs[0]="P12345";
		acs[1]="P97929";
		Collection<BinaryInteraction> output = null;
		try {
			output = exporter.exportToPsiMiTab(acs);
		} catch (ExporterException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
	}
	
	public void testExportToPsiMiGraphMLStringArray() {
		IntactExporter exporter = new IntactExporter();
		String [] acs = new String[2];
		acs[0]="P12345";
		acs[1]="P97929";
		try {
			Document graph = exporter.exportToGraphML(acs);
		} catch (ExporterException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		} catch (GraphException e) {
			e.printStackTrace();
		}
	}
}
