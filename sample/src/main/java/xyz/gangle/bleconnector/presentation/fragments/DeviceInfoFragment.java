package xyz.gangle.bleconnector.presentation.fragments;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tggg.util.CommonUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import butterknife.BindView;
import xyz.gangle.bleconnector.R;


public class DeviceInfoFragment extends BaseFragment {


    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.tv_manufacturer)
    TextView tvManufacturer;
    @BindView(R.id.tv_model)
    TextView tvModel;
    @BindView(R.id.tv_brand)
    TextView tvBrand;
    @BindView(R.id.tv_product)
    TextView tvProduct;
    @BindView(R.id.tv_ble_support)
    TextView tvBleSupport;
    @BindView(R.id.tv_peripheral_support)
    TextView tvPeripheralSupport;

    @BindView(R.id.tv_resolution)
    TextView tvResolution;
    @BindView(R.id.tv_dimensions_px)
    TextView tvDimensionsPx;
    @BindView(R.id.tv_dimensions_dip)
    TextView tvDimensionsDip;
    @BindView(R.id.tv_screen_size)
    TextView tvScreenSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_info, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.setting_device_info);

        tvDeviceName.setText(getDeviceName());
        tvVersion.setText(Build.VERSION.RELEASE);
        tvManufacturer.setText(Build.MANUFACTURER);
        tvModel.setText(Build.MODEL);
        tvBrand.setText(Build.BRAND);
        tvProduct.setText(Build.PRODUCT);

        tvBleSupport.setText(getBleSupport() ? "YES" : "NO");
        tvPeripheralSupport.setText(getBlePeripheralSupport() ? "YES" : "NO");

        tvResolution.setText(getDpi());
        tvDimensionsPx.setText(getDimensionsPx());
        tvDimensionsDip.setText(getDimensionsDip());
        tvScreenSize.setText(getScreenSize());
    }

    protected boolean getBleSupport() {
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    protected boolean getBlePeripheralSupport() {
        if (getBleSupport()) {
            return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        }
        return false;
    }

    protected String getDimensionsPx() {
        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Method mGetRawH = null, mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        return String.format("%d x %d", width, height);
    }

    protected String getDimensionsDip() {
        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Method mGetRawH = null, mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        return String.format("%d x %d", CommonUtil.px2dip(getActivity(), width), CommonUtil.px2dip(getActivity(), height));
    }

    protected String getScreenSize() {
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "xLarge";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "Large";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "Normal";
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "Small";
            default:
                return "unknow";
        }
    }

    protected String getDpi() {
        int density = getResources().getDisplayMetrics().densityDpi;

        if (density <= DisplayMetrics.DENSITY_LOW)
            return "ldpi";
        else if (density <= DisplayMetrics.DENSITY_MEDIUM)
            return "mdpi";
        else if (density <= DisplayMetrics.DENSITY_HIGH)
            return "hdpi";
        else if (density <= DisplayMetrics.DENSITY_XHIGH)
            return "xhdpi";
        else if (density <= DisplayMetrics.DENSITY_XXHIGH)
            return "xxhdpi";
        else if (density <= DisplayMetrics.DENSITY_XXXHIGH)
            return "xxxhdpi";
        else
            return "unknow";
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}