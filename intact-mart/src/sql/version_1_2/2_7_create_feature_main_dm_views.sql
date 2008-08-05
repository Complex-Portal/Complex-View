-- #############################################################################
-- #                      intact__feature__main                             
-- #############################################################################

-- for speed up store all information which are  
-- usefull for intact__feature__main.
CREATE MATERIALIZED VIEW v_feature AS
SELECT f.ac             AS feature_key,
       f.shortlabel     AS feature_short,
       f.fullname       AS feature_full,
       cv1.mi           AS feature_type_mi,
       cv1.shortlabel   AS feature_type_short,
       cv1.fullname     AS feature_type_full,
       cv2.mi           AS feature_identmethod_mi,
       cv2.shortlabel   AS feature_identmethod_short,
       cv2.fullname     AS feature_identmethod_full,
       f.component_ac
FROM ia_feature f LEFT OUTER JOIN v_cv_mi cv1
                               ON ( f.featuretype_ac = cv1.ac )
                  LEFT OUTER JOIN v_cv_mi cv2
                               ON ( f.identification_ac = cv2.ac);