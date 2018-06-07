package com.gangle.nble;

import java.util.Collections;
import java.util.List;

/**
 * Created by Gang Tong on 16/7/25.
 */
final class OperationManager {

    private List<Operation> operationList = Collections.synchronizedList(Collections.EMPTY_LIST);

    private OnValidateOperationListener listener;

    private OperationManager() {
    }

    /**
     * 单例
     */
    private static class LazyHolder {
        private static final OperationManager INSTANCE = new OperationManager();
    }

    public static OperationManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(OnValidateOperationListener listener) {
        this.listener = listener;
    }

    /**
     * "添加"操作
     */
    public synchronized void pend(Operation operation) {
        if (operation != null)
            operationList.add(operation);

        triggerNextPendingOperation();
    }

    /**
     * "完成"操作
     */
    public synchronized void done(Operation operation) {
        if (operation != null)
            operationList.remove(operation);

        triggerNextPendingOperation();
    }

    /**
     * 触发
     */
    protected void triggerNextPendingOperation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener != null && operationList.size() > 0) {
                    listener.onNextPendingOperation(operationList.get(0));
                }
            }
        }).start();
    }


    public interface OnValidateOperationListener {
        void onNextPendingOperation(Operation operation);
    }
}
