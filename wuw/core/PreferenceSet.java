/**
 * PreferenceSet.java
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
 * You should have received a copy of the GNU General Public License along with Foobar. 
 * If not, see http://www.gnu.org/licenses/.
 */

package wuw.core;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Scanner;


/**
 * This class implements a set of preferences, to be associated to a peer for a
 * content. It is supposed that the number and the names of preferences does not
 * change during the execution, after the initialization, as opposed to their
 * values.
 * 
 * @author Marco Biazzini
 * @date 2012 Feb 10
 */
class PreferenceSet implements Externalizable {

  public class PrefEntry implements Externalizable{
  
    private String key;
    private float value;
    
    public PrefEntry(){}
  
    public PrefEntry(String key, float value){
      this.key = key;
      this.value = value;
    }
  
    
    public String getKey() {
      return key;
    }
  
    
    public float getValue() {
      return value;
    }
    
    public String toString(){
      return "('" + key + "', " + Float.toString(value) + ")";
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException {
      // TODO Auto-generated method stub
      key = in.readUTF();
      value = in.readFloat();
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      // TODO Auto-generated method stub
      out.writeUTF(key);
      out.writeFloat(value);
      out.flush();
    }
  
  }

LinkedHashMap<String, PrefEntry[]> prefs;

PreferenceSet(){
  prefs = new LinkedHashMap<String, PrefEntry[]>();
}

PreferenceSet(Map<String, String> preferences){
  prefs = new LinkedHashMap<String, PrefEntry[]>();
  if(preferences.size() != 0){
    String keyStr, valueStr;
    PrefEntry[] value;
    Entry<String, String> entry;
    Iterator<Entry<String, String>> prefIte = preferences.entrySet().iterator();
    while(prefIte.hasNext()){
      entry = prefIte.next();
      keyStr = entry.getKey();
      valueStr = entry.getValue();
      value = getVectorOfPrefs(valueStr);
      prefs.put(keyStr, value);
    }
    Config.logger.writeLine(this.toString()+"&{}&0&");
  }
}


PrefEntry[] getValue(String name) {
  if (prefs.containsKey(name)) {
    return prefs.get(name);
  }
  return null;
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 */
@Override
public void writeExternal(ObjectOutput out) throws IOException {
  int size = prefs.size();
  out.writeInt(size);
  PrefEntry[] vector;
  Entry<String, PrefEntry[]> p;
  Iterator<Entry<String, PrefEntry[]>> entryItera = prefs.entrySet().iterator();
  while(entryItera.hasNext()){
    p = entryItera.next();
    out.writeUTF(p.getKey());
    vector = p.getValue();
    out.writeInt(vector.length);
    for(int i = 0; i < vector.length; i++)
      vector[i].writeExternal(out);
  }
  out.flush();
}


/*
 * (non-Javadoc)
 * 
 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 */
@Override
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
  PrefEntry[] vector;
  PrefEntry item;
  int vectorLength;
  int size = in.readInt();
  String name;
  for (int i = 0; i < size; i++) {
    name = in.readUTF();
    vectorLength = in.readInt();
    vector = new PrefEntry[vectorLength];
    for(int j = 0; j < vectorLength; j++){
      item = new PrefEntry();
      item.readExternal(in);
      vector[j] = item;
    }
    prefs.put(name, vector);
  }
}


public String toString() {
  int k = 0, i;
  Iterator<Entry<String, PrefEntry[]>> it = prefs.entrySet().iterator();
  Entry<String, PrefEntry[]> e;
  PrefEntry[] value;
  String res = "{";
  while (it.hasNext()) {
    e = it.next();
    if(k != prefs.size() - 1){
      res += "'" + e.getKey() + "': [";
      value = e.getValue();
      for(i = 0; i < value.length; i ++){
        if(i != value.length - 1){
          res += value[i].toString() + ", ";
        }else{
          res += value[i].toString() + "]";
        }
      }
      res += ", ";
    }else{
      res += "'" + e.getKey() + "': [";
      value = e.getValue();
      for(i = 0; i < value.length; i ++){
        if(i != value.length - 1){
          res += value[i].toString() + ", ";
        }else{
          res += value[i].toString() + "]";
        }
      }
      res += "}";
    }
    k++;
  }
  return res;
}

private PrefEntry[] getVectorOfPrefs(String entryStr){
  String pref, prefStr; float prefValue;
  PrefEntry[] result;
  PrefEntry item;
  ArrayList<PrefEntry> tmp = new ArrayList<PrefEntry>();
  Scanner tplDecoder;
  Scanner decoder = new Scanner(entryStr).useDelimiter("\\[\\]|\\[\\(|\\)\\]|\\)\\,\\(");
  while(decoder.hasNext()){
    pref = decoder.next();
    tplDecoder = new Scanner(pref).useDelimiter("\\,");
    while(tplDecoder.hasNext()){
       prefStr = tplDecoder.next();
       prefValue = Float.valueOf(tplDecoder.next());
       item = new PrefEntry(prefStr, prefValue);
       tmp.add(item);
    }
  }
  result = new PrefEntry[tmp.size()];
  for(int i = 0; i < result.length; i++)
    result[i] = tmp.get(i);
  return result;
}

/*
 * Initialization of preferences in a random way.
 * The input string, given from the XML file, has the next format:
 * [germany, france, italy, mexico]
 */
//private PrefEntry[] getVectorOfPrefs(String entryStr){
//  String pref; float step;
//  PrefEntry[] result;
//  PrefEntry item;
//  ArrayList<PrefEntry> tmp = new ArrayList<PrefEntry>();
//  Scanner decoder = new Scanner(entryStr).useDelimiter("\\[\\]|\\[|\\]|\\,\\s");
//  while(decoder.hasNext()){
//    pref = decoder.next();
//    step = Config.rand.nextInt(2) == 1 ? 0.1f : -0.1f;
//    item = new PrefEntry(pref, Config.rand.nextInt(11) * step);
//    tmp.add(item);
//  }
//  result = new PrefEntry[tmp.size()];
//  for(int i = 0; i < result.length; i++)
//    result[i] = tmp.get(i);
//  return result;
//}

}
