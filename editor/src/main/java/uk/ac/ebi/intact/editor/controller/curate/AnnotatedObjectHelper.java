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
package uk.ac.ebi.intact.editor.controller.curate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectService;
import uk.ac.ebi.intact.editor.controller.curate.util.IntactObjectComparator;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.io.Serializable;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("prototype")
public class AnnotatedObjectHelper implements Serializable {

    private AnnotatedObject annotatedObject;

    public AnnotatedObjectHelper() {
    }

    public AnnotatedObjectHelper(AnnotatedObject annotatedObject) {
        this.annotatedObject = annotatedObject;
    }

    // XREFS
    ///////////////////////////////////////////////

    public void newXref() {
        Xref xref = newXrefInstance();
        annotatedObject.addXref( xref );
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

        initAudit(xref);

        return xref;
    }

    public List<Xref> getXrefs() {
        if ( annotatedObject == null ) {
            return Collections.EMPTY_LIST;
        }

        AnnotatedObject ao = annotatedObject;

        if (!IntactCore.isInitialized(annotatedObject.getXrefs())) {
            ao = getAnnotatedObjectDao().getByAc(annotatedObject.getAc());
        }

        final ArrayList<Xref> xrefs = new ArrayList<Xref>( ao.getXrefs() );
        Collections.sort( xrefs, new IntactObjectComparator() );
        return xrefs;
    }

    public void setXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId ) {
        if (databaseIdOrLabel == null){
            throw new IllegalArgumentException("Impossible to create/update/delete cross references if the database is not set.");
        }

        if ( primaryId != null && !primaryId.isEmpty() ) {
            replaceOrCreateXref( databaseIdOrLabel, qualifierIdOrLabel, primaryId, secondaryId );
        } else {
            removeXref( databaseIdOrLabel, qualifierIdOrLabel );
        }
    }

    public void replaceOrCreateXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId ) {
        AnnotatedObject parent = getAnnotatedObject();

        if (databaseIdOrLabel == null){
            throw new IllegalArgumentException("Impossible to replace or create cross references if the database is not set.");
        }
        if (primaryId == null){
            throw new IllegalArgumentException("Impossible to replace or create cross references if the primary id is not set.");
        }

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
                        xref.setSecondaryId( secondaryId );
                    } else if ( qualifierIdOrLabel != null) {

                        String qualifierId = xref.getCvXrefQualifier() != null ? xref.getCvXrefQualifier().getIdentifier() : null;
                        String qualifierShortLabel = xref.getCvXrefQualifier() != null ? xref.getCvXrefQualifier().getShortLabel() : null;

                        if ( (qualifierIdOrLabel.equals( qualifierId )
                                || qualifierIdOrLabel.equals( qualifierShortLabel )) && !primaryId.equals( xref.getPrimaryId() ) ) {
                            xref.setPrimaryId( primaryId );
                        }
                        xref.setSecondaryId( secondaryId );
                    }
                    else if ( qualifierIdOrLabel == null && xref.getCvXrefQualifier() == null ) {
                        if ( !primaryId.equals( xref.getPrimaryId() ) ) {
                            xref.setPrimaryId( primaryId );
                        }
                        xref.setSecondaryId( secondaryId );
                    }

                    exists = true;
                }
            }
        }

        // create if not exists
        if ( !exists ) {
            addXref( databaseIdOrLabel, qualifierIdOrLabel, primaryId, secondaryId );
        }
    }

    public void removeXref( String databaseIdOrLabel, String qualifierIdOrLabel ) {
        Iterator<Xref> iterator = getAnnotatedObject().getXrefs().iterator();

        if (databaseIdOrLabel == null){
            throw new IllegalArgumentException("Impossible to delete cross references if no database is provided.");
        }

        while ( iterator.hasNext() ) {
            Xref xref = iterator.next();
            String databaseId = xref.getCvDatabase() != null ? xref.getCvDatabase().getIdentifier() : null;
            String databaseShortLabel = xref.getCvDatabase() != null ? xref.getCvDatabase().getShortLabel() : null;

            if ( databaseIdOrLabel.equals( databaseId ) || databaseIdOrLabel.equals( databaseShortLabel ) ) {
                if ( qualifierIdOrLabel == null || xref.getCvXrefQualifier() == null ) {
                    iterator.remove();
                } else if ( qualifierIdOrLabel != null ) {
                    String qualifierId = xref.getCvXrefQualifier() != null ? xref.getCvXrefQualifier().getIdentifier() : null;
                    String qualifierShortLabel = xref.getCvXrefQualifier() != null ? xref.getCvXrefQualifier().getShortLabel() : null;

                    if (qualifierIdOrLabel.equals( qualifierId) || qualifierIdOrLabel.equals( qualifierShortLabel) ){
                        iterator.remove();
                    }
                }
                else if ( qualifierIdOrLabel == null && xref.getCvXrefQualifier() == null ) {
                    iterator.remove();
                }
            }
        }
    }

    public void removeXref( Xref xref ) {
        getAnnotatedObject().removeXref( xref );

    }

    public void addXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId ) {
        if (databaseIdOrLabel == null){
            throw new IllegalArgumentException("Impossible to create cross references if the database is not set.");
        }
        if (primaryId == null){
            throw new IllegalArgumentException("Impossible to create cross references if the primary id is not set.");
        }

        CvDatabase db = getCvObjectService().findCvObject( CvDatabase.class, databaseIdOrLabel );
        CvXrefQualifier qual = getCvObjectService().findCvObject( CvXrefQualifier.class, qualifierIdOrLabel );

        if (db == null){
            throw new IllegalArgumentException("The database " + databaseIdOrLabel + " does not exist in the database. You must create it first before being able to create cross references using this database.");
        }

        Xref xref = newXrefInstance();
        xref.setCvDatabase( db );
        xref.setCvXrefQualifier( qual );
        xref.setPrimaryId( primaryId );
        xref.setSecondaryId( secondaryId );
        xref.setCreated(new Date());
        xref.setUpdated(new Date());

        getAnnotatedObject().addXref( xref );
    }

    public String findXrefPrimaryId( String databaseId, String qualifierId ) {
        final AnnotatedObject ao = getAnnotatedObject();
        if (databaseId == null){
            return null;
        }

        Collection<Xref> xrefs = AnnotatedObjectUtils.searchXrefs( ao, databaseId, qualifierId );

        if ( !xrefs.isEmpty() ) {
            return xrefs.iterator().next().getPrimaryId();
        }

        return null;

    }

    // ANNOTATIONS
    ///////////////////////////////////////////////

    public void newAnnotation() {
        Annotation annotationWithNullTopic = new Annotation() {
            @Override
            public void setCvTopic( CvTopic cvTopic ) {
                if ( cvTopic != null ) {
                    super.setCvTopic( cvTopic );
                }
            }
        };
        initAudit(annotationWithNullTopic);
        getAnnotatedObject().addAnnotation( annotationWithNullTopic );

    }

    public void addAnnotation( String topicIdOrLabel, String text ) {

        if (topicIdOrLabel == null){
            throw new IllegalArgumentException("The topic must be set before creating an annotation.");
        }

        CvTopic dataset = getCvObjectService().findCvObject( CvTopic.class, topicIdOrLabel );

        if (dataset == null){
            throw new IllegalArgumentException("The topic " +topicIdOrLabel + " does not exist in the database. You must create it first before being able to create an annotation with this topic.");
        }

        Annotation annotation = new Annotation( getIntactContext().getInstitution(), dataset );
        annotation.setAnnotationText( text );
        initAudit(annotation);

        getAnnotatedObject().addAnnotation( annotation );
    }

    public void replaceOrCreateAnnotation( String topicOrShortLabel, String text ) {
        AnnotatedObject parent = getAnnotatedObject();
        if (topicOrShortLabel == null){
            throw new IllegalArgumentException("The topic must be set before creating or replacing an annotation.");
        }

        // modify if exists
        boolean exists = false;

        for ( Annotation annotation : parent.getAnnotations() ) {
            if ( annotation.getCvTopic() != null ) {
                if ( topicOrShortLabel.equals( annotation.getCvTopic().getIdentifier() )
                        || topicOrShortLabel.equals( annotation.getCvTopic().getShortLabel() ) ) {
                    if ((text == null && annotation.getAnnotationText() != null) || (text != null && annotation.getAnnotationText() == null)){
                        annotation.setAnnotationText( text );
                    }
                    else if (text != null && !text.equals( annotation.getAnnotationText() )){
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

        if (topicIdOrLabel == null){
            throw new IllegalArgumentException("The topic must be set before removing an annotation.");
        }

        while ( iterator.hasNext() ) {
            Annotation annotation = iterator.next();
            String topicId = annotation.getCvTopic() != null ? annotation.getCvTopic().getIdentifier() : null;
            String topicShortLabel = annotation.getCvTopic() != null ? annotation.getCvTopic().getShortLabel() : null;

            if ( topicIdOrLabel.equals( topicId ) ||
                    topicIdOrLabel.equals( topicShortLabel ) ) {
                iterator.remove();
            }
        }
    }

    public void removeAnnotation( String topicIdOrLabel, String text ) {
        Iterator<Annotation> iterator = getAnnotatedObject().getAnnotations().iterator();

        if (topicIdOrLabel == null){
            throw new IllegalArgumentException("The topic must be set before removing an annotation.");
        }

        while ( iterator.hasNext() ) {
            Annotation annotation = iterator.next();
            String topicId = annotation.getCvTopic() != null ? annotation.getCvTopic().getIdentifier() : null;
            String topicShortLabel = annotation.getCvTopic() != null ? annotation.getCvTopic().getShortLabel() : null;

            if ( ( topicIdOrLabel.equals( topicId ) || topicIdOrLabel.equals( topicShortLabel ) )) {
                if (text == null && annotation.getAnnotationText() == null){
                    iterator.remove();
                }
                else if (text != null && text.equals( annotation.getAnnotationText() )){
                    iterator.remove();
                }
            }
        }
    }

    public void removeAnnotation( Annotation annotation ) {
        getAnnotatedObject().removeAnnotation( annotation );

    }

    public void setAnnotation( String topicIdOrLabel, Object value ) {
        if (topicIdOrLabel == null){
            throw new IllegalArgumentException("The topic must be set before creating an annotation.");
        }

        if ( value != null && !value.toString().isEmpty() ) {
            replaceOrCreateAnnotation( topicIdOrLabel, value.toString() );
        } else {
            removeAnnotation( topicIdOrLabel );
        }
    }

    public String findAnnotationText( String topicId ) {
        if (getAnnotatedObject() == null) return null;

        if (topicId == null){
            return null;
        }

        AnnotatedObject ao = getAnnotatedObject();

        if (!IntactCore.isInitialized(ao.getAnnotations())) {
            ao = IntactContext.getCurrentInstance().getDaoFactory().getAnnotatedObjectDao(ao.getClass())
                    .getByAc(getAnnotatedObject().getAc());
        }

        if (ao == null) { // this can happen if the object has been removed in the same request just before
            return null;
        }

        Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( ao, topicId );

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

    public void newAlias() {
        Alias alias = newAliasInstance();
        getAnnotatedObject().addAlias( alias );

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
        initAudit(alias);

        return alias;
    }

    public void addAlias( String aliasTypeIdOrLabel, String text ) {

        if (aliasTypeIdOrLabel == null){
            throw new IllegalArgumentException("The alias type must be set before adding a new alias.");
        }

        CvAliasType type = getCvObjectService().findCvObject( CvAliasType.class, aliasTypeIdOrLabel );

        if (type == null){
            throw new IllegalArgumentException("The alias type " + aliasTypeIdOrLabel + " does not exist in the database and must be created first before creating an alias of this type.");
        }

        final Alias alias = newAliasInstance();
        alias.setCvAliasType( type );
        alias.setName( text );

        getAnnotatedObject().addAlias( alias );
    }

    public void setAlias( String aliasTypeIdOrLabel, Object value ) {
        if (aliasTypeIdOrLabel == null){
            throw new IllegalArgumentException("The alias type must be set before creating a new alias.");
        }

        if ( value != null && !value.toString().isEmpty() ) {
            addOrReplace(aliasTypeIdOrLabel, value.toString());
        } else {
            removeAlias( aliasTypeIdOrLabel );
        }
    }

    public void removeAlias( String aliasTypeIdOrLabel, String text ) {
        Iterator<Alias> iterator = getAnnotatedObject().getAliases().iterator();

        if (aliasTypeIdOrLabel == null){
            throw new IllegalArgumentException("The alias type must be set before deleting aliases.");
        }

        while ( iterator.hasNext() ) {
            Alias alias = iterator.next();
            String aliasId = alias.getCvAliasType() != null ? alias.getCvAliasType().getIdentifier() : null;
            String aliasShortLabel = alias.getCvAliasType() != null ? alias.getCvAliasType().getShortLabel() : null;

            if ( ( aliasTypeIdOrLabel.equals( aliasId ) || aliasTypeIdOrLabel.equals( aliasShortLabel ) )) {
                if (text == null && alias.getName() == null){
                    iterator.remove();
                }
                else if (text != null && text.equals( alias.getName() )){
                    iterator.remove();
                }
            }
        }
    }

    public void removeAlias( String aliasTypeIdOrLabel ) {
        Iterator<Alias> iterator = getAnnotatedObject().getAliases().iterator();

        if (aliasTypeIdOrLabel == null){
            throw new IllegalArgumentException("The alias type must be set before deleting aliases.");
        }

        while ( iterator.hasNext() ) {
            Alias alias = iterator.next();
            String aliasId = alias.getCvAliasType() != null ? alias.getCvAliasType().getIdentifier() : null;
            String aliasShortLabel = alias.getCvAliasType() != null ? alias.getCvAliasType().getShortLabel() : null;

            if ( aliasTypeIdOrLabel.equals( aliasId ) ||
                    aliasTypeIdOrLabel.equals( aliasShortLabel ) ) {
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
        if (aliasTypeId == null){
            return null;
        }

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

        if (aliasTypeIdOrLabel == null){
            throw new IllegalArgumentException("The alias type must be set before creating or replacing aliases.");
        }

        boolean found = false;

        while ( iterator.hasNext() && !found ) {
            Alias alias = iterator.next();
            String aliasId = alias.getCvAliasType() != null ? alias.getCvAliasType().getIdentifier() : null;
            String aliasShortLabel = alias.getCvAliasType() != null ? alias.getCvAliasType().getShortLabel() : null;

            if ( aliasTypeIdOrLabel.equals( aliasId ) ||
                    aliasTypeIdOrLabel.equals( aliasShortLabel ) ) {
                // replace
                alias.setName( text );
                found = true;
            }
        }

        if( !found ) {
            // create new
            CvAliasType type = getCvObjectService().findCvObject( CvAliasType.class, aliasTypeIdOrLabel );

            if (type == null){
                throw new IllegalArgumentException("The alias type " + aliasTypeIdOrLabel + " does not exist in the database and must be created first before creating an alias of this type.");
            }

            final Alias alias = newAliasInstance();
            alias.setName( text );
            alias.setCvAliasType(type);
            getAnnotatedObject().addAlias(alias);
        }

    }

    // OTHER
    ////////////////////////////////////////////////////

    private void initAudit(Auditable auditable) {
        auditable.setCreated(new Date());
        auditable.setCreator(IntactContext.getCurrentInstance().getUserContext().getUserId());
        auditable.setUpdated(new Date());
        auditable.setUpdator(IntactContext.getCurrentInstance().getUserContext().getUserId());
    }

    protected PersistenceController getPersistenceController() {
        return (PersistenceController)getIntactContext().getSpringContext().getBean("persistenceController");
    }

    protected IntactContext getIntactContext() {
        return IntactContext.getCurrentInstance();
    }

    protected CvObjectService getCvObjectService() {
        return (CvObjectService) getIntactContext().getSpringContext().getBean("cvObjectService");
    }

    public AnnotatedObject getAnnotatedObject() {
        return annotatedObject;
    }

    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        this.annotatedObject = annotatedObject;
    }

    private AnnotatedObjectDao<? extends AnnotatedObject> getAnnotatedObjectDao() {
        return IntactContext.getCurrentInstance().getDaoFactory().getAnnotatedObjectDao(annotatedObject.getClass());
    }
}
