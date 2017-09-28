package com.github.ipcjs.baidumapsdk.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.ipcjs.explorer.ExplorerFragment;

/**
 * Created by maomaoku on 2017/9/28.
 */

public class Launcher extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExplorerFragment.setupExplorer(this,
                MainActivity.class,
                CoordTypeFragment.class
        );
    }
}
