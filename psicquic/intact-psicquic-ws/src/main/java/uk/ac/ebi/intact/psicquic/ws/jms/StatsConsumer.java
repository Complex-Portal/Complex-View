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
package uk.ac.ebi.intact.psicquic.ws.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.psicquic.ws.aop.StatsUnit;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psicquic.ws.util.StatsCsvWriter;

import javax.jms.*;
import java.io.File;

/**
 * Statistics consumer.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class StatsConsumer extends ConsumerBean implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(StatsConsumer.class);

    @Autowired
    @Qualifier("consumerJmsTemplate")
    private JmsTemplate template;

    @Autowired
    private Destination destination;

    @Autowired
    private PsicquicConfig psicquicConfig;

    private String myId = "stats";

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    private File statsFile = null;

    public void start() throws JMSException {
        this.statsFile = new File(psicquicConfig.getStatsDirectory(), "psicquic_usage.log");

        String selector = "type = '" + myId + "'";

        try {
            ConnectionFactory factory = template.getConnectionFactory();
            connection = factory.createConnection();

            // we might be a reusable connection in spring
            // so lets only set the client ID once if its not set
            synchronized (connection) {
                if (connection.getClientID() == null) {
                    connection.setClientID(myId);
                }
            }

            connection.start();

            session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(destination, selector, false);
            consumer.setMessageListener(this);
        } catch (JMSException ex) {
            throw ex;
        }
    }

    public void stop() throws JMSException {
        if (consumer != null) {
            consumer.close();
        }
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public void onMessage(Message message) {
        super.onMessage(message);
        try {
            if (message instanceof ObjectMessage) {
                StatsUnit statsUnit = (StatsUnit) ((ObjectMessage)message).getObject();
                StatsCsvWriter writer = new StatsCsvWriter();

                if (logger.isDebugEnabled()) {
                    logger.debug("Writing stats unit to file: "+statsUnit);
                }

                writer.appendToFile(statsFile, statsUnit);
            }
            message.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }
}