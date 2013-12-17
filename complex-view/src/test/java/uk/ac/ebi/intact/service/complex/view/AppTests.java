package uk.ac.ebi.intact.service.complex.view;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml")
public class AppTests {
    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected WebApplicationContext wac;
    @Autowired
    protected RestConnection restConnection;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void simple() throws Exception {
        mockMvc.perform(post("/").param("query", "saccharomyces cerevisiae"))
                .andExpect(status().isOk())
                .andExpect(view().name("results"));
    }

    @Test
    public void getPageNumber() throws Exception {
        String page = "1";
        ComplexRestResult result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), 0);
        Assert.assertEquals(result.getPage(), 1);
        Assert.assertEquals(result.getNextPage(), 2);
        page = "2";
        result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), 1);
        Assert.assertEquals(result.getPage(), 2);
        Assert.assertEquals(result.getNextPage(), 3);
        page = "0";
        result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), -1);
        Assert.assertEquals(result.getPage(), 0);
        Assert.assertEquals(result.getNextPage(), 1);
        page = "-1";
        result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), -1);
        Assert.assertEquals(result.getPage(), 0);
        Assert.assertEquals(result.getNextPage(), 1);
        page = "-100";
        result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), -1);
        Assert.assertEquals(result.getPage(), 0);
        Assert.assertEquals(result.getNextPage(), 1);
        page = "20000000";
        result = restConnection.query("*", page, null, null);
        Assert.assertEquals(result.getPrevPage(), result.getLastPage() - 1);
        Assert.assertEquals(result.getPage(), result.getLastPage());
        Assert.assertEquals(result.getNextPage(), -1);
    }

}
