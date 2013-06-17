package uk.ac.ebi.intact.editor.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/12/11</pre>
 */

public class AdminImportXmlIT extends EditorIT {

    @Test
    @DirtiesContext
    public void importXmlFile() throws Exception {
        // Given: I want to import an XML file as an Admin
        goToPageInContext("/admin/dbupdate/importXml.xhtml");

        loginAs("admin");

        // When: I introduce a URL to be imported and start the import
        typeUrlToImport(AdminImportXmlIT.class.getResource("/META-INF/data/10359607.xml"));
        startImport();

        // Then: it should be imported
        assertTrue(infoMessageSummaryExists("File successfully imported"));
    }

    private void typeUrlToImport(URL url) {
        driver.findElement(By.id("urlTxt")).sendKeys(url.toString());
    }

    private void startImport() {
        driver.findElement(By.id("startImportBtn")).click();
    }


}
