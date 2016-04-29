package com.project2.su.wififingerprinting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button buttonsave, buttonknn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonsave = (Button) findViewById(R.id.buttonSaveIntent);
        buttonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saveapsActivity = new Intent(MainActivity.this, SaveAPsActivity.class);
                MainActivity.this.startActivity(saveapsActivity);
            }
        });

        buttonknn = (Button) findViewById(R.id.buttonKNNIntent);
        buttonknn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findkNNActivity = new Intent(MainActivity.this, kNNActivity.class);
                MainActivity.this.startActivity(findkNNActivity);
            }
        });
    }
}