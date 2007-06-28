package uk.ac.ebi.intact.webapp.search.business.interpro;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.webapp.search.business.Constants;

import java.io.Serializable;
import java.util.*;

/**
 * Provides methods to extract Protein-related information out of the Uniprot Database.
 *
 * @author Christian Kohler (ckohler@ebi.ac.uk)
 * @version $Id$
 */
public class InterproSearch implements Serializable {

    /**
     * Logger for that class.
     */
    protected transient static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    /**
     * Indicates the maximum number of selectable Proteins, forwarded to the InterPro Website and displayed
     * in the Waiting Message respectively. Default value is 20.
     */
    public static final int MAXIMUM_NUMBER_OF_SELECTED_PROTEINS = 20;


    /**
     * Finds all the XRefs-primary IDs of a specified Protein out of the UniProt Database.
     *
     * @param p                     <code>Protein</code> to get the UniprotKB ID from.
     * @param mappedProteins        stores the found UniProtKB IDs (as <code>String</code>) of Proteins.
     * @param unmappedProteins      stores those <code>Protein</code> Objects, where no UniProtKB ID could be found.
     * @param proteinsWithUniprotKB stores the shortlabel (value) and the corresponding UniProtKB ID (key)
     *                              of those Proteins where an UniProtKB ID is available.
     * @throws ThresholdExceededException if the maximum number of Proteins, which do have an UniProtKB ID, is exceeded.
     * @see ThresholdExceededException
     */
    public static void findUniprotId(Protein p,
                                     Set mappedProteins,
                                     Set unmappedProteins,
                                     Map proteinsWithUniprotKB) throws ThresholdExceededException {

        String uniprotId = IntactContext.getCurrentInstance().getDataContext()
            .getDaoFactory().getProteinDao().getUniprotAcByProteinAc(p.getAc());

        if (uniprotId == null) {
            unmappedProteins.add(p);

        } else {
            if ((mappedProteins.size() >= MAXIMUM_NUMBER_OF_SELECTED_PROTEINS) && (!mappedProteins.contains(uniprotId))) {
                throw new ThresholdExceededException();
            }
            //add uniprotKB of Protein to List
            mappedProteins.add(uniprotId);

            proteinsWithUniprotKB.put(p.getShortLabel(), uniprotId);
        }



        /**
         * the UniProtKB ID of the Protein
         *
        String uniProtId = null;


        //the UniProt DB Identifier
        CvDatabase uniprot = null;


         //the XRef qualifier
        CvXrefQualifier identity = null;

        IntactHelper helper = null;
        try {
            try {
                helper = new IntactHelper();

                uniprot = (CvDatabase) helper.getObjectByPrimaryId(CvDatabase.class, CvDatabase.UNIPROT_MI_REF);
                identity = (CvXrefQualifier) helper.getObjectByPrimaryId(CvXrefQualifier.class,
                                                                         CvXrefQualifier.IDENTITY_MI_REF);
            } finally {
                if (helper != null) {
                    helper.closeStore();
                }
            }
        } catch (IntactException e) {
            logger.error("Error while creating/closing IntactHelper", e);
        }

        if (uniprot == null) {
            logger.error("The CvDatabase( UniProtKB ) could not be found. Please check your CVs.");
            return; // exit the method
        }

        if (identity == null) {
            logger.error("The CvXrefQualifier( identity ) could not be found. Please check your CVs.");
            return; // exit the method
        }

        // we know uniprot and identity were both correctly initialized.
        boolean isFound = false;
        for (Iterator iterator1 = p.getXrefs().iterator(); iterator1.hasNext() && !isFound;) {
            Xref xref = (Xref) iterator1.next();

            // database of xref = UniProt DB
            if (uniprot.equals(xref.getCvDatabase())) {
                if (identity.equals(xref.getCvXrefQualifier())) {
                    uniProtId = xref.getPrimaryId();
                    proteinsWithUniprotKB.put(p.getShortLabel(), uniProtId);

                    // uniprotID for Protein was found
                    isFound = true;

                    // leave loop!
                    break;
                }
            }
        }

        // if no UniprotKB was found, add Protein Object to List
        if (!isFound) {
            unmappedProteins.add(p);

        } else {
            if ((mappedProteins.size() >= MAXIMUM_NUMBER_OF_SELECTED_PROTEINS) && (!mappedProteins.contains(uniProtId))) {
                throw new ThresholdExceededException();
            }
            //add uniprotKB of Protein to List
            mappedProteins.add(uniProtId);
        }
         */
    }
       
    /**
     * Searches for all Proteins being part of a specified <code>Interaction</code> i.
     *
     * @param i the <code>Interaction</code> to get all its participating Proteins of.
     * @return Collection containing all Proteins participating in Interaction i. If the returned
     * <code>Collection</code> contains no Element, a <code>NullPointerException</code> will be thrown.
     */
    public static Collection getProteins(Interaction i) {

        Collection components = i.getComponents();
        List proteins = new ArrayList(components.size());

        for (Iterator iterator = components.iterator(); iterator.hasNext();) {
            Component component = (Component) iterator.next();
            Interactor interactor = component.getInteractor();
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                proteins.add(protein);
            } else {
                logger.info("skip " + interactor.getShortLabel() + " as this is not a Protein "+
                 (" + interactor.getClass().getName() + "));
            }
        }

        return proteins;
    }

    /**
     * Searches for all Proteins being part of a specified Experiment e.
     *
     * @param e the <code>Experiment</code> to be searched for all its
     *          <code>Interactions</code>, including ALL <code>Proteins</code> of EACH <code>Interaction</code>.
     * @return Collection containing all Proteins participating in Experiment e. If the returned <code>Collection</code>
     * contains no Element, a <code>NullPointerException</code> will be thrown.
     */
    public static Collection getProteins(Experiment e) {

        List proteins = new ArrayList();

        for (Iterator iterator = e.getInteractions().iterator(); iterator.hasNext();) {
            Interaction interaction = (Interaction) iterator.next();

            // adds all found Protein in the global collection
            proteins.addAll(getProteins(interaction));
        }

        return proteins;
    }
}