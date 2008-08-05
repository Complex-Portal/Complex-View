-- #############################################################################
-- #                      intact__interaction__main                            
-- #############################################################################


-- the number of interactor which are involved in a spezific interaction
CREATE MATERIALIZED VIEW v_interactor_count AS
SELECT interaction.ac AS ac,
       COUNT(*)       AS interactor_count
FROM ia_interactor interaction,
     ia_component com,
     ia_interactor interactor
WHERE     interaction.ac = com.interaction_ac
      and interactor.ac = com.interactor_ac
GROUP BY interaction.ac;


-- the number of component which are involved in a spezific interaction
CREATE MATERIALIZED VIEW v_component_count AS
SELECT interaction.ac AS ac,
       COUNT(*)       AS component_count
FROM ia_interactor interaction,
     ia_component com
WHERE interaction.ac = com.interaction_ac
GROUP BY interaction.ac;


-- use only the interaction from the table interactor
CREATE MATERIALIZED VIEW v_interaction AS
SELECT inter.ac,                   -- interaction_key
       cv.mi,                      -- interaction_type_mi
       cv.shortlabel,              -- interaction_type_short
       cv.fullname,                -- interaction_type_full
       int_count.interactor_count, -- interactor_count
       com_count.component_count   -- component_count
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
                         LEFT OUTER JOIN v_component_count com_count
                                      ON ( inter.ac  = com_count.ac )
                         LEFT OUTER JOIN v_interactor_count int_count
                                      ON ( inter.ac  = int_count.ac )
WHERE cv.mi = 'MI:0317';  -- 'interaction'



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
                                      


