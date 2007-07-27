package uk.ac.ebi.intact.util.imex.status;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: CatherineLeroy
 * Date: 09-Feb-2007
 * Time: 17:06:50
 * To change this template use File | Settings | File Templates.
 */
public class ImexPublicationStatus {


    private String statusName;
    private Collection<ImexPublicationStatus> nextPossibleStatus = new ArrayList<ImexPublicationStatus>();

    private ImexPublicationStatus() {
    }

    public ImexPublicationStatus(String statusName, Collection<ImexPublicationStatus> nextPossibleStatus) {
        this.statusName = statusName;
        this.nextPossibleStatus = nextPossibleStatus;
    }


    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public boolean isPossibleNextStatus(String nextStatus){
        if(nextPossibleStatus.contains(nextStatus)){
            return true;
        }
        return false;
    }
}
