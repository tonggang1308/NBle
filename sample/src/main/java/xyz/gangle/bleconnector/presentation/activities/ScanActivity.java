package xyz.gangle.bleconnector.presentation.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.tggg.nble.NBleUtil;
import com.tggg.nble.DeviceStateEvent;
import com.tggg.nble.NBle;
import com.tggg.nble.NBleDevice;
import com.tggg.nble.NBleScanner;
import com.tggg.util.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;
import xyz.gangle.bleconnector.R;
import xyz.gangle.bleconnector.data.DeviceInfo;
import xyz.gangle.bleconnector.presentation.adapters.DeviceRecyclerViewAdapter;
import xyz.gangle.bleconnector.presentation.customviews.DividerItemDecoration;
import xyz.gangle.bleconnector.presentation.fragments.ScanFilterFragment;
import xyz.gangle.bleconnector.presentation.fragments.ScanPeriodFragment;
import xyz.gangle.bleconnector.presentation.listener.OnListInteractionListener;
import xyz.gangle.bleconnector.presentation.comparators.RssiComparator;


@RuntimePermissions
public class ScanActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListInteractionListener {

    private final static int MENU_ITEM_ADD_MAINTAIN = 1;
    private final static int MENU_ITEM_REMOVE_MAINTAIN = 2;
    private final static int MENU_ITEM_CONNECT = 3;
    private final static int MENU_ITEM_DISCONNECT = 4;

    @BindView(R.id.listView)
    RecyclerView recyclerView;

    private DeviceInfo selectedDeviceInfo;

    private List<DeviceInfo> devList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
    private NBleScanner scanner;
    private RssiComparator comparator = new RssiComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(new DeviceRecyclerViewAdapter(devList, this));

        // 创建scanner
        scanner = new NBle.ScannerBuilder(this).setScanNames(new String[]{"iHere", "Zus", "Aio"}).build();

        // 添加已维护的设备列表
        addMaintainDevicesInfo();

        recyclerView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            }
        });
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


    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void onScanStart() {
        if (NBleUtil.isAdapterEnable(this)) {
            scanner.start(scanListener);
        } else {
            Toast.makeText(this, "Please enable bluetooth adapter!", Toast.LENGTH_SHORT).show();
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
        }

        @Override
        public void onScanStopped() {
            Timber.v("ble scan stopped");
        }

        @Override
        public void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord) {
            Timber.v("rssi:%d, device name:%s, device addr:%s", rssi, name, address);
            DeviceInfo info = getDeviceInfo(address);
            if (info == null) {
                final DeviceInfo newDevice = new DeviceInfo(address, name, rssi, DeviceInfo.DISCONNECTED);
                addDeviceInfo(newDevice);
            } else {

                info.setRssi(rssi);
                info.setName(name);
                info.setStatus(DeviceInfo.DISCONNECTED);
                ;
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
        menu.clear();
        if (scanner.isScanning())
            menu.add(0, R.id.scan_stop, 1, "Scan stop");
        else
            menu.add(0, R.id.scan_start, 1, "Scan start");
        return super.onPrepareOptionsMenu(menu);

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
            ScanActivityPermissionsDispatcher.onScanStartWithCheck(this);
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
            ScanSettingActivity.fragment = new ScanPeriodFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_filter) {
            ScanSettingActivity.fragment = new ScanFilterFragment();
            startActivity(new Intent(this, ScanSettingActivity.class));
        } else if (id == R.id.nav_sort) {

        } else if (id == R.id.nav_device_info) {

        } else if (id == R.id.nav_feedback) {

        } else if (id == R.id.nav_faq) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
                    if (device != null) {
                        device.setMaintain(true);
                    } else {
                        device = new NBle.DeviceBuilder(address, name).setMaintain(true).build();
                    }
                    break;
                case MENU_ITEM_REMOVE_MAINTAIN:
                    if (device != null) {
                        device.setMaintain(false);
                    }
                    break;
                case MENU_ITEM_CONNECT:
                    if (device == null) {
                        device = new NBle.DeviceBuilder(address, name).build();
                    }

                    if (device != null)
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
            recyclerView.getAdapter().notifyItemInserted(devList.size() - 1);
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
