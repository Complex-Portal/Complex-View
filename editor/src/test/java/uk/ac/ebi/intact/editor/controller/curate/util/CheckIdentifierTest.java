package uk.ac.ebi.intact.editor.controller.curate.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 30/05/2013
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class CheckIdentifierTest {

    @Test
    public void testCheckChebiIdValid() throws Exception {

        Assert.assertTrue(CheckIdentifier.checkChebiId("CHEBI:15377"));

        Assert.assertTrue(CheckIdentifier.checkChebiId("CHEBI:5585"));

        Assert.assertTrue(CheckIdentifier.checkChebiId("CHEBI:13352"));

        Assert.assertTrue(CheckIdentifier.checkChebiId("CHEBI:42857"));

        Assert.assertTrue(CheckIdentifier.checkChebiId("CHEBI:33693"));

    }

    @Test
    public void testCheckChebiIdInvalid() throws Exception {
        Assert.assertFalse(CheckIdentifier.checkChebiId("CHEBI_15377"));

    }

    @Test
    public void testCheckEnsembleIdValid() throws Exception {
        //HUMAN BRCA2
        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSG00000139618"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSG00000107949"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSG00000083093"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSG00000170037"));

        //SQUIRREL BRCA2
        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSSTOG00000005517"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSSTOG00000000145"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSSTOG00000011828"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSSTOG00000010936"));

        //Tetraodon BRCA2
        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSTNIG00000016261"));

        Assert.assertTrue(CheckIdentifier.checkEnsembleId("ENSTNIG00000010367"));


    }

    @Test
    public void testCheckEnsembleIdInvalid() throws Exception {
        Assert.assertFalse(CheckIdentifier.checkEnsembleId("ENSG0000139618"));

    }
}
