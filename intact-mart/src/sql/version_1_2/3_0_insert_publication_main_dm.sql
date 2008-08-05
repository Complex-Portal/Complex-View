-- #############################################################################
-- #                      intact__publication__main                            
-- #############################################################################

INSERT INTO intact__publication__main
SELECT pub.ac,                          -- publication_key
       exp.fullname,                    -- publication_title
       pub_count.interaction_count,     -- interactions_count
       pub_count.experiment_count       -- experiment_count
FROM ia_publication pub LEFT OUTER JOIN v_publication_counts pub_count
                                     ON (pub.ac = pub_count.pub_ac)
                        LEFT OUTER JOIN ia_experiment exp
                                     ON (pub.ac = exp.publication_ac);


-- #############################################################################
-- #                  intact__publication_alias__dm                            
-- #############################################################################                           

INSERT INTO intact__publication_alias__dm                                 
SELECT pub.ac,           -- publication_key
       ali.name,         -- name
       cv.mi,            -- alias_type_mi
       cv.shortlabel,    -- alias_type_short
       cv.fullname       -- alias_type_full
FROM ia_publication_alias ali LEFT OUTER JOIN v_cv_mi cv
                                           ON (cv.ac = ali.aliastype_ac)
                             RIGHT OUTER JOIN ia_publication pub
                                           ON ( ali.parent_ac = pub.ac );


-- #############################################################################
-- #                  intact__publication_xref__dm                             
-- #############################################################################

INSERT INTO intact__publication_xref__dm
SELECT pub.ac,             -- publication_key
       xref.primaryid,     -- primary_id
       xref.secondaryid,   -- secondary_id
       cv1.mi,             -- database_mi
       cv1.shortlabel,     -- database_short
       cv1.fullname,       -- database_full
       cv2.mi,             -- qualifier_mi
       cv2.shortlabel,     -- qualifier_short
       cv2.fullname        -- qualifier_full
FROM ia_publication_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                           ON (cv1.ac = xref.database_ac)
                              LEFT OUTER JOIN v_cv_mi cv2
                                           ON (cv2.ac = xref.qualifier_ac)
                             RIGHT OUTER JOIN ia_publication pub
                                           ON ( pub.ac = xref.parent_ac);



-- #############################################################################
-- #               intact__publication_annotation__dm                          
-- #############################################################################

INSERT INTO intact__publication_anno__dm
SELECT pub.ac,            -- publication_key
       anno.description,  -- description
       anno.mi,           -- topic_mi
       anno.shortlabel,   -- topic_short
       anno.fullname      -- topic_full
FROM ia_pub2annot pub_anno LEFT OUTER JOIN v_annotation anno
                                        ON (anno.annotation_ac = pub_anno.annotation_ac)
                          RIGHT OUTER JOIN ia_publication pub
                                        ON (pub.ac = pub_anno.publication_ac);

COMMIT;

