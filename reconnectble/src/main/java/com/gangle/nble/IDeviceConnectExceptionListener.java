package com.gangle.nble;

/**
 * Created by gangtong on 16/7/21.
 */
interface IDeviceConnectExceptionListener {
    /**
     * device 断开异常的监听，用来做后续的处理。
     *
     * @param device
     * @param e
     */
    void onDisconnected(NBleDevice device, Exception e);

}
