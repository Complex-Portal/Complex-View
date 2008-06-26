-- #############################################################################
-- #                      intact__experiment__main                            
-- #############################################################################

INSERT INTO intact__experiment__main
SELECT ac                    AS experiment_ac,
       experiment_short,
       experiment_full,
       host_organism_taxid,
       host_organism_short,
       host_organism_full,
       interaction_count
FROM v_experiment exp LEFT OUTER JOIN v_interaction_count inter
                                   ON ( inter.exp_ac = exp.ac);



-- #############################################################################
-- #                  intact__experiment_alias__dm                            
-- #############################################################################                           

-- no experiments have an alias...
-- all values at the moment null...
INSERT INTO intact__interaction_alias__dm
SELECT exp.ac,  -- experiment_key
       name,
       alias_type_mi,
       alias_type_short,
       alias_type_full
FROM ia_experiment exp LEFT OUTER JOIN v_experiment_alias_type ali_type
                                    ON ( ali_type.alias_ac = exp.ac );
                                              


-- #############################################################################
-- #                  intact__experiment_xref__dm                             
-- #############################################################################

INSERT INTO intact__experiment_xref__dm
SELECT exp.ac,  -- experiment_key
       primary_id,
       secondary_id,
       database_mi,   
       database_short,
       database_full,
       qualifier_mi,     
       qualifier_short,
       qualifier_full
FROM v_experiment exp LEFT OUTER JOIN v_experiment_xref exp_xref
                                   ON (exp_xref.xref_ac = exp.ac );



-- #############################################################################
-- #               intact__experiment_annotation__dm                          
-- #############################################################################

INSERT INTO intact__experiment_anno__dm
SELECT exp.ac, -- experiment_key
       description,
       topic_mi,
       topic_short,
       topic_full
FROM ia_exp2annot exp_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = exp_anno.annotation_ac)
                          RIGHT OUTER JOIN v_experiment exp
                                        ON (exp.ac = exp_anno.experiment_ac);



-- #############################################################################
-- #                          intact__tissue__dm                               
-- #############################################################################

INSERT INTO intact__hostorg_tissue__dm
SELECT DISTINCT bio.tissue_ac     AS experiment_key,
                cv.primaryid      AS brenda_id,    
                cv.shortlabel     AS tissue_short,
                cv.fullname       AS tissue_full
FROM ia_biosource bio LEFT OUTER JOIN v_cv_primaryid cv
                                   ON (bio.tissue_ac = cv.ac)
WHERE bio.tissue_ac IS NOT NULL;



-- #############################################################################
-- #                          intact__hostOrg_celltype__dm                             
-- #############################################################################

INSERT INTO intact__hostOrg_celltype__dm
SELECT DISTINCT bio.ac           AS experiment_key,
                cv.primaryid     AS cabri_id,
                cv.shortlabel    AS celltype_short,
                cv.fullname      AS celltype_full
FROM ia_biosource bio LEFT OUTER JOIN v_cv_primaryid cv
                                   ON (bio.celltype_ac = cv.ac)
WHERE bio.celltype_ac IS NOT NULL;



-- #############################################################################
-- #                       intact__publication__dm
-- #                Assumption that there is a doi or a 
-- #                    pubmedid stored in primaryid.
-- #############################################################################

-- insert all pubmed 
INSERT INTO intact__publication__dm 
SELECT int_exp.interaction_ac AS experiment_key,
       pub.pmid               AS pmid,         
       exp.fullname           AS title,
       null                   AS doi,
       interaction_count,
       experiment_count
FROM ia_experiment exp JOIN v_publication_pmid pub
                         ON ( pub.pub_ac = exp.publication_ac)
                       JOIN ia_int2exp int_exp
                         ON ( int_exp.experiment_ac = exp.ac )
            LEFT OUTER JOIN v_publication_counts pub_counts
                         ON ( pub_counts.pub_ac = exp.publication_ac );

-- there are at the moment no doi ids for a publication available
INSERT INTO intact__publication__dm 
SELECT int_exp.interaction_ac AS experiment_key,
       null                   AS pmid,         
       exp.fullname           AS title,
       pub.doi                AS doi,
       interaction_count,
       experiment_count
FROM ia_experiment exp JOIN v_publication_doi pub
                         ON ( pub.pub_ac = exp.publication_ac)
                       JOIN ia_int2exp int_exp
                         ON ( int_exp.experiment_ac = exp.ac )
            LEFT OUTER JOIN v_publication_counts pub_counts
                         ON ( pub_counts.pub_ac = exp.publication_ac );

-- all other possible mi identifier
INSERT INTO intact__publication__dm 
SELECT int_exp.interaction_ac AS experiment_key,
       null                   AS pmid,         
       exp.fullname           AS title,
       null                   AS doi,
       interaction_count,
       experiment_count
FROM ia_experiment exp JOIN v_publication_mi pub
                         ON ( pub.pub_ac = exp.publication_ac)
                       JOIN ia_int2exp int_exp
                         ON ( int_exp.experiment_ac = exp.ac )
            LEFT OUTER JOIN v_publication_counts pub_counts
                         ON ( pub_counts.pub_ac = exp.publication_ac );

