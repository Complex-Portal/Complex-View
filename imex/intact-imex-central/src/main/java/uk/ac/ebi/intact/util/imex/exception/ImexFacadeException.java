package uk.ac.ebi.intact.util.imex.exception;

/**
 * Created by IntelliJ IDEA.
 * User: CatherineLeroy
 * Date: 10-Feb-2007
 * Time: 11:59:49
 * To change this template use File | Settings | File Templates.
 */
public class ImexFacadeException extends Exception {

//    private String nestedMessage;
//    private Exception rootCause;
//
    public ImexFacadeException(){
    }

    public ImexFacadeException(String msg){
        super(msg);
    }

    public ImexFacadeException(String msg, Exception e){
        super(msg,e);
    }

    public ImexFacadeException(String message, Throwable cause){
        super(message, cause);
    }

    public ImexFacadeException(Throwable cause){
        super(cause);
    }

    public String toString(){
        return ( getMessage() + "\n" + getCause());
    }
}
    