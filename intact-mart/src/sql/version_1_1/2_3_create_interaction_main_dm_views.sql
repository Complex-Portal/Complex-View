-- #############################################################################
-- #                      intact__interaction__main                            
-- #############################################################################


CREATE MATERIALIZED VIEW v_interaction_type AS
SELECT inter.ac,          -- interaction_key
       cv.mi,             -- interaction_type_mi
       cv.shortlabel,     -- interaction_type_short
       cv.fullname        -- interaction_type_full
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
WHERE cv.mi = 'MI:0317';  -- 'interaction'


-- the number of interactor which are involved in a spezific interaction
CREATE MATERIALIZED VIEW v_interactor_count AS
SELECT interaction.ac AS ac,
       COUNT(*)       AS interactor_count
FROM ia_interactor interaction,
     ia_component com
WHERE interaction.ac = com.interaction_ac
GROUP BY interaction.ac;

-- for speed up use only the interaction from interactor
CREATE MATERIALIZED VIEW v_interaction AS
SELECT int_type.ac,              -- interaction_key
       int_type.mi,              -- interaction_type_mi
       int_type.shortlabel,      -- interaction_type_short
       int_type.fullname,        -- interaction_type_full
       i_count.interactor_count  -- interactor_count
FROM v_interaction_type int_type LEFT OUTER JOIN v_interactor_count i_count
                                              ON ( int_type.ac  = i_count.ac );



-- #############################################################################
-- #                  intact__interactor_owner__dm     
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
                                      


