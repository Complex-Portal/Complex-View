package uk.ac.ebi.intact.psicquic.wsclient;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface PsicquicClient<T> {

    T getByQuery(String query, int firstResult, int maxResults) throws PsicquicClientException;

    T getByInteractor(String identifier, int firstResult, int maxResults) throws PsicquicClientException;

    T getByInteraction(String identifier, int firstResult, int maxResults) throws PsicquicClientException;

    T getByInteractionList(String[] identifiers, int firstResult, int maxResults) throws PsicquicClientException;

    T getByInteractorList(String[] identifiers, QueryOperand operand, int firstResult, int maxResults) throws PsicquicClientException;
}
