package xyz.gangle.bleconnector.presentation.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.SortItemInfo;
import xyz.gangle.bleconnector.events.SortChangeEvent;
import xyz.gangle.bleconnector.preference.SharedPrefManager;
import xyz.gangle.bleconnector.presentation.adapters.SortRecyclerViewAdapter;
import xyz.gangle.bleconnector.presentation.customviews.DividerItemDecoration;


public class ScanSortFragment extends BaseFragment {

    @BindView(R.id.rv_sort)
    RecyclerView sortRecyclerView;
    SortRecyclerViewAdapter sortAdapter;

    private List<SortItemInfo> sortItemInfoList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_sort, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.setting_sort);

        initSortItems();

        // 初始化recyclerView
        sortAdapter = new SortRecyclerViewAdapter(sortItemInfoList);
        sortRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        sortRecyclerView.setAdapter(sortAdapter);
        sortRecyclerView.addItemDecoration(new SpaceItemDecoration(6));

        //为RecycleView绑定触摸事件
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//拖拽
//                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//侧滑删除
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
                Collections.swap(sortItemInfoList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                sortAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //侧滑事件
                sortItemInfoList.remove(viewHolder.getAdapterPosition());
                sortAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });
        helper.attachToRecyclerView(sortRecyclerView);
    }

    private void initSortItems() {
        sortItemInfoList = SharedPrefManager.getInstance().getSortOrder();
        int[] types = {SortItemInfo.ByName, SortItemInfo.ByMacAddress, SortItemInfo.ByRSSI};

        if (sortItemInfoList == null) {
            sortItemInfoList = Collections.synchronizedList(new ArrayList<SortItemInfo>());
        }

        // 遍历sortItemInfoList，如果缺少sort类型，就补一个。
        for (int type : types) {
            boolean found = false;
            for (SortItemInfo info: sortItemInfoList) {
                if (info.type == type) {
                    found = true;
                }
            }
            if (!found) {
                sortItemInfoList.add(new SortItemInfo(type, false, false));
            }
        }
    }


    /**
     * 保存设置和参数
     */
    protected void storeSortOrder() {
        SharedPrefManager.getInstance().setSortOrder(sortItemInfoList);
        // 序列化
        EventBus.getDefault().post(new SortChangeEvent());
    }

    @Override
    public void onStop() {
        super.onStop();
        storeSortOrder();
    }
}

class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    int mSpace;

    /**
     * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
     * the number of pixels that the item view should be inset by, similar to padding or margin.
     * The default implementation sets the bounds of outRect to 0 and returns.
     * <p>
     * <p>
     * If this ItemDecoration does not affect the positioning of item views, it should set
     * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
     * before returning.
     * <p>
     * <p>
     * If you need to access Adapter for additional data, you can call
     * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
     * View.
     *
     * @param outRect Rect to receive the output.
     * @param view    The child view to decorate
     * @param parent  RecyclerView this ItemDecoration is decorating
     * @param state   The current state of RecyclerView.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mSpace;
        }

    }

    public SpaceItemDecoration(int space) {
        this.mSpace = space;
    }
}