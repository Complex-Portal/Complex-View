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
                                      ON ( int_count.exp_ac = int_exp.experiment_ac)
WHERE exp.ac IS NOT NULL;

-- for experiment_key the STRING value '(null)' will be stored, because 
-- a primary key in Oracle can never get the null value...
-- solution remove the primary key of the maintable...
INSERT INTO intact__interaction__main
SELECT '(null)', -- experiment_key 
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
                                      ON ( int_count.exp_ac = int_exp.experiment_ac)
WHERE exp.ac IS NULL;



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

-- It seems to me that there is a equivalent number stored like the mi number
-- but there are more entries of that...
-- because of that I stored it in two ways...

-- all stored with mi
INSERT INTO intact__interaction_owner__dm
SELECT interaction_key,
       int_owner.primaryid,  -- interaction_owner_mi
       int_owner.shortlabel, -- interaction_owner_short
       int_owner.fullname    -- interaction_owner_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE int_owner.Primaryid LIKE 'MI:%';

-- stores the nullvalues and the special primaryid '14681455'
INSERT INTO intact__interaction_owner__dm
SELECT interaction_key,
       null,                 -- interaction_owner_mi
       int_owner.shortlabel, -- interaction_owner_short
       int_owner.fullname    -- interaction_owner_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE    int_owner.Primaryid NOT LIKE 'MI:%'  -- is it necessary to store all int_owner.Primaryid = '14681455' ???
      OR int_owner.primaryid IS NULL;


-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
--          TESTING AREA

SELECT * FROM v_interactor_owner;

SELECT int_owner.primaryid
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
GROUP BY int_owner.Primaryid;

SELECT COUNT(*) 
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE int_owner.Primaryid = '14681455';  -- seems to be equivalent to 'MI:0469' but a lot more entries...

SELECT COUNT(*)
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE int_owner.Primaryid = 'MI:0469';

SELECT *
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE int_owner.Primaryid is null;

SELECT interaction_key,
       int_owner.primaryid,  -- interaction_owner_mi
       int_owner.shortlabel, -- interaction_owner_short
       int_owner.fullname    -- interaction_owner_full
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_owner int_owner
                                              ON ( int_type.interaction_key = int_owner.ac)
WHERE int_owner.Primaryid NOT LIKE 'MI:%'
      OR int_owner.primaryid IS NULL;




















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


