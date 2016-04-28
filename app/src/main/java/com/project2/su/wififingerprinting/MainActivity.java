package com.project2.su.wififingerprinting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    WifiManager wifimanager;
    Button buttonScan, buttonAnalysis;
    String[] places = {"lugar1", "lugar2", "lugar3", "lugar4"};
    int[] nmeasures = {0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=(ListView)findViewById(R.id.listView);

        wifimanager=(WifiManager)getSystemService(Context.WIFI_SERVICE);

        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Scanning....", Toast.LENGTH_SHORT).show();
                EditText txtInput = (EditText) findViewById(R.id.editText);

                if (txtInput != null) {
                    String location = txtInput.getText().toString();
                    //System.out.println(location);
                    if (Objects.equals(location, places[0]))
                    {
                        APScan place0 = new APScan();
                        //System.out.println(location+" _0_ "+nmeasures[0]);
                        scanAPs(location, place0);
                        nmeasures[0]++;
                    } else if (Objects.equals(location, places[1]))
                    {
                        APScan place1 = new APScan();
                        //System.out.println(location+" _1_ "+nmeasures[1]);
                        scanAPs(location, place1);
                        nmeasures[1]++;
                    }
                }
            }
        });

        buttonAnalysis = (Button) findViewById(R.id.buttonAnalysis);
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Analysing....", Toast.LENGTH_SHORT).show();
                Intent analyse = new Intent(MainActivity.this, AnalysisActivity.class);
                MainActivity.this.startActivity(analyse);
            }
        });
    }

    public void scanAPs(String location, APScan scan){
        wifimanager.startScan();
        List<ScanResult> results = wifimanager.getScanResults();
        System.out.println("Location: "+location+"  APs Found: "+results.size());
        String apstring []= new String[results.size()];
        scan.setLocation(location);
        int i =0;
        for (ScanResult ap : results) {
            scan.setMap(ap.BSSID, ap.level);
            apstring[i] = "SSID: " + ap.SSID + "\nBSSID: "+ap.BSSID+ "\nSIGNAL: "+ap.level+" dB\n";
            i++;
        }
        lv.setAdapter(new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,apstring));
        scan.generateCsvFile(getApplicationContext());
    }
}