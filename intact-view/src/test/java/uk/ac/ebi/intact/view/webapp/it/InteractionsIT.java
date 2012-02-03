package uk.ac.ebi.intact.view.webapp.it;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InteractionsIT extends IntactViewIT {

    private final String[] MITAB_BASED_FORMATS = new String[]{"MI-TAB 2.5", "MI-TAB 2.5 (Expanded)"};
    private final String[] MI_XML_BASED_FORMATS = new String[]{"MI-XML 2.5.3", "MI-XML 2.5.4", "MI-XML 2.5(HTML view)", "BioPAX (Level 3)", "BioPAX (Level 2)",
            "RDF/XML", "RDF/XML (Abbrev)", "RDF (N3)", "RDF (N-Triples)", "RDF (Turtle)"};
    private final String XGMML = "XGMML (Cytoscape)";

    @Test
    public void moreResultsThanTheXmlAndXgmmlLimitsShouldDisableTheOptions() throws Exception {
        // Given: the export of XML formats is limited to 8 and the export of XGMML is limited to 12
        goToTheStartPage();


        // When: I go to the Interactions tab and check the options of the export formats list
        goToInteractionsTab();
        Select exportFormatSelect = new Select(driver.findElement(By.id("mainPanels:exportFormatSelect")));

        // Then: I will obtain 101 results, so the MITAB based formats should be enabled and the MIXML based formats should be disabled
        // and the XGMML format should be disabled
        assertThat(numberOfResultsDisplayed(), is(equalTo(101)));

        for (String mitabBasedFormat : MITAB_BASED_FORMATS) {
            assertThat(mitabBasedFormat, isEnabled(exportFormatSelect));
        }

        for (String miXmlBasedFormat : MI_XML_BASED_FORMATS) {
            assertThat(miXmlBasedFormat, isDisabled(exportFormatSelect));
        }

        assertThat(XGMML, isDisabled(exportFormatSelect));
    }

    @Test
    public void moreResultsThanTheXmlLimitsShouldDisableSomeOptionsButNotXgmml() throws Exception {
        // Given: the export of XML formats is limited to 8 and the export of XGMML is limited to 12
        goToTheStartPage();


        // When: I search for "10514511"
        search("10514511");
        Select exportFormatSelect = new Select(driver.findElement(By.id("mainPanels:exportFormatSelect")));

        // Then: I will obtain 10 results, so the MITAB based formats should be enabled and the MIXML based formats should be disabled
        // and the XGMML format should be enabled
        assertThat(numberOfResultsDisplayed(), is(equalTo(10)));

        for (String mitabBasedFormat : MITAB_BASED_FORMATS) {
            assertThat(mitabBasedFormat, isEnabled(exportFormatSelect));
        }

        for (String miXmlBasedFormat : MI_XML_BASED_FORMATS) {
            assertThat(miXmlBasedFormat, isDisabled(exportFormatSelect));
        }

        assertThat(XGMML, isEnabled(exportFormatSelect));
    }



    @Test
    public void fewResultsAllOptionsEnabled() throws Exception {
        // Given: the export of XML formats is limited to 8 and the export of XGMML is limited to 12
        goToTheStartPage();


        // When: I search for "11554746"
        search("11554746");
        Select exportFormatSelect = new Select(driver.findElement(By.id("mainPanels:exportFormatSelect")));

        // Then: I will obtain 4 results, so the MITAB based formats should be enabled and the MIXML based formats should be enabled
        // and the XGMML format should be enabled
        assertThat(numberOfResultsDisplayed(), is(equalTo(4)));

        for (String mitabBasedFormat : MITAB_BASED_FORMATS) {
            assertThat(mitabBasedFormat, isEnabled(exportFormatSelect));
        }

        for (String miXmlBasedFormat : MI_XML_BASED_FORMATS) {
            assertThat(miXmlBasedFormat, isEnabled(exportFormatSelect));
        }

        assertThat(XGMML, isEnabled(exportFormatSelect));
    }

    private Matcher<String> isDisabled(Select exportFormatSelect) {
        return new IsNot<String>(isEnabled(exportFormatSelect));
    }

    private Matcher<String> isEnabled(final Select exportFormatSelect) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                for (WebElement option : exportFormatSelect.getOptions()) {
                    if (o.equals(option.getText())) {
                        return option.isEnabled();
                    }
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("disabled");
            }
        };
    }


}
