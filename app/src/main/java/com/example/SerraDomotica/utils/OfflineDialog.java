package com.example.SerraDomotica.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import com.example.SerraDomotica.R;

public class OfflineDialog extends Dialog {

    public OfflineDialog(@NonNull Context context) {
        super(context, R.style.NoWifiDialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_offline);
    }
}
