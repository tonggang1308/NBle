package xyz.gangle.bleconnector.presentation.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.preference.SharedPrefManager;
import xyz.gangle.bleconnector.presentation.customviews.MacAddressTextWatcher;


public class ScanFilterFragment extends BaseFragment {

    private static final String MAC_ADDRES_RET = "^([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})$";
    private static final int MODE_CONTINUOUS = 0;
    private static final int MODE_MANUAL = 1;

    @BindView(R.id.rl_name)
    View nameLayout;

    @BindView(R.id.rl_mac)
    View macLayout;

    @BindView(R.id.rl_mac_scope)
    View macScopeLayout;

    @BindView(R.id.rl_rssi)
    View rssiLayout;

    @BindView(R.id.checkbox_name)
    CheckBox nameCkb;

    @BindView(R.id.checkbox_mac)
    CheckBox macCkb;

    @BindView(R.id.checkbox_mac_scope)
    CheckBox macScopeCkb;

    @BindView(R.id.checkbox_rssi)
    CheckBox rssiCkb;

    @BindView(R.id.et_name)
    EditText nameEdit;

    @BindView(R.id.et_mac)
    EditText macEdit;

    @BindView(R.id.et_mac_start)
    EditText macStartEdit;

    @BindView(R.id.et_mac_end)
    EditText macEndEdit;

    @BindView(R.id.tv_rssi)
    TextView rssiText;

    @BindView(R.id.seekBar)
    SeekBar rssiSeekbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_filter, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initValue();
        updateStatus();

        macEdit.addTextChangedListener(new MacAddressTextWatcher(macEdit, MAC_ADDRES_RET));
        macEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(17)});

        macStartEdit.addTextChangedListener(new MacAddressTextWatcher(macStartEdit, MAC_ADDRES_RET));
        macStartEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(17)});

        macEndEdit.addTextChangedListener(new MacAddressTextWatcher(macEndEdit, MAC_ADDRES_RET));
        macEndEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(17)});

    }

    protected void setParentLayoutEnableExcludeSelf(View v, boolean enable) {

        if (v.getParent() instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v.getParent();
            for (int i = 0; i < group.getChildCount(); i++) {
                View view = group.getChildAt(i);
                if (view != v)
                    view.setEnabled(enable); // Or whatever you want to do with the view.
            }
        }
    }

    /**
     * 更新状态
     */
    protected void updateStatus() {
        setParentLayoutEnableExcludeSelf(nameCkb, nameCkb.isChecked());

        setParentLayoutEnableExcludeSelf(macCkb, macCkb.isChecked());

        setParentLayoutEnableExcludeSelf(macScopeCkb, macScopeCkb.isChecked());

        setParentLayoutEnableExcludeSelf(rssiCkb, rssiCkb.isChecked());
    }

    /**
     * 初始化控件
     */
    protected void initValue() {
        nameCkb.setChecked(SharedPrefManager.getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_NAME_ENABLE));
        nameEdit.setText(SharedPrefManager.getInstance().getFilterName());

        macCkb.setChecked(SharedPrefManager.getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_MAC_ENABLE));
        macEdit.setText(SharedPrefManager.getInstance().getFilterMac());

        macScopeCkb.setChecked(SharedPrefManager.getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_MAC_SCOPE_ENABLE));
        macStartEdit.setText(SharedPrefManager.getInstance().getFilterMacStart());
        macEndEdit.setText(SharedPrefManager.getInstance().getFilterMacEnd());

        rssiCkb.setChecked(SharedPrefManager.getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_RSSI_ENABLE));
        int rssi = SharedPrefManager.getInstance().getFilterRssi();
        rssiText.setText(rssi + " dBm");
        rssiSeekbar.setProgress(Math.abs(rssi));
    }

    protected void storeValidFilter() {
        SharedPrefManager.getInstance().setFilterEnable(SharedPrefManager.KEY_FILTER_NAME_ENABLE, nameCkb.isChecked());
        SharedPrefManager.getInstance().setFilterName(nameEdit.getText().toString());

        String macAddress = macEdit.getText().toString();
        if (macAddress.matches(MAC_ADDRES_RET)) {
            SharedPrefManager.getInstance().setFilterEnable(SharedPrefManager.KEY_FILTER_MAC_ENABLE, macCkb.isChecked());
            SharedPrefManager.getInstance().setFilterMac(macAddress);
        } else {
            SharedPrefManager.getInstance().setFilterEnable(SharedPrefManager.KEY_FILTER_MAC_ENABLE, false);
            SharedPrefManager.getInstance().setFilterMac("");
        }

        String macAddressStart = macStartEdit.getText().toString();
        String macAddressEnd = macStartEdit.getText().toString();
        if ((macAddressStart.matches(MAC_ADDRES_RET) && macAddressEnd.matches(MAC_ADDRES_RET))) {
            SharedPrefManager.getInstance().setFilterEnable(SharedPrefManager.KEY_FILTER_MAC_SCOPE_ENABLE, macScopeCkb.isChecked());
            SharedPrefManager.getInstance().setFilterMacStart(macAddressStart);
            SharedPrefManager.getInstance().setFilterMacEnd(macAddressEnd);
        } else {
            SharedPrefManager.getInstance().setFilterEnable(SharedPrefManager.KEY_FILTER_MAC_SCOPE_ENABLE, false);
            SharedPrefManager.getInstance().setFilterMacStart("");
            SharedPrefManager.getInstance().setFilterMacEnd("");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        storeValidFilter();
    }

    @OnCheckedChanged({R.id.checkbox_mac, R.id.checkbox_mac_scope, R.id.checkbox_name, R.id.checkbox_rssi})
    protected void onCheckBoxCheckedChanged() {
        updateStatus();
    }
}