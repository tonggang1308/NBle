package xyz.gangle.bleconnector.presentation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import xyz.gangle.bleconnector.R;
import com.tggg.nble.NBleDeviceManager;

/**
 * Created by yiyidu on 5/29/16.
 */

public class DeviceListAdapter extends BaseAdapter {
    private List<DeviceInfo> devList = null;
    private Context context;

    public final class ViewHolder {
        public TextView textviewAddress;
        public TextView textviewName;
        public TextView textviewRssi;
        public TextView textviewState;
        public View viewMaintain;
    }

    public DeviceListAdapter(Context context, List<DeviceInfo> list) {
        this.devList = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return devList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_device_info, null);

            holder.textviewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
            holder.textviewName = (TextView) convertView.findViewById(R.id.textViewName);
            holder.textviewRssi = (TextView) convertView.findViewById(R.id.textViewRssi);
            holder.textviewState = (TextView) convertView.findViewById(R.id.textViewState);
            holder.viewMaintain = convertView.findViewById(R.id.layoutMaintain);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DeviceInfo info = devList.get(position);
        holder.textviewName.setText((info.name == null || info.name.isEmpty()) ? "Unknow Device" : info.name);
        holder.textviewAddress.setText(info.address);
        holder.textviewRssi.setText(info.rssi == null ? "---" : String.format("%ddb", info.rssi));
        if (info.status == DeviceInfo.DISCONNECTED) {
            holder.textviewState.setText("Disconnected");
            holder.textviewState.setTextColor(Color.parseColor("#999999"));
        } else if (info.status == DeviceInfo.CONNECTING) {
            holder.textviewState.setText("Connecting");
            holder.textviewState.setTextColor(Color.GRAY);
        } else if (info.status == DeviceInfo.CONNECTED) {
            holder.textviewState.setText("Connected");
            holder.textviewState.setTextColor(Color.parseColor("#99cc33"));
        } else if (info.status == DeviceInfo.CLOSE) {
            holder.textviewState.setText("Close");
            holder.textviewState.setTextColor(Color.parseColor("#999999"));
        }


        boolean isMaintain = NBleDeviceManager.getInstance().isMaintain(info.address);
        holder.viewMaintain.setVisibility(isMaintain ? View.VISIBLE : View.GONE);

        return convertView;
    }

}