package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import uk.ac.ebi.intact.model.Experiment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InteractionsIT extends AbstractAnnotatedObjectIT {

    @Override
    protected String getTabsComponentId() {
        return "interactionTabs";
    }

	@Test
	public void importProteinFromUniprotSuccessfully() throws Exception {
        Experiment experiment = getDaoFactory().getExperimentDao().getByShortLabel("bigexp-2012-1");

        // Given: I want to import a participant from uniprot in the interaction page
        final String interactionAc = experiment.getInteractions().iterator().next().getAc();
        goToInteractionPage(interactionAc);
        loginAs("curator");

        // When: I import P12345 using the Import... dialog
        showImportParticipantsDialog();
        searchParticipantsUsing("P12345");
        importSelectedByDefault();

        // Then: the first participant in the table should be the new P12345
        assertThat(identityForParticipantInFirstRow(), is(equalTo("P12345")));
	}
    
    @Test
    public void updateFigLegendShouldUpdateAnnotations() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAnnotationsTab();
        typeFigureLegend(" - lala");

        assertThat(secondRowAnnotationText(), is(equalTo("Fig 3 - lala")));
    }

    @Test
    public void updateAnnotationShouldUpdateFigLegend() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAnnotationsTab();
        typeSecondRowAnnotationText(" - lolo");

        assertThat(annotationTopicSelectedInRow(1), is(equalTo("figure legend")));
        assertThat(figureLegendText(), is(equalTo("Fig 3 - lolo")));
    }

    @Test
    public void newXrefShouldDisplayNewEmptyFieldOnTop() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnXrefsTab();
        clickOnNewXref();

        assertThat(identifierInXrefsRow(0), is(equalTo("")));
        assertThat(secondaryIdentifierInXrefsRow(0), is(equalTo("")));
        assertThat(databaseSelectedInRow(0), is(equalTo("-- Select database --")));
        assertThat(qualifierSelectedInRow(0), is(equalTo("-- Select qualifier --")));
    }

    @Test
    public void newAnnotationShouldDisplayNewEmptyFieldOnTop() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAnnotationsTab();
        clickOnNewAnnotation();

        assertThat(firstRowAnnotationText(), is(equalTo("")));
        assertThat(annotationTopicSelectedInRow(0), is(equalTo("-- Select topic --")));
    }

    @Test
    public void newAliasShouldDisplayNewEmptyFieldOnTop() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnAliasesTab();
        clickOnNewAlias();

        assertThat(aliasNameInRow(0), is(equalTo("")));
        assertThat(aliasTypeSelectedInRow(0), is(equalTo("-- Select type --")));
    }

    @Test
    public void newParameterShouldDisplayNewEmptyFieldOnTop() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnParametersTab();
        clickOnNewParameter();

        assertThat(paramValueInRow(0), is(equalTo("")));
        assertThat(paramBaseInRow(0), is(equalTo("10")));
        assertThat(paramExponentInRow(0), is(equalTo("0")));
        assertThat(paramUncertaintyNameInRow(0), is(equalTo("")));
        assertThat(paramTypeSelectedInRow(0), is(equalTo("-- Select type --")));
        assertThat(paramUnitSelectedInRow(0), is(equalTo("-- Select unit --")));
    }

    @Test
    public void newConfidenceShouldDisplayNewEmptyFieldOnTop() throws Exception {
        goToInteractionPageShortLabel("prp17-sap61");
        loginAs("curator");

        clickOnConfidencesTab();
        clickOnNewConfidence();

        assertThat(confidenceValueInRow(0), is(equalTo("")));
        assertThat(confidenceTypeSelectedInRow(0), is(equalTo("-- Select type --")));
    }

    protected void clickOnParametersTab() {
        findTabsElement().findElement(By.partialLinkText("Parameters (")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnConfidencesTab() {
        findTabsElement().findElement(By.partialLinkText("Confidences (")).click();
        waitUntilLoadingIsComplete();
    }

    protected void clickOnNewParameter() {
        driver.findElement(By.id(getTabsComponentId()+":newParamBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected String paramValueInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":parametersTable:"+rowIndex+":paramFactorTxt"));
    }

    protected String paramBaseInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":parametersTable:"+rowIndex+":paramBaseTxt"));
    }

    protected String paramExponentInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":parametersTable:"+rowIndex+":paramExponentTxt"));
    }

    protected String paramUncertaintyNameInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":parametersTable:"+rowIndex+":paramUncertaintyTxt"));
    }

    protected String paramTypeSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":parametersTable:" + rowIndex + ":paramTypeSel"));
    }

    protected String paramUnitSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":parametersTable:" + rowIndex + ":paramUnitSel"));
    }

    protected void clickOnNewConfidence() {
        driver.findElement(By.id(getTabsComponentId()+":newConfidenceBtn")).click();
        waitUntilLoadingIsComplete();
    }

    protected String confidenceValueInRow(int rowIndex) {
        return valueForElement(By.id(getTabsComponentId()+":confidencesTable:"+rowIndex+":confidenceValueTxt"));
    }

    protected String confidenceTypeSelectedInRow(int rowIndex) {
        return valueForSelect(By.id(getTabsComponentId() + ":confidencesTable:" + rowIndex + ":confidenceTypeSel"));
    }

    private void typeFigureLegend(String s) {
        driver.findElement(By.id("figLegendTxt")).sendKeys(s);
    }

    protected void typeSecondRowAnnotationText(String s) {
        driver.findElement(By.id(getTabsComponentId()+":annotationsTable:1:annotationTxt")).sendKeys(s);
    }

    private String figureLegendText() {
        return valueForElement(By.id("figLegendTxt"));
    }

    private void importSelectedByDefault() {
        driver.findElement(By.id("interactionTabs:candidatesDialogContent:importSelected")).click();
        waitUntilElementIsVisible(By.linkText("Participants (3)"));
    }

    private void searchParticipantsUsing(String query) {
        driver.findElement(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt")).sendKeys(query);
        driver.findElement(By.id("interactionTabs:ipDialogPanel:participantSearchBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:candidatesDialogContent:importSelected"));
    }

    private void showImportParticipantsDialog() {
        waitUntilElementIsVisible(By.id("interactionTabs:importParticipantBtn"));
        driver.findElement(By.id("interactionTabs:importParticipantBtn")).click();
        waitUntilElementIsVisible(By.id("interactionTabs:ipDialogPanel:searchParticipantTxt"));
    }

    private String identityForParticipantInFirstRow() {
        final By firstIdInRowElement = By.xpath("//span[@id=\"interactionTabs:participantsTable:0:participantId\"]");
        return driver.findElement(firstIdInRowElement).getText();
    }


}
