package uk.ac.ebi.intact.service.psicquic.ws;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.*;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.converter.EntryConverter;
import psidev.psi.mi.xml.jaxb.EntryType;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactConfigurator;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.persistence.dao.entry.IntactEntryFactory;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * PSICQUIC Web Service
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: SearchServiceClient.java 8975 2007-07-17 13:26:43Z baranda $
 */
@WebService(endpointInterface = "org.hupo.psi.mi.psicquic.PsicquicPortType")
public class Search implements PsicquicPortType {

    private static final Log log = LogFactory.getLog(Search.class);

    private IntactSession intactSession;

    @Resource
    WebServiceContext ctx;

    private static final List<String> SUPPORTED_DATA_TYPES = Arrays.asList("mif");

    public Search() {

    }

    public synchronized void initialize() {
        if (ctx == null) {
            throw new NullPointerException("WebServiceContext");
        }

        MessageContext mc = ctx.getMessageContext();

        ServletContext sContext = (ServletContext) mc.get(MessageContext.SERVLET_CONTEXT);
        ServletRequest request = (ServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        this.intactSession = new WebappSession(sContext, null, (HttpServletRequest) request);

        // start the intact application (e.g. load Institution, etc)
        IntactConfigurator.createIntactContext(intactSession);
    }

    public synchronized void initialize(IntactSession intactSession) {
        this.intactSession = intactSession;

        // start the intact application (e.g. load Institution, etc)
        IntactConfigurator.createIntactContext(intactSession);
    }

    public QueryResponse getByInteractor(DbRefRequestType dbRef) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        checkResultType(dbRef.getResultType());

        beginTransaction();

        IntactEntry intactEntry = IntactEntryFactory.createIntactEntry(IntactContext.getCurrentInstance())
                .addInteractorWithAc(dbRef.getDbRef().getAc());

        EntrySet entrySet = PsiExchange.exportToEntrySet(intactEntry);

        psidev.psi.mi.xml.jaxb.EntrySet jaxbEntrySet = new psidev.psi.mi.xml.jaxb.EntrySet();

        EntryConverter entryConverter = new EntryConverter();

        try {
            for (Entry entry : entrySet.getEntries()) {
                EntryType jaxbEntry = entryConverter.toJaxb(entry);
                jaxbEntrySet.getEntries().add(jaxbEntry);
            }
        } catch (ConverterException e) {
            throw new PsicquicServiceException("Problem converting EntrySet", null, e);
        }

        commitTransaction();

        QueryResponse response = new QueryResponse();

        ResultSetType resultSetType = new ResultSetType();
        resultSetType.setEntrySet(jaxbEntrySet);
        response.setResultSet(resultSetType);

        return response;
    }

    public QueryResponse getByInteraction(DbRefRequestType dbRef) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByInteractorList(DbRefListRequestType dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getBetweenList(DbRefListRequestType dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByInteractionList(GetByInteractionListRequest dbRefList) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public QueryResponse getByQuery(GetByQueryStringRequest query) throws NotSupportedDataTypeException, NotSupportedMethodException, PsicquicServiceException {
        return null;
    }

    public String getVersion() throws PsicquicServiceException {
        Properties props = new Properties();
        try {
            props.load(Search.class.getResourceAsStream("/META-INF/BuildInfo.properties"));
        } catch (IOException e) {
            throw new PsicquicServiceException("Problem loading BuildInfo properties", null, e);
        }
        return (String) props.get("version");
    }

    public SupportedDataTypes getSupportedDataTypes() throws PsicquicServiceException {
        SupportedDataTypes supportedDataTypes = new SupportedDataTypes();
        supportedDataTypes.getDataTypes().add("mif");
        //supportedDataTypes.getDataTypes().add("mitab");
        return supportedDataTypes;
    }

    private void checkResultType(String resultType) throws NotSupportedDataTypeException {
        if (!SUPPORTED_DATA_TYPES.contains(resultType)) {
            throw new NotSupportedDataTypeException("Type is not supported: "+resultType+" - Supported types are "+SUPPORTED_DATA_TYPES, null);
        }
    }

    private void beginTransaction() {
        initialize();
        getDataContext().getDaoFactory().beginTransaction();
    }

    private void commitTransaction() {
        try {
            getDataContext().commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
           getDataContext().getDaoFactory().getEntityManager().close();
        }
    }

    private DataContext getDataContext() {
        return IntactContext.getCurrentInstance().getDataContext();
    }
}
