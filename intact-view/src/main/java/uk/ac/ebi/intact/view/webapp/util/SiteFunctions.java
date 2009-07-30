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
package uk.ac.ebi.intact.view.webapp.util;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Set of general functions that used to live in the ebifaces project.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class SiteFunctions {

    private SiteFunctions()
    {
    }

    public static String absoluteContextPath() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        String path = request.getScheme()+"://" +
                      request.getServerName()+":" +
                      request.getServerPort() +
                      request.getContextPath();

        return path;
    }

    /**
     * See guidelines at http://www.ebi.ac.uk/inc/template/#important
     * @return
     */
     public static String absoluteEbiUrl() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

         String serverName = request.getServerName();

         if (!serverName.equals("www.embl-ebi.ac.uk")) {
             serverName = "www.ebi.ac.uk";
         }

        String path = request.getScheme()+"://" +
                      serverName;

        return path;
    }

    public static Date convertToDate(Object date, String sourceFormat)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(sourceFormat);
        try
        {
            return formatter.parse(date.toString());
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static int convertToInteger(Object s)
    {
        if (s == null) return 0;

        if (s instanceof String) {
            return Integer.valueOf(String.valueOf(s));
        } else if (s instanceof Integer) {
            return Integer.valueOf(s.toString());
        }

        throw new IllegalArgumentException("Argument for the convertToBoolean method was expected to be String or Integer, but found "+s.getClass());
    }

    public static boolean convertToBoolean(Object s)
    {
        if (s == null) return false;

        if (s instanceof String) {
            return Boolean.valueOf(String.valueOf(s));
        } else if (s instanceof Boolean) {
            return Boolean.valueOf(s.toString());
        }

        throw new IllegalArgumentException("Argument for the convertToBoolean method was expected to be String or Boolean, but found "+s.getClass());
    }

    public static boolean isTrimmable(String text, int maxLength) {
        return text.length() > maxLength;
    }

    public static String trim(String text, int maxLength) {
        String trimmedText = text;

        if (isTrimmable(text, maxLength)) {
            trimmedText = text.substring(0, maxLength);
        }

        return trimmedText;
    }

    public static String clientIdFor(UIComponent component) {
        return component.getClientId(FacesContext.getCurrentInstance());
    }

    public static String uniqueId() {
        return FacesContext.getCurrentInstance().getViewRoot().createUniqueId();
    }

    public static boolean isBrowserExplorer() {
        Map requestHeader = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
        String userAgent = (String) requestHeader.get("User-Agent");

        if (userAgent == null) {
            return false;
        }

        return userAgent.contains("MSIE");
    }

    public static String urlEncode(String str) {
        try
        {
            return URLEncoder.encode(str, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String urlDecode(String str) {
        try
        {
            return URLDecoder.decode(str, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String resolveExpression(String elExpression) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExpressionFactory elFactory = context.getApplication().getExpressionFactory();
        ValueExpression valueExpression = elFactory.createValueExpression(context.getELContext(), elExpression, String.class);

        return (String) valueExpression.getValue(context.getELContext());
    }
}
