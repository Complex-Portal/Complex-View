-- #############################################################################
-- #                      intact__experiment__main                            
-- #############################################################################

INSERT INTO intact__experiment__main
SELECT pub.publication_key,     -- publication_key
       exp.ac,                  -- experiment_key
       pub.publication_title,   -- publication_title
       pub.interactions_count,  -- interactions_count
       pub.experiment_count,    -- experiment_count
       exp.shortlabel,          -- experiment_short
       exp.fullname,            -- experiment_full
       bio.taxid,               -- host_organism_taxid
       bio.shortlabel,          -- host_organism_short
       bio.fullname,            -- host_organism_full
       cv1.mi,                  -- participant_identMethod_mi
       cv1.shortlabel,          -- participant_identMethod_short
       cv1.fullname,            -- participant_identMethod_full
       cv2.mi,                  -- interaction_detectMethod_mi
       cv2.shortlabel,          -- interaction_detectMethod_short
       cv2.fullname,            -- interaction_detectMethod_full
       inter.interaction_count  -- interaction_count
FROM ia_experiment exp LEFT OUTER JOIN  ia_biosource bio
                                    ON (bio.ac = exp.biosource_ac)                        
                       LEFT OUTER JOIN v_cv_mi cv1
                                    ON (cv1.ac = exp.identmethod_ac)
                       LEFT OUTER JOIN v_cv_mi cv2
                                    ON (cv2.ac = exp.detectmethod_ac)
                       LEFT OUTER JOIN v_interaction_count inter
                                    ON ( inter.exp_ac = exp.ac)
                       LEFT OUTER JOIN intact__publication__main pub
                                    ON ( pub.publication_key = exp.publication_ac);



-- #############################################################################
-- #                  intact__experiment_alias__dm                            
-- #############################################################################                           

-- no experiments have an alias...
-- all values at the moment null...
INSERT INTO intact__interaction_alias__dm                                 
SELECT exp.ac,           -- experiment_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_experiment_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac)
                            RIGHT OUTER JOIN ia_experiment exp
                                          ON ( ali.parent_ac = exp.ac );


-- #############################################################################
-- #                  intact__experiment_xref__dm                             
-- #############################################################################

INSERT INTO intact__experiment_xref__dm
SELECT exp.ac,             -- experiment_key
       xref.primaryid,     -- primary_id
       xref.secondaryid,   -- secondary_id
       cv1.mi,             -- database_mi
       cv1.shortlabel,     -- database_short
       cv1.fullname,       -- database_full
       cv2.mi,             -- qualifier_mi
       cv2.shortlabel,     -- qualifier_short
       cv2.fullname        -- qualifier_full
FROM ia_experiment_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac)
                            RIGHT OUTER JOIN ia_experiment exp
                                          ON ( exp.ac = xref.parent_ac);



-- #############################################################################
-- #               intact__experiment_annotation__dm                          
-- #############################################################################

INSERT INTO intact__experiment_anno__dm
SELECT exp.ac,            -- experiment_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_exp2annot exp_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = exp_anno.annotation_ac)
                          RIGHT OUTER JOIN ia_experiment exp
                                        ON (exp.ac = exp_anno.experiment_ac);



-- #############################################################################
-- #                          intact__tissue__dm                               
-- #############################################################################

INSERT INTO intact__hostorg_tissue__dm
SELECT DISTINCT bio.tissue_ac,     -- experiment_key
                cv.primaryid,      -- brenda_id
                cv.shortlabel,     -- tissue_short
                cv.fullname        -- tissue_full
FROM ia_biosource bio LEFT OUTER JOIN v_cv_primaryid cv
                                   ON (bio.tissue_ac = cv.ac)
WHERE bio.tissue_ac IS NOT NULL;



-- #############################################################################
-- #                          intact__hostOrg_celltype__dm                             
-- #############################################################################

INSERT INTO intact__hostOrg_celltype__dm
SELECT DISTINCT bio.ac,           -- experiment_key
                cv.primaryid,     -- cabri_id
                cv.shortlabel,    -- celltype_short
                cv.fullname       -- celltype_full
FROM ia_biosource bio LEFT OUTER JOIN v_cv_primaryid cv
                                   ON (bio.celltype_ac = cv.ac)
WHERE bio.celltype_ac IS NOT NULL;


COMMIT;