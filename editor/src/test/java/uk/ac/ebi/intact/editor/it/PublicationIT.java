package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tester of publication features
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/04/12</pre>
 */

public class PublicationIT extends AbstractAnnotatedObjectIT  {

    @Autowired
    private LifecycleManager lifecycleManager;

    @Test
    public void select_imex_curation_depth_show_imex_button(){
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // show IMEx button
        goToPublicationWithId(experiment.getPublication().getPublicationId());
        loginAs("curator");
        new Select(driver.findElement(By.id("selectCurationDepth"))).selectByVisibleText("IMEx");
        driver.findElement(By.cssSelector("option[value=\"imex curation\"]")).click();
        waitUntilElementIsDisplayed(By.id("unsavedSaveButton"));
        assertTrue(driver.findElement(By.id("assignImexBtn")).isDisplayed());

        driver.findElement(By.id("unsavedSaveButton")).click();

        waitUntilLoadingIsComplete();

        // hide IMEx button
        new Select(driver.findElement(By.id("selectCurationDepth"))).selectByVisibleText("MIMIx");
        driver.findElement(By.cssSelector("option[value=\"mimix curation\"]")).click();
        waitUntilElementIsDisplayed(By.id("unsavedSaveButton"));

        try {
            WebElement webel = driver.findElement(By.id("assignImexBtn"));
            assertNull(webel);
        } catch (org.openqa.selenium.NoSuchElementException ex) {
            /* do nothing, button is not present, assert is passed */
        }

        driver.findElement(By.id("unsavedSaveButton")).click();

        waitUntilLoadingIsComplete();
    }

    @Test
    public void select_imex_curation_depth_unassigned_show_button(){
        // Create a user "mike" with a random publication that he owns
        Publication pubUnassigned = getMockBuilder().createPublication("unassigned604");
        lifecycleManager.getNewStatus().assignToCurator(pubUnassigned, getUserByLogin("curator"));
        lifecycleManager.getAssignedStatus().startCuration(pubUnassigned);
        getCorePersister().saveOrUpdate(pubUnassigned);

        // show IMEx button
        goToPublicationWithId("unassigned604");
        loginAs("curator");
        new Select(driver.findElement(By.id("selectCurationDepth"))).selectByVisibleText("IMEx");
        driver.findElement(By.cssSelector("option[value=\"imex curation\"]")).click();
        waitUntilElementIsDisplayed(By.id("unsavedSaveButton"));
        assertTrue(driver.findElement(By.id("assignImexBtn")).isDisplayed());

        driver.findElement(By.id("unsavedSaveButton")).click();

        waitUntilLoadingIsComplete();

        // hide IMEx button
        new Select(driver.findElement(By.id("selectCurationDepth"))).selectByVisibleText("MIMIx");
        driver.findElement(By.cssSelector("option[value=\"mimix curation\"]")).click();
        waitUntilElementIsDisplayed(By.id("unsavedSaveButton"));

        try {
            WebElement webel = driver.findElement(By.id("assignImexBtn"));
            assertNull(webel);
        } catch (org.openqa.selenium.NoSuchElementException ex) {
            /* do nothing, button is not present, assert is passed */
        }

        driver.findElement(By.id("unsavedSaveButton")).click();

        waitUntilLoadingIsComplete();

    }

    /*@Test
    public void assignNewImex(){
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // show IMEx button
        goToPublicationWithId(experiment.getPublication().getPublicationId());
        loginAs("curator");
        new Select(driver.findElement(By.id("selectCurationDepth"))).selectByVisibleText("IMEx");
        driver.findElement(By.cssSelector("option[value=\"imex curation\"]")).click();
        waitUntilElementIsDisplayed(By.id("unsavedSaveButton"));
        driver.findElement(By.id("assignImexBtn")).click();

        waitUntilElementHasValue(By.id("imexIdTxt"), "IM-1");
    }*/

    @Override
    protected String getTabsComponentId() {
        return "publicationTabs";
    }
}
