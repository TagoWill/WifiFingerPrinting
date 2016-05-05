package com.project2.su.wififingerprinting;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class kNNActivity extends AppCompatActivity {
    ListView lv2;
    Button find;
    List<APScan> validationplaces = new ArrayList<>();
    int k = 3;
    List<Sample> best_knn;

    static class Sample {
        int distance;
        String location;

        Sample(int l, String location){
            this.distance = l;
            this.location = location;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knn);

        find = (Button) findViewById(R.id.buttonFind);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputText = (EditText) findViewById(R.id.textPlace);

                if (!inputText.getText().toString().trim().equals(""))
                {
                    String location = inputText.getText().toString();
                    findkNN(location);
                } else {
                    Toast.makeText(kNNActivity.this, "Please Enter Location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void findkNN(String location){
        APScan trainingplace = new APScan();
        WifiManager wifimanager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled())
        {
            wifimanager.startScan();
            List<ScanResult> results = wifimanager.getScanResults();
            System.out.println("Location: "+location+"  APs Found: "+results.size());
            String apstring []= new String[results.size()];
            trainingplace.setLocation(location);
            int i =0;
            for (ScanResult ap : results) {
                trainingplace.setMap(ap.BSSID, ap.level);
                apstring[i] = "SSID: " + ap.SSID + "\nBSSID: "+ap.BSSID+ "\nSIGNAL: "+ap.level+" dB\n";
                i++;
            }
            TextView result = (TextView) findViewById(R.id.submittextResult);
            lv2=(ListView)findViewById(R.id.listViewResults);
            lv2.setAdapter(new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,apstring));

            try {
                BufferedReader reader = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/FingerPrinting/WifiScans.csv"));
                String printaps;
                APScan savedplace;
                String printapssplit[];
                while ((printaps=reader.readLine()) != null){
                    //System.out.println(printaps);
                    savedplace = new APScan();
                    printapssplit = printaps.split(";");
                    savedplace.setLocation(printapssplit[0]);
                    int h = 1;
                    while (h<printapssplit.length){
                        savedplace.setMap(printapssplit[h], Integer.parseInt(printapssplit[h+1]));
                        h+=2;
                    }
                    validationplaces.add(savedplace);
                }
                String result_location = "";
                best_knn = new ArrayList<>();
                for(APScan sample:validationplaces) {
                    System.out.println("sample location: "+sample.getLocation());
                    check_kNN(trainingplace, sample);
                }
                result_location = voter();
                result.setText(result_location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(kNNActivity.this, "Please Enable Wi-Fi", Toast.LENGTH_SHORT).show();
        }
    }

    public static int distance(int a, int b) {
        int sum = 0;
        sum += (a - b) * (a - b);
        return (int)Math.sqrt(sum);
    }

    public void check_kNN(APScan trainingplace, APScan validationplaces_sample)
    {
        String local = validationplaces_sample.getLocation();
        HashMap<String, Integer> tmap = sortByValues(trainingplace.map);
        //System.out.println(local+" : "+key+": "+vmap.get(key));
        for (String tkey : tmap.keySet()) {
            //System.out.println("check_kNN"+tkey);
            int lvl;
            if (validationplaces_sample.map.containsKey(tkey)) {
                lvl = validationplaces_sample.map.get(tkey);
            } else {
                lvl = -99;
            }
            int dist = distance(tmap.get(tkey), lvl);
            best_knn.add(new Sample(dist, local));
            System.out.println("check_kNN: " + tmap.get(tkey) + " : " + lvl + " Distance: " + dist);
        }
    }

    public String voter(){
        Collections.sort(best_knn, new Comparator<Sample>() {
            @Override
            public int compare(Sample lhs, Sample rhs) {
                if(lhs.distance == rhs.distance){
                    return 0;
                }else if(lhs.distance < rhs.distance){
                    return -1;
                }else{
                    return 1;
                }
            }
        });

        Map<String,Integer> result = new HashMap<>();
        Integer freq;
        for(int i=0;i<k;i++) {
            best_knn.get(i).location = (best_knn.get(i).location).replaceAll("[^A-Za-z]","");
            System.out.println("Voter location ["+i+"]: "+best_knn.get(i).location);
            freq = result.get(best_knn.get(i).location);
            result.put(best_knn.get(i).location, (freq == null) ? 1 : freq + 1);
        }

        int max = -1;
        String mostFrequent = "";
        for(Map.Entry<String, Integer> x: result.entrySet()){
            if (x.getValue() > max) {
                mostFrequent = x.getKey();
                max = x.getValue();
            }
        }
        System.out.println("Voter Result: " + mostFrequent);
        double acc = (double)max/(double)k * 100.0;
        TextView accuracy = (TextView) findViewById(R.id.submittextAccuracy);
        accuracy.setText(String.format("%.2f",acc)+"%");
        System.out.println("Accuracy: "+acc+"%");
        return mostFrequent;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
