-- #############################################################################
-- #                    fills a temporary table with 
-- #             the sequence of an interactor and it's length
-- #############################################################################

DECLARE
  CURSOR get_seq IS
    SELECT inter.ac,
           seq.sequence_chunk
    FROM ia_interactor inter LEFT OUTER JOIN ia_sequence_chunk seq
                                          ON (inter.ac = seq.parent_ac)
    WHERE seq.parent_ac IS NOT NULL
    ORDER BY inter.ac, seq.sequence_index;
  -- results from the cursor
  interactor_ac ia_interactor.ac%TYPE;
  sequence_chunk ia_sequence_chunk.sequence_chunk%TYPE;
  -- concatinated sequence  
  seq tbl_sequence_tmp.interactor_sequence%TYPE;
  -- stores the last interactor_ac
  last_ac ia_interactor.ac%TYPE := '';
BEGIN
  OPEN get_seq;
  FETCH get_seq INTO last_ac, seq;
  IF get_seq%FOUND THEN
    LOOP 
      FETCH get_seq INTO interactor_ac, sequence_chunk;
      EXIT WHEN get_seq%NOTFOUND;
      IF last_ac = interactor_ac THEN
        seq := seq || sequence_chunk;
      ELSE 
        INSERT INTO tbl_sequence_tmp
               VALUES ( last_ac, seq, LENGTH(seq)); 
        seq := sequence_chunk;
      END IF;
      last_ac := interactor_ac;
    END LOOP;
    INSERT INTO tbl_sequence_tmp
           VALUES ( last_ac, seq, LENGTH(seq)); 
  END IF;
  CLOSE get_seq;
  COMMIT;
END;