/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.application.hierarchview.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builds a new DataService object
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class DataServiceFactory {

    private static final Log logger = LogFactory.getLog( DataServiceFactory.class );

    public static DataService buildDataService( String source ) {
        DataService dataservice = null;

        if ( source.equals( "database" ) ) {
            dataservice = new DatabaseService();
        }

        if ( source.equals( "webservice" ) ) {
            dataservice = new BinaryWebService();
        }

        if ( source.equals( "mock" ) ) {
            dataservice = new DataServiceMock();
        }

        if ( source.equals( "local" ) ) {
            dataservice = new LocalIndexDataSevice();
        }

        if ( logger.isDebugEnabled() ) logger.debug( "Used data source=" + source );

        return dataservice;
    }
}
