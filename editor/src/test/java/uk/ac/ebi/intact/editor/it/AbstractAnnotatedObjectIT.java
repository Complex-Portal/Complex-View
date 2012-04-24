package uk.ac.ebi.intact.editor.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public abstract class AbstractAnnotatedObjectIT extends EditorIT {

	protected abstract String getTabsComponentId();

    protected WebElement findTabsElement() {
       return driver.findElement(By.id(getTabsComponentId()));
    }

    protected void save() {
        driver.findElement(By.id("topSaveButton")).click();
        waitUntilLoadingIsComplete();
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
        waitUntilElementIsDisplayed(By.id(getTabsComponentId()+":newAnnotBtn"));
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
        waitUntilElementIsEnabled(By.id(getTabsComponentId()+":annotationsTable:" + rowIndex + ":annotationTopicSel"));
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
        waitUntilElementIsDisplayed(by);
        final WebElement element = driver.findElement(by);

        Select exportFormatSelect = new Select(element);
        return exportFormatSelect.getFirstSelectedOption().getText();
    }

    protected void createAnnotation(String topic, String text) {
        clickOnAnnotationsTab();
        waitUntilLoadingIsComplete();
        clickOnNewAnnotation();
        selectAnnotationTopicInRow(0, topic);
        typeAnnotationTextInRow(0, text);
    }

    private void typeAnnotationTextInRow(int rowIndex, String text) {
        driver.findElement(By.id(getTabsComponentId()+":annotationsTable:"+rowIndex+":annotationTxt")).sendKeys(text);
    }

    private void selectAnnotationTopicInRow(int rowIndex, String interactionType) {
        final By id = By.id(getTabsComponentId() + ":annotationsTable:" + rowIndex + ":annotationTopicSel");

        waitUntilElementIsPresent(id);

        Select select = new Select(driver.findElement(id));
        select.selectByVisibleText(interactionType);
    }

    protected void typeShortLabel(String label) {
        driver.findElement(By.id("shortlabelTxt")).sendKeys(label);
    }


}
