/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import java.io.File;
import java.util.HashMap;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>26 Sep 2007</pre>
 */
public class GlobalData {
	
	private static int count;
	public static long startTime = -1;
	public static long endTime;
	public static long totalProts;
	
	public static HashMap<String,File> getRightPahts(){
		HashMap<String, File> paths = new HashMap<String,File>(3);
		String osName = System.getProperty("os.name");
		
		String pathBlastArchive ="";
		String pathUniprotDb ="";
		File pathWorkDir = null;
		File pathBlastDb = null;
		
		if (osName.startsWith("Linux")){
			pathBlastArchive = "/net/nfs7/vol22/sp-pro5/20071216_iarmean";
			pathUniprotDb = "/net/nfs7/vol22/sp-pro5/20071216_iarmean";
			pathWorkDir = new File("/net/nfs6/vol1/homes/iarmean/tmp");
			pathBlastDb = new File("/net/nfs6/vol1/homes/iarmean/tmp/blastDb");
			
		}else if (osName.startsWith("Windows")){
			pathBlastArchive = "E:/20071016_iarmean";
			pathUniprotDb = "E:/tmp";
			pathWorkDir = 	new File("E:/tmp");		
			pathBlastDb = new File("E:/tmp/blastDb");
		}
		File pathBlast = new File(pathBlastArchive);
		File pathUniprot = new File(pathUniprotDb);	
		
		testDir(pathBlast);
		testDir(pathUniprot);
		testDir(pathWorkDir);
		testDir(pathBlastDb);
		paths.put("blastArchive", pathBlast);
		paths.put("uniprot", pathUniprot);
		paths.put("workDir", pathWorkDir);
		paths.put("blastDb", pathBlastDb);
	//	paths.add(Arrays.asList(pathBlast, pathUniprot, pathWorkDir));
		return paths;
	}
	
	public static void testDir(File workDir){
		if (!workDir.exists()) {
			throw new IllegalArgumentException("WorkDir must exist! " + workDir.getPath());
		}
		if (!workDir.isDirectory()) {
			throw new IllegalArgumentException("WorkDir must be a directory! " + workDir.getPath());
		}
		if (!workDir.canWrite()) {
			throw new IllegalArgumentException("WorkDir must be writable! " + workDir.getPath());
		}
	}

	/**
	 * @return the count
	 */
	public static int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public static void setCount(int nr) {
		count = nr;
	}
	
	public static void increment(int nr){
		count += nr;
	}
	
	public static long eta(long processed, long time, long total){
		return (total * time)/ processed;
	}
}
