package org.cfb.ungentry.census.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

	public static String[] retrieveFilesFromPattern(String iFile, String iPattern){
		
		File aDirectory = new File( new File(iFile).getParent() );
		
		//System.out.println("Directory:"+aDirectory.getAbsolutePath());
		File[] aFiles =  aDirectory.listFiles();
		Vector<String> aFileList = new Vector<String>();
		for (File aEntry: aFiles){
			
				if (Pattern.matches(iPattern, aEntry.getName())) {
					//System.out.println("Selected:"+aEntry.getName());
					aFileList.add(aEntry.getAbsolutePath());
				}
			
		}
		
		return aFileList.toArray(new String[aFileList.size()]);
		
	}
	
	public static void unzipFile(String iFileName, String iOutputFolder){

		File aFile = new File(iFileName);
		String  aParent  = aFile.getParent();

		File aOutFolder = new File(aParent+ File.separator +iOutputFolder);
		aOutFolder.mkdirs();

		try {
			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(iFileName));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){

				String fileName = ze.getName();
				File newFile = new File(aOutFolder.getAbsolutePath() + File.separator + fileName);

				//System.out.println("file unzip : "+ newFile.getAbsoluteFile());

				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos;
				try {
					fos = new FileOutputStream(newFile);

					int len;
					byte[] buffer = new byte[1024];
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();   
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}         
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
