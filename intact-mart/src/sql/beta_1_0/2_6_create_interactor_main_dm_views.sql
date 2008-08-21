-- #############################################################################
-- #                      intact__interactor__main                             
-- #############################################################################


-- counts in how many interaction one interactor is involved
CREATE MATERIALIZED VIEW v_involved_interaction_count AS 
SELECT interactor.ac AS interactor_ac,      -- interactor_key
       COUNT(*)      AS interaction_count   -- interaction_count
FROM v_interaction interaction,
     ia_component com,
     ia_interactor interactor
WHERE     interaction.ac = com.interaction_ac
      and com.interactor_ac = interactor.ac
GROUP BY interactor.ac;


-- stores alle the uniportkb identifiers for a federal query
CREATE MATERIALIZED VIEW v_uniprotkb AS
SELECT inter.ac            AS interactor_key,
       xref.primaryid      AS uniprotkb
FROM ia_interactor_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON ( cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON ( cv2.ac = xref.qualifier_ac)
                            RIGHT OUTER JOIN ia_interactor inter
                                          ON ( inter.ac = xref.parent_ac)
WHERE     cv1.mi = 'MI:0486'  -- cvDatabase(uniport knowledge base) 
      and cv2.mi = 'MI:0356'; -- cvQualifier(identity)



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
       inter_count.interaction_count,
       uniprotkb.uniprotkb
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
                         LEFT OUTER JOIN ia_biosource bio
                                      ON ( inter.biosource_ac = bio.ac)
                         LEFT OUTER JOIN v_involved_interaction_count inter_count
                                      ON ( inter.ac = inter_count.interactor_ac)
                         LEFT OUTER JOIN v_uniprotkb uniprotkb
                                      ON ( inter.ac = uniprotkb.interactor_key)
WHERE cv.mi <> 'MI:0317';    -- 'MI:0317' is equals 'interaction'

