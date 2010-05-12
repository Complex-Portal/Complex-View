/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.cvobject.CvObjectService;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AnnotatedObjectController extends JpaAwareController implements ValueChangeAware {

    private static final Log log = LogFactory.getLog( AnnotatedObjectController.class );

    private boolean unsavedChanges;
    private Date lastSaved;

    @Autowired
    private CvObjectService cvObjectService;

    public AnnotatedObjectController() {
    }

    public abstract AnnotatedObject getAnnotatedObject();

    public void doSave( ActionEvent evt ) {        
        PersistenceController persistenceController = getPersistenceController();
        boolean saved = persistenceController.doSave(getAnnotatedObject());

        if (saved) {
            lastSaved = new Date();
            unsavedChanges = false;
        }
    }

    @Override
    public void changed(AjaxBehaviorEvent evt) {
        unsavedChanges = true;
    }

    // XREFS
    ///////////////////////////////////////////////

    public void newXref( ActionEvent evt ) {
        Xref xref = newXrefInstance();
        getAnnotatedObject().addXref( xref );
        setUnsavedChanges( true );
    }

    private Xref newXrefInstance() {
        Class<? extends Xref> xrefClass = AnnotatedObjectUtils.getXrefClassType( getAnnotatedObject().getClass() );

        Xref xref = null;
        try {
            xref = xrefClass.newInstance();
        } catch ( Throwable e ) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext( ctx, e );
            ctx.getApplication().publishEvent( ctx, ExceptionQueuedEvent.class, eventContext );
        }

        xref.setOwner( getIntactContext().getInstitution() );
        return xref;
    }

    public List<Xref> getXrefs() {
        if ( getAnnotatedObject() == null ) {
            return Collections.EMPTY_LIST;
        }

        final ArrayList<Xref> xrefs = new ArrayList<Xref>( getAnnotatedObject().getXrefs() );
        Collections.sort( xrefs, new IntactObjectComparator() );
        return xrefs;
    }

    public void setXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        if ( primaryId != null && !primaryId.isEmpty() ) {
            replaceOrCreateXref( databaseIdOrLabel, qualifierIdOrLabel, primaryId );
        } else {
            removeXref( databaseIdOrLabel, qualifierIdOrLabel );
        }
    }

    public void replaceOrCreateXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        AnnotatedObject parent = getAnnotatedObject();

        // modify if exists
        boolean exists = false;

        for ( Object objXref : parent.getXrefs() ) {
            Xref xref = ( Xref ) objXref;

            if ( xref.getCvDatabase() != null ) {
                if ( databaseIdOrLabel.equals( xref.getCvDatabase().getIdentifier() )
                     || databaseIdOrLabel.equals( xref.getCvDatabase().getShortLabel() ) ) {
                    if ( xref.getCvXrefQualifier() == null || qualifierIdOrLabel == null ) {
                        if ( !primaryId.equals( xref.getPrimaryId() ) ) {
                            xref.setPrimaryId( primaryId );
                        }
                    } else if ( qualifierIdOrLabel.equals( xref.getCvXrefQualifier().getIdentifier() )
                                || qualifierIdOrLabel.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {
                        if ( !primaryId.equals( xref.getPrimaryId() ) ) {
                            xref.setPrimaryId( primaryId );
                        }
                    }

                    exists = true;
                }
            }
        }

        // create if not exists
        if ( !exists ) {
            addXref( databaseIdOrLabel, qualifierIdOrLabel, primaryId );
        }
    }

    public void removeXref( String databaseIdOrLabel, String qualifierIdOrLabel ) {
        Iterator<Xref> iterator = getAnnotatedObject().getXrefs().iterator();

        while ( iterator.hasNext() ) {
            Xref xref = iterator.next();
            if ( databaseIdOrLabel.equals( xref.getCvDatabase().getIdentifier() ) || databaseIdOrLabel.equals( xref.getCvDatabase().getShortLabel() ) ) {
                if ( qualifierIdOrLabel == null || xref.getCvXrefQualifier() == null ) {
                    iterator.remove();
                } else if ( qualifierIdOrLabel.equals( xref.getCvXrefQualifier().getIdentifier() ) || qualifierIdOrLabel.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {
                    iterator.remove();
                }
            }
        }
    }

    public void removeXref( Xref xref ) {
        getAnnotatedObject().removeXref( xref );
        setUnsavedChanges( true );
    }

    public void addXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        CvDatabase db = cvObjectService.findCvObject( CvDatabase.class, databaseIdOrLabel );
        CvXrefQualifier qual = cvObjectService.findCvObject( CvXrefQualifier.class, qualifierIdOrLabel );

        Xref xref = newXrefInstance();
        xref.setCvDatabase( db );
        xref.setCvXrefQualifier( qual );
        xref.setPrimaryId( primaryId );

        getAnnotatedObject().addXref( xref );
    }

    public String findXrefPrimaryId( String databaseId, String qualifierId ) {
        final AnnotatedObject ao = getAnnotatedObject();

        Collection<Xref> xrefs = AnnotatedObjectUtils.searchXrefs( ao, databaseId, qualifierId );

        if ( !xrefs.isEmpty() ) {
            return xrefs.iterator().next().getPrimaryId();
        }

        return null;

    }

    // ANNOTATIONS
    ///////////////////////////////////////////////

    public void newAnnotation( ActionEvent evt ) {
        Annotation annotationWithNullTopic = new Annotation() {
            @Override
            public void setCvTopic( CvTopic cvTopic ) {
                if ( cvTopic != null ) {
                    super.setCvTopic( cvTopic );
                }
            }
        };
        getAnnotatedObject().addAnnotation( annotationWithNullTopic );
        setUnsavedChanges( true );
    }

    public void addAnnotation( String topicIdOrLabel, String text ) {
        CvTopic dataset = cvObjectService.findCvObject( CvTopic.class, topicIdOrLabel );

        Annotation annotation = new Annotation( getIntactContext().getInstitution(), dataset );
        annotation.setAnnotationText( text );

        getAnnotatedObject().addAnnotation( annotation );
    }

    public void replaceOrCreateAnnotation( String topicOrShortLabel, String text ) {
        AnnotatedObject parent = getAnnotatedObject();

        // modify if exists
        boolean exists = false;

        for ( Annotation annotation : parent.getAnnotations() ) {
            if ( annotation.getCvTopic() != null ) {
                if ( topicOrShortLabel.equals( annotation.getCvTopic().getIdentifier() )
                     || topicOrShortLabel.equals( annotation.getCvTopic().getShortLabel() ) ) {
                    if ( !text.equals( annotation.getAnnotationText() ) ) {
                        annotation.setAnnotationText( text );
                    }
                    exists = true;
                }
            }
        }

        // create if not exists
        if ( !exists ) {
            addAnnotation( topicOrShortLabel, text );
        }
    }

    public void removeAnnotation( String topicIdOrLabel ) {
        Iterator<Annotation> iterator = getAnnotatedObject().getAnnotations().iterator();

        while ( iterator.hasNext() ) {
            Annotation annotation = iterator.next();
            if ( topicIdOrLabel.equals( annotation.getCvTopic().getIdentifier() ) ||
                 topicIdOrLabel.equals( annotation.getCvTopic().getShortLabel() ) ) {
                iterator.remove();
            }
        }
    }

    public void removeAnnotation( String topicIdOrLabel, String text ) {
        Iterator<Annotation> iterator = getAnnotatedObject().getAnnotations().iterator();

        while ( iterator.hasNext() ) {
            Annotation annotation = iterator.next();
            if ( ( topicIdOrLabel.equals( annotation.getCvTopic().getIdentifier() ) || topicIdOrLabel.equals( annotation.getCvTopic().getShortLabel() ) )
                 && text.equals( annotation.getAnnotationText() ) ) {
                iterator.remove();
            }
        }
    }

    public void removeAnnotation( Annotation annotation ) {
        getAnnotatedObject().removeAnnotation( annotation );
        setUnsavedChanges( true );
    }

    public void setAnnotation( String topicIdOrLabel, Object value ) {
        if ( value != null && !value.toString().isEmpty() ) {
            replaceOrCreateAnnotation( topicIdOrLabel, value.toString() );
        } else {
            removeAnnotation( topicIdOrLabel );
        }
    }

    public String findAnnotationText( String topicId ) {
        Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( getAnnotatedObject(), topicId );

        if ( annotation != null ) {
            return annotation.getAnnotationText();
        }

        return null;

    }

    public List<Annotation> getAnnotations() {
        if ( getAnnotatedObject() == null ) {
            return Collections.EMPTY_LIST;
        }

        final ArrayList<Annotation> annotations = new ArrayList<Annotation>( getAnnotatedObject().getAnnotations() );
        Collections.sort( annotations, new IntactObjectComparator() );
        return annotations;
    }

    // ALIASES
    ///////////////////////////////////////////////

    public void newAlias( ActionEvent evt ) {
        Alias alias = newAliasInstance();
        getAnnotatedObject().addAlias( alias );
        setUnsavedChanges( true );
    }

    private Alias newAliasInstance() {
        Class<? extends Alias> aliasClass = AnnotatedObjectUtils.getAliasClassType( getAnnotatedObject().getClass() );

        Alias alias = null;
        try {
            alias = aliasClass.newInstance();
        } catch ( Throwable e ) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext( ctx, e );
            ctx.getApplication().publishEvent( ctx, ExceptionQueuedEvent.class, eventContext );
        }

        alias.setOwner( getIntactContext().getInstitution() );
        return alias;
    }

    public void addAlias( String aliasTypeIdOrLabel, String text ) {
        CvAliasType type = cvObjectService.findCvObject( CvAliasType.class, aliasTypeIdOrLabel );
        final Alias alias = newAliasInstance();
        alias.setCvAliasType( type );
        alias.setName( text );

        getAnnotatedObject().addAlias( alias );
    }

    public void setAlias( String aliasTypeIdOrLabel, Object value ) {
        if ( value != null && !value.toString().isEmpty() ) {
            removeAlias( aliasTypeIdOrLabel, value.toString() );
        } else {
            removeAlias( aliasTypeIdOrLabel );
        }
    }

    public void removeAlias( String aliasTypeIdOrLabel, String text ) {
        Iterator<Alias> iterator = getAnnotatedObject().getAliases().iterator();

        while ( iterator.hasNext() ) {
            Alias alias = iterator.next();
            if ( ( aliasTypeIdOrLabel.equals( alias.getCvAliasType().getIdentifier() ) || aliasTypeIdOrLabel.equals( alias.getCvAliasType().getShortLabel() ) )
                 && text.equals( alias.getName() ) ) {
                iterator.remove();
            }
        }
    }

    public void removeAlias( String aliasTypeIdOrLabel ) {
        Iterator<Alias> iterator = getAnnotatedObject().getAliases().iterator();

        while ( iterator.hasNext() ) {
            Alias alias = iterator.next();
            if ( aliasTypeIdOrLabel.equals( alias.getCvAliasType().getIdentifier() ) ||
                 aliasTypeIdOrLabel.equals( alias.getCvAliasType().getShortLabel() ) ) {
                iterator.remove();
            }
        }
    }

    public List<Alias> getAliases() {
        if ( getAnnotatedObject() == null ) {
            return Collections.EMPTY_LIST;
        }

        final ArrayList<Alias> aliases = new ArrayList<Alias>( getAnnotatedObject().getAliases() );
        Collections.sort( aliases, new IntactObjectComparator() );
        return aliases;
    }
    
    public String findAliasName( String aliasTypeId ) {
        Collection<Alias> aliases = AnnotatedObjectUtils.getAliasByType( getAnnotatedObject(), aliasTypeId );
        if( aliases != null && aliases.size() > 0 ) {
            return aliases.iterator().next().getName();
        }
        return null;
    }

    /**
     * This method is to be used if only one instance of an aliasType is expected to be stored in a given annotatedObject.
     * @param aliasTypeIdOrLabel
     * @param text
     */
    public void addOrReplace( String aliasTypeIdOrLabel, String text ) {
        Iterator<Alias> iterator = getAnnotatedObject().getAliases().iterator();

        boolean found = false;
        while ( iterator.hasNext() && !found ) {
            Alias alias = iterator.next();
            if ( aliasTypeIdOrLabel.equals( alias.getCvAliasType().getIdentifier() ) ||
                 aliasTypeIdOrLabel.equals( alias.getCvAliasType().getShortLabel() ) ) {
                // replace
                alias.setName( text );
            }
        }

        if( !found ) {
            // create new
            final Alias alias = newAliasInstance();
            alias.setName( text );
        }

    }

    // OTHER
    ////////////////////////////////////////////////////

    protected PersistenceController getPersistenceController() {
        return (PersistenceController)getSpringContext().getBean("persistenceController");
    }

    public boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }

    public Date getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Date lastSaved) {
        this.lastSaved = lastSaved;
    }

    private class IntactObjectComparator implements Comparator<IntactObject> {
        @Override
        public int compare( IntactObject o1, IntactObject o2 ) {
            if ( o1.getAc() != null ) return 1;
            return 0;
        }
    }
}
