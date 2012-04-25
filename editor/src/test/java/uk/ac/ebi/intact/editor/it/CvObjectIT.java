package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import uk.ac.ebi.intact.model.CvObject;

import static junit.framework.Assert.assertTrue;

public class CvObjectIT extends AbstractAnnotatedObjectIT{

	@Test
	public void unsavePanelShouldBeVisibleAfterParentRemoved() throws Exception {
        CvObject comment = getDaoFactory().getCvObjectDao().getByShortLabel("comment");

        final String commentAc = comment.getAc();
        goToCvPage(commentAc);
        loginAs("curator");

        removeSixthParentFromPickList();
        waitUntilLoadingIsComplete();

        //waitUntilElementIsDisplayed(By.className("intact-notSavedPanel"));
        assertTrue(driver.findElement(By.className("intact-notSavedPanel")).isDisplayed());
	}

    /*@Test
    public void createNewCvAndAddParents(){

        goToCuratePage();

        loginAs("curator");

        createCvFromTopMenu("Feature type");

        typeShortLabel("test-comment-"+System.currentTimeMillis());
        
        addFirstParentFromPickList();

        save();

        assertTrue(infoMessageSummaryExists("Saved"));
    } */

    private void createCvFromTopMenu(String cvClass) {
        driver.findElement(By.cssSelector("span.ui-menuitem-text")).click();
        driver.findElement(By.cssSelector("#newCvObjectItem > span.ui-menuitem-text")).click();
        new Select(driver.findElement(By.id("j_id120_j_id_64:cvType"))).selectByVisibleText(cvClass);
        driver.findElement(By.id("j_id120_j_id_64:createCv")).click();
    }

    private void removeSixthParentFromPickList() {
        driver.findElement(By.xpath("//table[@id='parentsPick']/tbody/tr/td[3]/ul/li[6]")).click();
        driver.findElement(By.xpath("(//button[@type='button'])[4]")).click();
    }

    private void addFirstParentFromPickList() {
        driver.findElement(By.xpath("//table[@id='parentsPick']/tbody/tr/td/ul/li[4]")).click();
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
    }

    @Override
    protected String getTabsComponentId() {
        return "cvobjectTabs";
    }
}
