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
import java.io.FileNotFoundException;
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
import java.util.Set;
import java.util.TreeMap;


public class kNNActivity extends AppCompatActivity {
    ListView lv2;
    Button find;
    List<APScan> validationplaces = new ArrayList<>();
    int bestDistance = Integer.MAX_VALUE;
    int k = 3;
    int i = k;

    static class Sample {
        int level;
        String mac;
        String location;

        public void setLevel(int l)
        {
            this.level = l;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setMac(String mac) {
            this.mac = mac;
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
        TextView accuracy = (TextView) findViewById(R.id.submittextAccuracy);
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
            int numCorrect = 0;
            String result_location = "";
            for(APScan sample:validationplaces) {
                System.out.println("sample location: "+sample.getLocation());
                result_location = check_kNN(trainingplace, sample);
                if(classify(result_location, sample.getLocation())) {
                    numCorrect++;
                }
            }
            double acc = (double)numCorrect / (double)validationplaces.size() * 100.0;
            System.out.println("Accuracy: "+acc+"%");
            result.setText(result_location);
            accuracy.setText(String.format("%.2f",acc)+"%");
            k=3;
            bestDistance = Integer.MAX_VALUE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int distance(int a, int b) {
        int sum = 0;
        sum += (a - b) * (a - b);
        return (int)Math.sqrt(sum);
    }

    public String check_kNN(APScan trainingplace, APScan validationplaces_sample)
    {
        //System.out.println("check_kNN");
        String local = validationplaces_sample.getLocation();
        Sample[] best_kNN = new Sample[k];
        HashMap<String, Integer> tmap = sortByValues(trainingplace.map);
        //System.out.println(local+" : "+key+": "+vmap.get(key));
        for (String tkey : tmap.keySet()) {
            //System.out.println("check_kNN"+tkey);
            int lvl;
            if (validationplaces_sample.map.containsKey(tkey)) {
                lvl = validationplaces_sample.map.get(tkey);
                System.out.println("check_kNN: " + tmap.get(tkey) + " : " + lvl + " Best Distance: " + bestDistance);
                int dist = distance(tmap.get(tkey), lvl);
                if (dist < bestDistance) {
                    if (k != 0) {
                        System.out.println("Best Distance: " + bestDistance + " dist: " + dist);
                        bestDistance = dist;
                        best_kNN[i - k] = new Sample();
                        best_kNN[i - k].setMac(tkey);
                        best_kNN[i - k].setLevel(lvl);
                        best_kNN[i - k].setLocation(local);
                        System.out.println("best_kNN[" + i + "-" + k + "]: " + best_kNN[i - k].location);
                        System.out.println("best_kNN[" + i + "-" + k + "]: " + best_kNN[i - k].mac);
                        System.out.println("best_kNN[" + i + "-" + k + "]: " + best_kNN[i - k].level);
                        k--;
                    }
                }
            } else {
                lvl = -99;
                System.out.println("check_kNN: mac: " + tkey + " level: " + lvl);
            }
        }
        // VOTACAO!
        String result = "";
        int vote = 0;
        for (Sample best : best_kNN)
        {
            System.out.println(best.location);
            if (best.location!=null)
            {
                if(best.location.equals(result)){
                    vote++;
                } else {
                    result = best.location;
                }
            } else {
                vote++;
            }
        }
        if (vote>=k/2)
            return result;
        else
            return "";
    }

    public static boolean classify(String best_knn_sample_location, String location) {
        if (best_knn_sample_location.equals(location))
        {
            System.out.println("classify");
            return true;
        } else {
            return false;
        }
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
