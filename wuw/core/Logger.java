package wuw.core;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author gomez-r
 *
 */
public class Logger {
	
	private static Logger instance = null;
	private static FileWriter logFile = null;
	private String actualLogLine = null;
	
	protected Logger(){
		actualLogLine = "";
		try {
			logFile = new FileWriter((String)Config.get("logFile"), true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized Logger getInstance(){
		if(instance == null){
			instance = new Logger();
		}
		return instance;
	}

	public void writeCurrentLogLine(){
		try {
			logFile.write(actualLogLine + "\n");
			logFile.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void resetLogLine(){
		actualLogLine = "";
	}

	public void updateLogLine(String subStr){
		actualLogLine = actualLogLine.concat(subStr);
	}

}
