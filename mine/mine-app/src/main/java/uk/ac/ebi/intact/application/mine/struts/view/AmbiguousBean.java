/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.struts.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import uk.ac.ebi.intact.application.mine.business.Constants;
import uk.ac.ebi.intact.model.*;

/**
 * The <tt>AmbiguousBean</tt> is used when the search of the MiNe application
 * returns an ambiguous result. It stores for each search class (
 * <tt>Protein</tt>,<tt>Interaction</tt> and <tt>Experiments</tt>) a
 * collection of the found results.
 * 
 * @author Andreas Groscurth
 */
public class AmbiguousBean {
	private static final String HELP_LINK = "/displayDoc.jsp?section=";
	private Collection<ProteinImpl> proteins = null;
	private Collection<InteractionImpl> interactions = null;
	private Collection<Experiment> experiments = null;
	private String searchAc;
	private String context;

	public int hashCode() {
		return searchAc.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AmbiguousBean)) {
			return false;
		}
		AmbiguousBean element = (AmbiguousBean) o;
		return element.searchAc.equals(searchAc);
	}

	/**
	 * @param searchAc The searchAc to set.
	 */
	public void setSearchAc(String searchAc) {
		this.searchAc = searchAc;
	}

	/**
	 * @return Returns the searchAc.
	 */
	public String getSearchAc() {
		return searchAc;
	}

	/**
	 * Returns the number of all found results. This is the sum of the size of
	 * all three collections.
	 * 
	 * @return the number of found results.
	 */
	public int getNumberOfResults() {
		int protSize = proteins == null ? 0 : proteins.size();
		int interSize = interactions == null ? 0 : interactions.size();
		int expSize = experiments == null ? 0 : experiments.size();
		return protSize + interSize + expSize;
	}

	/**
	 * Returns all found results which are an <tt>Experiment</tt>
	 * 
	 * @return Returns the experiments.
	 */
	public Collection<Experiment> getExperiments() {
		return experiments;
	}

	/**
	 * Sets the found results which are an <tt>Experiment</tt>
	 * 
	 * @param experiments The experiments to set.
	 */
	public void setExperiments(Collection<Experiment> experiments) {
		this.experiments = experiments;
	}

	/**
	 * Returns all found results which are an <tt>Interaction</tt>
	 * 
	 * @return Returns the interactions.
	 */
	public Collection<InteractionImpl> getInteractions() {
		return interactions;
	}

	/**
	 * Sets the found results which are an <tt>Interaction</tt>
	 * 
	 * @param interactions The interactions to set.
	 */
	public void setInteractions(Collection<InteractionImpl> interactions) {
		this.interactions = interactions;
	}

	/**
	 * Returns all found results which are a <tt>Protein</tt>
	 * 
	 * @return Returns the proteins.
	 */
	public Collection<ProteinImpl> getProteins() {
		return proteins;
	}

	/**
	 * Returns whether the search returned an ambigious result. This is the case
	 * when more than one protein was found or at least one interaction or on
	 * experiment.
	 * 
	 * @return whether an ambiguous result exists.
	 */
	public boolean hasAmbiguousResult() {
		// if an interaction was found we have an ambiguous result
		if (interactions != null && interactions.size() > 0) {
			return true;
		}

		// if an experiment was found we have an ambiguous result
		if (experiments != null && experiments.size() > 0) {
			return true;
		}

		// if more than one protein was found we have an ambiguous result
		return proteins != null && proteins.size() > 1;
	}

	/**
	 * Sets the found results which are a <tt>Protein</tt>
	 * 
	 * @param proteins The proteins to set.
	 */
	public void setProteins(Collection<ProteinImpl> proteins) {
		this.proteins = proteins;
	}

	/**
	 * Prints the results as html to the given writer.
	 * 
	 * @param out the writer to write html
	 * @param contextPath the contextpath of the application
	 */
	public void printHTML(Writer out, String contextPath) {
		try {
			out.write(searchAc + " returned<br>");
			context = contextPath;
			int interactorNumber = getNumberOfResults();
			// if no results where found
			if (interactorNumber == 0) {
				out.write("<i>no results</i><br>");
			}
			// if more results than allowed were found
			else if (interactorNumber > Constants.MAX_NUMBER_RESULTS) {
				out.write("<i>more than " + Constants.MAX_NUMBER_RESULTS);
				out.write(" results, please refine your query</i><br>");
			} else {
				out.write("<table width=\"100%\" bgcolor=\"#336666\">");
				Iterator iter;
				// all found proteins are displayed
				if (proteins != null) {
					for (iter = proteins.iterator(); iter.hasNext();) {
						printProtein(out, (Protein) iter.next());
					}
				}
				// all found interactions are displayed				
				if (interactions != null) {
					for (iter = interactions.iterator(); iter.hasNext();) {
						printInteraction(out, (Interaction) iter.next());
					}
				}
				if (experiments != null) {
					// all found eperiments are displayed
					for (iter = experiments.iterator(); iter.hasNext();) {
						printExperiment(out, (Experiment) iter.next());
					}
				}
				out.write("</table><br>\n");
			}
			out.write("<br>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints all results which are an <tt>Experimeriment</tt>. Each
	 * experiment is decomposed into its interaction which are then decomposed
	 * into their components.
	 * 
	 * @param out the writer
	 * @param exp the experiment to display
	 * @throws IOException when something failed with the writer
	 */
	private void printExperiment(Writer out, Experiment exp)
		throws IOException {
		Collection interactions = exp.getInteractions();
		// if the number of interactions is greater than allowed
		if (interactions.size() > Constants.MAX_INTERACTION_SIZE) {
			out.write(
				"<i>"
					+ exp.getAc()
					+ " has more than "
					+ Constants.MAX_INTERACTION_SIZE
					+ " number of interactions</i>");
			return;
		}

		// every interaction is displayed
		for (Iterator iter = interactions.iterator(); iter.hasNext();) {
			printInteraction(out, (Interaction) iter.next());
		}
	}

	/**
	 * Prints all results which are an <tt>Interaction</tt>. Each interaction
	 * is decomposed into its components.
	 * 
	 * @param out the writer
	 * @param in the interaction to display
	 * @throws IOException when something failed with the writer
	 */
	private void printInteraction(Writer out, Interaction in)
		throws IOException {
		Collection components = in.getComponents();
		// if an interaction has more than the allowed number of components
		if (components.size() > Constants.MAX_INTERACTION_SIZE) {
			out.write(
				"<i>"
					+ in.getAc()
					+ " has more than "
					+ Constants.MAX_INTERACTION_SIZE
					+ " number of interactors</i>");
			return;
		}
		Interactor inter;
		// every component is displayed
		for (Iterator iter = components.iterator(); iter.hasNext();) {
			inter = ((Component) iter.next()).getInteractor();
			if (inter instanceof Protein) {
				printProtein(out, (Protein) inter);
			} else if (inter instanceof Interaction) {
				printInteraction(out, (Interaction) inter);
			}
		}

	}

	/**
	 * Prints the information of a protein to the writer. The text is formatted
	 * via HTML.
	 * 
	 * @param out The writer
	 * @param prot the protein to display
	 * @throws IOException when something failed with the writer
	 */
	private void printProtein(Writer out, Protein prot) throws IOException {
		String ac = prot.getAc();

		out.write("<tr bgcolor=\"#eeeeee\">");
		out.write("<td class=\"objectClass\"><nobr>");
		// a checkbox is added so that the user can select the protein for the
		// algorithm
		out.write("<input type=\"checkbox\" name=\"" + ac + "\"");
		// if only one protein is given the checkbox is checked by default
		if (!hasAmbiguousResult()) {
			out.write(" checked");
		}
		out.write(">");

		out.write("<b>Protein</b>");
		out.write("<a href=\"" + context + HELP_LINK + "search.TableLayout\"");
		out.write(" target=\"new\"><sup><b><font color=\"red\">?</font>");
		out.write("</b></sup></a></nobr></td>");

		// the ac number is added
		out.write("<td><a href=\"" + context + HELP_LINK + "BasicObject.ac\"");
		out.write("target=\"new\">Ac:</a>" + ac + "</td>");

		// the shortlabel is added
		out.write(
			"<td><a href=\""
				+ context
				+ HELP_LINK
				+ "AnnotatedObject.shortLabel\"");
		out.write(" target=\"new\">Name:</a> ");
		out.write(
			"<a href=\""
				+ context
				+ "/search/do/search?searchString="
				+ ac
				+ "&searchClass=Protein\">");
		out.write("<b><i>" + prot.getShortLabel() + "</i></b></a></td>");
		out.write("<td>" + prot.getFullName() + "</td>");
		out.write("</tr>");
	}
}