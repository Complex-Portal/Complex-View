package uk.ac.ebi.intact.service.complex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.springframework.web.servlet.view.xml.MarshallingView;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSolrSearcher;

import javax.xml.bind.JAXBException;
import javax.xml.stream.events.Comment;
import java.util.Arrays;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ComplexSolrSearcher searcher ;
    private DataProvider dataProvider = new DataProvider() ;

    @RequestMapping(value = "/{query}",method = RequestMethod.GET)
	public ComplexRestResult search(@PathVariable String query/*, @RequestParam (required = false) String format*/) {
        //if (format == null) format = "json";
        return dataProvider.getData(query, this.searcher);
	}

   /* @Bean
    public ViewResolver viewResolver() throws JAXBException {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Comments.class, Comment.class);
        MarshallingView marshallingView = new MarshallingView(jaxb2Marshaller);

        ContentNegotiatingViewResolver contentNegotiatingViewResolver =
                new ContentNegotiatingViewResolver();
        contentNegotiatingViewResolver.setDefaultViews(Arrays.<View>
                asList(new MappingJacksonJsonView(), marshallingView));
        return contentNegotiatingViewResolver;
    }
   */
}
