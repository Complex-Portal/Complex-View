package uk.ac.ebi.intact.editor.it;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/12/11</pre>
 */

public class ExperimentDetailedViewIT extends EditorIT {

    @Test
    public void linkToInteractions() throws Exception {
        // Given I am in the detailed page for experiment with label ren-2011-1
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // When I click on the label for interaction dre4-luc7
        clickOnLinkWithText("dre4-luc7");

        // Then I should navigate to the page for interaction with label dre4-luc7
        assertThat(titleForCurrentPage(), contains("Interaction: dre4-luc7"));
    }

    @Test
    public void linkToParticipants() throws Exception {
        // Given I am in the detailed page for experiment with label ren-2011-1
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // When I click on the label for interaction dre4-luc7
        clickOnLinkWithText("O59734");

        // Then I should navigate to the page for interaction with label dre4-luc7
        assertThat(titleForCurrentPage(), contains("Participant: O59734"));
    }

    @Test
    public void interactionsArePaginated() throws Exception {
        // Given bigexp-2012-1 is an experiment with 60 interactions
        // And the default number of interactions per page is 50
        
        // When I am in the detailed page for that experiment
        goToExperimentDetailedViewPageFor("bigexp-2012-1");

        // Then I whould see only 50 interactions
        assertThat(interactionsInThePage(), is(equalTo(50)));

        // And the paginator to navigate to other results
        assertTrue(paginatorIsPresent());
    }

    private int interactionsInThePage() {
        return 0;
    }

    private boolean paginatorIsPresent() {
        return false;
    }

    private Matcher<String> contains(final String substring) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().contains(substring);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains: ");
            }
        };
    }

    private void goToExperimentDetailedViewPageFor(String experimentLabel) {
        Experiment exp = getDaoFactory().getExperimentDao().getByShortLabel(experimentLabel);
        goToPageInContext("/experiment/"+exp.getAc());

        loginAs("curator", driver);
    }

    private void clickOnLinkWithText(String linkText) {

    }


}
