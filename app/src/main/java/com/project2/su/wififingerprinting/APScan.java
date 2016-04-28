package com.project2.su.wififingerprinting;

import android.content.Context;

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
        System.out.println("AP Map: "+mac+"  AP Signal: "+signal);
        map.put(mac, signal);
    }

    public String getLocation() {
        return location;
    }

    public int getLevel(String mac)
    {
        return map.get(mac);
    }

    public void generateCsvFile(Context context)
    {
        StringBuilder out = new StringBuilder();
        for (String key : map.keySet())
        {
            out.append(key + ",");
        }

        // remove last ',' from line
        out = new StringBuilder(out.substring(0, out.length() - 1));

        for (int occurence : map.values())
        {
            out.append(occurence + ",");
        }

        // remove last ',' from line
        out = new StringBuilder(out.substring(0, out.length() - 1));
        System.out.println(context.getApplicationContext().getFilesDir());
        try (FileWriter writer = new FileWriter(context.getApplicationContext().getFilesDir()+"WifiScans.csv"))
        {
            writer.append(out.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
