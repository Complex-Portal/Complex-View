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
package uk.ac.ebi.intact.view.webapp.servlet;

import com.sun.syndication.feed.synd.SyndFeed;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.FeedType;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.NewsUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that returns a feed with the news
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class NewsFeedServlet extends HttpServlet {

    public static final String NEWS_URL = "uk.ac.ebi.faces.NEWS_URL";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String newsXml = request.getSession().getServletContext().getInitParameter(NEWS_URL);

        if (newsXml == null)
        {
            throw new IOException("To use the NewsFilterServlet add the init-parameter '"+NEWS_URL+"' to your web.xml. " +
                                  "Its value has to be a URL containing an XML file with the news.");
        }

        SyndFeed feed = NewsUtil.createNewsFeed(NewsUtil.readNews(newsXml));

        try
        {
            if (feed != null)
            {
                response.setContentType(NewsUtil.XML_MIME_TYPE);
                NewsUtil.writeFeed(feed, FeedType.DEFAULT, response.getWriter());
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

}