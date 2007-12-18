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

import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        13-Dec-2007
 *        </pre>
 */
public class ProteinSimplifiedWriterImpl implements ProteinSimplifiedWriter {

    public void append( ProteinSimplified protein, File outFile ) throws IOException {
        //write protein annotations  -GO
        String goFileName = fileName(outFile) + "_go.txt";
        Writer writer = new FileWriter(goFileName, true);
        writer.append( protein.convertGOAnnotationToString() +"\n");
        writer.close();

        //write protein annotations  -InterPro
        String ipFileName = fileName(outFile) + "_ip.txt";
        writer = new FileWriter(ipFileName, true);
        writer.append( protein.convertIpAnnotationToString() +"\n");
        writer.close();

        //write protein annotations  -InterPro
        String seqFileName = fileName(outFile) + "_seq.txt";
        writer = new FileWriter(seqFileName, true);
        writer.append( protein.convertSeqAnnotationToFasta() +"\n");
        writer.close();
    }

    public void append( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void write( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

     private String fileName( File outFile ) {
        String fileName = outFile.getPath();
        int index = fileName.lastIndexOf( ".");
        String result = fileName.substring( 0, index);
        return result;
    }

    public void appendGO( ProteinSimplified protein, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile, true);
        writer.append( protein.convertGOAnnotationToString() +"\n");
        writer.close();
    }

    public void appendGO( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeGO( proteins, outFile, true);
    }

    public void writeGO( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeGO( proteins, outFile, false);
    }

    public void appendIp( ProteinSimplified protein, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile);
        writer.append( protein.convertIpAnnotationToString() + "\n");
        writer.close();
    }

    public void appendIp( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeIp( proteins, outFile, true);
    }

    public void writeIp( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeIp(proteins, outFile, false);
    }

    public void appendSeq( ProteinSimplified protein, File outFile ) throws IOException {
        Writer writer =  new FileWriter(outFile, true);
        writer.append( protein.convertSeqAnnotationToFasta() + "\n");
        writer.close();
    }

    public void appendSeq( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeSeq( proteins, outFile, true);
    }

    public void writeSeq( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        writeSeq(proteins, outFile, false);
    }


    /////////////////////
    // Private method(s)
     private void writeGO(List<ProteinSimplified> proteins, File outFile, boolean append) throws IOException {
        Writer writer = new FileWriter(outFile, append);
        for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
            ProteinSimplified simplified = iterator.next();
            writer.append( simplified.convertGOAnnotationToString() +"\n");
        }
        writer.close();
    }

    private void writeIp(List<ProteinSimplified> proteins, File outFile, boolean append) throws IOException {
        Writer writer = new FileWriter(outFile, append);
        for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
            ProteinSimplified simplified = iterator.next();
            writer.append( simplified.convertIpAnnotationToString() +"\n");
        }
        writer.close();
    }

    private void writeSeq(List<ProteinSimplified> proteins, File outFile, boolean append) throws IOException{
        Writer writer = new FileWriter(outFile, true);
        for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
            ProteinSimplified simplified = iterator.next();
            writer.append( simplified.convertSeqAnnotationToFasta() +"\n");
        }
        writer.close();
    }
}
