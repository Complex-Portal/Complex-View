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
package uk.ac.ebi.intact.view.webapp.controller.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Datasets;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Datasets.Dataset;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.SiteUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: DataSetsBean.java 7881 2007-03-08 15:49:39Z baranda $
 */

@Controller( "dataSetsBean" )
@Scope( "session" )
public class DataSetsBean implements Serializable {

    //public static final String DATASET_OF_THE_MONTH_URL = "intact.dotm.url";

    private List<uk.ac.ebi.intact.view.webapp.controller.news.items.Datasets.Dataset> dataSets;
    private Datasets.Dataset dataSetOfTheMonth;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    public DataSetsBean() {

    }

    @PostConstruct
    public void setUp(){
        String datasetsXml = intactViewConfiguration.getDotmUrl();

        try {
            dataSets = SiteUtils.readDatasets( datasetsXml );
        }
        catch ( IntactViewException e ) {
            e.printStackTrace();
            dataSets = new ArrayList<Dataset>();
        }

        if ( !dataSets.isEmpty() ) {
            dataSetOfTheMonth = dataSets.iterator().next();
        }
    }

    public List<Datasets.Dataset> getDataSets() {
        return dataSets;
    }

    public void setDataSets( List<Dataset> dataSets ) {
        this.dataSets = dataSets;
    }

    public Dataset getDataSetOfTheMonth() {
        return dataSetOfTheMonth;
    }

    public void setDataSetOfTheMonth( Dataset dataSetOfTheMonth ) {
        this.dataSetOfTheMonth = dataSetOfTheMonth;
    }
}
