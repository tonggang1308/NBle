package xyz.gangle.bleconnector.presentation.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gangle.nble.NBle;
import com.gangle.nble.NBleUtil;

import java.util.List;

import timber.log.Timber;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.DeviceInfo;
import xyz.gangle.bleconnector.presentation.listener.OnListInteractionListener;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {

    private final List<DeviceInfo> mValues;
    private final OnListInteractionListener mListener;

    public DeviceRecyclerViewAdapter(List<DeviceInfo> items, OnListInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_device_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.textViewName.setText((holder.mItem.getName() == null || holder.mItem.getName().isEmpty()) ? "N/A" : holder.mItem.getName());
        holder.textViewAddress.setText(holder.mItem.getAddress());
        holder.textViewRssi.setText(NBleUtil.rssiToString(holder.mItem.getRssi()));
        if (holder.mItem.getStatus() == DeviceInfo.DISCONNECTED) {
            holder.textViewState.setTextColor(Color.parseColor("#999999"));
        } else if (holder.mItem.getStatus() == DeviceInfo.CONNECTING) {
            holder.textViewState.setTextColor(Color.GRAY);
        } else if (holder.mItem.getStatus() == DeviceInfo.CONNECTED) {
            holder.textViewState.setTextColor(Color.parseColor("#99cc33"));
        } else if (holder.mItem.getStatus() == DeviceInfo.DISCONNECTED) {
            holder.textViewState.setTextColor(Color.parseColor("#999999"));
        }
        holder.textViewState.setText(NBleUtil.connectionStateToString(holder.mItem.getStatus()));

        boolean isMaintain = NBle.manager().isMaintain(holder.mItem.getAddress());
        holder.viewMaintain.setVisibility(isMaintain ? View.VISIBLE : View.GONE);

        holder.mView.setTag(holder.mItem);


//        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (null != mListener) {
//                    mListener.onItemLongClick(holder.mItem);
//                }
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewAddress;
        public final TextView textViewName;
        public final TextView textViewRssi;
        public final TextView textViewState;
        public final View viewMaintain;
        public final View mView;
        public DeviceInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
            textViewName = (TextView) view.findViewById(R.id.textViewName);
            textViewRssi = (TextView) view.findViewById(R.id.textViewRssi);
            textViewState = (TextView) view.findViewById(R.id.textViewState);
            viewMaintain = view.findViewById(R.id.layoutMaintain);


            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onItemClick((DeviceInfo) v.getTag());
                    }
                }
            });

            mView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    Timber.e("getAdapterPosition:" + ViewHolder.this.getAdapterPosition());
                    if (null != mListener) {
                        mListener.onCreateContextMenu(menu, v, (DeviceInfo) v.getTag());
                    }
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textViewAddress.getText() + "'";
        }
    }
}
