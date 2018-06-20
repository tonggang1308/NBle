package xyz.gangle.bleconnector.presentation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.gangle.util.CommonUtil;

import java.util.List;

import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.SortItemInfo;

public class SortRecyclerViewAdapter extends RecyclerView.Adapter<SortRecyclerViewAdapter.ViewHolder> {

    private final List<SortItemInfo> list;

    public SortRecyclerViewAdapter(List<SortItemInfo> items) {
        this.list = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_sort_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SortItemInfo info = list.get(position);
        holder.textViewTile.setText(info.getTitle());
        holder.checkBoxEnable.setChecked(info.isEnable);
        holder.textViewTile.setEnabled(info.isEnable);
//        CommonUtil.setGroupLayoutEnable((ViewGroup) holder.textViewTile.getParent(), info.isEnable);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewTile;
//        public final TextView textViewSummary;
        public final CheckBox checkBoxEnable;

        public ViewHolder(View view) {
            super(view);
            textViewTile = (TextView) view.findViewById(R.id.tv_name);
//            textViewSummary = (TextView) view.findViewById(R.id.tv_summary);
            checkBoxEnable = (CheckBox) view.findViewById(R.id.cb_enable);
            checkBoxEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    SortItemInfo info = list.get(position);
                    textViewTile.setEnabled(isChecked);
                    info.isEnable = isChecked;
                }
            });
        }


    }
}
