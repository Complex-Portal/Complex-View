-- #############################################################################
-- #                      intact__interaction__main                            
-- #############################################################################

INSERT INTO intact__interaction__main
SELECT ac, -- experiment_key
       interaction_key,
       experiment_short,
       experiment_full,
       host_organism_taxid,
       host_organism_short,
       host_organism_full,
       interaction_count,
       interaction_type_mi,
       interaction_type_short,
       interaction_type_full,
       interaction_detectMethod_mi,     
       interaction_detectMethod_short,  
       interaction_detectMethod_full,
       interactor_count
FROM ia_int2exp int_exp RIGHT OUTER JOIN v_interaction inter
                                      ON (int_exp.interaction_ac = inter.interaction_key)             
                         LEFT OUTER JOIN v_experiment exp
                                      ON (int_exp.experiment_ac = exp.ac)
                         LEFT OUTER JOIN v_interaction_count int_count
                                      ON ( int_count.exp_ac = int_exp.experiment_ac);

-- #############################################################################
-- #                  intact__interaction_alias__dm                            
-- #############################################################################                           

-- no interaction have an alias...
-- all values at the moment null...
INSERT INTO intact__interaction_alias__dm
SELECT interaction_key,
       name,
       alias_type_mi,
       alias_type_short,
       alias_type_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_alias_type ali_type
                                              ON ( ali_type.alias_ac = int_type.interaction_key );



-- #############################################################################
-- #                  intact__interaction_xref__dm                             
-- #############################################################################

INSERT INTO intact__interaction_xref__dm
SELECT interaction_key,
       primary_id,
       secondary_id,
       database_mi,   
       database_short,
       database_full,
       qualifier_mi,     
       qualifier_short,
       qualifier_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_xref int_xref
                                      ON (int_xref.xref_ac = int_type.interaction_key);



-- #############################################################################
-- #                  intact__interaction_annotation__dm                          
-- #############################################################################

INSERT INTO intact__interaction_anno__dm
SELECT interaction_key,
       description,
       topic_mi,
       topic_short,
       topic_full
FROM ia_int2annot int_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = int_anno.annotation_ac)
                           RIGHT OUTER JOIN v_interaction_type int_type
                                         ON (int_type.interaction_key = int_anno.interactor_ac);



-- #############################################################################
-- #                    intact__interaction_owner__dm                          
-- #############################################################################

INSERT INTO intact__interaction_owner__dm
SELECT int_type.interaction_key, -- interaction_key
       int_owner.mi,             -- interaction_owner_mi
       int_owner.shortlabel,     -- interaction_owner_short
       int_owner.fullname        -- interaction_owner_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interaction_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac);