/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.model.*;

import java.util.*;

/**
 * Generates the low confidence set from a given proteom filtering the ones in IntAct.
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version 1.0
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class InteractionGenerator {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log	log	= LogFactory.getLog(InteractionGenerator.class);
	
	private List<InteractionSimplified> highconf;
	private List<InteractionSimplified> lowconf;
	private List<InteractionSimplified> medconf;
	private Set<String> proteinACs;
	
	InteractionGenerator() {
	}

    @Deprecated
    public BinaryInteractionSet generate(Set<String> yeastProt, BinaryInteractionSet forbiddenBiSet, int nr){
		Collection<ProteinPair> generated = new ArrayList<ProteinPair>();

		Random random = new Random();

		int i = 0;
		while (i < nr) {
			int index1 = random.nextInt(yeastProt.size());
			int index2 = random.nextInt(yeastProt.size());

			// get uniprot ids
			String uniprotId1 = (String) (yeastProt.toArray())[index1];
			String uniprotId2 = (String) (yeastProt.toArray())[index2];

			if (validProteinPair(uniprotId1, uniprotId2, forbiddenBiSet)) {
				// create aprotein pair
				ProteinPair pp = new ProteinPair(uniprotId1, uniprotId2);
				if (!generated.contains( pp)){
                    generated.add(pp);
                    i++;
                }
			}
		}
	
		return new BinaryInteractionSet(generated);
	}

    public List<BinaryInteraction> generate(Set<Identifier> yeastProts, List<BinaryInteraction> forbidden, int nr){
        if (nr<=0){
            nr = forbidden.size();
        }
        List<Identifier> yeastProtList = new ArrayList<Identifier>(yeastProts);
        List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>(nr);
        Random random = new Random();

		int i = 0;
		while (i < nr) {
			int index1 = random.nextInt(yeastProtList.size());
			int index2 = random.nextInt(yeastProtList.size());

			// get uniprot ids
            Identifier uniprotId1 = yeastProtList.get( index1);
			Identifier uniprotId2 = yeastProtList.get(index2);

            BinaryInteraction auxBin = new BinaryInteraction(uniprotId1, uniprotId2, Confidence.UNKNOWN );
            if (!forbidden.contains(auxBin)) {
				interactions.add(auxBin);
				i++;
			}
		}

		return interactions;
    }

    private boolean validProteinPair(String uniprotId1, String uniprotId2, BinaryInteractionSet forbiddenBiSet) {
		for (ProteinPair pp : forbiddenBiSet.getSet()) {
			if ((pp.getFirstId().equals(uniprotId1) && pp.getSecondId().equals(uniprotId2)) ||
					(pp.getFirstId().equals(uniprotId2) && pp.getSecondId().equals(uniprotId1))){
				return false;
			}
		}
		return true;
	}

    @Deprecated
    public void setHighconfidence(List<InteractionSimplified> highconf2) {
		if (isValid(highconf2))
			this.highconf = highconf2;
		else
			throw new IllegalArgumentException();
	}

    @Deprecated
    public void setLowconfidence(List<InteractionSimplified> lowconfidence) {
		if (isValid(lowconfidence))
			this.lowconf = lowconfidence;
		else
			throw new IllegalArgumentException();
	}

    @Deprecated
    public void setMediumconfidence(List<InteractionSimplified> mediumconfidence) {
		if (isValid(mediumconfidence))
			this.medconf = mediumconfidence;
		else
			throw new IllegalArgumentException();
	}

    @Deprecated
    public void setProteinACs(Set<String> proteinACs) {
		this.proteinACs = proteinACs;
	}
	
	private boolean isValid(List<InteractionSimplified> interactList) {
		for (InteractionSimplified item : interactList) {
			if (!isBinary(item))
				return false;
		}
		return true;
	}

	private boolean isBinary(InteractionSimplified interaction) {

		if (interaction == null) {
			throw new IllegalArgumentException("Interaction must not be null.");
		}

		return interaction.getInteractors().size() == 2;
	}

    /**
     *
     * @param nr  : if nr<=0,  nr = high_confidences.size()
     * @return
     */
    @Deprecated
    public List<InteractionSimplified> generate(int nr) {
		if (nr<=0){
            nr = highconf.size();
        }
        List<InteractionSimplified> generated = new ArrayList<InteractionSimplified>(nr);

		Random random = new Random();

		int i = 0;
		while (i < nr) {
			int index1 = random.nextInt(proteinACs.size());
			int index2 = random.nextInt(proteinACs.size());

			// get uniprot ids
			String uniprotId1 = (String) (proteinACs.toArray())[index1];
			String uniprotId2 = (String) (proteinACs.toArray())[index2];

			if (validInteraction(uniprotId1, uniprotId2)) {
				// create interactor objects
				ProteinSimplified int1 = new ProteinSimplified(new UniprotAc(uniprotId1), "neutral");
				ProteinSimplified int2 = new ProteinSimplified(new UniprotAc(uniprotId2), "neutral");

				// create an interaction
				InteractionSimplified intAux = new InteractionSimplified("generated" + i, Arrays.asList(int1, int2));
				generated.add(intAux);
				i++;
			}
		}

		return generated;
	}

	private boolean validInteraction(String uniprotId1, String uniprotId2) {
		return !in(highconf, uniprotId1, uniprotId2) && !in(medconf, uniprotId1, uniprotId2)
				&& !in(lowconf, uniprotId1, uniprotId2);
	}

	private boolean in(List<InteractionSimplified> interactions, String uniprotId1, String uniprotId2) {
		for (InteractionSimplified item : interactions) {
			List<ProteinSimplified> comps = item.getInteractors();
			
			if (((ProteinSimplified) comps.toArray()[0]).getUniprotAc().getAcNr().equalsIgnoreCase(uniprotId1) && ((ProteinSimplified) comps.toArray()[1]).getUniprotAc().getAcNr().equalsIgnoreCase(uniprotId2) ||
				((ProteinSimplified) comps.toArray()[1]).getUniprotAc().getAcNr().equalsIgnoreCase(uniprotId1) && ((ProteinSimplified) comps.toArray()[0]).getUniprotAc().getAcNr().equalsIgnoreCase(uniprotId2))
				return true;

		}
		return false;
	}
}
