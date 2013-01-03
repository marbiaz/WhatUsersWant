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
      return "'" + key + "': " + Float.toString(value);
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
  int i, j = 0;
  Iterator<Entry<String, PrefEntry[]>> it = prefs.entrySet().iterator();
  Entry<String, PrefEntry[]> e;
  PrefEntry[] value;
  String res = "{", strTmp;
  while (it.hasNext()) {
    e = it.next();
    res += "'" + e.getKey() + "': ";
    value = e.getValue();
    strTmp = "{";
    for(i = 0; i < value.length; i++){
      if(i != value.length - 1)
        strTmp += value[i].toString() + ", ";
      else
        strTmp += value[i].toString() + "}";
    }
    if(j != prefs.size() -1)
      res += strTmp + ", ";
    else
      res += strTmp + "}";
    j++;
  }
  return res;
}

private PrefEntry[] getVectorOfPrefs(String entryStr){
  PrefEntry[] result;
  String pref;
  PrefEntry item;
  ArrayList<PrefEntry> tmp = new ArrayList<PrefEntry>();
  Scanner decoder = new Scanner(entryStr).useDelimiter("\\[\\]|\\[|\\]|\\,\\s");
  while(decoder.hasNext()){
    pref = decoder.next();
    item = new PrefEntry(pref, Config.rand.nextInt(11) * 0.1f);
    tmp.add(item);
  }
  result = new PrefEntry[tmp.size()];
  for(int i = 0; i < result.length; i++)
    result[i] = tmp.get(i);
  return result;
}

}
