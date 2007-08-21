/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 16 Aug 2007
 * </pre>
 */
public class IntActDb {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log log = LogFactory.getLog(IntActDb.class);
	private Collection<InteractionSimplified> highConfidenceSet;
	private Collection<InteractionSimplified> lowConfidenceSet; // TODO: get a
																// query for the
																// low
																// confidence
																// set

	private DaoFactory daoFactory;

	public IntActDb() {
		daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
	}

	public Collection<InteractionSimplified> getHighconfidenceSet() {
		return highConfidenceSet;
	}
	
	/**
	 * reads only the protein information(uniprotAc and sequence and role) out
	 * of the DB
	 * 
	 * @param Collection
	 *            <String> uniprotACs
	 * @return Collection<ProteinSimplified>
	 */
	public Collection<ProteinSimplified> readSeq(Collection<String> uniportAcs) {
		Collection<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();
		ProteinDao proteinDao = daoFactory.getProteinDao();
		for (String ac : uniportAcs) {
			Protein protein = proteinDao.getByAc(ac);
			ProteinSimplified proteinS = saveProteinInformation(protein);
			proteins.add(proteinS);
		}

		closeDb();

		return proteins;
	}

	/**
	 * saves out of DB the interactionAc, and components -uniprotAc into
	 * InteractionSimplified structures
	 * 
	 * @param Collection
	 *            <String> ebiACs
	 * @return Collection<InteractionSimplified>
	 */
	public Collection<InteractionSimplified> read(Collection<String> ebiACs) {
		Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();
		InteractionDao interactionDao = daoFactory.getInteractionDao();
		for (String ac : ebiACs) {
			Interaction interaction = interactionDao.getByAc(ac);
			if (interaction != null) {
				InteractionSimplified intS = saveInteractionInformation(interaction);
				log.info("read: " + intS.getAc());
				interactions.add(intS);
			} else
				log.debug("interaction accession nr not found : " + ac);
		}

		closeDb();

		return interactions;
	}

	/**
	 * retrieves the medium confidence set, which is defined by all the
	 * interactions in IntAct which do not belong to the high confidence set, or
	 * the low confidence set (sets defined through curators)
	 * 
	 * @return Collection<InteractionSimplified>
	 */
	public Collection<InteractionSimplified> readMediumConfidenceSet() {
		highConfidenceSet = new ArrayList<InteractionSimplified>();
		Collection<InteractionSimplified> medconf = new ArrayList<InteractionSimplified>();

		InteractionDao interactionDao = daoFactory.getInteractionDao();

		int totalNr = interactionDao.countAll();
		System.out.println("nr total: " + totalNr);
	//	totalNr = 10; // TODO: remove after test
		for (int i = 0; i < totalNr; i += 50) {
			Collection<InteractionImpl> interactionsAll = interactionDao.getAll(i, 50);

			for (InteractionImpl interaction : interactionsAll) {
				if (isInteractionEligible(interaction)) {

					if (isHighConfidenceOrComplexes(interaction) || isEnzymeOrFluorescenceRole(interaction)) {
						highConfidenceSet.add(saveInteractionInformation(interaction));
					} else {
						medconf.add(saveInteractionInformation(interaction));
					}

				}
			}
		}

		return medconf;
	}

	/**
	 * checks if the interaction contains at least 2 proteins from uniprot
	 * 
	 * @param interaction
	 * @return true or false
	 */
	private boolean isInteractionEligible(InteractionImpl interaction) {
		int nr = 0;

		for (Component comp : interaction.getComponents()) {
			Interactor interactor = comp.getInteractor();
			if (Protein.class.isAssignableFrom(interactor.getClass())
					&& ProteinUtils.isFromUniprot((Protein) interactor)) {
				nr += 1;
			}
			if (nr == 2) {
				return true;
			}
		}

		return false;
	}

	/**
	 * checks if the interaction belongs to a complex curated or if the author
	 * confidence is high
	 * 
	 * @param interaction
	 * @return true or false
	 */
	private boolean isHighConfidenceOrComplexes(InteractionImpl interaction) {
		for (Annotation item : interaction.getAnnotations()) {
			CvTopic authorConf = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvTopic.class,
					CvTopic.AUTHOR_CONFIDENCE_MI_REF);
			CvTopic curatedComplex = IntactContext.getCurrentInstance().getCvContext().getByLabel(CvTopic.class,
					CvTopic.CURATED_COMPLEX);
			String authorConfDesc = "high";
			if (curatedComplex.equals(item.getCvTopic())) {
				return true;
			}
			if (authorConf.equals(item.getCvTopic()) && authorConfDesc.equals(item.getAnnotationText())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * checks if the interaction contains enzymes, enzymes targets, fluorescence
	 * acceptors / donors
	 * 
	 * @param interaction
	 * @return true or false
	 */
	private boolean isEnzymeOrFluorescenceRole(InteractionImpl interaction) {
		CvBiologicalRole enzymeRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
				CvBiologicalRole.class, CvBiologicalRole.ENZYME_PSI_REF);
		CvBiologicalRole enzymeTargetRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
				CvBiologicalRole.class, CvBiologicalRole.ENZYME_TARGET_PSI_REF);
		CvExperimentalRole fluorescenceAcceptorRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
				CvExperimentalRole.class, CvExperimentalRole.FLUROPHORE_ACCEPTOR_MI_REF);
		CvExperimentalRole fluorescenceDonorRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
				CvExperimentalRole.class, CvExperimentalRole.FLUROPHORE_DONOR_MI_REF);

		int enzymeNr = 0;
		int enzymeTargetNr = 0;
		int fluorescenceAcceptorNr = 0;
		int fluorescenceDonorNr = 0;

		for (Component component : interaction.getComponents()) {
			if (enzymeRole.equals(component.getCvBiologicalRole())) {
				enzymeNr++;
			}
			if (enzymeTargetRole.equals(component.getCvBiologicalRole())) {
				enzymeTargetNr++;
			}
			if (fluorescenceAcceptorRole.equals(component.getCvExperimentalRole())) {
				fluorescenceAcceptorNr++;
			}
			if (fluorescenceDonorRole.equals(component.getCvExperimentalRole())) {
				fluorescenceDonorNr++;
			}
		}

		// TODO: ask if it is possible to have an interaction between an enzyme
		// and another protein (but not a target enzyme), if yes, are these considered as high confidence?
		if (enzymeNr + enzymeTargetNr >= 2) {
			return true;
		}
		if (fluorescenceAcceptorNr + fluorescenceDonorNr >= 2) {
			return true;
		}

		return false;
	}

	/**
	 * saves the interaction information: interactionAc, the protein, the
	 * proteins role into an InteractionSimplified object
	 * 
	 * @param interaction
	 * @return InteractionSimplified
	 */
	private InteractionSimplified saveInteractionInformation(Interaction interaction) {
		InteractionSimplified interactionS = new InteractionSimplified();

		interactionS.setAc(interaction.getAc());

		Collection<Component> components = interaction.getComponents();
		Collection<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();
		for (Component comp : components) {
			Interactor interactor = comp.getInteractor();

			String role = "neutral";
			CvExperimentalRole expRole = comp.getCvExperimentalRole();
			CvObjectXref psiMiXref = CvObjectUtils.getPsiMiIdentityXref(expRole);
			if (CvExperimentalRole.BAIT_PSI_REF.equals(psiMiXref.getPrimaryId())) {
				role = "bait";
			}
			if (CvExperimentalRole.PREY_PSI_REF.equals(psiMiXref.getPrimaryId())) {
				role = "prey";
			}

			// this is because an interactor could be a small molecule, you want
			// to make sure you have a protein
			if (Protein.class.isAssignableFrom(interactor.getClass())) {
				ProteinSimplified protein = saveProteinInformation((Protein) interactor);
				protein.setRole(role);
				proteins.add(protein);
			}
		}
		interactionS.setInteractors(proteins);

		return interactionS;
	}

	/**
	 * saves the uniprotAc and the sequence into the new protein object
	 * 
	 * @param protein
	 * @return ProteinSimplified object
	 */
	private ProteinSimplified saveProteinInformation(Protein protein) {
		ProteinSimplified proteinS = new ProteinSimplified();
		InteractorXref uniprotXref = ProteinUtils.getUniprotXref(protein);
		if (uniprotXref != null)
			proteinS.setUniprotAc(uniprotXref.getPrimaryId());
		else
			proteinS.setUniprotAc("null");
		proteinS.setSeq(protein.getSequence());

		return proteinS;
	}

	private void closeDb() {
		try {
			IntactContext.getCurrentInstance().getDataContext().commitTransaction();
		} catch (IntactTransactionException e) {
			// If committing the transaction failed (ex : a shortlabel was
			// longer
			// then 20 characters), try to rollback.
			try {
				IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCurrentTransaction().rollback();
			} catch (IntactTransactionException e1) {
				// If rollback was not successfull do what you want :
				// printStackTrace, throw Exception...
				throw new IntactException("Problem at commit time, couldn't rollback : " + e1);
			}
			// If commit is it could not commit do what you want :
			// printStackTrace, throw Exception...
			throw new IntactException("Problem at commit time, rollback done : " + e);
		} finally {
			// Commiting the transaction close as well the session if everything
			// goes fine but in case of an exception
			// sent at commit time then the session would not be closed, so it's
			// really important that you close it here
			// otherwise you might get again this fishy connection and have very
			// nasty bugs.
			Session hibernateSession = getSession();
			if (hibernateSession.isOpen()) {
				hibernateSession.close();
			}
		}
	}

	private static Session getSession() {
		AbstractHibernateDataConfig abstractHibernateDataConfig = (AbstractHibernateDataConfig) IntactContext
				.getCurrentInstance().getConfig().getDefaultDataConfig();
		SessionFactory factory = abstractHibernateDataConfig.getSessionFactory();
		return factory.getCurrentSession();
	}


}
