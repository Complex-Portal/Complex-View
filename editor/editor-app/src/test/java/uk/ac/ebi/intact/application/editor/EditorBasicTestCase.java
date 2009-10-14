package uk.ac.ebi.intact.application.editor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * EditorCorePersister Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.9.3
 */

@ContextConfiguration( locations = { "classpath*:/META-INF/intact-test.spring.xml",
                                     "classpath*:/META-INF/beans.spring.xml"} )
public abstract class EditorBasicTestCase extends IntactBasicTestCase {

    @Autowired
    private PersisterHelper persisterHelper;

    public PersisterHelper getPersisterHelper() {
        return persisterHelper;
    }
}
