package com.luigivampa92.xlogger.xposed;

import android.content.Context;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostCardEmulationHooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context context;

    public HostCardEmulationHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        this.lpparam = lpparam;
        this.context = context;
    }



}
