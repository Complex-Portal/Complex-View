-- #############################################################################
-- #                      intact__interactor__main                             
-- #############################################################################

-- counts in how many interaction one interactor is involved
CREATE MATERIALIZED VIEW v_involved_interaction_count AS 
SELECT interactor.interactor_ac,       -- interactor_key
       COUNT(*) AS interaction_count   -- interaction_count
FROM v_interaction_type interaction,
     ia_component com,
     v_interactor_type interactor
WHERE     interaction.ac = com.interaction_ac
      and com.interactor_ac = interactor.interactor_ac
GROUP BY interactor.interactor_ac;



CREATE MATERIALIZED VIEW v_interactor AS
SELECT inter.ac                       AS ac,
       inter.shortlabel               AS interactor_shortlabel,
       inter.fullname                 AS interactor_fullname,
       cv.mi                          AS interactor_type_mi,
       cv.shortlabel                  AS interactor_type_short,
       cv.fullname                    AS interactor_type_full,
       bio.taxid                      AS interactor_biosource_taxid,
       bio.shortlabel                 AS interactor_biosource_short,
       bio.fullname                   AS interactor_biosource_full,
       inter.crc64                    AS crc64,
       inter_count.interaction_count  AS interaction_count
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
                         LEFT OUTER JOIN ia_biosource bio
                                      ON ( inter.biosource_ac = bio.ac)
                         LEFT OUTER JOIN v_involved_interaction_count inter_count
                                      ON ( inter.ac = inter_count.interactor_ac)
WHERE cv.mi <> 'MI:0317';    -- 'MI:0317' is equals 'interaction'