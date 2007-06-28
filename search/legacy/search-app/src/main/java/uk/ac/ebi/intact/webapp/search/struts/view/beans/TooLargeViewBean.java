package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Michael Kleen
 * @version TooLargeViewBean.java Date: Feb 25, 2005 Time: 4:26:31 PM
 */
public class TooLargeViewBean {

    private Collection<SingleResultViewBean> someSingleResultViewBeans;

    public TooLargeViewBean() {
        this.someSingleResultViewBeans = new ArrayList<SingleResultViewBean>();
    }

    public void add( final SingleResultViewBean aSingleResultViewBean ) {
        this.someSingleResultViewBeans.add( aSingleResultViewBean );
    }

    public Collection<SingleResultViewBean> getSingleResults() {
        return this.someSingleResultViewBeans;
    }

    /**
     * Information for the view if we got a searchable value in the statistics
     *
     * @return boolean is true if a searchable value is in the bean, false if not
     */
    public boolean isSelectable() {
        boolean result = false;
        for ( Iterator iterator = someSingleResultViewBeans.iterator(); iterator.hasNext(); ) {
            SingleResultViewBean singleResultViewBean = (SingleResultViewBean) iterator.next();
            if ( singleResultViewBean.isSearchable() ) {
                result = true;
            }
        }
        return result;
    }
}
