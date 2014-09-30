package uk.ac.ebi.intact.service.complex.ws;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/complex-ws-servlet.xml"})
public class AppTests extends IntactBasicTestCase {
    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected WebApplicationContext wac;
    protected int numberOfComplexes = 742; //You have to update this value
                                           //when you indexed new complexes

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Ignore
    @Test
    public void searchHeader() throws Exception {
        // Without Header -> It must be a Json response
        mockMvc.perform(get("/search/*"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(numberOfComplexes))
        ;
        // Test Header Json
        mockMvc.perform(get("/search/*").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        ;
        // Test Header XML and return a Json
        mockMvc.perform(get("/search/*").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
        ;
    }

    @Ignore
    @Test
    public void searchFormatParameter() throws Exception {
        // Test Parameter Json
        mockMvc.perform(get("/search/*?format=json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(numberOfComplexes))
        ;
        // Test Parameter XML
        mockMvc.perform(get("/search/*?format=xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
        ;
    }

    @Ignore
    @Test
    public void searchFirstParameter() throws Exception {
        int offset = 21; // random value
        StringBuilder query = new StringBuilder() .append("/search/*?first=")
                                                  .append(offset);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(numberOfComplexes - offset))
        ;
    }

    @Ignore
    @Test
    public void searchNumberParameter() throws Exception {
        int number = 50; // random value
        StringBuilder query = new StringBuilder() .append("/search/*?number=")
                .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(number))
        ;
    }

    @Ignore
    @Test
    public void searchFirstAndNumberParameters() throws Exception {
        int offset = 21;
        int number = 50;
        StringBuilder query = new StringBuilder() .append("/search/*?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(number))
        ;

        // Ask for two elements only
        offset = 0;
        number = 2;
        query = new StringBuilder() .append("/search/EBI-1250344 EBI-2529973?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(number))
        ;
        // Ask for two elements, but only return one
        offset = 1;
        number = 2;
        query = new StringBuilder() .append("/search/EBI-1250344 EBI-2529973?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(1))
        ;
        // Ask for three elements, but only return two
        offset = 0;
        number = 3;
        query = new StringBuilder() .append("/search/EBI-1250344 EBI-2529973?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(2))
        ;
        // Ask for three elements, but only return one
        offset = 1;
        number = 3;
        query = new StringBuilder() .append("/search/EBI-1250344 EBI-2529973?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(1))
        ;
        // Ask for two elements, but return nothing
        offset = 3;
        number = 2;
        query = new StringBuilder() .append("/search/EBI-1250344 EBI-2529973?first=")
                .append(offset) .append("&number=") .append(number);
        mockMvc.perform(get(query.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // this test is dependent on the number of indexed complexes
                .andExpect(jsonPath("$.complexRestResult.size").value(0))
        ;
    }

    @Ignore
    @Test
    public void testDetails() throws Exception {
        mockMvc.perform(get("/details/EBI-1245484?format=json"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}
