package uk.ac.ebi.intact.editor.controller.curate.util;

import uk.ac.ebi.intact.model.CvExperimentalRole;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: ntoro
 * Date: 29/04/2013
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentalRoleComparator implements Comparator<CvExperimentalRole> {

    @Override
    public int compare(CvExperimentalRole cvExpRole1, CvExperimentalRole cvExpRole2) {
        if (cvExpRole1 != null && cvExpRole2 != null) {
            if (cvExpRole1.getShortLabel() != null && cvExpRole2.getShortLabel() != null) {
                if (cvExpRole1.getShortLabel().equals("bait")) {
                    return -1;
                } else if (cvExpRole2.getShortLabel().equals("bait")) {
                    return 1;
                } else {
                    return cvExpRole1.getShortLabel().compareTo(cvExpRole2.getShortLabel());
                }
            }
            else {
                if (cvExpRole1.getShortLabel() == null && cvExpRole2.getShortLabel() != null) {
                    return -1;
                } else if (cvExpRole1.getShortLabel() != null && cvExpRole2.getShortLabel() == null) {
                    return 1;
                }
            }
        } else {
            if (cvExpRole1 == null && cvExpRole2 != null) {
                return -1;
            } else if (cvExpRole1 != null && cvExpRole2 == null) {
                return 1;
            }
        }
        return 0;
    }
}
