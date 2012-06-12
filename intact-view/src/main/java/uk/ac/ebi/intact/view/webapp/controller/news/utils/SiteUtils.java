/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.news.utils;

import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Datasets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for displaying datasets of the month.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: SiteUtils.java 7881 2007-03-08 15:49:39Z baranda $
 */
public class SiteUtils {

    public static final String XML_MIME_TYPE = "application/xml; charset=UTF-8";

    private SiteUtils() {}

    public static List<Datasets.Dataset> readDatasets( String datasetsXml ) throws IntactViewException {
        List<Datasets.Dataset> dataSets;
        Datasets datasets = null;

        try {
            URL datasetsUrl = new URL( datasetsXml );
            final URLConnection urlConnection = datasetsUrl.openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setReadTimeout(1000);
            
            urlConnection.connect();
            
            final InputStream is = urlConnection.getInputStream();
            try{
                datasets = ( Datasets ) readDatasetsXml( is );
            }
            finally {
                is.close();
            }
        } catch ( Throwable e ) {
            e.printStackTrace();
        }

        if ( datasets != null ) {
            dataSets = datasets.getDatasets();
        } else {
            dataSets = new ArrayList<Datasets.Dataset>();
        }

        return dataSets;
    }

    private static Object readDatasetsXml( InputStream is ) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance( Datasets.class.getPackage().getName() );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller.unmarshal( is );
    }
}