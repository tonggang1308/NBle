package xyz.gangle.bleconnector.presentation.listener;

import android.view.ContextMenu;
import android.view.View;

import com.gangle.nble.NBleDevice;

/**
 * Created by Tong Gang on 7/23/16.
 */

public interface OnListInteractionListener {
    void onItemClick(NBleDevice item);

    void onCreateContextMenu(ContextMenu menu, View v, NBleDevice item);
}
