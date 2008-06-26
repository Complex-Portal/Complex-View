-- #############################################################################
-- #                 use these views for every cv term                           
-- #############################################################################

-- all primaryids saved (MI, brenda, cabri,.. )
CREATE MATERIALIZED VIEW v_cv_primaryid AS
SELECT cv.ac           AS ac,
       xref.primaryid  AS primaryid,
       cv.shortlabel   AS shortlabel,
       cv.fullname     AS fullname
FROM ia_controlledvocab cv LEFT OUTER JOIN ia_controlledvocab_xref xref
                                        ON ( cv.ac = xref.parent_ac );


-- only cv's stored with mi
CREATE MATERIALIZED VIEW v_cv_mi AS
SELECT cv.ac           AS ac,
       xref.primaryid  AS mi,
       cv.shortlabel   AS shortlabel,
       cv.fullname     AS fullname
FROM ia_controlledvocab cv LEFT OUTER JOIN ia_controlledvocab_xref xref
                                        ON ( cv.ac = xref.parent_ac )
WHERE xref.primaryid LIKE 'MI:%';

-- #############################################################################
-- #               intact__XXX_annotation__dm
-- #  XXX is a variable for experiment, interaction, interactor and feature
-- #############################################################################

CREATE MATERIALIZED VIEW v_annotation AS
SELECT anno.ac          AS annotation_ac,
       anno.description AS description,
       cv.mi            AS topic_mi,
       cv.shortlabel    AS topic_short,
       cv.fullname      AS topic_full
FROM ia_annotation anno LEFT OUTER JOIN v_cv_mi cv
                                     ON (cv.ac = anno.topic_ac);