-- #############################################################################
-- #                    intact__interactor__main                          
-- #############################################################################

INSERT INTO intact__interactor__main
SELECT int_main.experiment_key,
       int_main.interaction_key,
       int_type.interactor_key,
       int_main.experiment_short,
       int_main.experiment_full,
       int_main.host_organism_taxid,
       int_main.host_organism_short,
       int_main.host_organism_full,
       int_main.interaction_count,
       int_main.interaction_type_mi,
       int_main.interaction_type_short,
       int_main.interaction_type_full,
       int_main.interaction_detectMethod_mi,     
       int_main.interaction_detectMethod_short,  
       int_main.interaction_detectMethod_full,
       int_main.interactor_count,
       int_type.interactor_shortlabel,
       int_type.interactor_fullname,
       int_type.interactor_type_mi,
       int_type.interactor_type_short,
       int_type.interactor_type_full,
       com_roles.experimental_role_mi,
       com_roles.experimental_role_short,
       com_roles.experimental_role_full,
       com_roles.biological_role_mi,
       com_roles.biological_role_short,
       com_roles.biological_role_full,
       int_type.interactor_biosource_taxid,
       int_type.interactor_biosource_short,
       int_type.interactor_biosource_full,
       com_roles.component_expressed_in_taxid,
       com_roles.component_expressed_in_short,
       com_roles.component_expressed_in_full,
       com_roles.stoichiometry,
       null AS molecule_count,
       null AS interactor_sequence,
       null AS interactor_sequence_length,
       int_type.crc64
FROM v_com_roles com_roles RIGHT OUTER JOIN v_interactor_type int_type
                                         ON ( int_type.interactor_key = com_roles.interactor_key)
                            LEFT OUTER JOIN intact__interaction__main int_main
                                         ON ( int_main.interaction_key = com_roles.interaction_key);



-- #############################################################################
-- #                  intact__interactor_alias__dm                            
-- #############################################################################                           

INSERT INTO intact__interactor_alias__dm
SELECT com.ac, -- interactor_key
       name,
       alias_type_mi,
       alias_type_short,
       alias_type_full
FROM v_interactor_type int_type LEFT OUTER JOIN v_interactor_alias_type ali_type
                                             ON ( ali_type.alias_ac = int_type.interactor_key )
                               RIGHT OUTER JOIN ia_component com
                                             ON ( int_type.interactor_key = com.interactor_ac);

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
-- #############################################################################

INSERT INTO intact__interactor_xref__dm
SELECT com.ac, -- interactor_key
       primary_id,
       secondary_id,
       database_mi,   
       database_short,
       database_full,
       qualifier_mi,     
       qualifier_short,
       qualifier_full
FROM v_interactor_type int_type LEFT OUTER JOIN v_interactor_xref int_xref
                                             ON (int_xref.xref_ac = int_type.interactor_key)
                               RIGHT OUTER JOIN ia_component com
                                             ON ( int_type.interactor_key = com.interactor_ac);

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
-- #                  intact__interactor_annotation__dm                          
-- #############################################################################

INSERT INTO intact__interactor_anno__dm
SELECT com.ac, -- interactor_key
       description,
       topic_mi,
       topic_short,
       topic_full
FROM ia_int2annot int_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = int_anno.annotation_ac)
                          RIGHT OUTER JOIN v_interactor_type int_type
                                        ON (int_type.interactor_key = int_anno.interactor_ac)
                          RIGHT OUTER JOIN ia_component com
                                        ON ( com.interactor_ac = int_anno.interactor_ac);

-- there are at the moment also no annotations stored for component...
INSERT INTO intact__interactor_anno__dm
SELECT com.ac, -- interactor_key
       description,
       topic_mi,
       topic_short,
       topic_full
FROM ia_component2annot com_anno LEFT OUTER JOIN v_annotation anno
                                              ON (anno.annotation_ac = com_anno.annotation_ac)
                                            JOIN ia_component com
                                              ON ( com.ac = com_anno.component_ac);
