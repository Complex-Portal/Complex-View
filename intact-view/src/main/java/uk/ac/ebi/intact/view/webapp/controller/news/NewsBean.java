/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.news;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.news.items.News;
import uk.ac.ebi.intact.view.webapp.controller.news.items.NewsItem;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.FeedType;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.NewsUtil;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * News backing bean.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Controller("newsBean")
@Scope("request")
public class NewsBean implements Serializable {

    private News newsObject;
    private List<NewsItem> news;
    private List<NewsItem> urgentNews;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private static final Log log = LogFactory.getLog( NewsBean.class );

    public NewsBean() {
    }

    @PostConstruct
    public void setup() {
        String newsXml = intactViewConfiguration.getNewsUrl();
        if ( log.isTraceEnabled() ) {
            log.trace( "newsXml " + newsXml);
        }
        newsObject = NewsUtil.readNews( newsXml );
        news = newsObject.getNewsItems();

        // urgent news
        urgentNews = new ArrayList<NewsItem>();

        for ( NewsItem newsItem : news ) {
            if ( newsItem.isUrgent() != null && newsItem.isUrgent() ) {
                urgentNews.add( newsItem );
            }
        }
    }

    public void exportFeed( ActionEvent evt ) {
        SyndFeed feed = NewsUtil.createNewsFeed( newsObject );

        FacesContext context = FacesContext.getCurrentInstance();

        try {
            NewsUtil.writeFeed( feed, FeedType.DEFAULT, context );
        }
        catch ( FeedException e ) {
            e.printStackTrace();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }

        context.getApplication().getStateManager().saveSerializedView( context );
        context.renderResponse();
    }

    public List<NewsItem> getNews() {
        return news;
    }

    public void setNews( List<NewsItem> news ) {
        this.news = news;
    }

    public News getNewsObject() {
        return newsObject;
    }

    public void setNewsObject( News newsObject ) {
        this.newsObject = newsObject;
    }

    public List<NewsItem> getUrgentNews() {
        return urgentNews;
    }

    public void setUrgentNews( List<NewsItem> urgentNews ) {
        this.urgentNews = urgentNews;
    }
}
