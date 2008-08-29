-- #############################################################################
-- #                    intact__component__main                          
-- #############################################################################

INSERT INTO intact__component__main
SELECT int_main.publication_key,                -- publication_key
       int_main.experiment_key,                 -- experiment_key
       int_main.interaction_key,                -- interaction_key
       com_roles.component_key,                 -- component_key
       int_main.publication_title,              -- publication_title
       int_main.interactions_count,             -- interactions_count
       int_main.experiment_count,               -- experiment_count
       int_main.experiment_short,               -- experiment_short
       int_main.experiment_full,                -- experiment_full
       int_main.host_organism_taxid,            -- host_organism_taxid
       int_main.host_organism_short,            -- host_organism_short
       int_main.host_organism_full,             -- host_organism_full
       int_main.participant_identMethod_mi,     -- participant_identMethod_mi
       int_main.participant_identMethod_short,  -- participant_identMethod_short
       int_main.participant_identMethod_full,   -- participant_identMethod_full
       int_main.interaction_detectMethod_mi,    -- interaction_detectMethod_mi
       int_main.interaction_detectMethod_short, -- interaction_detectMethod_short
       int_main.interaction_detectMethod_full,  -- interaction_detectMethod_full
       int_main.interaction_count,              -- interaction_count
       int_main.interaction_short,              -- interaction_short
       int_main.interaction_full,               -- interaction_full
       int_main.interaction_type_mi,            -- interaction_type_mi
       int_main.interaction_type_short,         -- interaction_type_short
       int_main.interaction_type_full,          -- interaction_type_full
       int_main.interactor_count,               -- interactor_count
       int_main.component_count,                -- component_count
       int_main.molecule_count,                 -- molecule_count
       com_roles.experimental_role_mi,          -- experimental_role_mi
       com_roles.experimental_role_short,       -- experimental_role_short
       com_roles.experimental_role_full,        -- experimental_role_full
       com_roles.biological_role_mi,            -- biological_role_mi
       com_roles.biological_role_short,         -- biological_role_short
       com_roles.biological_role_full,          -- biological_role_full
       com_roles.component_expressed_in_taxid,  -- component_expressed_in_taxid
       com_roles.component_expressed_in_short,  -- component_expressed_in_short
       com_roles.component_expressed_in_full,   -- component_expressed_in_full
       com_roles.stoichiometry                  -- stoichiometry
FROM v_com_roles com_roles RIGHT OUTER JOIN intact__interaction__main int_main
                                         ON ( int_main.interaction_key = com_roles.interaction_ac);
                                         

-- #############################################################################
-- #                  intact__component_alias__dm                            
-- #############################################################################                           

INSERT INTO intact__component_alias__dm                                 
SELECT com.ac,           -- component_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_component_alias ali LEFT OUTER JOIN v_cv_mi cv
                                           ON (cv.ac = ali.aliastype_ac)
                             RIGHT OUTER JOIN ia_component com
                                           ON ( ali.parent_ac = com.ac );


-- #############################################################################
-- #                  intact__component_xref__dm                             
-- #############################################################################

INSERT INTO intact__component_xref__dm
SELECT com.ac,             -- component_key
       xref.primaryid,     -- primary_id
       xref.secondaryid,   -- secondary_id
       cv1.mi,             -- database_mi
       cv1.shortlabel,     -- database_short
       cv1.fullname,       -- database_full
       cv2.mi,             -- qualifier_mi
       cv2.shortlabel,     -- qualifier_short
       cv2.fullname        -- qualifier_full
FROM ia_component_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                           ON (cv1.ac = xref.database_ac)
                              LEFT OUTER JOIN v_cv_mi cv2
                                           ON (cv2.ac = xref.qualifier_ac)
                             RIGHT OUTER JOIN ia_component com
                                           ON ( com.ac = xref.parent_ac);



-- #############################################################################
-- #               intact__component_annotation__dm                          
-- #############################################################################

INSERT INTO intact__component_anno__dm
SELECT com.ac,            -- component_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_component2annot com_anno LEFT OUTER JOIN v_annotation anno
                                              ON (anno.annotation_ac = com_anno.annotation_ac)
                                RIGHT OUTER JOIN ia_component com
                                              ON ( com.ac = com_anno.component_ac);

COMMIT;

