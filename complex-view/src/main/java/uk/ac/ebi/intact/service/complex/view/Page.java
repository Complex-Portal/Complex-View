package uk.ac.ebi.intact.service.complex.view;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 06/12/13
 */
public class Page {
    int numberOfElementsPerPage;
    int totalNumberOfElements;
    int prevPage, page, nextPage, lastPage;

    public Page ( String p, int num, int max ) {
        this.numberOfElementsPerPage = num;
        this.totalNumberOfElements = max;
        this.page = setPage(p, num, max);
        this.prevPage = this.page - 1;
        this.nextPage = this.page;
        this.nextPage = ++this.nextPage * num >= max ? -1 : this.nextPage;
        this.lastPage = this.totalNumberOfElements / this.numberOfElementsPerPage;
    }

    private int setPage( String p, int num, int max ) {
        int pageNumber = 0;
        if ( p != null ) {
            try{
                pageNumber = Integer.parseInt(p);
            }
            catch (NumberFormatException e) {
                // We must log that
            }
            if ( pageNumber > 0 ){
                if ( pageNumber * num >= max )
                    pageNumber = (max / num);
            }
            else
                pageNumber = 0;
        }
        return pageNumber;
    }

    public int getNumberOfElementsPerPage () { return this.numberOfElementsPerPage; }
    public int getTotalNumberOfElements () { return this.totalNumberOfElements; }
    public int getPrevPage () { return this.prevPage; }
    public int getPage () { return this.page; }
    public int getNextPage () { return this.nextPage; }
    public int getLastPage() { return this.lastPage; }
    public int getStartListCount () { return this.page * this.numberOfElementsPerPage + 1; }
}
