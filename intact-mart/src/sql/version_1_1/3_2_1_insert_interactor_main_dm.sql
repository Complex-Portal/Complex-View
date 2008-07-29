-- #############################################################################
-- #                    intact__interactor__main                          
-- #############################################################################

INSERT INTO intact__interactor__main
SELECT int_main.experiment_key,                 -- experiment_key
       int_main.interaction_key,                -- interaction_key
       com_roles.interactor_key,                -- interactor_key
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
       int_main.interaction_type_mi,            -- interaction_type_mi
       int_main.interaction_type_short,         -- interaction_type_short
       int_main.interaction_type_full,          -- interaction_type_full
       int_main.component_count,                -- component_count
       int_main.molecule_count,                 -- molecule_count
       int_type.interactor_shortlabel,          -- interactor_shortlabel
       int_type.interactor_fullname,            -- interactor_fullname
       int_type.interactor_type_mi,             -- interactor_type_mi
       int_type.interactor_type_short,          -- interactor_type_short
       int_type.interactor_type_full,           -- interactor_type_full
       com_roles.experimental_role_mi,          -- experimental_role_mi
       com_roles.experimental_role_short,       -- experimental_role_short
       com_roles.experimental_role_full,        -- experimental_role_full
       com_roles.biological_role_mi,            -- biological_role_mi
       com_roles.biological_role_short,         -- biological_role_short
       com_roles.biological_role_full,          -- biological_role_full
       int_type.interactor_biosource_taxid,     -- interactor_biosource_taxid
       int_type.interactor_biosource_short,     -- interactor_biosource_short
       int_type.interactor_biosource_full,      -- interactor_biosource_full
       com_roles.component_expressed_in_taxid,  -- component_expressed_in_taxid
       com_roles.component_expressed_in_short,  -- component_expressed_in_short
       com_roles.component_expressed_in_full,   -- component_expressed_in_full
       com_roles.stoichiometry,                 -- stoichiometry
       tmp.interactor_sequence,                 -- interactor_sequence
       tmp.interactor_sequence_length,          -- interactor_sequence_length
       int_type.crc64,                          -- crc64
       int_count.interaction_count              -- involved_interaction_count
FROM v_com_roles com_roles RIGHT OUTER JOIN intact__interaction__main int_main
                                         ON ( int_main.interaction_key = com_roles.interaction_ac) 
                            LEFT OUTER JOIN v_interactor_type int_type
                                         ON ( int_type.interactor_ac = com_roles.interactor_ac)
                            LEFT OUTER JOIN tbl_sequence_tmp tmp
                                         ON ( tmp.intactor_key = com_roles.interactor_ac)
                            LEFT OUTER JOIN v_involved_interaction_count int_count
                                         ON ( int_count.interactor_ac = com_roles.interactor_ac);


-- #############################################################################
-- #                  intact__interactor_alias__dm
-- #            here are aliases for interactor and component stored
-- #############################################################################                           

INSERT INTO intact__interactor_alias__dm
SELECT inter.interactor_key,         -- interactor_key
       ali.name,                     -- name
       cv.mi,                        -- alias_type_mi
       cv.shortlabel,                -- alias_type_short
       cv.fullname                   -- alias_type_full
FROM ia_interactor_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac)
                            RIGHT OUTER JOIN v_get_interactor_key inter
                                          ON ( ali.parent_ac = inter.interactor_ac );


-- there are at the moment no aliases stored for component...
INSERT INTO intact__interactor_alias__dm
SELECT com.ac,        -- interactor_key
       ali.name,
       cv.mi,         -- alias_type_mi
       cv.shortlabel, -- alias_type_short
       cv.fullname    -- alias_type_full
FROM ia_component_alias ali LEFT OUTER JOIN v_cv_mi cv
                                         ON ( cv.ac = ali.aliastype_ac)
                                       JOIN ia_component com
                                         ON ( ali.parent_ac = com.ac);



-- #############################################################################
-- #                  intact__interactor_xref__dm   
-- #            here are xref for interactor and component stored
-- #############################################################################

INSERT INTO intact__interactor_xref__dm
SELECT inter.interactor_key,    -- interactor_key
       xref.primary_id,         -- primary_id
       xref.secondary_id,       -- secondary_id
       xref.database_mi,        -- database_mi
       xref.database_short,     -- database_short
       xref.database_full,      -- database_full
       xref.qualifier_mi,       -- qualifier_mi
       xref.qualifier_short,    -- qualifier_short
       xref.qualifier_full      -- qualifier_full
FROM v_interactor_xref xref RIGHT OUTER JOIN v_get_interactor_key inter
                                          ON ( inter.interactor_ac = xref.interaction_ac);


-- there are at the moment no xref for component available
INSERT INTO intact__interactor_xref__dm
SELECT com.ac,     -- interactor_key
       primary_id,
       secondary_id,
       database_mi,   
       database_short,
       database_full,
       qualifier_mi,     
       qualifier_short,
       qualifier_full
FROM v_component_xref xref JOIN ia_component com
                             ON ( com.ac = xref.xref_ac);


-- #############################################################################
-- #                      intact__interactor_anno__dm       
-- #         here are annotations for interactor and component stored
-- #############################################################################

INSERT INTO intact__interactor_anno__dm
SELECT inter.interactor_key,        -- interactor_key
       anno.description,            -- description
       anno.mi,                     -- topic_mi
       anno.shortlabel,             -- topic_short
       anno.fullname                -- topic_full
FROM ia_int2annot int_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = int_anno.annotation_ac)
                          RIGHT OUTER JOIN v_get_interactor_key inter
                                        ON (inter.interactor_ac = int_anno.interactor_ac);


-- there are at the moment also no annotations stored for component...
INSERT INTO intact__interactor_anno__dm
SELECT com.ac,                      -- interactor_key
       anno.description,            -- description
       anno.mi,                     -- topic_mi
       anno.shortlabel,             -- topic_short
       anno.fullname                -- topic_full
FROM ia_component2annot com_anno LEFT OUTER JOIN v_annotation anno
                                              ON (anno.annotation_ac = com_anno.annotation_ac)
                                            JOIN ia_component com
                                              ON ( com.ac = com_anno.component_ac);

COMMIT;
