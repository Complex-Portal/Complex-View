/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp;

import org.h2.tools.Server;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StartH2Server {

    public static void main(String[] args) throws Exception {
        System.out.println("////////////////////////////");
        System.out.println("Starting H2 database...");

        final Server server = Server.createTcpServer("-tcpPort", "39101", "-baseDir", "target", "-tcpDaemon");
        server.start();

        System.out.println("URL: " + server.getURL());
        System.out.println("Port: " + server.getPort());
        System.out.println("Status: " + server.getStatus());
        System.out.println("Type: " + server.getService().getType());
        System.out.println("Allow others: " + server.getService().getAllowOthers());


        System.out.println("////////////////////////////");
    }

}
