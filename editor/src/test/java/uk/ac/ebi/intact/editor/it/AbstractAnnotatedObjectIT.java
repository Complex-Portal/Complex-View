package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractAnnotatedObjectIT extends EditorIT {

	protected abstract String getTabsComponentId();

    protected WebElement findTabsElement() {
       return driver.findElement(By.id(getTabsComponentId()));
    }

    protected void clickOnAnnotationsTab() {
        findTabsElement().findElement(By.partialLinkText("Annotations (")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnXrefsTab() {
        findTabsElement().findElement(By.partialLinkText("Xrefs (")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnAliasesTab() {
        findTabsElement().findElement(By.partialLinkText("Aliases (")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnNewAnnotation() {
        driver.findElement(By.id(getTabsComponentId()+":newAnnotBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnNewXref() {
        driver.findElement(By.id(getTabsComponentId()+":newXrefBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnNewAlias() {
        driver.findElement(By.id(getTabsComponentId()+":newAliasBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected String firstRowAnnotationText() {
        return textInAnnotationRow(0);
    }

    protected String secondRowAnnotationText() {
        return textInAnnotationRow(1);
    }

    protected String textInAnnotationRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":annotationsTable:"+rowIndex+":annotationTxt"));
    }

    protected String annotationTopicSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId()+":annotationsTable:" + rowIndex + ":annotationTopicSel"));
    }

    protected String identifierInXrefsRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":xrefsTable:"+rowIndex+":primaryIdTxt"));
    }

    protected String secondaryIdentifierInXrefsRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":xrefsTable:"+rowIndex+":secondaryTxt"));
    }

    protected String databaseSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":xrefsTable:" + rowIndex + ":databaseSel"));
    }

    protected String qualifierSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":xrefsTable:" + rowIndex + ":qualifierSel"));
    }

    protected String aliasNameInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":aliasTable:"+rowIndex+":aliasNameTxt"));
    }

    protected String aliasTypeSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":aliasTable:" + rowIndex + ":aliasTypeSel"));
    }

    protected String valueForSelect(By by) {
        final WebElement element = driver.findElement(by);

        Select exportFormatSelect = new Select(element);
        return exportFormatSelect.getFirstSelectedOption().getText();
    }


}
