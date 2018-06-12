package xyz.gangle.bleconnector.presentation.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.gangle.nble.DeviceStateEvent;
import com.gangle.nble.NBle;
import com.gangle.nble.NBleDevice;
import com.gangle.nble.NBleDeviceManager;
import com.gangle.nble.NBleScanner;
import com.gangle.nble.NBleUtil;
import com.gangle.nble.ScanFilter.AddressScanFilter;
import com.gangle.nble.ScanFilter.IScanFilter;
import com.gangle.nble.ScanFilter.NameScanFilter;
import com.gangle.nble.ScanFilter.RssiScanFilter;
import com.gangle.util.CommonUtil;
import com.gangle.util.DeviceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.ConstData;
import xyz.gangle.bleconnector.data.DeviceInfo;
import xyz.gangle.bleconnector.data.SortItemInfo;
import xyz.gangle.bleconnector.events.FilterChangeEvent;
import xyz.gangle.bleconnector.events.ScanDurationChangeEvent;
import xyz.gangle.bleconnector.events.SortChangeEvent;
import xyz.gangle.bleconnector.preference.SharedPrefManager;
import xyz.gangle.bleconnector.presentation.adapters.DeviceRecyclerViewAdapter;
import xyz.gangle.bleconnector.presentation.comparators.RssiComparator;
import xyz.gangle.bleconnector.presentation.customviews.DividerItemDecoration;
import xyz.gangle.bleconnector.presentation.fragments.DeviceInfoFragment;
import xyz.gangle.bleconnector.presentation.fragments.ScanDurationFragment;
import xyz.gangle.bleconnector.presentation.fragments.ScanFilterFragment;
import xyz.gangle.bleconnector.presentation.fragments.ScanSortFragment;
import xyz.gangle.bleconnector.presentation.listener.OnListInteractionListener;

import static xyz.gangle.bleconnector.preference.SharedPrefManager.getInstance;


@RuntimePermissions
public class ScanActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListInteractionListener {

    private final static int MENU_ITEM_ADD_MAINTAIN = 1;
    private final static int MENU_ITEM_REMOVE_MAINTAIN = 2;
    private final static int MENU_ITEM_CONNECT = 3;
    private final static int MENU_ITEM_DISCONNECT = 4;

    @BindView(R.id.content_scan)
    ConstraintLayout constraintLayout;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.listView)
    RecyclerView recyclerView;

    private DeviceInfo selectedDeviceInfo;

    private List<DeviceInfo> devList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
    private NBleScanner scanner;
    private int scanDuration = NBleScanner.INDEFINITE;
    private Snackbar snackbar;
    private Timer countDownTimer;
    private long startScanTime;
//    FeedbackAgent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // 初始化recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(new DeviceRecyclerViewAdapter(devList, this));

        // 创建scanner
        scanner = new NBle.ScannerBuilder(this).build();

        // 设置 scan filter
        updateFilter();

        // 设置 scan duration
        updateDuration();

        // 添加已维护的设备列表
        addMaintainDevicesInfo();

        recyclerView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            }
        });

        // 下拉效果
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null != scanner && scanner.isScanning()) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                } else {
                    recyclerView.removeAllViews();
                    devList.clear();
                    ScanActivityPermissionsDispatcher.onScanStartWithPermissionCheck(ScanActivity.this);
                }
            }
        });

        navigationView.getMenu().findItem(R.id.nav_about).setTitle(DeviceUtil.getAppVersion(ScanActivity.this));

//        // LeanCloud 反馈初始化
//        agent = new FeedbackAgent(this);
//        agent.sync();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStateEvent(DeviceStateEvent event) {
        if (event instanceof DeviceStateEvent) {
            Timber.d("get a event, %s", event.toString());
            DeviceInfo deviceInfo = getDeviceInfo(event.address);
            if (deviceInfo != null) {
                if (event.type == DeviceStateEvent.CONNECTED) {
                    deviceInfo.setStatus(DeviceInfo.CONNECTED);
                } else if (event.type == DeviceStateEvent.DISCONNECTED) {
                    deviceInfo.setStatus(DeviceInfo.DISCONNECTED);
                    deviceInfo.setRssi(null);
                } else if (event.type == DeviceStateEvent.CONNECTING) {
                    deviceInfo.setStatus(DeviceInfo.CONNECTING);
                } else if (event.type == DeviceStateEvent.CONNECT_FINISH) {
                    deviceInfo.setStatus(DeviceInfo.CLOSE);
                    deviceInfo.setRssi(null);
                } else if (event.type == DeviceStateEvent.RSSI) {
                    int rssi = CommonUtil.byte2int(event.value);
                    deviceInfo.setRssi(rssi);
                } else if (event.type == DeviceStateEvent.NOTIFY) {
                } else {
                    return;
                }

                int index = devList.indexOf(deviceInfo);
                recyclerView.getAdapter().notifyItemChanged(index);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        onScanStart();
    }

    /**
     * 判断开启蓝牙扫描的前提条件是否满足
     * 1，开启GPS定位，2，开启蓝牙功能
     */
    protected boolean checkConditions() {
        if (!CommonUtil.isGPSEnable(this)) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.hint_turn_on_gps)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
            return false;
        } else if (!NBleUtil.isAdapterEnable(this)) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.hint_turn_on_bluetooth)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, 1);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();

            return false;
        }
        return true;
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void onScanStart() {
        if (checkConditions()) {
            if (!scanner.isScanning())
                scanner.start(scanListener, scanDuration);
            else {
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showRationaleForBluetooth(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("Could you allow GPS?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();
            }
        }).show();

    }


    private NBleScanner.BleScanListener scanListener = new NBleScanner.BleScanListener() {
        @Override
        public void onScanStarted() {
            Timber.v("ble scan start!");
            // 关闭下拉
            swipeRefreshLayout.setRefreshing(false);

            // 弹出提示栏
            if (snackbar == null) {
                snackbar = Snackbar.make(constraintLayout, "Scanning", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Stop", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        scanner.stop();
                    }
                });
            }
            snackbar.show();

            // 在弹出栏上显示倒计时
            countDownTimer = new Timer();
            startScanTime = System.currentTimeMillis();
            countDownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (snackbar.isShown()) {
                        if (scanDuration != NBleScanner.INDEFINITE) {
                            long diff = System.currentTimeMillis() - startScanTime;
                            final long left = scanDuration / 1000 - ((diff + 100) / 1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    snackbar.setText(String.format("SCANNING...      %s SEC", left));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    snackbar.setText(String.format("SCANNING..."));
                                }
                            });
                        }
                    }
                }
            }, 0, 1000);
        }

        @Override
        public void onScanStopped() {
            Timber.v("ble scan stopped");
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
            countDownTimer.cancel();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord) {
            Timber.v("rssi:%d, device name:%s, device addr:%s", rssi, name, address);
            DeviceInfo info = getDeviceInfo(address);
            if (info == null) {
                final DeviceInfo newDevice = new DeviceInfo(address, name, rssi, DeviceInfo.DISCONNECTED);
                addDeviceInfo(newDevice);
            } else {
                // Found it, update info!
                info.setRssi(rssi);
                info.setName(name);
                info.setStatus(DeviceInfo.DISCONNECTED);

                recyclerView.getAdapter().notifyItemChanged(devList.indexOf(info));
            }
        }

    };

    protected int getPositionByAddress(String address) {
        for (int i = 0; i < devList.size(); i++) {
            if (devList.get(i).getAddress().equals(address))
                return i;
        }
        return -1;
    }

    protected View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
//        menu.clear();
//        if (scanner.isScanning())
//            menu.add(0, R.id.scan_stop, 1, "Scan stop");
//        else
//            menu.add(0, R.id.scan_start, 1, "Scan start");
//        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.scan_start) {
            ScanActivityPermissionsDispatcher.onScanStartWithPermissionCheck(this);
        } else if (id == R.id.scan_stop) {
            scanner.stop();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            // Handle the camera action
            ScanSettingActivity.fragment = new ScanDurationFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_filter) {
            ScanSettingActivity.fragment = new ScanFilterFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_sort) {
            ScanSettingActivity.fragment = new ScanSortFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_device_info) {
            ScanSettingActivity.fragment = new DeviceInfoFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_feedback) {
//            Intent intent = new Intent(this, FeedbackThreadActivity.class);
//            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//            startActivity(intent);
        } else if (id == R.id.nav_faq) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
//    void showRationaleForFeedback(final PermissionRequest request) {
//        new AlertDialog.Builder(this)
//                .setMessage("Could you allow External storage access?")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        request.proceed();
//                    }
//                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                request.cancel();
//            }
//        }).show();
//
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //获得AdapterContextMenuInfo,以此来获得选择的listview项目
//        int position = menuInfo.position;
        if (selectedDeviceInfo != null) {
            String address = selectedDeviceInfo.getAddress();
            String name = selectedDeviceInfo.getName();
            NBleDevice device = NBle.getManager().getDevice(address);
            switch (item.getItemId()) {
                case MENU_ITEM_ADD_MAINTAIN:
                    if (device == null) {
                        device = NBle.getManager().createDevice(address, name);
                    }
                    NBle.getManager().setMaintain(device, true);
                    break;
                case MENU_ITEM_REMOVE_MAINTAIN:
                    if (device != null) {
                        NBle.getManager().setMaintain(device, false);
                    }
                    break;
                case MENU_ITEM_CONNECT:
                    if (device == null) {
                        device = NBle.getManager().createDevice(address, name);
                    }
                    device.connect();
                    break;
                case MENU_ITEM_DISCONNECT:
                    if (device != null) {
                        device.disconnect();
                    }
                    break;
            }

            recyclerView.getAdapter().notifyItemChanged(devList.indexOf(selectedDeviceInfo));

            selectedDeviceInfo = null;
        }
        return super.onContextItemSelected(item);
    }


    protected DeviceInfo getDeviceInfo(String address) {
        for (DeviceInfo info : devList) {
            if (info.getAddress().equals(address)) {
                return info;
            }
        }
        return null;
    }

    protected boolean addDeviceInfo(DeviceInfo info) {
        if (getDeviceInfo(info.getAddress()) == null) {
            devList.add(info);
            updateSort();
//            recyclerView.getAdapter().notifyItemInserted(devList.size() - 1);
            return true;
        }
        return false;
    }

    protected void addMaintainDevicesInfo() {
        for (NBleDevice device : NBle.getManager().getAllDevices()) {
            DeviceInfo info = getDeviceInfo(device.getAddress());
            if (info == null) {
                int state = device.getConnectionState();
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    state = DeviceInfo.CONNECTED;
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    state = DeviceInfo.DISCONNECTED;
                } else if (state == BluetoothProfile.STATE_CONNECTING) {
                    state = DeviceInfo.CONNECTING;
                } else {
                    state = DeviceInfo.DISCONNECTED;
                }

                info = new DeviceInfo(device.getAddress(), device.getName(), null, state);

                addDeviceInfo(info);
            }
        }
    }

    @Override
    public void onItemClick(DeviceInfo item) {
//                DeviceActivity.start(MultiDevActivity.this, devList.get(position).address, devList.get(position).name);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, DeviceInfo item) {
//        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        DeviceInfo info = item;
        selectedDeviceInfo = item;

        Toast.makeText(ScanActivity.this, info.getStatusString(), Toast.LENGTH_SHORT).show();


        if (!NBle.getManager().isMaintain(info.getAddress()))
            menu.add(0, MENU_ITEM_ADD_MAINTAIN, 0, "add to Maintain list");
        else
            menu.add(0, MENU_ITEM_REMOVE_MAINTAIN, 0, "remove from Maintain list");

        NBleDevice device = NBle.getManager().getDevice(info.getAddress());
        if (device == null) {
            menu.add(0, MENU_ITEM_CONNECT, 0, "Connect");
        } else if (device.getConnectionState() == BluetoothProfile.STATE_DISCONNECTED) {
            menu.add(0, MENU_ITEM_CONNECT, 0, "Connect");
        } else if (device.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            menu.add(0, MENU_ITEM_DISCONNECT, 0, "Disconnect");
        }

    }

    @Subscribe
    public void onScanDurationChange(ScanDurationChangeEvent event) {
        updateDuration();
    }

    @Subscribe
    public void onSortChangeChange(SortChangeEvent event) {
        updateSort();
    }

    @Subscribe
    public void onFilterChange(FilterChangeEvent event) {
        updateFilter();
    }

    protected void updateDuration() {
        int mode = getInstance().getScanMode();
        if (mode == ConstData.Scan.MODE_CONTINUOUS) {
            scanDuration = NBleScanner.INDEFINITE;
        } else if (mode == ConstData.Scan.MODE_MANUAL) {
            scanDuration = 1000 * getInstance().getScanDuration();
        }
    }

    protected void updateSort() {
        final List<SortItemInfo> sortInfoList = getInstance().getSortOrder();

        if (sortInfoList != null && devList != null) {
            for (int i = sortInfoList.size() - 1; i >= 0; i--) {
                final SortItemInfo sortInfo = sortInfoList.get(i);
                if (sortInfo.isEnable) {
                    Collections.sort(devList, new Comparator<DeviceInfo>() {
                        @Override
                        public int compare(DeviceInfo o1, DeviceInfo o2) {
                            if (sortInfo.type == SortItemInfo.ByName) {
                                String n1 = (o1.getName() == null) ? "N/A" : o1.getName();
                                String n2 = (o2.getName() == null) ? "N/A" : o2.getName();
                                return n1.compareTo(n2);
                            } else if (sortInfo.type == SortItemInfo.ByRSSI) {
                                return o1.getRssi().compareTo(o2.getRssi());
                            } else if (sortInfo.type == SortItemInfo.ByMacAddress) {
                                return o1.getAddress().compareTo(o2.getAddress());
                            }
                            return 0;
                        }
                    });
                }
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 更新filter
     */
    protected void updateFilter() {
        if (scanner != null) {
            List<IScanFilter> scanFilterList = Collections.synchronizedList(new ArrayList<IScanFilter>());

            // name filter
            boolean ignoreUnknown = getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_UNKNOWN_DEVICE_ENABLE);
            if (getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_NAME_ENABLE)) {
                String name = getInstance().getFilterName();
                scanFilterList.add(new NameScanFilter(new String[]{name}, ignoreUnknown, true));
            } else {
                scanFilterList.add(new NameScanFilter(null, ignoreUnknown, true));
            }

            // mac filter
            if (getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_MAC_ENABLE)) {
                String mac = getInstance().getFilterMac();
                scanFilterList.add(new AddressScanFilter(mac));
            }

            // mac range filter
            if (getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_MAC_SCOPE_ENABLE)) {
                String start = getInstance().getFilterMacStart();
                String end = getInstance().getFilterMacEnd();
                scanFilterList.add(new AddressScanFilter(start, end));
            }

            // rssi filter
            if (getInstance().isFilterEnable(SharedPrefManager.KEY_FILTER_RSSI_ENABLE)) {
                int rssi = -getInstance().getFilterRssi();
                scanFilterList.add(new RssiScanFilter(rssi));
            }

            scanner.setFilters(scanFilterList.toArray(new IScanFilter[scanFilterList.size()]));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
