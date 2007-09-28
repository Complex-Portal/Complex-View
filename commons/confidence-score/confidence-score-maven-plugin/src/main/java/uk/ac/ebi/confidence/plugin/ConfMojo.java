package uk.ac.ebi.confidence.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import uk.ac.ebi.intact.confidence.ConfidenceModel;

/**
 * Mojo confidence score
 * 
 * @goal confidence
 * @phase install
 * 
 */
public class ConfMojo extends AbstractMojo {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log		log	= LogFactory.getLog(ConfMojo.class);
	 /**
     * The path to the uniprot_sprot directory
     *
     * @parameter expression="${uniprotDirPath}" default-value="${project.build.outputDirectory}"
     */
    private String uniprotDirPath;
    
    /**
     * The path to the uniprot_sprot directory
     *
     * @parameter expression="${workDirPath}" default-value="${project.build.outputDirectory}"
     */
    private String workDirPath;
    /**
     * The path to the blast archive directory
     *
     * @parameter expression="${blastArchivePath}" default-value="${project.build.outputDirectory}"
     */
    private String blastArchivePath;
    
    /**
     * The path to the blast archive directory
     *
     * @parameter expression="${email}" default-value="${iarmean@ebi.ac.uk}"
     */
    private String email;

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("Mojo started ...");
		System.out.println("uniprot: "+ uniprotDirPath);
		System.out.println("workDir: "+ workDirPath);
		System.out.println("blast archive: " + blastArchivePath);

		ConfidenceModel cm = new ConfidenceModel(uniprotDirPath, workDirPath, blastArchivePath, email);
	//	cm.buildModel();
		//or
		classify(cm);

		System.out.println("Mojo done. :)");
	}
	
	private void classify(ConfidenceModel cm){
		long start = System.currentTimeMillis();
		cm.getConfidenceListsFromDb();
		long aux1 = System.currentTimeMillis();
		long timeDb = aux1 - start;
		log.info("time for db retrieve (milisec): " + timeDb);

		aux1 = System.currentTimeMillis();
		cm.generateLowconf(10000);
		long aux2 = System.currentTimeMillis();
		long timeGenerate = aux2 - aux1;
		log.info("time for generating lowconf (milisec): " + timeGenerate);

		aux1 = System.currentTimeMillis();
		//cm.getInterProGoAndAlign();
		aux2 = System.currentTimeMillis();
		long timeAttribs = aux2 - aux1;
		log.info("time for getting the attributes (milisec): " + timeAttribs);

		aux1 = System.currentTimeMillis();
//		cm.createTadmClassifierInput();
//		cm.runTadm();
//		cm.createModel();
//		aux2 = System.currentTimeMillis();
		long timeCreateModel = aux2 - aux1;
		log.info("time for training the model (milisec): " + timeCreateModel);

		aux1 = System.currentTimeMillis();
	//	cm.classifyMedConfSet();
		long stop = System.currentTimeMillis();

		log.info("time for db read (milisec): " + timeDb);
		log.info("time to generate lowconf (milisec): " + timeGenerate);
		log.info("time for getting the attributes (milisec): " + timeAttribs);
		log.info("time for training the model (milisec): " + timeCreateModel);
		log.info("time for classifying the medconf set (milisec): " + (stop - aux1));
		log.info("total time in milisec: " + (stop - start));
	}
}
