package uk.ac.ebi.intact.view.webapp.it;

import org.junit.Test;
import org.openqa.selenium.By;
import uk.ac.ebi.intact.model.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DetailsIT extends IntactViewIT {

	@Test
	public void experimentShouldNotShowPublicationAnnotations() throws Exception {
        // Given I have an interaction whose experiments contains same annotations as the publication
        Interaction interaction = getMockBuilder().createInteractionRandomBinary();

        Experiment experiment = interaction.getExperiments().iterator().next();
        Publication publication = experiment.getPublication();

        addAnnotation(experiment, publication, "Cancer dataset", createTopic(CvTopic.DATASET_MI_REF, CvTopic.DATASET));

        getCorePersister().saveOrUpdate(interaction);

        // When: I go to the interaction details page
        goToInteractionDetailsPage(interaction.getAc());

        // Then: I don't expect to see those annotations in the experiment
        assertThat(driver.findElements(By.linkText(CvTopic.DATASET)).size(), is(equalTo(1)));
        assertThat(driver.findElement(By.id("mainPanels:j_id_18r:publicationAnnotTable:publicationAnnotTable:0:annotText")).getText(), is(equalTo("Cancer dataset")));
	}

    @Test
    public void publicationAnnotationsShouldNotRepeatWhenValuePresentInMainLabels() throws Exception {
        // Given I have an interaction from a publication with "dataset", "journal", "author-list", "publication year"
        Interaction interaction = getMockBuilder().createInteractionRandomBinary();

        Experiment experiment = interaction.getExperiments().iterator().next();
        Publication publication = experiment.getPublication();

        addAnnotation(experiment, publication, "Proteomics", createTopic(CvTopic.JOURNAL_MI_REF, CvTopic.JOURNAL));
        addAnnotation(experiment, publication, "2020", createTopic(CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR));
        addAnnotation(experiment, publication, "Peter, Kath", createTopic(CvTopic.AUTHOR_LIST_MI_REF, CvTopic.AUTHOR_LIST));
        addAnnotation(experiment, publication, "Apoptosis dataset", createTopic(CvTopic.DATASET_MI_REF, CvTopic.DATASET));

        getCorePersister().saveOrUpdate(interaction);

        // When: I go to the interaction details page
        goToInteractionDetailsPage(interaction.getAc());

        // Then: we expect only one row to be shown in the publication annotations - "journal", "author-list" and "publication year" should not be shown as they have their own labels
        assertThat(rowCountForDataTableWithId("mainPanels:j_id_18r:publicationAnnotTable:publicationAnnotTable"), is(equalTo(1)));
    }

    private CvTopic createTopic(String topicMi, String topicLabel) {
        return getMockBuilder().createCvObject(CvTopic.class, topicMi, topicLabel);
    }

    private void addAnnotation(Experiment experiment, Publication publication, String annotationText, CvTopic cvTopic) {
        Annotation journalAnnot1 = getMockBuilder().createAnnotation(annotationText, cvTopic);
        Annotation journalAnnot2 = getMockBuilder().createAnnotation(annotationText, cvTopic);
        experiment.addAnnotation(journalAnnot1);
        publication.addAnnotation(journalAnnot2);
    }
}
