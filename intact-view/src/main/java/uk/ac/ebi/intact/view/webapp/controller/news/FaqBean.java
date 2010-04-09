/**
 * 
 */
package uk.ac.ebi.intact.view.webapp.controller.news;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Faq;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Faq.Topic;
import uk.ac.ebi.intact.view.webapp.controller.news.items.Faq.Topic.Item;
import uk.ac.ebi.intact.view.webapp.controller.news.utils.FaqUtil;

/**
 * News backing bean.
 *
 * @author Erik Pfeiffenberger (epfeif@ebi.ac.uk)
 * @version $Id$
 */
@Controller("faqBean")
@Scope("request")
public class FaqBean implements Serializable{

    private static final Log log = LogFactory.getLog( FaqBean.class );

	@Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private Faq faqObject;

    private List<Topic> topics;

    public FaqBean() {
    }
    
//    private void test(){
//    	Topic testTopic = new Topic();
//
//    	testTopic.getTopicInformation().getName();
//
//    	testTopic.getTopicInformation().getDescription();
//
//    	List<Item> items = testTopic.getItems();
//
//    	Item item = items.get(0);
//    	item.getId();
//    	item.getAnswer();
//    	item.getQuestion();
//    }

    @PostConstruct
    public void setup() {
        String faqXml = intactViewConfiguration.getFaqUrl();
        
        try {
            faqObject = FaqUtil.readFaq( faqXml );
            topics = faqObject.getTopics();
        } catch (Throwable e) {
            log.error( "Failure while initiaizing the FAQ from: " + faqXml, e );
        }
    }
    
    public List<Topic> getTopics(){
    	return topics;
    }
}
