package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
        assertThat(titleForCurrentPage(), startsWith("Interaction: dre4-luc7"));
    }

    @Test
    public void linkToParticipants() throws Exception {
        // Given I am in the detailed page for experiment with label ren-2011-1
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // When I click on the label for interaction dre4-luc7
        clickOnLinkWithText("O59734");

        // Then I should navigate to the page for interaction with label dre4-luc7
        assertThat(titleForCurrentPage(), startsWith("Participant: O59734"));
    }
    
    @Test
    public void navigateToExperimentPage() throws Exception {
        // Given I want to go to the experiment page
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // When I click on the navigation element to the experiment page
        clickOnGoToExperimentPage();

        // Then I should go to the experiment page
        assertThat(titleForCurrentPage(), startsWith("Experiment: ren-2011-1"));
    }

    @Test
    public void interactionsNotPaginatedWhenLessThan50() throws Exception {
        // Given ren-2011-1 is an experiment with less than 50 interactions
        // And the default number of interactions per page is 50

        // When I am in the detailed page for that experiment
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // Then I whould see only 50 interactions
        assertThat(interactionsInThePage(), is(equalTo(3)));

        // And the paginator to navigate to other results
        assertFalse(paginatorIsPresent());
    }

    @Test
    public void interactionsArePaginatedWhenMoreThan50() throws Exception {
        // Given bigexp-2012-1 is an experiment with more than 50 interactions
        // And the default number of interactions per page is 50

        // When I am in the detailed page for that experiment
        goToExperimentDetailedViewPageFor("bigexp-2012-1");

        // Then I whould see only 50 interactions
        assertThat(interactionsInThePage(), is(equalTo(50)));

        // And the paginator to navigate to other results
        assertTrue(paginatorIsPresent());
    }

    @Test
    public void figureLegendDisplayed() throws Exception {
        // Given the figure legend should be displayed if present
        goToExperimentDetailedViewPageFor("ren-2011-1");

        // When I look at interaction with label dre4-luc7
        // Then the figure legend should be "Fig. 3"
        assertThat(figureLegendForInteraction("dre4-luc7"), is(equalTo("Fig. 3")));
    }

    @Test
    public void commentDisplayed() throws Exception {
        // Given comment should be displayed if present
        // When I look at interaction with label prp17-sap61
        // Then the comment should be "This interaction is very nice"
        assertThat(commentForInteraction("prp17-sap61"), is(equalTo("This interaction is very nice")));
    }

    @Test
    public void oneFeatureInParticipant() throws Exception {
        // Given a comma-separated list of features is shown for the participants
        // When I look at the participant Q09685 in interaction with label dre4-luc7
        // Then the features should be "region[?-?]"
        assertThat(featuresForParticipant("Q09685"), is(equalTo("region[?-?]")));
    }

    @Test
    public void multipleFeaturesInParticipants() throws Exception {
        // Given a comma-separated list of features is shown for the participants
        // When I look at the participant O13615 in interaction with label dre4-luc7
        // Then the features should be "mut[5-6], region[1-4]"
        assertThat(featuresForParticipant("O13615"), is(equalTo("mut[5-6], region[1-4]")));
    }

    @Test
    public void iconForLinkedFeature() throws Exception {
        // Given an icon is displayed for linked features
        // When I look at the features for participant O14011 in interaction with label dre4-luc7
        // Then the "linked-feature" icon should be displayed after the feature
        assertTrue(linkedFeatureIconIsPresentIn("014011"));
    }

    private void clickOnGoToExperimentPage() {
        driver.findElement(By.id("goBackExp")).click();
    }

    private int interactionsInThePage() {
        return driver.findElements(By.xpath("//div[@class='interaction-item']")).size();
    }

    private boolean paginatorIsPresent() {
        return isElementPresent(By.xpath("//div[@id='interactionDataList_paginator_top']"));
    }

    private String figureLegendForInteraction(String interactionLabel) {
        final WebElement element = driver.findElement(By.xpath("//span[@class='fig-legend-value "+interactionLabel+"']"));
        return element.getText();
    }

    private String commentForInteraction(String interactionLabel) {
        final WebElement element = driver.findElement(By.xpath("//span[@class='comment-value "+interactionLabel+"']"));
        return element.getText();
    }

    private String featuresForParticipant(String participantPrimaryId) {
        throw new UnsupportedOperationException();
    }

    private boolean linkedFeatureIconIsPresentIn(String participantPrimaryId) {
        throw new UnsupportedOperationException();
    }

    private void goToExperimentDetailedViewPageFor(String experimentLabel) {
        Experiment exp = getDaoFactory().getExperimentDao().getByShortLabel(experimentLabel);
        goToPageInContext("/expdetail/" + exp.getAc());

        loginAs("curator");
    }

    private void clickOnLinkWithText(String linkText) {
        driver.findElement(By.linkText(linkText)).click();
    }


}
