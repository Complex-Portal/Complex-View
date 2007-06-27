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
package uk.ac.ebi.intact.application.statisticView.webapp;

import uk.ac.ebi.intact.application.statisticView.business.model.IntactStatistics;
import uk.ac.ebi.intact.config.impl.StandardCoreDataConfig;
import uk.ac.ebi.intact.context.IntactSession;

import java.util.Arrays;
import java.util.List;

/**
 * DataConfig class for statistics
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
class StatisticsDataConfig extends StandardCoreDataConfig {

    public StatisticsDataConfig( IntactSession session ) {
        super( session );
    }

    @Override
    protected List<String> getPackagesWithEntities() {
        return Arrays.asList( IntactStatistics.class.getPackage().getName() );
    }
}
