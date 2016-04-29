package com.project2.su.wififingerprinting;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import java.util.List;


public class kNNActivity extends AppCompatActivity {
    ListView lv2;
    Button find;

    /*static class Sample {
        int level;
        String mac;
    }*/

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
        APScan place = new APScan();
        WifiManager wifimanager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifimanager.startScan();
        List<ScanResult> results = wifimanager.getScanResults();
        System.out.println("Location: "+location+"  APs Found: "+results.size());
        String apstring []= new String[results.size()];
        place.setLocation(location);
        int i =0;
        for (ScanResult ap : results) {
            place.setMap(ap.BSSID, ap.level);
            apstring[i] = "SSID: " + ap.SSID + "\nBSSID: "+ap.BSSID+ "\nSIGNAL: "+ap.level+" dB\n";
            i++;
        }
        TextView result = (TextView) findViewById(R.id.textResult2);
        result.setText("DEU");
        lv2=(ListView)findViewById(R.id.listViewResults);
        lv2.setAdapter(new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,apstring));

        /*try {
            List<Sample> trainingSet = readFile("trainingsample.csv");
            List<Sample> validationSet = readFile("validationsample.csv");
            int numCorrect = 0;
            for(Sample sample:validationSet) {
                if(classify(trainingSet, sample.level) == sample.label) numCorrect++;
            }
            System.out.println("Accuracy: " + (double)numCorrect / validationSet.size() * 100 + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /*public static List<Sample> readFile(String file) throws IOException {
        List<Sample> samples = new ArrayList<Sample>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = reader.readLine(); // ignore header
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                APScan sample = new APScan();
                sample.label = Integer.parseInt(tokens[0]);
                sample.pixels = new int[tokens.length - 1];
                for(int i = 1; i < tokens.length; i++) {
                    sample.pixels[i-1] = Integer.parseInt(tokens[i]);
                }
                samples.add(sample);
            }
        } finally { reader.close(); }
        return samples;
    }

    public static int distance(int[] a, int[] b) {
        int sum = 0;
        for(int i = 0; i < a.length; i++) {
            sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return (int)Math.sqrt(sum); // euclidian distance would be sqrt(sum)...
    }

    public static int classify(List<Sample> trainingSet, int[] pixels) {
        int label = 0, bestDistance = Integer.MAX_VALUE;
        for(Sample sample: trainingSet) {
            int dist = distance(sample.pixels, pixels);
            if(dist < bestDistance) {
                bestDistance = dist;
                label = sample.label;
            }
        }
        return label;
    }*/
}
