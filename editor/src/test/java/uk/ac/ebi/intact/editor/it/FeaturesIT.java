package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FeaturesIT extends AbstractAnnotatedObjectIT {

    @Override
    protected String getTabsComponentId() {
        return "featureTabs";
    }

    @Test
	public void createNewFeatureWithRangeFromParticipantPage() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");
        Component component = experiment.getInteractions().iterator().next().getComponents().iterator().next();

        goToParticipantPage(component.getAc());
        loginAs("curator");

        clickOnNewFeature();
        typeShortLabel("featlala");
        selectType("protein modification");
        selectMethod("autoradiography");

        createNewRange("1-5");
        
        assertTrue(theValueForTheRangeInTheFirstRowIs("1-5"));

        save();

        assertTrue(infoMessageSummaryExists("Saved"));
        assertTrue(acFieldNotEmpty());
	}

    @Test
    public void invalidRangeShouldWarnUser() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");
        Component component = experiment.getInteractions().iterator().next().getComponents().iterator().next();

        goToParticipantPage(component.getAc());
        loginAs("curator");

        clickOnNewFeature();
        createNewRange("5000-5005");

        assertTrue(errorMessageSummaryExists("Range is not valid"));
    }

    private boolean acFieldNotEmpty() {
        return !valueForElement(By.id("acTxt")).isEmpty();
    }

    private boolean theValueForTheRangeInTheFirstRowIs(String range) {
        final By id = By.id("featureTabs:rangesTable:0:rangeTxt");
        waitUntilElementIsPresent(id);
        return range.equals(valueForElement(id));
    }

    private void createNewRange(String newRange) {
        driver.findElement(By.id("featureTabs:newRangeTxt")).sendKeys(newRange);
        driver.findElement(By.id("featureTabs:newRangeBtn")).click();
    }

    private void selectType(String featureType) {
        Select select = new Select(driver.findElement(By.id("featureTypeTxt:selectObject")));
        select.selectByVisibleText(featureType);
    }

    private void selectMethod(String detectionMethod) {
        Select select = new Select(driver.findElement(By.id("featureDetectionTxt:selectObject")));
        select.selectByVisibleText(detectionMethod);
    }

    private void clickOnNewFeature() {
        driver.findElement(By.id("participantTabs:newFeatureBtn")).click();
    }


}
