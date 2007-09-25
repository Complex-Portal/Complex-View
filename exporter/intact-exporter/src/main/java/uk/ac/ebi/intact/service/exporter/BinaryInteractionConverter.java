package uk.ac.ebi.intact.service.exporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.AuthorImpl;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionDetectionMethodImpl;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.tab.model.InteractionTypeImpl;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.OrganismImpl;
import psidev.psi.mi.xml.converter.ConverterException;


public class BinaryInteractionConverter {
	
	
	static final Log log = LogFactory.getLog(BinaryInteractionConverter.class);
	
	
	private Collection<Alias> convertWebAliases (List <uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias> webAliases){
		Collection<Alias> tabAliases = new ArrayList<Alias>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias webAlias : webAliases){
			Alias alias = new AliasImpl();
			
			if (webAlias.getName() != null){
				alias.setName(webAlias.getName());
			}
			
			if (webAlias.getDbSource() != null){
				alias.setDbSource(webAlias.getDbSource());
			}
			
			if (webAlias.getAliasType() != null){
				alias.setAliasType(webAlias.getAliasType());
			}
			
			tabAliases.add(alias);
		}
		
		return tabAliases;
	}

	private Organism convertWebOrganism(uk.ac.ebi.intact.binarysearch.wsclient.generated.Organism webOrganism) {
		Organism tabOrganism = new OrganismImpl();
		
		if (webOrganism.getIdentifiers() != null){
			Collection<CrossReference> identifiers = convertWebCrossReferences(webOrganism.getIdentifiers());
			tabOrganism.setIdentifiers(identifiers);
		}
		
		return tabOrganism;
	}

	private List<CrossReference> convertWebCrossReferences(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> webCrossReferences) {
		List<CrossReference> tabCrossReferences = new ArrayList<CrossReference>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference webCrossReference : webCrossReferences){
			
			CrossReference tabCrossReference = new CrossReferenceImpl();
			
			if(webCrossReference.getIdentifier() != null){
				tabCrossReference.setIdentifier(webCrossReference.getIdentifier());
			}
			
			if(webCrossReference.getDatabase() != null){
				tabCrossReference.setDatabase(webCrossReference.getDatabase());
			}
			
			if(webCrossReference.getText() != null){
				tabCrossReference.setText(webCrossReference.getText());
			}
			
			tabCrossReferences.add(tabCrossReference);
		}
		return	tabCrossReferences; 
	}
	
	private Interactor convertWebInteractor(uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor webInteractor){
		Interactor tabInteractor = new Interactor();
		
		if(webInteractor.getAliases() != null){
			Collection<Alias> aliases = convertWebAliases(webInteractor.getAliases());
			tabInteractor.setAliases(aliases);
		}
		
		if (webInteractor.getAlternativeIdentifiers() != null){
			Collection<CrossReference> alternativeIdentifiers = convertWebCrossReferences(webInteractor.getAlternativeIdentifiers());
			tabInteractor.setAlternativeIdentifiers(alternativeIdentifiers);
		}
		
		if (webInteractor.getIdentifiers() != null){
			Collection<CrossReference> identifiers = convertWebCrossReferences(webInteractor.getIdentifiers());
			tabInteractor.setIdentifiers(identifiers);
		}
		
		if (webInteractor.getOrganism() != null){
			Organism organism = convertWebOrganism(webInteractor.getOrganism());
			tabInteractor.setOrganism(organism);
		}
		
		
		return tabInteractor;
	}
	
	private List<Author> convertWebAuthors(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Author> webAuthors) {
		List<Author> tabAuthors = new ArrayList<Author>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.Author webAuthor : webAuthors){
			
			if(webAuthor.getName() != null){
				Author tabAuthor = new AuthorImpl();
				tabAuthor.setName(webAuthor.getName());	
				tabAuthors.add(tabAuthor);
			}			
		}
		
		return tabAuthors;
	}

	private List<Confidence> convertWebConfidence(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence> webConfidenceValues) {
		List<Confidence> tabConfidences = new ArrayList<Confidence>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence webConfidence : webConfidenceValues){
			Confidence tabConfidence = new ConfidenceImpl();
			
			if (webConfidence.getText() != null){
				tabConfidence.setText(webConfidence.getText());
			}
			
			if (webConfidence.getType() != null){
				tabConfidence.setType(webConfidence.getType());
			}
			
			if (webConfidence.getValue() != null){
				tabConfidence.setValue(webConfidence.getValue());
			}

			tabConfidences.add(tabConfidence);
		}
		
		return tabConfidences;
	}
	
	private List<InteractionDetectionMethod> convertWebDetectionMethodes(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod> webDetectionMethods) {
		
		List<InteractionDetectionMethod> tabDetectionMethods = new ArrayList<InteractionDetectionMethod>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod webDetectionMethod :webDetectionMethods){
			InteractionDetectionMethod tabDetectionMethod = new InteractionDetectionMethodImpl();
			
			if (webDetectionMethod.getIdentifier() != null){
				tabDetectionMethod.setIdentifier(webDetectionMethod.getIdentifier());
			}
			
			if (webDetectionMethod.getDatabase() != null){
				tabDetectionMethod.setDatabase(webDetectionMethod.getDatabase());
			}
			
			if (webDetectionMethod.getText() != null){
				tabDetectionMethod.setText(webDetectionMethod.getText());
			}
			
			tabDetectionMethods.add(tabDetectionMethod);
		}
		
		return tabDetectionMethods;
	}

	private List<InteractionType> convertWebInteractionType(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType> webInteractionTypes) {
		List<InteractionType> tabInteractionTypes = new ArrayList<InteractionType>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType webInteractionType : webInteractionTypes){
			InteractionType tabInteractionType = new InteractionTypeImpl();
			
			if (webInteractionType.getIdentifier() != null){
				tabInteractionType.setIdentifier(webInteractionType.getIdentifier());
			}
			
			if (webInteractionType.getDatabase() != null){
				tabInteractionType.setDatabase(webInteractionType.getDatabase());
			}
			
			if (webInteractionType.getText() != null){
				tabInteractionType.setText(webInteractionType.getText());
			}
			
			tabInteractionTypes.add(tabInteractionType);
			
		}
		
		return tabInteractionTypes;
	}

	
	public Collection<BinaryInteraction> convertWeb2Tab(List<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction> webInteractions)
			throws ConverterException{
		if (webInteractions == null){
			throw new ConverterException("You must give an valid Collection of uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction");
		}
		
		Collection<BinaryInteraction> tabBinaryInteractions = new ArrayList<BinaryInteraction>();
		
		for (uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction webInteraction : webInteractions){
			Interactor interactorA = convertWebInteractor(webInteraction.getInteractorA());
			Interactor interactorB = convertWebInteractor(webInteraction.getInteractorB());
			
			BinaryInteraction binaryInteraction = new BinaryInteractionImpl(interactorA, interactorB);
			
			if (!webInteraction.getAuthors().isEmpty()){
				List<Author> authors = convertWebAuthors(webInteraction.getAuthors());
				binaryInteraction.setAuthors(authors);
			}
			
			if (!webInteraction.getConfidenceValues().isEmpty()){
				List<Confidence> confidenceValues = convertWebConfidence(webInteraction.getConfidenceValues());
				binaryInteraction.setConfidenceValues(confidenceValues);
			}
			
			if (!webInteraction.getDetectionMethods().isEmpty()){
				List<InteractionDetectionMethod> detectionMethods = convertWebDetectionMethodes(webInteraction.getDetectionMethods());
				binaryInteraction.setDetectionMethods(detectionMethods);
			}
			
			if (!webInteraction.getInteractionAcs().isEmpty()){
				List<CrossReference> interactionAcs = convertWebCrossReferences(webInteraction.getInteractionAcs());
				binaryInteraction.setInteractionAcs(interactionAcs);
			}
			
			if (!webInteraction.getInteractionTypes().isEmpty()){
				List<InteractionType> interactionTypes = convertWebInteractionType(webInteraction.getInteractionTypes());
				binaryInteraction.setInteractionTypes(interactionTypes);
			}
			
			if (!webInteraction.getPublications().isEmpty()){
				List<CrossReference> publications = convertWebCrossReferences(webInteraction.getPublications());
				binaryInteraction.setPublications(publications);
			}
			
			if (!webInteraction.getSourceDatabases().isEmpty()){
				List<CrossReference> sourceDatabases = convertWebCrossReferences(webInteraction.getSourceDatabases());
				binaryInteraction.setSourceDatabases(sourceDatabases);
			}
			
			tabBinaryInteractions.add(binaryInteraction);
		}
		
		return tabBinaryInteractions;
	}

	
	

	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias> convertTabAliases (Collection <Alias> tabAliases){
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias> webAliases = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias>();
		
		for (Alias tabAlias : tabAliases){
			uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias alias = new uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias();
			
			if (tabAlias.getName() != null){
				alias.setName(tabAlias.getName());
			}
			
			if (tabAlias.getDbSource() != null){
				alias.setDbSource(tabAlias.getDbSource());
			}
			
			if (tabAlias.getAliasType() != null){
				alias.setAliasType(tabAlias.getAliasType());
			}
			
			webAliases.add(alias);
		}
		
		return webAliases;
	}

	private uk.ac.ebi.intact.binarysearch.wsclient.generated.Organism convertTabOrganism(Organism tabOrganism) {
		
		uk.ac.ebi.intact.binarysearch.wsclient.generated.Organism webOrganism = new uk.ac.ebi.intact.binarysearch.wsclient.generated.Organism();
		
		if (tabOrganism.getIdentifiers() != null){
			List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> identifiers = convertTabCrossReferences(tabOrganism.getIdentifiers());
			if (!webOrganism.getIdentifiers().addAll(identifiers)){
				log.warn("Could not add Identifiers to Organsim.");
			}
		}
		
		return webOrganism;
	}

	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> convertTabCrossReferences(Collection<CrossReference> tabCrossReferences) {
		
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> webCrossReferences = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference>();
		
		for (CrossReference tabCrossReference : tabCrossReferences){
			
			uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference webCrossReference = new uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference();
			
			if(tabCrossReference.getIdentifier() != null){
				webCrossReference.setIdentifier(tabCrossReference.getIdentifier());
			}
			
			if(tabCrossReference.getDatabase() != null){
				webCrossReference.setDatabase(tabCrossReference.getDatabase());
			}
			
			if(tabCrossReference.getText() != null){
				webCrossReference.setText(tabCrossReference.getText());
			}
			
			webCrossReferences.add(webCrossReference);
		}
		return	webCrossReferences; 
	}
	
	private uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor convertTabInteractor(Interactor tabInteractor){
		uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor webInteractor = new uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor();
		
		if(tabInteractor.getAliases() != null){
			List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Alias> aliases = convertTabAliases(tabInteractor.getAliases());
			if (!webInteractor.getAliases().addAll(aliases)){
				log.warn("Aliases could not add to the Interactor.");
			}
		}
		
		if (tabInteractor.getAlternativeIdentifiers() != null){
			List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> alternativeIdentifiers = convertTabCrossReferences(tabInteractor.getAlternativeIdentifiers());
			if (!webInteractor.getAlternativeIdentifiers().addAll(alternativeIdentifiers)){
				log.warn("AlternativeIdentifiers could not add to the Interactor.");
			}	
		}
		
		if (tabInteractor.getIdentifiers() != null){
			List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> identifiers = convertTabCrossReferences(tabInteractor.getIdentifiers());
			if (!webInteractor.getIdentifiers().addAll(identifiers)){
				log.warn("Identifiers could not add to the Interactor.");
			}	
		}
		
		if (tabInteractor.getOrganism() != null){
			uk.ac.ebi.intact.binarysearch.wsclient.generated.Organism organism = convertTabOrganism(tabInteractor.getOrganism());
			webInteractor.setOrganism(organism);
		}
		
		
		return webInteractor;
	}
	
	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Author> convertTabAuthors(Collection<Author> tabAuthors) {
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Author> webAuthors = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.Author>();
		
		for (Author tabAuthor : tabAuthors){
			
			if(tabAuthor.getName() != null){
				uk.ac.ebi.intact.binarysearch.wsclient.generated.Author webAuthor = new uk.ac.ebi.intact.binarysearch.wsclient.generated.Author();
				webAuthor.setName(tabAuthor.getName());	
				webAuthors.add(webAuthor);
			}			
		}
		
		return webAuthors;
	}

	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence> convertTabConfidence(Collection<Confidence> tabConfidenceValues) {
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence> webConfidences = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence>();
		
		for (Confidence tabConfidence : tabConfidenceValues){
			uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence webConfidence = new uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence();
			
			if (tabConfidence.getText() != null){
				webConfidence.setText(tabConfidence.getText());
			}
			
			if (tabConfidence.getType() != null){
				webConfidence.setType(tabConfidence.getType());
			}
			
			if (tabConfidence.getValue() != null){
				webConfidence.setValue(tabConfidence.getValue());
			}

			webConfidences.add(webConfidence);
		}
		
		return webConfidences;
	}
	
	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod> convertTabDetectionMethodes(Collection<InteractionDetectionMethod> tabDetectionMethods) {
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod> webDetectionMethods = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod>();
		
		for (InteractionDetectionMethod tabDetectionMethod :tabDetectionMethods){
			uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod webDetectionMethod = new uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod();
			
			if (tabDetectionMethod.getIdentifier() != null){
				webDetectionMethod.setIdentifier(tabDetectionMethod.getIdentifier());
			}
			
			if (tabDetectionMethod.getDatabase() != null){
				webDetectionMethod.setDatabase(tabDetectionMethod.getDatabase());
			}
			
			if (tabDetectionMethod.getText() != null){
				webDetectionMethod.setText(tabDetectionMethod.getText());
			}
			
			webDetectionMethods.add(webDetectionMethod);
		}
		
		return webDetectionMethods;
	}

	private List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType> convertTabInteractionType(Collection<InteractionType> tabInteractionTypes) {
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType> webInteractionTypes = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType>();
		
		for (InteractionType tabInteractionType : tabInteractionTypes){
			uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType webInteractionType = new uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType();
			
			if (tabInteractionType.getIdentifier() != null){
				webInteractionType.setIdentifier(tabInteractionType.getIdentifier());
			}
			
			if (tabInteractionType.getDatabase() != null){
				webInteractionType.setDatabase(tabInteractionType.getDatabase());
			}
			
			if (tabInteractionType.getText() != null){
				webInteractionType.setText(tabInteractionType.getText());
			}
			
			webInteractionTypes.add(webInteractionType);
			
		}
		
		return webInteractionTypes;
	}

	
	public List<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction> convertTab2Web (Collection<BinaryInteraction> tabInteractions) throws ConverterException{
		
		if (tabInteractions == null){
			throw new ConverterException("You must give an valid Collection of psidev.psi.mi.tab.model.BinaryInteraction");
		}
		
		List<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction> webBinaryInteractions = new ArrayList<uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction>();
		
		for (BinaryInteraction tabInteraction : tabInteractions){
			
			uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction binaryInteraction = new uk.ac.ebi.intact.binarysearch.wsclient.generated.BinaryInteraction();
			
			if (tabInteraction.getInteractorA() != null){
				uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor interactorA = convertTabInteractor(tabInteraction.getInteractorA());
				binaryInteraction.setInteractorA(interactorA);
			}
			
			if (tabInteraction.getInteractorB() != null){
				uk.ac.ebi.intact.binarysearch.wsclient.generated.Interactor interactorB = convertTabInteractor(tabInteraction.getInteractorB());
				binaryInteraction.setInteractorB(interactorB);
			}
			
			if (!tabInteraction.getAuthors().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Author> authors = convertTabAuthors(tabInteraction.getAuthors());
				if (!binaryInteraction.getAuthors().addAll(authors)){
					log.warn("Authors could not add to Interaction.");
				}
			}
			
			if (!tabInteraction.getConfidenceValues().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.Confidence> confidenceValues = convertTabConfidence(tabInteraction.getConfidenceValues());
				if (!binaryInteraction.getConfidenceValues().addAll(confidenceValues)){
					log.warn("ConfidenceValues could not add to Interaction.");
				}
			}
			
			if (!tabInteraction.getDetectionMethods().isEmpty()){
			List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionDetectionMethod> detectionMethods = convertTabDetectionMethodes(tabInteraction.getDetectionMethods());
				if (!binaryInteraction.getDetectionMethods().addAll(detectionMethods)){
					log.warn("ConfidenceValues could not add to Interaction.");
				}
			}
			
			if (!tabInteraction.getInteractionAcs().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> interactionAcs = convertTabCrossReferences(tabInteraction.getInteractionAcs());
				if (!binaryInteraction.getInteractionAcs().addAll(interactionAcs)){
					log.warn("InteractionAcs could not add to Interaction");
				}
			}
			
			if (!tabInteraction.getInteractionTypes().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.InteractionType> interactionTypes = convertTabInteractionType(tabInteraction.getInteractionTypes());
				if (!binaryInteraction.getInteractionTypes().addAll(interactionTypes)){
					log.warn("InteractionTypes could not add to Interaction");
				}
			}
			
			if (!tabInteraction.getPublications().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> publications = convertTabCrossReferences(tabInteraction.getPublications());
				if (!binaryInteraction.getPublications().addAll(publications)){
					log.warn("Publications could not add to Interaction");
				}
			}
			
			if (!tabInteraction.getSourceDatabases().isEmpty()){
				List<uk.ac.ebi.intact.binarysearch.wsclient.generated.CrossReference> sourceDatabases = convertTabCrossReferences(tabInteraction.getSourceDatabases());
				if (!binaryInteraction.getSourceDatabases().addAll(sourceDatabases)){
					log.warn("SourceDatabases could not add to Interaction");
				}
			}
			
			webBinaryInteractions.add(binaryInteraction);
		}
		
		return webBinaryInteractions;
	}
}
