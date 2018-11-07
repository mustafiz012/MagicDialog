package com.musta.libraries.magicdialogdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.musta.libraries.magic_dialog.CustomDialog;

public class MainActivity extends Activity {
    private CustomDialog sampleDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showCustomDialog(View view) {
        sampleDialog = new CustomDialog(this);
        sampleDialog.setTitle("Custom Dialog!");
        sampleDialog.setMessage("Connect to internet and retry");
        sampleDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sampleDialog.dismiss();
            }
        });
        sampleDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sampleDialog.dismiss();
            }
        });
        sampleDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode==KeyEvent.KEYCODE_BACK){
                    sampleDialog.dismiss();
                }
                return false;
            }
        });
        sampleDialog.setCancelable(true);
        sampleDialog.show();
    }
}
