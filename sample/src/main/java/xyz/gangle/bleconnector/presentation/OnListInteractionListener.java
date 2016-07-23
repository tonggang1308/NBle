package xyz.gangle.bleconnector.presentation;

import android.view.ContextMenu;
import android.view.View;

/**
 * Created by Tong Gang on 7/23/16.
 */

public interface OnListInteractionListener {
    void onItemClick(DeviceInfo item);

    void onCreateContextMenu(ContextMenu menu, View v, DeviceInfo item);
}
