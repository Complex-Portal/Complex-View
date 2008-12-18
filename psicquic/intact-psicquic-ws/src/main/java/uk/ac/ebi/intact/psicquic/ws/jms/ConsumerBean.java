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

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConsumerBean implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(ConsumerBean.class);

    private List<Message> messages = new ArrayList<Message>();
    private Object semaphore;

    /**
     * Constructor.
     */
    public ConsumerBean() {
        this(new Object());
    }

    /**
     * Constructor, initialized semaphore object.
     *
     * @param semaphore
     */
    public ConsumerBean(Object semaphore) {
        this.semaphore = semaphore;
    }

    /**
     * @return all the messages on the list so far, clearing the buffer
     */
    public synchronized List<Message> flushMessages() {
        List<Message> answer = new ArrayList<Message>(messages);
        messages.clear();
        return answer;
    }

    /**
     * Method implemented from MessageListener interface.
     *
     * @param message
     */
    public synchronized void onMessage(Message message) {
        messages.add(message);

        synchronized (semaphore) {
            semaphore.notifyAll();
        }
    }

    /**
     * Use to wait for a single message to arrive.
     */
    public void waitForMessageToArrive() {
        logger.debug("Waiting for message to arrive");

        long start = System.currentTimeMillis();

        try {
            if (hasReceivedMessage()) {
                synchronized (semaphore) {
                    semaphore.wait(4000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis() - start;

        logger.debug("End of wait for " + end + " millis");
    }

    /**
     * Used to wait for a message to arrive given a particular message count.
     *
     * @param messageCount
     */
    public void waitForMessagesToArrive(int messageCount) {
        logger.debug("Waiting for message to arrive");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            try {
                if (hasReceivedMessages(messageCount)) {
                    break;
                }
                synchronized (semaphore) {
                    semaphore.wait(1000);
                }
            } catch (InterruptedException e) {
                logger.debug("Caught: " + e);
            }
        }
        long end = System.currentTimeMillis() - start;

        logger.debug("End of wait for " + end + " millis");
    }

    /**
     * Identifies if the message is empty.
     *
     * @return
     */
    protected boolean hasReceivedMessage() {
        return messages.isEmpty();
    }

    /**
     * Identifies if the message count has reached the total size of message.
     *
     * @param messageCount
     * @return
     */
    protected synchronized boolean hasReceivedMessages(int messageCount) {
        return messages.size() >= messageCount;
    }
}

