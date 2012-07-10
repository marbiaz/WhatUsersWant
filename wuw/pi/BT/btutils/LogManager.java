/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package wuw.pi.BT.btutils;

import java.io.*;
import java.util.*;

/**
 * Utility class to output information to a file
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class LogManager {
    private String filename;
    @SuppressWarnings("unused")
    private OutputStream os;
    public FileWriter fw;

    public LogManager(String logfile) {
        this.filename = logfile;
    }

    /**
     * Write the given string to the file corresponding to this manager
     * @param s String
     */
    synchronized public void writeLog(String s){
        try{
            this.fw = new FileWriter(this.filename, true);
            Date d = new Date();

            this.fw.write(d+" : "+s+"\r\n");
            this.fw.flush();
            this.fw.close();
        }catch(Exception e){
            System.out.println("Not able to write to log file");
        }
    }

}