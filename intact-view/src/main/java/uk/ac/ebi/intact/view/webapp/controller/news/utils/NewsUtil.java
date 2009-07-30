/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.news.utils;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.news.items.News;
import uk.ac.ebi.intact.view.webapp.controller.news.items.NewsItem;
import uk.ac.ebi.intact.view.webapp.util.SiteFunctions;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class NewsUtil {

    private static final Log log = LogFactory.getLog( NewsUtil.class );

    public static final String XML_MIME_TYPE = "application/xml; charset=UTF-8";

    private static final String NEWS_ITEMS_ATTR = NewsUtil.class+".NEWS_ITEMS";

    private NewsUtil() {
    }

    public static News readNews(String newsXml) {
        News objNews;
        try {
            URL datasetsUrl = new URL(newsXml);
            objNews = (News) readNewsXml(datasetsUrl.openStream());
        } catch (Throwable e) {
            log.error(e);
            objNews = new News();
        }

        return objNews;
    }

    public static List<NewsItem> readNewsItems(String newsXml, FacesContext facesContext) throws IntactViewException {

        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        if (request.getAttribute(NEWS_ITEMS_ATTR) != null)
        {
            return (List<NewsItem>) request.getAttribute(NEWS_ITEMS_ATTR);
        }

        List<NewsItem> items = readNews(newsXml).getNewsItems();

        if (items == null)
        {
            items = Collections.EMPTY_LIST;
        }

        request.setAttribute(NEWS_ITEMS_ATTR, items);

        return items;
    }

    public static List<NewsItem> readImportantNewsItems(String newsXml, FacesContext facesContext) throws IntactViewException {
        List<NewsItem> urgentItems = new ArrayList<NewsItem>();
        List<NewsItem> items = readNewsItems(newsXml, facesContext);

        for (NewsItem item : items) {
            if (item.isUrgent() != null && item.isUrgent()) {
                urgentItems.add(item);
            }
        }

        return urgentItems;
    }

    public static SyndFeed createNewsFeed(News news) {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("EBI IntAct News");
        feed.setLink("http://www.ebi.ac.uk/intact");
        feed.setDescription("News of the IntAct Project");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        for (NewsItem newsItem : news.getNewsItems()) {
            SyndEntry entry = createNewsFeedEntry(newsItem);
            entries.add(entry);
        }

        feed.setEntries(entries);

        return feed;
    }

    private static SyndEntry createNewsFeedEntry(NewsItem pieceOfNews) {
        SyndEntry entry;
        SyndContent description;

        entry = new SyndEntryImpl();
        entry.setTitle(pieceOfNews.getTitle());

        if (pieceOfNews.getMoreLink() != null) {
            entry.setLink(pieceOfNews.getMoreLink());
        }

        entry.setPublishedDate(SiteFunctions.convertToDate(pieceOfNews.getDate(), "yyyyMMdd"));

        description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue(pieceOfNews.getDescription().getValue());
        entry.setDescription(description);

        return entry;
    }

    private static Object readNewsXml(InputStream is) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(News.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller.unmarshal(is);
    }

    public static void writeFeed(SyndFeed feed, FeedType feedType, Writer outputWriter) throws FeedException, IOException {
        try {
            feed.setFeedType(feedType.getType());

            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, outputWriter);
        }
        catch (FeedException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeFeed(SyndFeed feed, FeedType feedType, FacesContext context) throws FeedException, IOException {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType(XML_MIME_TYPE);
        writeFeed(feed, feedType, response.getWriter());
    }
}