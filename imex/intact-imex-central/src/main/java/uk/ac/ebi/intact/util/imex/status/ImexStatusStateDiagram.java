package uk.ac.ebi.intact.util.imex.status;

import uk.ac.ebi.intact.util.imex.status.ImexPublicationStatus;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: CatherineLeroy
 * Date: 10-Feb-2007
 * Time: 11:16:23
 * To change this template use File | Settings | File Templates.
 */
public class ImexStatusStateDiagram {

    private static ImexPublicationStatus postPublication;
    private static ImexPublicationStatus prePublication;
    private static ImexPublicationStatus reserved;
    private static ImexPublicationStatus inProgress;
    private static ImexPublicationStatus discarded;
    private static ImexPublicationStatus incomplete;
    private static ImexPublicationStatus released;
    private static Collection<ImexPublicationStatus> statusCol = new ArrayList();
    static{
        Collection<ImexPublicationStatus> status = new ArrayList();

        //postPublication and prePublication
        status.add(reserved);
        postPublication = new ImexPublicationStatus("POSTPUBLICATION",status);
        prePublication = new ImexPublicationStatus("PREPUBLICATION",status);
        statusCol.add(postPublication);
        statusCol.add(prePublication);


        //reserved
        status.clear();
        status.add(inProgress);
        status.add(discarded);
        status.add(incomplete);
        reserved = new ImexPublicationStatus("RESERVED",status);
        statusCol.add(reserved);

        //inProgress
        status.clear();
        status.add(discarded);
        status.add(released);
        status.add(incomplete);
        inProgress = new ImexPublicationStatus("INPROGRESS",status);
        statusCol.add(inProgress);

        //discarded
        status.clear();
        discarded = new ImexPublicationStatus("DISCARDED",status);
        statusCol.add(discarded);

        //released
        status.clear();
        released = new ImexPublicationStatus("RELEASED", status);
        statusCol.add(released);

        //inComplete
        status.clear();
        status.add(inProgress);
        incomplete = new ImexPublicationStatus("INCOMPLETE",status);
        statusCol.add(incomplete);
    }


    public ImexStatusStateDiagram() {
    }


    public static ImexPublicationStatus getPostPublication() {
        return postPublication;
    }

    public static ImexPublicationStatus getPrePublication() {
        return prePublication;
    }

    public static ImexPublicationStatus getReserved() {
        return reserved;
    }

    public static ImexPublicationStatus getInProgress() {
        return inProgress;
    }

    public static ImexPublicationStatus getDiscarded() {
        return discarded;
    }

    public static ImexPublicationStatus getIncomplete() {
        return incomplete;
    }

    public static ImexPublicationStatus getReleased() {
        return released;
    }


    public static Collection<ImexPublicationStatus> getStatusCol() {
        return statusCol;
    }
}
