package com.liwg.android.preloader;


import com.liwg.android.preloader.interfaces.DataListener;

/**
 * pre-load finished
 * Preloader.refresh() and PreLoader.listen(DataListener listener) are valid
 */
class StateDone extends StateBase {
    StateDone(Worker<?> worker) {
        super(worker);
    }

    @Override
    public boolean refresh() {
        super.refresh();
        return worker.doStartLoadWork() && worker.listenData();
    }

    @Override
    public boolean listenData() {
        super.listenData();
        return worker.doSendLoadedDataToListenerWork();
    }

    @Override
    public boolean listenData(DataListener listener) {
        super.listenData(listener);
        return worker.doSendLoadedDataToListenerWork(listener);
    }

    @Override
    public String name() {
        return "StateDone";
    }
}
