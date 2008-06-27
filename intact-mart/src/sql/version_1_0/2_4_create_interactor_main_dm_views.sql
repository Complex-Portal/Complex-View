-- #############################################################################
-- #                      intact__interactor__main                             
-- #############################################################################

CREATE MATERIALIZED VIEW v_interactor_type AS
SELECT inter.ac         AS interactor_key,
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
-- 'MI:0317' is equals 'interaction'
WHERE cv.mi <> 'MI:0317';


CREATE MATERIALIZED VIEW v_com_roles AS
SELECT com.interactor_ac     AS interactor_ac,    -- this is the real interactor_ac
       com.ac                AS interactor_key,
       com.interaction_ac    AS interaction_key,
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



-- #############################################################################
-- #                  intact__interactor_xref__dm
-- #             useable for interaction and interactor, 
-- #            because both are stored in ia_interactor
-- #############################################################################


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
