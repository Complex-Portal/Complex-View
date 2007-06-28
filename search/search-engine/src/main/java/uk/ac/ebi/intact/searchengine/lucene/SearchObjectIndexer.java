package uk.ac.ebi.intact.searchengine.lucene;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.intact.searchengine.SearchEngineConstants;
import uk.ac.ebi.intact.searchengine.lucene.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides methods to create a lucene index for the search objects (Experiment, Interaction, Protein,
 * CvObject) and to transform a IntAct object into a Lucene document. It contains different methods for different
 * indexing proceeding. The fastest index proceeding is the one using the RAM directory.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class SearchObjectIndexer {

    // object to control synchronization
    private final static Object mutex = new Object();

    // file where to write the index
    private File index = null;

    // analyzer that should be used for indexing
    private final Analyzer analyzer;

    /**
     * Constructs a SearchObjectIndexer object.
     */
    public SearchObjectIndexer() {
        analyzer = new IntactAnalyzer();
    }

    /**
     * this constructor was used for the trivial indexing method.
     *
     * @param anAnalyzer analyzer to analyze the text that should be indexed
     * @param dir        name of the directory to store the index
     */
    public SearchObjectIndexer( final Analyzer anAnalyzer, final String dir ) {
        System.out.println( "SearchObjectIndexer with dir: " + dir );
        this.analyzer = anAnalyzer;
        index = new File( dir );
        if ( !index.exists() ) {
            index.mkdir();
            System.out.println( "created dir: " + index.getPath() );
        }
        synchronized ( mutex ) {
            final IndexWriter writer;
            try {
                writer = new IndexWriter( index, new IntactAnalyzer(), true );
                writer.close();
            } catch ( IOException e ) {
                throw new RuntimeException( "unable to create Index on Filesystem", e );
            }
        }

    }

    /**
     * This method is just a dispatcher that chooses the right indexing method for the specific search object. It is
     * only used for the simple indexing.
     *
     * @param searchObject the object to create the index for
     */
    public void createIndex( final SearchObject searchObject ) {

        if ( ( ExperimentSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            createExperimentIndex( ( ExperimentSearchObject ) searchObject );
        } else if ( ( InteractionSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            createInteractionIndex( ( InteractionSearchObject ) searchObject );
        } else if ( ( ProteinSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            createProteinIndex( ( ProteinSearchObject ) searchObject );
        } else if ( ( CvSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            createCvObjectIndex( ( CvSearchObject ) searchObject );
        } else if ( ( BioSourceSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            createBioSourceObjectIndex( ( BioSourceSearchObject ) searchObject );
        }
    }


    /**
     * This method transforms a search object into a lucene document. It distributes the search objects to the
     * respective method to transform them into a lucene document It is used for the indexing using the RAM directory.
     *
     * @param searchObject object that holds the information that should be indexed
     *
     * @return lucene document containing the information of the search object
     */
    public Document getDocument( final SearchObject searchObject ) {
        Document document = new Document();
        if ( ( ExperimentSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            document = createExperimentDoc( ( ExperimentSearchObject ) searchObject, document );
        } else if ( ( InteractionSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            document = createInteractionDoc( ( InteractionSearchObject ) searchObject, document );
        } else if ( ( ProteinSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            document = createProteinDoc( ( ProteinSearchObject ) searchObject, document );
        } else if ( ( CvSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            document = createCvObjectDoc( ( CvSearchObject ) searchObject, document );
        } else if ( ( BioSourceSearchObject.class.isAssignableFrom( searchObject.getClass() ) ) ) {
            document = createBioSourceObjectDoc( ( BioSourceSearchObject ) searchObject, document );
        }
        return document;
    }

    /**
     * This method creates a Lucene document from a protein search object. A protein search object holds only the
     * standard search object fields.
     *
     * @param protein search object to be transformed
     * @param doc     a new document to be filled
     *
     * @return resulting lucene document repectivly to the protein search object
     */
    private Document createProteinDoc( ProteinSearchObject protein, Document doc ) {
        // add the standard fields to the document
        this.addSearchObjectFields( doc, protein );
        return doc;
    }

    /**
     * This method creates a Lucene document from a cv search object. A cv search object holds only the standard search
     * object fields.
     *
     * @param cvObject search object to be transformed into a lucene document
     * @param doc      a new document to be filled
     *
     * @return resulting lucene document respectivly to the cv search object
     */
    private Document createCvObjectDoc( CvSearchObject cvObject, Document doc ) {
        // add the standard fields to the document
        this.addSearchObjectFields( doc, cvObject );
        return doc;
    }


    /**
     * This method creates a Lucene document from an Interaction search object. An interaction search object has in
     * addition to the standard search objects fields, the fields that describe the interaction type.
     *
     * @param interaction search object to be transformed into a Lucene document
     * @param doc         a new document to be filled
     *
     * @return resulting lucene document respectivly to the interaction search object
     */
    private Document createInteractionDoc( InteractionSearchObject interaction, Document doc ) {
        // add the standard fields to the document
        this.addSearchObjectFields( doc, interaction );

        // add the interaction type of the interaction to the document
        CvSearchObject cvInterType = interaction.getCvInteractionsType();
        // only add the CvInteractionType to the document, if it really exists
        if ( cvInterType != null ) {
            doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_AC, cvInterType.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvInterType.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_SHORTLABEL, cvInterType.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvInterType.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_FULLNAME, cvInterType.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }
        return doc;
    }

    /**
     * This method creates a Lucene document from an Experiment search object. An experiment search object has in
     * addition to the standard search objects fields, the fields that describe the interaction detection method and the
     * participant detection method.
     *
     * @param experiment earch object to be transformed into a Lucene document
     * @param doc        a new document to be filled
     *
     * @return resulting lucene document respectivly to the experiment search object
     */
    private Document createExperimentDoc( ExperimentSearchObject experiment, Document doc ) {
        // add the standard fields to the document
        this.addSearchObjectFields( doc, experiment );

        // add the Identification method to the experiment document
        CvSearchObject cvIdent = experiment.getCvIdentification();
        // only add the CvIdentification to the document, if it really exists
        if ( cvIdent != null ) {
            doc.add( new Field( SearchEngineConstants.IDENT_AC, cvIdent.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvIdent.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.IDENT_SHORTLABEL, cvIdent.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvIdent.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.IDENT_FULLNAME, cvIdent.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }

        // add the interaction detection method to the experiment to the document
        CvSearchObject cvInter = experiment.getCvInteraction();
        // only add the CvInteraction to the document, if it really exists
        if ( cvInter != null ) {
            doc.add( new Field( SearchEngineConstants.INTERACTION_AC, cvInter.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvInter.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_SHORTLABEL, cvInter.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvInter.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_FULLNAME, cvInter.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }
        return doc;
    }


    /**
     * This method creates a Lucene document from a biosource search object. A biosource search object holds only the
     * standard search object fields.
     *
     * @param biosource search object to be transformed into a lucene document
     * @param doc       a new document to be filled
     *
     * @return resulting lucene document respectivly to the biosource search object
     */
    private Document createBioSourceObjectDoc( BioSourceSearchObject biosource, Document doc ) {
        // add the standard fields to the document
        this.addSearchObjectFields( doc, biosource );
        return doc;
    }


    /**
     * This method creates a lucene document for a specific experiment search object and adds that document to the
     * index.. The created document contains the standard fields, like ac, shortlabel, fullname, xrefs and annotation.
     * In addition to that fields for the CvIdentificaion and the CvInteraction are also added to the document.
     * <p/>
     * It was used for the trivial indexing, but not for the RAM indexing.
     *
     * @param experiment object to create a lucene document of
     */
    public void createExperimentIndex( final ExperimentSearchObject experiment ) {

        final Document doc = new Document();
        // add the standard fields to the document
        this.addSearchObjectFields( doc, experiment );

        // add the Identification method to the experiment document
        CvSearchObject cvIdent = experiment.getCvIdentification();
        // only add the CvIdentification to the document, if it really exists
        if ( cvIdent != null ) {
            doc.add( new Field( SearchEngineConstants.IDENT_AC, cvIdent.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvIdent.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.IDENT_SHORTLABEL, cvIdent.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvIdent.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.IDENT_FULLNAME, cvIdent.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }

        // add the interaction detection method to the experiment to the document
        CvSearchObject cvInter = experiment.getCvInteraction();
        // only add the CvInteraction to the document, if it really exists
        if ( cvInter != null ) {
            doc.add( new Field( SearchEngineConstants.INTERACTION_AC, cvInter.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvInter.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_SHORTLABEL, cvInter.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvInter.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_FULLNAME, cvInter.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }
        // write the document to the index
        this.writeDocument( doc );
    }

    /**
     * This method creates a lucene document for a specific interaction search object and adds that document to the
     * index. The created document contains the standard fields, like ac, shortlabel, fullname, xrefs and annotation. In
     * addition to that fields for the CvInteractionType are also added to the document.
     * <p/>
     * It was used for the trivial indexing, but not for the RAM indexing.
     *
     * @param interaction object to create a lucene document of
     */
    public void createInteractionIndex( final InteractionSearchObject interaction ) {

        final Document doc = new Document();
        // add the standard fields to the document
        this.addSearchObjectFields( doc, interaction );

        // add the interaction detection method to the experiment to the document
        CvSearchObject cvInterType = interaction.getCvInteractionsType();
        // only add the CvInteraction to the document, if it really exists
        if ( cvInterType != null ) {
            doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_AC, cvInterType.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            if ( cvInterType.getShortLabel() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_SHORTLABEL, cvInterType.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
            if ( cvInterType.getFullName() != null ) {
                doc.add( new Field( SearchEngineConstants.INTERACTION_TYPE_FULLNAME, cvInterType.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }
        }
        this.writeDocument( doc );
    }

    /**
     * This method creates a lucene document for a specific protein search object and adds that document to the index.
     * The created document contains the standard fields, like ac, shortlabel, fullname, xrefs and annotation. At the
     * moment there are no additional fields.
     * <p/>
     * It was used for the trivial indexing, but not for the RAM indexing.
     *
     * @param protein to create a lucene document of
     */
    public void createProteinIndex( final ProteinSearchObject protein ) {

        final Document doc = new Document();
        // add the standard fields to the document
        this.addSearchObjectFields( doc, protein );

        this.writeDocument( doc );
    }

    /**
     * This method creates a lucene document for a specific cv search object and adds that document to the index. The
     * created document contains the standard fields, like ac, shortlabel, fullname, xrefs and annotation. At the moment
     * there are no additional fields.
     * <p/>
     * It was used for the trivial indexing, but not for the RAM indexing.
     *
     * @param cvObject to create a lucene document of
     */
    public void createCvObjectIndex( final CvSearchObject cvObject ) {

        final Document doc = new Document();
        // add the standard fields to the document
        this.addSearchObjectFields( doc, cvObject );
        this.writeDocument( doc );

    }

    /**
     * This method creates a lucene document for a specific biosource search object and adds that document to the index.
     * The created document contains the standard fields, like ac, shortlabel, fullname, xrefs and annotation. At the
     * moment there are no additional fields.
     * <p/>
     * It was used for the trivial indexing, but not for the RAM indexing.
     *
     * @param bioSourceObject to create a lucene document of
     */
    public void createBioSourceObjectIndex( BioSourceSearchObject bioSourceObject ) {
        final Document doc = new Document();
        // add the standard fields to the document
        this.addSearchObjectFields( doc, bioSourceObject );
        this.writeDocument( doc );
    }


    /**
     * This method fills the document (first argument) with the information of the search object (second argument). It
     * just fills the standard fields that are in all search objects
     *
     * @param doc          lucene document to insert the information and to be returned
     * @param searchObject search object holding the information to be inserted into the fields
     *
     * @return lucene document having all information in fields
     */
    private Document addSearchObjectFields( final Document doc, final SearchObject searchObject ) {


        doc.add( new Field( SearchEngineConstants.AC, searchObject.getAc(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        // it is not allowed to index a field with null, therefore check if it is not null
        if ( searchObject.getShortLabel() != null ) {
            doc.add( new Field( SearchEngineConstants.SHORTLABEL, searchObject.getShortLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }
        if ( searchObject.getFullName() != null ) {
            doc.add( new Field( SearchEngineConstants.FULLNAME, searchObject.getFullName(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }
        if ( searchObject.getObjClass() != null ) {
            doc.add( new Field( SearchEngineConstants.OBJCLASS, searchObject.getObjClass(), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }
        if ( searchObject.getAnnotations().isEmpty() == false ) {
            this.addAnnotations( doc, searchObject.getAnnotations() );
        }
        if ( searchObject.getXRefs().isEmpty() == false ) {
            this.addXrefs( doc, searchObject.getXRefs() );
        }
        if ( searchObject.getAlias().isEmpty() == false ) {
            this.addAlias( doc, searchObject.getAlias() );
        }
        return doc;
    }

    /**
     * This method adds all annotation fields to the Lucene document. The information about the annotations is given in
     * a Map, as an argument. The key of the map is always the fieldname (annotation topic) and the value is a
     * collection of annotaions to fill the field. The key will be the name of the field and the annotation is the
     * content to be searched for in the index.
     *
     * @param doc         document to hold the fields
     * @param annotations Map containing annotation information. The key is the topic name and the value the
     *                    annotation.
     *
     * @return document filled with the annotation information
     */
    private Document addAnnotations( final Document doc, Map annotations ) {

        // Map containing all annotations with their topics
        IterableMap annotMap = new HashedMap();
        annotMap.putAll( annotations );
        MapIterator it = annotMap.mapIterator();
        // iterate through the map and add the fields to the documents
        while ( it.hasNext() ) {
            String key = ( String ) it.next();
            Collection value = ( Collection ) it.getValue();
            if ( !value.isEmpty() ) {
                for ( Iterator iterator = value.iterator(); iterator.hasNext(); ) {
                    String content = ( String ) iterator.next();
                    // the key is the cvTopic of the annotation
                    doc.add( new Field( key, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                    // add all values to the field 'annotation'
                    doc.add( new Field( SearchEngineConstants.ANNOTATION, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                }
            }
        }
        return doc;
    }

    /**
     * This method adds all cross reference fields to the given Lucene document. The xref information is stored in a
     * Map, which key is the name of the database and the value is a list of primaryIDs. The key is going to be the
     * field name and the primaryIDs are the content of the field to be searched for in the index.
     *
     * @param doc   document to hold the fields
     * @param xrefs Map containing cross reference information. The key is the name of the database and the value is a
     *              list of all primaryIDs
     *
     * @return document filled with all xref information
     */
    private Document addXrefs( final Document doc, Map xrefs ) {

        // map containing all xrefs with their database names
        IterableMap xrefMap = new HashedMap();
        xrefMap.putAll( xrefs );
        MapIterator it = xrefMap.mapIterator();
        // iterate through the map and add the fields to the documents
        while ( it.hasNext() ) {
            String key = ( String ) it.next();
            Collection value = ( Collection ) it.getValue();
            if ( !value.isEmpty() ) {
                for ( Iterator iterator = value.iterator(); iterator.hasNext(); ) {
                    String content = ( String ) iterator.next();
                    //the lucene document field is named after the name of the database
                    doc.add( new Field( key, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                    // add all values to the field xrefs
                    doc.add( new Field( SearchEngineConstants.XREF, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                }
            }
        }
        return doc;
    }

    /**
     * This method adds all alias fields to the given Lucene document. The alias information is stored in a Map, which
     * key is the type of the alias and the value is a list of the names of the alias. The key is going to be the field
     * name and the alias names are the content of the field to be searched for in the index.
     *
     * @param doc   document to hold the fields
     * @param alias Map containing alias information. The key is the type of the alias and the value is a list of all
     *              alias names
     *
     * @return document filled with all alias information
     */
    private Document addAlias( final Document doc, Map alias ) {

        // map containing all alias types with their alias names
        IterableMap aliasMap = new HashedMap();
        aliasMap.putAll( alias );

        MapIterator it = aliasMap.mapIterator();
        // iterate through the map and add the fields to the documents
        while ( it.hasNext() ) {
            String key = ( String ) it.next();
            Collection value = ( Collection ) it.getValue();
            if ( !value.isEmpty() ) {
                for ( Iterator iterator = value.iterator(); iterator.hasNext(); ) {
                    String content = ( String ) iterator.next();
                    // the lucene document is named after the alias type
                    doc.add( new Field( key, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                    // add all values to the field alias
                    doc.add( new Field( SearchEngineConstants.ALIAS, content, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
                }
            }
        }
        return doc;
    }

    /**
     * This method was used for the trivial indexing. It gets a lucene document and adds this document to the lucene
     * index.
     *
     * @param doc lucene document to be inserted into the index
     */
    private void writeDocument( final Document doc ) {
        synchronized ( mutex ) {
            final IndexWriter writer;

            try {
                writer = new IndexWriter( index, analyzer, false );
                writer.setMergeFactor( 100 );
                writer.addDocument( doc );
                writer.optimize();
                writer.close();
            } catch ( IOException e ) {
                new RuntimeException( "Problems writing the Index", e );
            }
        }
    }

    /**
     * This method was used with the indexing using threads. It merges the given temporary index directory (dir) to the
     * final index directory (index).
     *
     * @param dir directory to be merged to the index
     */
    public void mergeIndexes( Directory[] dir ) {
        synchronized ( mutex ) {
            final IndexWriter writer;
            try {
                writer = new IndexWriter( index, analyzer, false );
                writer.setMergeFactor( 100 );
                writer.addIndexes( dir );
                writer.optimize();
                writer.close();
            } catch ( IOException e ) {
                new RuntimeException( "Problems merging the Indexes", e );
            }
        }
    }
}



