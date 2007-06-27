/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*
*
* - ** XHTML/F ready **
*
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

import org.apache.log4j.Logger;
import org.displaytag.decorator.TableDecorator;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;


/**
 * Decorator which should be used by the display:table tag in order to display properly
 * highlight sources.
 *
 * <p> Example : <br>
 *      <b>sources</b> is the name under which you have stored a List object to display in the session.
 *   <p>
 *      <display:table width="100%" name="source"
 *       <p>
 *          decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.SourceDecorator">
 *            <p>
 *              <display:column property="label" title="<%= user.getQueryString() %>" /><br>
 *              <display:column property="description"  />
 *            </p>
 *       </p>
 *      </display:table>
 *   </p>
 * </p>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk) & Alexandre Liban (aliban@ebi.ac.uk)
 * @version $Id$
 *
 * UPDATE Alexandre Liban (aliban@ebi.ac.uk) | 13/08/2005
 *        Class SourceDecorator modified to be compatible with new "display.tld"
 *
 */
public class SourceDecorator extends TableDecorator
{

    static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /**
     * Transform the label and value data contained by the LabelValueBean being
     * displayed by the Display:* tag into a link.
     *
     * e.g. label = XY:0000123 and value = http://www.mysourceXY.com/displayIt?id=XY:0000123
     *      it format it as : <a href="http://www.mysourceXY.com/displayIt?id=XY:0000123"> XY:0000123 </a>
     *
     * @return data properly formated in order to be displayed by the display taglib
     */
    public String getid() {
        SourceBean bean = (SourceBean) this.getCurrentRowObject();

        final String id = bean.getId();
        final String sourceBrowserUrl = bean.getSourceBrowserUrl();

        logger.info("getId() for " + id);

        return "<a href=\"javascript:self.location();\" onClick=\"javascript:window.open('"
                    + sourceBrowserUrl + "','" + id + "',"
                    + "'width=600,height=500,scrollbars=yes');\">" + id + "</a>";
    }


    /**
     * Format the link "Browse" to the GO-diagram when the souce list is displayed
     *
     * @return a link "Browse" which displays the GO-diagram in the "selectSourceFrame"
     */
    public String getGraph() {
        SourceBean bean = (SourceBean) this.getCurrentRowObject();
        if ( bean == null ) return "";

        if ( bean.getType().equals("Go") ) {
            final String sourceBrowserGraphUrl = bean.getSourceBrowserGraphUrl();

            String applicationPath = bean.getApplicationPath();

            return "<a href=\"" + sourceBrowserGraphUrl + "\" target=\"selectedSourcetFrame\" ><img src=\""+ applicationPath +"/images/graph.png\" border=\"0\" height=\"25\" width=\"25\" align=\"middle\" alt=\"(*)\" /></a>";
        }
        else {
            return null;
        }
    }


    /**
     * Transform the label and value data contained by the LabelValueBean beiing
     * displayed by the Display:* tag into a link.
     *
     * e.g. label = XY:0000123 and value = http://www.mysourceXY.com/displayIt?id=XY:0000123
     *      it format it as : <a href="http://www.mysourceXY.com/displayIt?id=XY:0000123"> XY:0000123 </a>
     *
     * @return data properly formated in order to be displayed by the display taglib
     */
    public String getDirectHighlightUrl() {
        SourceBean bean = (SourceBean) this.getCurrentRowObject();
        if ( bean == null ) return "";

        String applicationPath = bean.getApplicationPath();

        if (bean.isSelected() == false) {
            final String url = bean.getDirectHighlightUrl();
            return "<a href=\"" + url + "\" target=\"_top\"><img src=\""+ applicationPath +
                   "/images/ok-grey.png\" border=\"0\" align=\"middle\" alt=\"(*)\" /></a>";
        }
        else {
            return "<img src=\""+ applicationPath +
                   "/images/ok-red.png\" border=\"0\" align=\"middle\" alt=\"(-)\" />";
            }
    }


    /**
     * Send back the desciption of the source and in case it doesn't exists,
     * give a meaningfull message.
     * @return the source description or an explanation messages.
     */
    public String getDescription () {
        SourceBean bean = (SourceBean) this.getCurrentRowObject();
        if (bean == null) return "";

        String description = bean.getDescription();

        if (description == null || description.trim().length() == 0) {
            description = "<span style=\"font-color:#898989;text-align:left;\"> No description available </span>";
        }

        return description;
    }


    /**
     * Send back the count of the source (proteins related)
     * @return the source count
     */
    public int getCount () {
        SourceBean bean = (SourceBean) this.getCurrentRowObject();
        if (bean == null) return 0;

        int count = bean.getCount();

        return count;
    }

}
