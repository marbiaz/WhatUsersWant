/**
 * Logger.java
 * 
 * Copyright 2012
 * This file is part of the WUW (What Users Want) service.
 * 
 * WUW is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version.
 * 
 * WUW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with WUW. 
 * If not, see http://www.gnu.org/licenses/.
 */

package wuw.core;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Singleton object that lets records logs in a text file
 * @author carvajal-r
 *
 */
public class Logger {

private static Logger instance = null;
private static FileWriter logFile = null;
private String actualLogLine = null;

protected Logger(String nameFile){
  actualLogLine = "";
  try {
    logFile = new FileWriter(nameFile);

  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
}

/**
 * Gets the current instance of this object 
 * @param nameFile Text file's full path 
 * @return Instance of this object
 */
public static synchronized Logger getInstance(String nameFile){
  if(instance == null){
    instance = new Logger(nameFile);
  }
  return instance;
}

/**
 * Write the current value of {@code actualLogLine} in the text file
 */
public void writeCurrentLogLine(){
  try {
    logFile.write(actualLogLine + "\n");
    logFile.flush();
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
}

public void writeLine(String line){
  try {
    logFile.write(line + "\n");
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
