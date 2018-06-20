package com.gangle.nble;

/**
 * Created by gangtong on 16/7/21.
 */
interface IDeviceConnectExceptionListener {
    void onConnectException(NBleDevice device, int status);
}
