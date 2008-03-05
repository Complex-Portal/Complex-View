package uk.ac.ebi.intact.services.faces.athena.imex.ftp;

import uk.ac.ebi.intact.dataexchange.imex.repository.ftp.ImexFTPFile;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.net.URL;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexFTPFileWrapper {

    private ImexFTPFile ftpFile;
    private int entriesCount;

    public ImexFTPFileWrapper (ImexFTPFile ftpFile) {
        this.ftpFile = ftpFile;

//        if (isReadable()) {
//            try {
//                CounterFromStream counter = new CounterFromStream(ftpFile.openStream());
//                entriesCount = counter.getEntriesCount();
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

    }

    public ImexFTPFile getFtpFile() {
        return ftpFile;
    }

    public String getName() {
        return ftpFile.getName();
    }

    public long getSize() {
        return ftpFile.getSize();
    }

    public boolean isReadable() {
        return ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION);
    }

    public Calendar getTimestamp() {
        return ftpFile.getTimestamp();
    }

    public URL getUrl() {
        return ftpFile.getUrl();
    }

    public int getEntriesCount() {
        return entriesCount;
    }

    private class CounterFromStream {

        private int entriesCount;
        private int experimentsCount;

        public CounterFromStream(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<entry>")) {
                    entriesCount++;
                }
            }
            reader.close();
        }

        public int getEntriesCount() {
            return entriesCount;
        }

        public int getExperimentsCount() {
            return experimentsCount;
        }
    }
}
