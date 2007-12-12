/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.model.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.iterator.BinaryInteractionIterator;
import uk.ac.ebi.intact.confidence.model.parser.BinaryInteractionParserUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        11-Dec-2007
 *        </pre>
 */
public class BinaryInteractionReaderImpl implements BinaryInteractionReader {
    /**
	 * Sets up a logger for that class.
	 */
	public static final Log log	= LogFactory.getLog( BinaryInteractionReaderImpl.class);

    private Confidence conf = Confidence.UNKNOWN;

    public void setConfidence(Confidence confidence){
        this.conf = confidence;
    }

    public List<BinaryInteraction> read( File inFile ) throws IOException {
        List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String line ="";
        while ((line = br.readLine() ) !=null){
            BinaryInteraction binaryInteraction = BinaryInteractionParserUtils.parseBinaryInteraction( line, conf);
            if (binaryInteraction != null){
                interactions.add( binaryInteraction);
            }
        }
        br.close();
        return interactions;
    }

    public Iterator<BinaryInteraction> iterate( File inFile ) throws FileNotFoundException {
       BinaryInteractionIterator iterator = new BinaryInteractionIterator( new FileInputStream( inFile ));
       if (!conf.equals( Confidence.UNKNOWN)){
           iterator.setConfidence( conf);
       }
        return iterator;
    }
}
