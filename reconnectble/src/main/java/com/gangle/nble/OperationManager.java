package com.gangle.nble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

/**
 * Created by Gang Tong on 16/7/25.
 */
final class OperationManager {

    private List<Observable> operationList = Collections.synchronizedList(new ArrayList<Observable>());
    private Observable curObservable = null;

    /**
     * "添加"操作
     */
    public synchronized void pend(Observable operation) {
        operationList.add(operation);
        this.trigger();
    }

    /**
     * 触发
     */
    protected synchronized void trigger() {
        if (null == curObservable) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    curObservable = operationList.remove(0);
                    if (null != curObservable) {
                        curObservable.doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                curObservable = null;
                                OperationManager.this.trigger();
                            }
                        }).subscribe();
                    }
                }
            }).start();
        }
    }
}
