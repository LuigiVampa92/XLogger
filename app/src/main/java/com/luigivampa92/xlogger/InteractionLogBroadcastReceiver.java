package com.luigivampa92.xlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.luigivampa92.xlogger.data.db.AppDatabase;
import com.luigivampa92.xlogger.data.db.InteractionLogEntity;
import com.luigivampa92.xlogger.domain.InteractionLog;
import com.luigivampa92.xlogger.hooks.XLog;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public final class InteractionLogBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG:
                handleLogRawNfcTag(context, intent);
                break;
        }
    }

    private void handleLogRawNfcTag(Context context, Intent intent) {
        InteractionLog interactionLog = intent.getParcelableExtra(BroadcastConstants.EXTRA_DATA);
        if (interactionLog == null || interactionLog.getEntries() == null || interactionLog.getEntries().isEmpty()) {
            return;
        }

        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        InteractionLogEntity entity = InteractionLogEntity.fromInteractionLog(interactionLog);
        XLog.v("save entity - start");
        Disposable d = db.interactionLogDao().insert(entity)  // todo wtf ?
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        XLog.d("save entity - success");
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        XLog.d("save entity - error");
                    }
                }
        );
    }
}
