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

import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.io.ProteinSimplifiedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 * Persists the ProteinSimplified objects in 3 files.
 * The information ist splited in a goFile, ip-File and seq-File.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        13-Dec-2007
 *        </pre>
 */
public class ProteinSimplifiedWriterImpl implements ProteinSimplifiedWriter {

    public void append( ProteinSimplified protein, File outFile ) throws IOException {
        //write protein annotations  -GO
        String fileName = fileName(outFile);
        String goFileName = fileName + "_go.txt";
        Writer writer = new FileWriter(goFileName, true);
        writer.append( protein.convertGOAnnotationToString() +"\n");
        writer.close();

        //write protein annotations  -InterPro
        String ipFileName = fileName + "_ip.txt";
        writer = new FileWriter(ipFileName, true);
        writer.append( protein.convertIpAnnotationToString() +"\n");
        writer.close();

        //write protein annotations  -Seq
        String seqFileName = fileName + "_seq.txt";
        writer = new FileWriter(seqFileName, true);
        writer.append( protein.convertSeqAnnotationToFasta() +"\n");
        writer.close();
    }

    public void append( List<ProteinSimplified> proteins, File outFile ) throws IOException {
        for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
            ProteinSimplified proteinSimplified = iterator.next();
            append(proteinSimplified, outFile);
        }
    }

    public void write( List<ProteinSimplified> proteins, File outFile ) throws IOException {
         cleanIfExists(outFile);
         append(proteins, outFile);
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
    //////////////////////
    // Protected method(s)
     protected void cleanIfExists( File outFile ) {
        existsDelete(outFile);
        String fileName = fileName( outFile );

        File ipFile = new File(fileName + "_ip.txt");
        existsDelete(ipFile);

        File goFile = new File(fileName + "_go.txt");
        existsDelete(goFile);

        File seqFile = new File(fileName + "_seq.txt");
        existsDelete(seqFile);
    }

    protected void existsDelete( File outFile ) {
        if (outFile.exists()){
            outFile.delete();
        }
    }

     protected String fileName( File outFile ) {
        String fileName = outFile.getPath();
        int index = fileName.lastIndexOf( ".");
        String result = fileName.substring( 0, index);
        return result;
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
