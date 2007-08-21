/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * TODO comment this ... someday
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class InteractionGenerator {
	private Collection<InteractionSimplified> highconf;
	private Collection<InteractionSimplified> lowconf;
	private Collection<InteractionSimplified> medconf;
	private Collection<String> proteinACs;

	InteractionGenerator() {
	}

	public void setHighconfidence(Collection<InteractionSimplified> highconf2) {
		if (isValid(highconf2))
			this.highconf = highconf2;
		else
			throw new IllegalArgumentException();
	}

	public void setLowconfidence(Collection<InteractionSimplified> lowconfidence) {
		if (isValid(lowconfidence))
			this.lowconf = lowconfidence;
		else
			throw new IllegalArgumentException();
	}

	public void setMediumconfidence(Collection<InteractionSimplified> mediumconfidence) {
		if (isValid(mediumconfidence))
			this.medconf = mediumconfidence;
		else
			throw new IllegalArgumentException();
	}

	public void setProteinACs(Collection<String> proteinACs) {
		this.proteinACs = proteinACs;
	}
	
	private boolean isValid(Collection<InteractionSimplified> interactList) {
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

		if (interaction.getInteractors().size() == 2) {
			return true;
		}

		return false;
	}

	public Collection<InteractionSimplified> generate(int nr) {
		Collection<InteractionSimplified> generated = new ArrayList<InteractionSimplified>();

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
				ProteinSimplified int1 = new ProteinSimplified(uniprotId1, "neutral");
				ProteinSimplified int2 = new ProteinSimplified(uniprotId2, "neutral");

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

	private boolean in(Collection<InteractionSimplified> interactions, String uniprotId1, String uniprotId2) {
		for (InteractionSimplified item : interactions) {
			Collection<ProteinSimplified> comps = item.getInteractors();
			
			if (((ProteinSimplified) comps.toArray()[0]).getUniprotAc().equals(uniprotId1) && ((ProteinSimplified) comps.toArray()[1]).getUniprotAc().equals(uniprotId2) ||
				((ProteinSimplified) comps.toArray()[1]).getUniprotAc().equals(uniprotId1) && ((ProteinSimplified) comps.toArray()[0]).getUniprotAc().equals(uniprotId2)	)
				return true;

		}
		return false;
	}
}
