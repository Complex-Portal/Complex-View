/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.global;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>3 Sep 2007</pre>
 */
public class GlobalTestDataTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.confidence.global.GlobalTestData#getTargetDirectory()}.
	 */
	@Test
	public final void testGetTargetDirectory() {
		File targetDir = GlobalTestData.getInstance().getTargetDirectory();
		System.out.print(targetDir.getAbsoluteFile());
		assertNotNull(targetDir);
		try {
			File newDir = new File(targetDir.getPath(),"mytest");
			newDir.mkdir();
			String path = newDir.getPath();
			File newfile = new File(path,"test.txt");
			
			FileWriter fw =  new FileWriter(newfile);
			fw.append("testing....");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
