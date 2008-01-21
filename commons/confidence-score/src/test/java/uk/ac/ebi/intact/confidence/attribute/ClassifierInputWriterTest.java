package uk.ac.ebi.intact.confidence.attribute;

import org.junit.Test;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.confidence.weights.ClassifierInputWriter;

import java.io.IOException;

/**
 * ClassifierInputWriter Tester.
 *
 * @author <Authors name>
 * @since <pre>10/30/2007</pre>
 * @version 1.6.0
 */
public class ClassifierInputWriterTest {
    @Test
    public void gameLocationWeights() throws Exception {
        String pPath = ClassifierInputWriterTest.class.getResource( "pozitiveExamples.txt" ).getPath();
        String nPath = ClassifierInputWriterTest.class.getResource( "negativeExamples.txt" ).getPath();
        ClassifierInputWriter ciw = new ClassifierInputWriter( pPath,
                                                               nPath, GlobalTestData.getInstance().getTargetDirectory().getPath() + "/tadmExample.input", "TADM" );
        ciw.writeAttribList(GlobalTestData.getInstance().getTargetDirectory().getPath() + "/all_attribsExample.txt");

    }


}
