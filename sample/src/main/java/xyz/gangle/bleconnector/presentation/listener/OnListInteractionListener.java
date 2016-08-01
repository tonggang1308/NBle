package xyz.gangle.bleconnector.presentation.listener;

import android.view.ContextMenu;
import android.view.View;

import xyz.gangle.bleconnector.data.DeviceInfo;

/**
 * Created by Tong Gang on 7/23/16.
 */

public interface OnListInteractionListener {
    void onItemClick(DeviceInfo item);

    void onCreateContextMenu(ContextMenu menu, View v, DeviceInfo item);
}
