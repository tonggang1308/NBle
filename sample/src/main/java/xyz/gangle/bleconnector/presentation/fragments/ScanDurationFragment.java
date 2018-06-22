package xyz.gangle.bleconnector.presentation.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.ConstData;
import xyz.gangle.bleconnector.events.ScanDurationChangeEvent;
import xyz.gangle.bleconnector.preference.SharedPrefManager;


public class ScanDurationFragment extends BaseFragment {

    @BindView(R.id.tv_period)
    TextView periodTextView;

    @BindView(R.id.tv_continuous)
    View continuousTextView;

    @BindView(R.id.tv_manual)
    View manualTextView;

    @BindView(R.id.rl_manual)
    View manualLayout;

    @BindView(R.id.switch_scan)
    Switch continuousSwitch;

//    @BindView(R.id.rl_number_picker)
//    View numberPickerLayout;

    boolean dirty = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_period, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.setting_scan);

        updateStatus();

        continuousSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    SharedPrefManager.getInstance().setScanMode(isChecked ? ConstData.Scan.MODE_CONTINUOUS : ConstData.Scan.MODE_MANUAL);
                    updateStatus();
                    dirty = true;
                } else {
//                    Toast.makeText(getActivity(), "code checked change", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void updateStatus() {
        int mode = SharedPrefManager.getInstance().getScanMode();
        int period = SharedPrefManager.getInstance().getScanDuration();
        if (mode == ConstData.Scan.MODE_CONTINUOUS) {
            manualLayout.setEnabled(false);
            manualTextView.setEnabled(false);
            continuousTextView.setEnabled(true);
            continuousSwitch.setChecked(true);
            periodTextView.setVisibility(View.INVISIBLE);
        } else {
            manualLayout.setEnabled(true);
            manualTextView.setEnabled(true);
            continuousTextView.setEnabled(false);
            continuousSwitch.setChecked(false);
            periodTextView.setVisibility(View.VISIBLE);
            periodTextView.setText(String.format("%d sec", period));
        }
    }

    @OnClick(R.id.rl_manual)
    protected void onManualClick() {
        int period = SharedPrefManager.getInstance().getScanDuration();
        RelativeLayout linearLayout = new RelativeLayout(getActivity());
        final NumberPicker aNumberPicker = new NumberPicker(getActivity());
        aNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        aNumberPicker.setMaxValue(60);
        aNumberPicker.setMinValue(1);
        aNumberPicker.setValue(period);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker, numPickerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Select the second");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int sec = aNumberPicker.getValue();
                                SharedPrefManager.getInstance().setScanDuration(sec);
                                updateStatus();
                                dirty = true;
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dirty)
            EventBus.getDefault().post(new ScanDurationChangeEvent());
    }
}