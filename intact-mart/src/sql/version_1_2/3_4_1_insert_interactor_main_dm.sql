-- #############################################################################
-- #                    intact__interactor__main                          
-- #############################################################################

INSERT INTO intact__interactor__main
SELECT com_main.publication_key,                -- publication_key
       com_main.experiment_key,                 -- experiment_key
       com_main.interaction_key,                -- interaction_key
       com_main.component_key,                  -- component_key
       inter.ac,                                -- interactor_key
       com_main.publication_title,              -- publication_title
       com_main.interactions_count,             -- interactions_count
       com_main.experiment_count,               -- experiment_count
       com_main.experiment_short,               -- experiment_short
       com_main.experiment_full,                -- experiment_full
       com_main.host_organism_taxid,            -- host_organism_taxid
       com_main.host_organism_short,            -- host_organism_short
       com_main.host_organism_full,             -- host_organism_full
       com_main.participant_identMethod_mi,     -- participant_identMethod_mi
       com_main.participant_identMethod_short,  -- participant_identMethod_short
       com_main.participant_identMethod_full,   -- participant_identMethod_full
       com_main.interaction_detectMethod_mi,    -- interaction_detectMethod_mi
       com_main.interaction_detectMethod_short, -- interaction_detectMethod_short
       com_main.interaction_detectMethod_full,  -- interaction_detectMethod_full
       com_main.interaction_count,              -- interaction_count
       com_main.interaction_type_mi,            -- interaction_type_mi
       com_main.interaction_type_short,         -- interaction_type_short
       com_main.interaction_type_full,          -- interaction_type_full
       com_main.interactor_count,               -- interactor_count
       com_main.component_count,                -- component_count
       com_main.molecule_count,                 -- molecule_count
       com_main.experimental_role_mi,           -- experimental_role_mi
       com_main.experimental_role_short,        -- experimental_role_short
       com_main.experimental_role_full,         -- experimental_role_full
       com_main.biological_role_mi,             -- biological_role_mi
       com_main.biological_role_short,          -- biological_role_short
       com_main.biological_role_full,           -- biological_role_full
       com_main.component_expressed_in_taxid,   -- component_expressed_in_taxid
       com_main.component_expressed_in_short,   -- component_expressed_in_short
       com_main.component_expressed_in_full,    -- component_expressed_in_full
       com_main.stoichiometry,                  -- stoichiometry
       inter.interactor_shortlabel,             -- interactor_shortlabel
       inter.interactor_fullname,               -- interactor_fullname
       inter.interactor_type_mi,                -- interactor_type_mi
       inter.interactor_type_short,             -- interactor_type_short
       inter.interactor_type_full,              -- interactor_type_full
       inter.interactor_biosource_taxid,        -- interactor_biosource_taxid
       inter.interactor_biosource_short,        -- interactor_biosource_short
       inter.interactor_biosource_full,         -- interactor_biosource_full       
       tmp.interactor_sequence,                 -- interactor_sequence
       tmp.interactor_sequence_length,          -- interactor_sequence_length
       inter.crc64,                             -- crc64
       inter.interaction_count,                 -- involved_interaction_count
       inter.uniprotkb
FROM ia_component com RIGHT OUTER JOIN  v_interactor inter 
                                    ON ( inter.ac = com.interactor_ac )
                       LEFT OUTER JOIN intact__component__main com_main
                                    ON ( com_main.component_key = com.ac )
                       LEFT OUTER JOIN tbl_sequence_tmp tmp
                                    ON ( tmp.intactor_key = com.interactor_ac);


-- #############################################################################
-- #                  intact__interactor_alias__dm                            
-- #############################################################################                           

INSERT INTO intact__interactor_alias__dm                                 
SELECT inter.ac,         -- interactor_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_interactor_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac)
                            RIGHT OUTER JOIN v_interactor inter
                                          ON ( ali.parent_ac = inter.ac );


-- #############################################################################
-- #                  intact__interactor_xref__dm                             
-- #############################################################################

INSERT INTO intact__interactor_xref__dm
SELECT inter.ac,           -- interactor_key
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
                            RIGHT OUTER JOIN v_interactor inter
                                          ON ( inter.ac = xref.parent_ac);



-- #############################################################################
-- #               intact__interactor_annotation__dm                          
-- #############################################################################

INSERT INTO intact__interactor_anno__dm
SELECT inter.ac,          -- interactor_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_int2annot int_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = int_anno.annotation_ac)
                          RIGHT OUTER JOIN v_interactor inter
                                        ON ( inter.ac = int_anno.interactor_ac);

COMMIT;

