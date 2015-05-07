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

    public Page() {
    }

    public Page ( String p, int num, int max ) {
        this.numberOfElementsPerPage = num;
        this.totalNumberOfElements = max;
        this.page = setPage(p, num, max);
        this.prevPage = this.page - 1;
        this.nextPage = this.page;
        this.nextPage = ++this.nextPage * num >= max ? -1 : this.nextPage;
        this.lastPage = this.totalNumberOfElements / this.numberOfElementsPerPage;
        if(max%num==0) this.lastPage--;
        if(this.page > this.lastPage) this.page = this.lastPage;
    }

    private int setPage( String p, int num, int max ) {
        int pageNumber = 0;
        if ( p != null ) {
            try{
                pageNumber = Integer.parseInt(p);
            }
            catch (NumberFormatException e) {
                pageNumber = 0;
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

    public void setNumberOfElementsPerPage(int numberOfElementsPerPage) {
        this.numberOfElementsPerPage = numberOfElementsPerPage;
    }

    public void setTotalNumberOfElements(int totalNumberOfElements) {
        this.totalNumberOfElements = totalNumberOfElements;
    }

    public void setPrevPage(int prevPage) {
        this.prevPage = prevPage;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getNumberOfElementsPerPage () { return this.numberOfElementsPerPage; }
    public int getTotalNumberOfElements () { return this.totalNumberOfElements; }
    public int getPrevPage () { return this.prevPage; }
    public int getPage () { return this.page; }
    public int getNextPage () { return this.nextPage; }
    public int getLastPage() { return this.lastPage; }
    public int getStartListCount () { return this.page * this.numberOfElementsPerPage + 1; }
    public int getPageForShow () { return this.page + 1; }
    public int getLastPageForShow () { return this.lastPage + 1; }
}
