package com.project2.su.wififingerprinting;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class APScan {

    HashMap<String, Integer> map;
    String location;

    public APScan() {
        map = new HashMap<String, Integer>();
        location = "";
    }

    public void setLocation(String l){
        //System.out.println("setLocation: "+l);
        location = l;
    }

    public void setMap(String mac, int signal){
        //System.out.println("AP Map: "+mac+"  AP Signal: "+signal);
        map.put(mac, signal);
    }

    public String getLocation() {
        return location;
    }

    public int getLevel(String mac)
    {
        if(map.containsKey(mac))
            return map.get(mac);
        return -99;
    }

    public void generateCsvFile(Context context)
    {
        StringBuilder out = new StringBuilder();
        out.append(location + ";");

        for (String key : map.keySet())
        {
            out.append(key + ";");
            out.append(map.get(key)+";");
        }

        out.append("\n");
        File dir = new File(Environment.getExternalStorageDirectory(), "FingerPrinting/");
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(dir.getAbsolutePath()+"/WifiScans.csv", true))
        {
            writer.append(out.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
