package edu.cwru.eecs.statianalysis.pattern.display;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.cwru.eecs.statianalysis.dbutil.DBUtil;

public class MainOutputPatternData {
	
	public static void main(String args[])
	{
		DBUtil.createDatasource("abb_cir_cirld_5_10");
		String src = "C:/source/rel5_10.0146.release";
		int[] patternToDisplay ={197};
		String textToDisplay = "";
		for(int i=0; i<patternToDisplay.length;i++)
		{
			int patternKey = patternToDisplay[i];
			String text = "";
			DisplayPattern dp = new DisplayPattern(patternKey,"C:/source/rel5_10.0146.release", DBUtil.getDataSource());
			dp.computeDisplayText();
			text = dp.textToDisplay();
			textToDisplay = textToDisplay+"\n"+text;
		}
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter("Pattern197.txt"));
			bw.write(textToDisplay);
			bw.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}

}
