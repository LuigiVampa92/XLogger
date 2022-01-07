package com.luigivampa92.xlogger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.text_debug);
        DebugLastLogStorage storage = new DebugLastLogStorage(this);
        textView.setText(storage.getLastLog());
    }
}