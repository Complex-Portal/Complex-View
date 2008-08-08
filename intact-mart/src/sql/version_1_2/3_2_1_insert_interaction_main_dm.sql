-- #############################################################################
-- #                      intact__interaction__main                            
-- #############################################################################

INSERT INTO intact__interaction__main
SELECT exp.publication_key,                -- publication_key
       exp.experiment_key,                 -- experiment_key
       inter.ac,                           -- interaction_key
       exp.publication_title,              -- publication_title
       exp.interactions_count,             -- interactions_count
       exp.experiment_count,               -- experiment_count
       exp.experiment_short,               -- experiment_short
       exp.experiment_full,                -- experiment_full
       exp.host_organism_taxid,            -- host_organism_taxid
       exp.host_organism_short,            -- host_organism_short
       exp.host_organism_full,             -- host_organism_full
       exp.participant_identMethod_mi,     -- participant_identMethod_mi
       exp.participant_identMethod_short,  -- participant_identMethod_short
       exp.participant_identMethod_full,   -- participant_identMethod_full
       exp.interaction_detectMethod_mi,    -- interaction_detectMethod_mi
       exp.interaction_detectMethod_short, -- interaction_detectMethod_short
       exp.interaction_detectMethod_full,  -- interaction_detectMethod_full
       exp.interaction_count,              -- interaction_count
       inter.mi,                           -- interaction_type_mi
       inter.shortlabel,                   -- interaction_type_short
       inter.fullname,                     -- interaction_type_full
       inter.interactor_count,             -- interactor_count
       inter.component_count,              -- component_count
       tmp.molecule_count                  -- molecule_count
FROM ia_int2exp int_exp RIGHT OUTER JOIN v_interaction inter
                                      ON (int_exp.interaction_ac = inter.ac)             
                         LEFT OUTER JOIN intact__experiment__main exp
                                      ON (int_exp.experiment_ac = exp.experiment_key)
                         LEFT OUTER JOIN tbl_molecule_count_tmp tmp
                                      ON (int_exp.interaction_ac = tmp.interaction_key);
                                      
  

-- #############################################################################
-- #                  intact__interaction_alias__dm                            
-- #############################################################################                           

-- no interaction have an alias...
-- all values at the moment null...
INSERT INTO intact__interaction_alias__dm
SELECT inter.ac,         -- interaction_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_interactor_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac)
                            RIGHT OUTER JOIN v_interaction inter
                                          ON ( ali.parent_ac = inter.ac );

        

-- #############################################################################
-- #                  intact__interaction_xref__dm                             
-- #############################################################################

INSERT INTO intact__interaction_xref__dm
SELECT inter.ac,           -- interaction_key
       xref.primaryid,     -- primary_id
       xref.secondaryid,   -- secondary_id
       cv1.mi,             -- database_mi
       cv1.shortlabel,     -- database_short
       cv1.fullname,       -- database_full
       cv2.mi,             -- qualifier_mi
       cv2.shortlabel,     -- qualifier_short
       cv2.fullname        -- qualifier_full
FROM ia_interactor_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac)
                            RIGHT OUTER JOIN v_interaction inter
                                          ON ( inter.ac = xref.parent_ac);



-- #############################################################################
-- #                  intact__interaction_annotation__dm                          
-- #############################################################################

INSERT INTO intact__interaction_anno__dm
SELECT int_type.ac,       -- interaction_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_int2annot int_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = int_anno.annotation_ac)
                          RIGHT OUTER JOIN v_interaction int_type
                                        ON (int_type.ac = int_anno.interactor_ac);



-- #############################################################################
-- #                    intact__interaction_owner__dm                          
-- #############################################################################

INSERT INTO intact__interaction_owner__dm
SELECT interaction.ac,           -- interaction_key
       int_owner.mi,             -- interaction_owner_mi
       int_owner.shortlabel,     -- interaction_owner_short
       int_owner.fullname        -- interaction_owner_full
FROM v_interaction interaction LEFT OUTER JOIN v_interaction_owner int_owner
                                            ON ( interaction.ac = int_owner.ac);


COMMIT;