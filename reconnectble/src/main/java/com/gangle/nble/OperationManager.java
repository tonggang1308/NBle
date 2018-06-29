package com.gangle.nble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

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
    protected void trigger() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (OperationManager.this) {
                    if (null == curObservable && operationList.size() > 0) {
                        curObservable = operationList.remove(0);
                        if (null != curObservable) {
                            curObservable.subscribe(new Consumer() {
                                @Override
                                public void accept(Object o) throws Exception {

                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    curObservable = null;
                                    OperationManager.this.trigger();
                                }
                            }, new Action() {
                                @Override
                                public void run() throws Exception {
                                    curObservable = null;
                                    OperationManager.this.trigger();
                                }
                            });
                        }
                    }
                }
            }
        }).start();
    }

}
