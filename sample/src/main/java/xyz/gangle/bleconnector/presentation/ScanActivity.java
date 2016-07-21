package xyz.gangle.bleconnector.presentation;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tggg.nble.BluetoothUtil;
import com.tggg.nble.DeviceStateEvent;
import com.tggg.nble.NBle;
import com.tggg.nble.NBleDevice;
import com.tggg.nble.NBleScanner;
import com.tggg.util.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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


@RuntimePermissions
public class ScanActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static int MENU_ITEM_ADD_MAINTAIN = 1;
    private final static int MENU_ITEM_REMOVE_MAINTAIN = 2;
    private final static int MENU_ITEM_CONNECT = 3;
    private final static int MENU_ITEM_DISCONNECT = 4;
    private final static int POST_DELAY_WHAT_MSG = 0x1234;

    @BindView(R.id.listView)
    ListView listView;

    private List<DeviceInfo> devList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
    private NBleScanner scanner;
    private RssiComparator comparator = new RssiComparator();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case POST_DELAY_WHAT_MSG:
                    Collections.sort(devList, comparator);
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    };

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


        listView.setAdapter(new DeviceListAdapter(ScanActivity.this, devList));
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
                DeviceInfo info = devList.get(position);

                if (!NBle.getManager().isMaintain(info.address))
                    menu.add(0, MENU_ITEM_ADD_MAINTAIN, 0, "add to Maintain list");
                else
                    menu.add(0, MENU_ITEM_REMOVE_MAINTAIN, 0, "remove from Maintain list");

                NBleDevice device = NBle.getManager().getDevice(info.address);
                if (device == null) {
                    menu.add(0, MENU_ITEM_CONNECT, 0, "Connect");
                } else if (device.getConnectionState() == BluetoothProfile.STATE_DISCONNECTED) {
                    menu.add(0, MENU_ITEM_CONNECT, 0, "Connect");
                } else if (device.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
                    menu.add(0, MENU_ITEM_DISCONNECT, 0, "Disconnect");
                }

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                DeviceActivity.start(MultiDevActivity.this, devList.get(position).address, devList.get(position).name);
            }
        });

        EventBus.getDefault().register(this);


        scanner = new NBle.ScannerBuilder(this).setScanNames(new String[]{"iHere", "Zus", "Aio"}).build();

        addMaintainDevsInfo();
    }

    @Subscribe
    public void onEventMainThread(DeviceStateEvent event) {
        if (event instanceof DeviceStateEvent) {
            Timber.d("get a event, type:%d", event.type);
            DeviceInfo deviceInfo = getDeviceInfo(event.address);
            if (deviceInfo != null) {
                if (event.type == DeviceStateEvent.CONNECTED) {
                    deviceInfo.status = DeviceInfo.CONNECTED;
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (event.type == DeviceStateEvent.DISCONNECTED) {
                    deviceInfo.status = DeviceInfo.DISCONNECTED;
                    deviceInfo.rssi = null;
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (event.type == DeviceStateEvent.CONNECTING) {
                    deviceInfo.status = DeviceInfo.CONNECTING;
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (event.type == DeviceStateEvent.CLOSE) {
                    deviceInfo.status = DeviceInfo.CLOSE;
                    deviceInfo.rssi = null;
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (event.type == DeviceStateEvent.RSSI) {
                    int rssi = CommonUtil.byte2int(event.value);
                    deviceInfo.rssi = rssi;
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (event.type == DeviceStateEvent.NOTIFY) {
                    int position = getPositionByAddress(event.address);
                    if (position >= 0) {
                        View v = getViewByPosition(position, listView);
                        TextView name = (TextView) v.findViewById(R.id.textViewName);
                    }
                }
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
        if (BluetoothUtil.isAdapterEnable(this)) {
            scanner.start(scanListener);
        } else {
            Toast.makeText(this, "Please enable bluetooth adapter!", Toast.LENGTH_SHORT).show();
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("s")
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
            Timber.i("rssi:%d, device name:%s, device addr:%s", rssi, name, address);
            DeviceInfo info = getDeviceInfo(address);
            if (info == null) {
                final DeviceInfo newDevice = new DeviceInfo(address, name, rssi, DeviceInfo.DISCONNECTED);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDeviceInfo(newDevice);
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                });

            } else {
                info.rssi = rssi;
                info.name = name;
                info.status = DeviceInfo.DISCONNECTED;
                delayUpdateListView();
            }

        }

    };

    public void delayUpdateListView() {
        if (!handler.hasMessages(POST_DELAY_WHAT_MSG)) {
            handler.sendEmptyMessageDelayed(POST_DELAY_WHAT_MSG, 500);
        }
    }


    protected int getPositionByAddress(String address) {
        for (int i = 0; i < devList.size(); i++) {
            if (devList.get(i).address.equals(address))
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();  //获得AdapterContextMenuInfo,以此来获得选择的listview项目
        int position = menuInfo.position;
        String address = devList.get(position).address;
        String name = devList.get(position).name;
        NBleDevice device = NBle.getManager().getDevice(devList.get(position).address);
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });

        return super.onContextItemSelected(item);
    }


    protected DeviceInfo getDeviceInfo(String address) {
        for (DeviceInfo info : devList) {
            if (info.address.equals(address)) {
                return info;
            }
        }
        return null;
    }

    protected boolean addDeviceInfo(DeviceInfo info) {
        if (getDeviceInfo(info.address) == null) {
            devList.add(info);
            return true;
        }
        return false;
    }

    protected void addMaintainDevsInfo() {
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

}
