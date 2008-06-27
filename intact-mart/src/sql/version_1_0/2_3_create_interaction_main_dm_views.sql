-- #############################################################################
-- #                      intact__interaction__main                            
-- #############################################################################


CREATE MATERIALIZED VIEW v_interaction_type AS
SELECT inter.ac         AS interaction_key,
       cv.mi            AS interaction_type_mi,
       cv.shortlabel    AS interaction_type_short,
       cv.fullname      AS interaction_type_full
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
-- 'MI:0317' is equals 'interaction'
WHERE cv.mi = 'MI:0317'; 


-- the number of interactor which are involved in a spezific interaction
CREATE MATERIALIZED VIEW v_interactor_count AS
SELECT interaction.ac AS ac,
       COUNT(*)       AS interactor_count
FROM ia_interactor interaction,
     ia_component com
WHERE interaction.ac = com.interaction_ac
GROUP BY interaction.ac;


CREATE MATERIALIZED VIEW v_interaction AS
SELECT interaction_key,
       interaction_type_mi,
       interaction_type_short,
       interaction_type_full,
       interactor_count
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_count int_count
                                              ON ( int_type.interaction_key  = int_count.ac );



-- #############################################################################
-- #                  intact__interaction_alias__dm                            
-- #############################################################################

CREATE MATERIALIZED VIEW v_interactor_alias_type AS
SELECT ali.parent_ac    AS alias_ac,
       ali.name         AS name,
       cv.mi            AS alias_type_mi,
       cv.shortlabel    AS alias_type_short,
       cv.fullname      AS alias_type_full
FROM ia_interactor_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac);


-- #############################################################################
-- #                  intact__interactor_xref__dm
-- #             useable for interaction and interactor, 
-- #            because both are stored in ia_interactor
-- #############################################################################

CREATE MATERIALIZED VIEW v_interactor_xref AS
SELECT xref.parent_ac    AS xref_ac,
       xref.primaryid    AS primary_id,
       xref.secondaryid  AS secondary_id,
       cv1.mi            AS database_mi,
       cv1.shortlabel    AS database_short,
       cv1.fullname      AS database_full,
       cv2.mi            AS qualifier_mi,
       cv2.shortlabel    AS qualifier_short,
       cv2.fullname      AS qualifier_full
FROM ia_interactor_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac);



-- #############################################################################
-- #                  intact__interactor_owner__dm     
-- #             useable for interaction and interactor, 
-- #            because both are stored in ia_interactor
-- #############################################################################

-- get all interaction owner of database psi
CREATE MATERIALIZED VIEW v_interaction_owner_xref AS
SELECT xref.parent_ac  AS parent_ac, 
       xref.primaryid  AS mi
FROM ia_institution_xref xref,
     v_cv_mi cv
WHERE     xref.database_ac = cv.ac
      and cv.mi = 'MI:0488';        -- 'psi-mi'
      

CREATE MATERIALIZED VIEW v_interaction_owner AS
SELECT inter.ac        AS ac,
       xref.mi         AS mi,
       inst.shortlabel AS shortlabel,
       inst.fullname   AS fullname
FROM ia_institution inst LEFT OUTER JOIN v_interaction_owner_xref xref
                                      ON ( inst.ac = xref.parent_ac)
                        RIGHT OUTER JOIN ia_interactor inter
                                      ON ( inter.owner_ac = inst.ac);
                                      


