/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business;

import org.apache.log4j.Logger;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.CvDagObjectUtils;
import uk.ac.ebi.intact.webapp.search.business.Constants;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This class provides methods which build a SQL like query out of the given information.
 *
 * @author Anja Friedrichsen
 * @version $Id:QueryBuilder.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 *
 * @deprecated
 */
public class QueryBuilder {

    //set up the logger
    private static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    // the different possible terms
    private static final int AC = 1;
    private static final int SHORTLABEL = 2;
    private static final int FULLNAME = 3;
    private static final int XREF = 4;
    private static final int ANNOTATION = 5;
    private static final int CV_INTERACTION = 6;
    private static final int CV_IDENTIFICATION = 7;
    private static final int CV_INTERACTIONTYPE = 8;
    private static final int FULLTEXT = 9;

    private static final int MAX_NUMBER_OF_TERMS = 9;

    private String searchClass;
    private String ac;
    private String shortlabel;
    private String description;
    private String fulltext;
    private String connection;
    private String xref;
    private String cvDB;
    private String annotation;
    private String cvTopic;
    private String cvInteraction;
    private String cvIdentification;
    private String cvInteractionType;
    private String interDetChildren;
    private String partDetChildren;
    private String interTypeChildren;


    /**
     * Constructs a QueryBuilder object.
     *
     * @param form a DynaActionForm object
     */
    public QueryBuilder( DynaActionForm form ) {
        // todo better: work with constants!

        this.searchClass = (String) form.get( "searchObject" );
        this.ac = (String) form.get( "acNumber" );
        this.shortlabel = (String) form.get( "shortlabel" );
        this.description = (String) form.get( "description" );
        this.fulltext = (String) form.get( "fulltext" );
        this.connection = (String) form.get( "connection" );
        this.xref = (String) form.get( "xRef" );
        this.cvDB = (String) form.get( "cvDB" );
        this.cvTopic = (String) form.get( "cvTopic" );
        this.annotation = (String) form.get( "annotation" );
        this.cvInteraction = (String) form.get( "cvInteraction" );
        this.cvIdentification = (String) form.get( "cvIdentification" );
        this.cvInteractionType = (String) form.get( "cvInteractionType" );
        this.interDetChildren = (String) form.get( "interDetChildren" );
        this.interTypeChildren = (String) form.get( "interTypeChildren" );
        this.partDetChildren = (String) form.get( "partDetChildren" );

        // cut the leading and ending blanks
        searchClass = searchClass.trim();
        ac = ac.trim();
        shortlabel = shortlabel.trim();
        description = description.trim();
        fulltext = fulltext.trim();
        xref = xref.trim();
        cvDB = cvDB.trim();
        cvTopic = cvTopic.trim();
        annotation = annotation.trim();
        cvInteraction = cvInteraction.trim();
        cvInteractionType = cvInteractionType.trim();
        cvIdentification = cvIdentification.trim();
    }

    /**
     * This method creates a SQL-like statement out of the properties of the form.
     * <pre>
     * It has several strings which are filled in this method and at the end they are connected together.
     * some of theses strings are:
     * - select:     which forms a simple select statement without any searchconditon
     *               the search object is taken from the form
     * - condition:  this is the most complex part of the SQL-like statement. The search condition is build of
     *               one or more value-comparison-term blocks which are connected with the connection string.
     * - where:      builds the where string for the query
     * - connection: the connection to combine the conditions
     * <p>
     * At the end the SQL-like statement is a concatenation of:
     *     select + where + leftPar + condition + rightPar + semicolon;
     * </pre>
     *
     * @return an sql statement.
     *
     * @throws IntactException
     */
    public String getSqlLikeStatement() throws IntactException {

        logger.info( "Xref: " + xref + " in database: " + cvDB );
        logger.info( "Annotation: " + annotation + " with topic: " + cvTopic );

        // SQL-like statement to be returned
        String sqlLikeStatement = null;
        // counts the number of the existing search conditions
        int conditionCount = 0;

        // 'select'-part of the sqlLikeStatement
        String select = "select " + searchClass + " from intact";
        // 'search condition'-part of the sqlLikeStatement
        String condition = "";
        // left parenthesis (if there is at least one search condition)
        String leftPar = "";
        // right parenthesis (if there is at least one search condition)
        String rightPar = "";
        // 'where'-part of the sqlLikeStatement
        String where = "";
        // part of the sqlLikeStatement to connect the search conditions
        // semicolon for the end of the statement
        String semicolon = ";";

        // set up the condition by parsing all textfields
        // check if there is a AC number specified
        if ( ! ( ( ac == null ) || "".equals( ac ) || "\"\"".equals( ac ) ) ) {
            // insert a connection if there is already a search condition, to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( AC, ac );

            conditionCount += 1;
        }
        // check if there is a description specified
        if ( ! ( ( description == null ) || ( "".equals( description ) ) || "\"\"".equals( description ) ) ) {
            // insert a connection, if there is already a search condition,
            // to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( FULLNAME, description );
            conditionCount += 1;
        }
        // check if there is a shortlabel specified
        if ( !( ( shortlabel == null ) || "".equals( shortlabel ) || ( "\"\"".equals( shortlabel ) ) ) ) {
            // insert a connection, if there is already a search condition,
            // to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( SHORTLABEL, shortlabel );
            conditionCount += 1;
        }

        // check if there is a fulltext search specified
        if ( !( ( fulltext == null ) || "".equals( fulltext ) || "\"\"".equals( fulltext ) ) ) {
            // insert a connection, if there is already a search condition,
            // to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( FULLTEXT, fulltext );
            conditionCount += 1;
        }

        // check if there is a xref specified
        if ( !( ( xref == null ) || "".equals( xref ) || "\"\"".equals( xref ) ) ) {
            // insert a connection, if there is already a search condition,
            // to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( XREF, xref );
            conditionCount += 1;
        }

        // check if there is a annotation specified
        if ( !( ( annotation == null ) || "".equals( annotation ) || "\"\"".equals( annotation ) ) ) {
            // insert a connection, if there is already a search condition,
            // to combine them
            if ( conditionCount != 0 ) {
                condition += " " + connection + " ";
            }
            condition += this.buildCondition( ANNOTATION, annotation );
            conditionCount += 1;
        }

        // check if there is a CvInteraction specified

        if ( !( ( cvInteraction == null ) || "".equals( cvInteraction ) || "\"\"".equals( cvInteraction ) ) ) {
            if ( ( !( cvInteraction.equals( "-no CvInteraction-" ) ) ) && ( searchClass.equalsIgnoreCase( "any" ) || searchClass.equalsIgnoreCase( "experiment" ) ) )
            {
                // insert a connection, if there is already a search condition,
                // to combine them
                if ( conditionCount != 0 ) {
                    condition += " " + connection + " ";
                }
                // check if the checkbox to search all Interaciton Detection children is checked
                if ( interDetChildren.equals( "true" ) ) {
                    logger.info( "interDetChildren: " + interDetChildren );
                    String subcondition = this.buildConditionCvWithChildren( cvInteraction, "interaction_ac" );
                    // the parent is already in the condition string, so we always need a OR-connection
                    // subcondition could be null if there is no child node
                    if ( subcondition != null ) {
                        // put quotes around the cvInteraction shortlabel to get exact that one,
                        // ohterwise lucene will split a term with two words into two term connected with or
                        condition += "(" + this.buildCondition( CV_INTERACTION, "\"" + cvInteraction + "\"" );
                        condition += " or " + subcondition + ")";
                        conditionCount += 1;
                    } else {
                        // put quotes around the cvInteraction shortlabel to get exact that one,
                        // ohterwise lucene will split a term with two words into two term connected with or
                        condition += this.buildCondition( CV_INTERACTION, "\"" + cvInteraction + "\"" );
                        conditionCount += 1;
                    }
                } else {
                    condition += this.buildCondition( CV_INTERACTION, "\"" + cvInteraction + "\"" );
                    conditionCount += 1;
                }
            }
        }

        // check if there is a CvIdentification specified
        if ( !( ( cvIdentification == null ) || "".equals( cvIdentification ) || "\"\"".equals( cvIdentification ) ) ) {
            if ( !( cvIdentification.equals( "-no CvIdentification-" ) ) && ( searchClass.equalsIgnoreCase( "any" ) || searchClass.equalsIgnoreCase( "experiment" ) ) )
            {
                // insert a connection, if there is already a search condition,
                // to combine them
                if ( conditionCount != 0 ) {
                    condition += " " + connection + " ";
                }

                // check if the checkbox to search all Participant Detection children is checked
                if ( partDetChildren.equals( "true" ) ) {
                    logger.info( "partDetChildren: " + partDetChildren );
                    logger.info( "parent: " + cvIdentification );
                    String subcondition = this.buildConditionCvWithChildren( cvIdentification, "identification_ac" );
                    // the parent is already in the condition string, so we always need a OR-connection
                    // subcondition could be null if there is no child node
                    if ( subcondition != null ) {
                        condition += "(" + this.buildCondition( CV_IDENTIFICATION, "\"" + cvIdentification + "\"" );
                        condition += " or " + subcondition + ")";
                        conditionCount += 1;
                    } else {
                        // put quotes around the cvIdentification shortlabel to get exact that one,
                        // ohterwise lucene will split a term with two words into two term connected with or
                        condition += this.buildCondition( CV_IDENTIFICATION, "\"" + cvIdentification + "\"" );
                        conditionCount += 1;
                    }
                } else {
                    // put quotes around the cvIdentification shortlabel to get exact that one,
                    // ohterwise lucene will split a term with two words into two term connected with or
                    condition += this.buildCondition( CV_IDENTIFICATION, "\"" + cvIdentification + "\"" );
                    conditionCount += 1;
                }
            }
        }
        // check if there is a CvInteractionType specified
        if ( !( ( cvInteractionType == null ) || "".equals( cvInteractionType ) || "\"\"".equals( cvInteractionType ) ) )
        {

            if ( !( cvInteractionType.equals( "-no CvInteractionType-" ) ) && ( searchClass.equalsIgnoreCase( "any" ) || searchClass.equalsIgnoreCase( "interaction" ) ) )
            {
                // insert a connection, if there is already a search condition,
                // to combine them
                if ( conditionCount != 0 ) {
                    condition += " " + connection + " ";
                }
                // check if the checkbox to search all CvInteracitonType children is checked
                if ( interTypeChildren.equals( "true" ) ) {
                    logger.info( "interTypeChildren: " + interTypeChildren );
                    String subcondition = this.buildConditionCvWithChildren( cvInteractionType, "interactiontype_ac" );
                    // the parent is already in the condition string, so we always need a OR-connection
                    // subcondition could be null if there is no child node
                    if ( subcondition != null ) {
                        // put quotes around the cvInteractionType shortlabel to get exact that one,
                        // ohterwise lucene will split a term with two words into two term connected with or
                        condition += "(" + this.buildCondition( CV_INTERACTIONTYPE, "\"" + cvInteractionType + "\"" );
                        condition += " or " + subcondition + ")";
                        conditionCount += 1;
                    } else {
                        // put quotes around the cvInteractionType shortlabel to get exact that one,
                        // ohterwise lucene will split a term with two words into two term connected with or
                        condition += this.buildCondition( CV_INTERACTIONTYPE, "\"" + cvInteractionType + "\"" );
                        conditionCount += 1;
                    }

                } else {

                    // put quotes around the cvInteractionType shortlabel to get exact that one,
                    // ohterwise lucene will split a term with two words into two term connected with or
                    condition += this.buildCondition( CV_INTERACTIONTYPE, "\"" + cvInteractionType + "\"" );
                    conditionCount += 1;
                }
            }
        }

        // insert the 'where'- and 'parenthesis'-part only
        // if there is at least one search condition
        if ( conditionCount != 0 ) {
            where = " where ";
            leftPar = "(";
            rightPar = ")";
        }
        // connect all parts to the full sqlLikeStatement
        sqlLikeStatement = select + where + leftPar + condition + rightPar + semicolon;
        logger.info( "SQLLIKE: " + sqlLikeStatement );
        return sqlLikeStatement;
    }

    // TODO question what should happen if the user types commas and plus in one textfield?

    /**
     * This method builds the search condition out of the value and the term. If the value contains commas it is an
     * multi-value and the parts of this value should be connected with 'or' If the value contains plus it is an
     * multi-value and the parts of this value should be connected with 'and' Otherwise it is a single value and can be
     * send to the method buildSingleCondition()
     *
     * @param term  the search term of the condition
     * @param value the value of the search condition
     *
     * @return the condition string for one term
     *
     * @throws IntactException ...
     */
    public String buildCondition( int term, String value ) throws IntactException {
        if ( value == null ) {
            throw new NullPointerException( "value is not allowed to be null " );
        }
        if ( !( ( 0 < term ) && ( term <= MAX_NUMBER_OF_TERMS ) ) ) {
            throw new NullPointerException( " that term is not specified" );
        }

        String condition = null;
        // if the value contains a comma it is a multi value which should be connected with or
        // the following regular expression match every string containing a comma
        if ( value.matches( ".+,.+" ) ) {
            // todo exception needed to be catched in the Action
            if ( value.matches( ".+\\+.+" ) ) {
                throw new IntactException( "no combination of plus and comma please!" );
            }
            condition = this.connectWithOr( term, value );
            // if the value contains a plus it is a multi value which should be connected with and
            // the following regular expression means every string containing a plus
        } else if ( value.matches( ".+\\+.+" ) ) {
            condition = this.connectWithAnd( term, value );

            // if the value either contains a comma nor a plus it is a single value
        } else {
            condition = this.buildSingleCondition( term, value );
        }
        return condition;
    }


    /**
     * if the value contains plus cut it at that points and build a single value per each and connect them with and.
     *
     * @param term  the search term of the condition
     * @param value the value of the search condition
     *
     * @return the condition string connected with and
     */
    public String connectWithAnd( int term, String value ) {
        if ( value == null ) {
            throw new NullPointerException( "value is not allowed to be null " );
        }
        if ( !( ( 0 < term ) && ( term <= MAX_NUMBER_OF_TERMS ) ) ) {
            throw new NullPointerException( " that term is not specified" );
        }

        String andCondition = "(";
        String token = null;
        StringTokenizer st = new StringTokenizer( value, "+" );
        while ( st.hasMoreTokens() ) {
            token = st.nextToken();
            andCondition += this.buildSingleCondition( term, token );
            // only insert an 'and' if there is another token following
            if ( st.hasMoreTokens() ) {
                andCondition += " and ";
            }
        }
        // if there is no more token add an closing parenthesis
        andCondition += ")";
        return andCondition;
    }


    /**
     * If the value contains commas cut it at that points and build a single value per each and connect them with or.
     *
     * @param term  the search term of the condition
     * @param value the value of the search condition
     *
     * @return the condition string connected with or
     */
    public String connectWithOr( int term, String value ) {
        if ( value == null ) {
            throw new NullPointerException( "value is not allowed to be null " );
        }
        if ( !( ( 0 < term ) && ( term <= MAX_NUMBER_OF_TERMS ) ) ) {
            throw new NullPointerException( " that term is not specified" );
        }

        String orCondition = "(";
        String token = null;
        StringTokenizer st = new StringTokenizer( value, "," );
        while ( st.hasMoreTokens() ) {
            token = st.nextToken();
            orCondition += this.buildSingleCondition( term, token );
            // only insert an 'or' if there is another token following
            if ( st.hasMoreTokens() ) {
                orCondition += " or ";
            }
        }
        // if there is no more token add an closing parenthesis
        orCondition += ")";
        return orCondition;
    }


    /**
     * Take the term and value and build a single search condition out of it. If the value contains a asterix choose the
     * comparisor 'like' otherwise choose '='. If the value contains any 'forbidden' sign replace it with a whitespace
     *
     * @param term  the search term of the condition
     * @param value the value of the search condition
     *
     * @return a single condition without any connection
     */
    public String buildSingleCondition( int term, String value ) {
        if ( value == null ) {
            throw new NullPointerException( "value is not allowed to be null " );
        }
        if ( !( ( 0 < term ) && ( term <= MAX_NUMBER_OF_TERMS ) ) ) {
            throw new NullPointerException( " that term is not specified" );
        }


        String singleCondition = null;

        // special case with fulltext: search with all termNames
        if ( term == FULLTEXT ) {
            if ( value.matches( ".*\\*.*" ) ) {
                singleCondition = "(ac like '" + value + "'";
                singleCondition += " or shortlabel like '" + value + "'";
                singleCondition += " or fullname like '" + value + "'";
                singleCondition += " or alias like '" + value + "'";
                singleCondition += " or annotation like '" + value + "'";
                singleCondition += " or xref like '" + value + "'";
                // search also for cvInteractionType if the searchClass is interaction
                if ( searchClass.equalsIgnoreCase( "interaction" ) ) {
                    singleCondition += "or interactiontype_shortlabel like '" + value + "'";
                    // search also for CvInteraction and CvIdentification if the searchClass is experiment
                } else if ( searchClass.equalsIgnoreCase( "experiment" ) ) {
                    singleCondition += "or interaction_shortlabel like '" + value + "'";
                    singleCondition += "or identification_shortlabel like '" + value + "'";
                }
                singleCondition += ")";
            } else {
                singleCondition = "(ac = '" + value + "'";
                singleCondition += " or shortlabel = '" + value + "'";
                singleCondition += " or fullname = '" + value + "'";
                singleCondition += " or alias = '" + value + "'";
                singleCondition += " or annotation = '" + value + "'";
                singleCondition += " or xref = '" + value + "'";
                // search also for cvInteractionType if the searchClass is interaction
                if ( searchClass.equalsIgnoreCase( "interaction" ) ) {
                    singleCondition += "or interactiontype_shortlabel = '" + value + "'";
                    // search also for CvInteraction and CvIdentification if the searchClass is experiment
                } else if ( searchClass.equalsIgnoreCase( "experiment" ) ) {
                    singleCondition += "or interaction_shortlabel = '" + value + "'";
                    singleCondition += "or identification_shortlabel = '" + value + "'";
                }
                singleCondition += ")";
            }
        } else {
            String termName = this.getTermName( term );
            // if the value contains a ' replace it with an whitespace
            // todo are there more signs that are not allowed??
            value = value.replaceAll( "'", " " );
            value = value.trim();
            if ( value.matches( ".*\\*.*" ) ) {
                singleCondition = termName + " like '" + value + "'";
            } else {
                singleCondition = termName + " = '" + value + "'";
            }
        }
        return singleCondition;
    }

    /**
     * Gets one shortlabel, which should be a CVDagObject shortlabel and creates an IQL condition that searches also for
     * all children of this CvDagObject.
     *
     * @param parentShortlabel parent cv to retrieve the children for.
     * @param term             the name of the lucene field to search in.
     *
     * @return a string that specifies the IQL condition to search also all children of the parent cv
     */
    private String buildConditionCvWithChildren( String parentShortlabel, String term ) {
        // string to be returned
        String condition = null;
        String acParent = null;
        // list of all children ACs
        Collection children = null;

        // retrieve the intact object for the parent
        CvDagObject parent = getDaoFactory().getCvObjectDao(CvDagObject.class).getByShortLabel(parentShortlabel);
        if ( parent == null ) {
            throw new IntactException( "invalid shortlabel: " + parentShortlabel );
        }
        acParent = parent.getAc();
        logger.info( "parents AC: " + acParent );
        CvDagObjectUtils dagUtil = new CvDagObjectUtils();

        logger.info( "dagUtil" + dagUtil );
        // get all children ACs
        children = dagUtil.getCvWithChildren( acParent );
        // start the condition string with the name of the lucene field
        condition = term + " = '";

        logger.info( "children: " + children );
        if ( children.isEmpty() ) {
            return null;
        }
        for ( Iterator iterator = children.iterator(); iterator.hasNext(); ) {
            String childAc = (String) iterator.next();
            // add all children ac to the condition
            condition += childAc + " ";
        }

        // end the condition
        condition += "'";

        return condition;
    }

    /**
     * This method returns a term name depending on the given number.
     *
     * @param term number to specify the term name
     *
     * @return term name
     */
    public String getTermName( int term ) {
        String termName = null;

        switch ( term ) {
            case 1:
                termName = "ac";
                break;
            case 2:
                termName = "shortlabel";
                break;
            case 3:
                termName = "fullname";
                break;
            case 4:
                if ( cvDB.equalsIgnoreCase( "-all databases-" ) ) {
                    termName = "xref";
                } else {
                    termName = cvDB;
                }
                break;
            case 5:
                if ( cvTopic.equalsIgnoreCase( "-all topics-" ) ) {
                    termName = "annotation";
                } else {
                    termName = cvTopic;
                }
                break;
            case 6:
                termName = "interaction_shortlabel";
                break;
            case 7:
                termName = "identification_shortlabel";
                break;
            case 8:
                termName = "interactiontype_shortlabel";
                break;
        }
        return termName;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}