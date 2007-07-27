/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.attribute.Filter;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public class FilterTest01 implements TestConstants {

    public static void main(String[] args) throws IOException {

          FilterTest01 test = new FilterTest01();
    }

    public FilterTest01() throws IOException {

        Filter filter = new Filter(lowConfAll, lowConfFiltered);
        filter.filterBySet(hiConfAll);
    }

}
