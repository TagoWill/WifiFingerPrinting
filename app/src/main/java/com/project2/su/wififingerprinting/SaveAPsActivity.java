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
import android.widget.Toast;

import java.util.List;
import java.util.Objects;


public class SaveAPsActivity extends AppCompatActivity {
    ListView lv;
    WifiManager wifimanager;
    Button buttonSave;
    String[] places = {"lugar1", "lugar2", "lugar3", "lugar4"};
    int[] nmeasures = {0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputText = (EditText) findViewById(R.id.editText);

                if (!inputText.getText().toString().trim().equals("")) {
                    String location = inputText.getText().toString();
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
                } else {
                    Toast.makeText(SaveAPsActivity.this, "Please Enter Location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void scanAPs(String location, APScan scan){
        wifimanager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
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
        lv=(ListView)findViewById(R.id.listView);
        lv.setAdapter(new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,apstring));
        scan.generateCsvFile(getApplicationContext());
    }
}
