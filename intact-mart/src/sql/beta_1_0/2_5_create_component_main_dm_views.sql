-- #############################################################################
-- #                      intact__component__main                             
-- #############################################################################

-- create as view for speed up reason
CREATE MATERIALIZED VIEW v_com_roles AS
SELECT com.ac                AS component_key,   
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