-- #############################################################################
-- #                      intact__interactor__main                             
-- #############################################################################

-- for speed up store all information which are  
-- usefull for intact__interactor__main.
CREATE MATERIALIZED VIEW v_interactor_type AS
SELECT inter.ac         AS interactor_ac,
       inter.shortlabel AS interactor_shortlabel,
       inter.fullname   AS interactor_fullname,
       cv.mi            AS interactor_type_mi,
       cv.shortlabel    AS interactor_type_short,
       cv.fullname      AS interactor_type_full,
       bio.taxid        AS interactor_biosource_taxid,
       bio.shortlabel   AS interactor_biosource_short,
       bio.fullname     AS interactor_biosource_full,
       inter.crc64      AS crc64
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
                         LEFT OUTER JOIN ia_biosource bio
                                      ON ( inter.biosource_ac = bio.ac)
WHERE cv.mi <> 'MI:0317';    -- 'MI:0317' is equals 'interaction'


CREATE MATERIALIZED VIEW v_com_roles AS
SELECT com.ac                AS interactor_key,
       com.interactor_ac     AS interactor_ac,    
       com.interaction_ac    AS interaction_ac,
       cv1.mi                AS biological_role_mi,
       cv1.shortlabel        AS biological_role_short,
       cv1.fullname          AS biological_role_full,
       cv2.mi                AS experimental_role_mi,
       cv2.shortlabel        AS experimental_role_short,
       cv2.fullname          AS experimental_role_full,
       expres_bio.taxid      AS component_expressed_in_taxid,
       expres_bio.shortlabel AS component_expressed_in_short,
       expres_bio.fullname   AS component_expressed_in_full,
       com.stoichiometry     AS stoichiometry
FROM ia_component com LEFT OUTER JOIN v_cv_mi cv1
                                   ON ( com.biologicalrole_ac = cv1.ac)
                      LEFT OUTER JOIN v_cv_mi cv2
                                   ON ( com.experimentalrole_ac = cv2.ac)
                      LEFT OUTER JOIN ia_biosource expres_bio
                                   ON ( com.expressedin_ac = expres_bio.ac);


-- counts in how many interaction on interactor is involved
CREATE MATERIALIZED VIEW v_involved_interaction_count AS 
SELECT interactor.interactor_ac,       -- interactor_key
       COUNT(*) AS interaction_count   -- interaction_count
FROM v_interaction_type interaction,
     ia_component com,
     v_interactor_type interactor
WHERE     interaction.ac = com.interaction_ac
      and com.interactor_ac = interactor.interactor_ac
GROUP BY interactor.interactor_ac;


-- #############################################################################
-- #                  intact__interactor_xref__dm
-- #############################################################################

-- it shows to every component with its interactor_ac
CREATE MATERIALIZED VIEW v_get_interactor_key AS
SELECT com.ac                  AS interactor_key,  -- interactor_key
       int_type.interactor_ac                      -- interactor_ac
FROM ia_component com LEFT OUTER JOIN v_interactor_type int_type 
                                   ON ( int_type.interactor_ac = com.interactor_ac);

-- for speed up this is a own view
CREATE MATERIALIZED VIEW v_interactor_xref AS
SELECT xref.parent_ac        AS interaction_ac,
       xref.primaryid        AS primary_id,
       xref.secondaryid      AS secondary_id,
       cv1.mi                AS database_mi,
       cv1.shortlabel        AS database_short,
       cv1.fullname          AS database_full,
       cv2.mi                AS qualifier_mi,
       cv2.shortlabel        AS qualifier_short,
       cv2.fullname          AS qualifier_full
FROM ia_interactor_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac);


CREATE MATERIALIZED VIEW v_component_xref AS
SELECT xref.parent_ac    AS xref_ac,
       xref.primaryid    AS primary_id,
       xref.secondaryid  AS secondary_id,
       cv1.mi            AS database_mi,
       cv1.shortlabel    AS database_short,
       cv1.fullname      AS database_full,
       cv2.mi            AS qualifier_mi,
       cv2.shortlabel    AS qualifier_short,
       cv2.fullname      AS qualifier_full
FROM ia_component_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                         ON (cv1.ac = xref.database_ac)
                            LEFT OUTER JOIN v_cv_mi cv2
                                         ON (cv2.ac = xref.qualifier_ac);
