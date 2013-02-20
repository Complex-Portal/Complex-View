package uk.ac.ebi.intact.view.webapp.controller.application;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Entry containing stats information
 *
 * @author Rafael C Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since 4.0.2-SNAPSHOT
 */
public class StatsEntry {
    Integer ac;
    Date timestamp;
    Integer proteinNumber;
    Integer interactionNumber;
    Integer binaryInteractions;
    Integer complexInteractions;
    Integer experimentNumber;
    Integer termNumber;
    Integer publicationCount;

    public StatsEntry(Integer ac, Date timestamp, Integer proteinNumber, Integer interactionNumber, Integer binaryInteractions, Integer complexInteractions, Integer experimentNumber, Integer termNumber, Integer publicationCount) {
        this.ac = ac;
        this.timestamp = timestamp;
        this.proteinNumber = proteinNumber;
        this.interactionNumber = interactionNumber;
        this.binaryInteractions = binaryInteractions;
        this.complexInteractions = complexInteractions;
        this.experimentNumber = experimentNumber;
        this.termNumber = termNumber;
        this.publicationCount = publicationCount;
    }

    public Integer getAc() {
        return ac;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getTimestampYear(){
        SimpleDateFormat simpleDateformat =new SimpleDateFormat("yyyy");
        return Integer.parseInt(simpleDateformat.format(timestamp));
    }

    public int getTimestampMonth(){
        SimpleDateFormat simpleDateformat =new SimpleDateFormat("M");
        return Integer.parseInt(simpleDateformat.format(timestamp));
    }

    public int getTimestampDay(){
        SimpleDateFormat simpleDateformat =new SimpleDateFormat("d");
        return Integer.parseInt(simpleDateformat.format(timestamp));
    }

    public String getTimestampJavaScript(){
        String jsT = "Date.UTC(";
        jsT += getTimestampYear() + ",";
        /*  months start at 0 for January, 1 for February etc. */
        jsT += getTimestampMonth()-1 + ",";
        jsT += getTimestampDay();
        jsT += ")";
        return jsT;
    }

    public Integer getProteinNumber() {
        return proteinNumber;
    }

    public Integer getInteractionNumber() {
        return interactionNumber;
    }

    public Integer getBinaryInteractions() {
        return binaryInteractions;
    }

    public Integer getComplexInteractions() {
        return complexInteractions;
    }

    public Integer getExperimentNumber() {
        return experimentNumber;
    }

    public Integer getTermNumber() {
        return termNumber;
    }

    public Integer getPublicationCount() {
        return publicationCount;
    }
}
