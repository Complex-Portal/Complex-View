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
package uk.ac.ebi.intact.confidence.model.io.impl;

import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * Writes BinaryInteractionAttributes in the format needed to train the GISModel.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class GisAttributesWriterImpl implements BinaryInteractionAttributesWriter {

    public void append( BinaryInteractionAttributes binaryInteractionAttributes, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile, true);
        writer.append( gisLine(binaryInteractionAttributes) + "\n" );
        writer.close();
    }

    public void append(List<BinaryInteractionAttributes> binrayInteractions, File outFile) throws IOException{
        Writer writer = new FileWriter(outFile, true);
        for ( Iterator<BinaryInteractionAttributes> iterator = binrayInteractions.iterator(); iterator.hasNext(); ) {
             BinaryInteractionAttributes binaryInt =  iterator.next();
             writer.append( gisLine( binaryInt) +"\n");             
         }
         writer.close();
    }

    public void write( List<BinaryInteractionAttributes> binaryInteractionsWithAttributes, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile);
        for ( Iterator<BinaryInteractionAttributes> attrIter = binaryInteractionsWithAttributes.iterator(); attrIter.hasNext(); )
        {
            BinaryInteractionAttributes binAttr = attrIter.next();
            writer.write( gisLine(binAttr) + "\n");
        }
        writer.close();
    }

    private String gisLine(BinaryInteractionAttributes binAttr){
        String line = binAttr.convertToString();
        int i = line.indexOf( ",");
        line = line.substring( i+1);
        line = line.replace( ",", " ");
        line += " " + binAttr.getConfidence().toString();
        return line;
    }
}
