

package com.android.tv.settings.bluetooth;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.TwoStatePreference;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPointPreference;
import java.util.List;
import com.android.internal.net.VpnProfile;
import com.android.tv.settings.R;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import android.util.Log;
import android.security.Credentials;
import android.security.KeyStore;
import android.net.IConnectivityManager;
import android.os.ServiceManager;
import android.util.ArraySet;
import java.util.Map;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.tv.settings.R;
import com.android.tv.settings.search.Index;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.tv.settings.data.ConstData;
import com.android.tv.settings.vpn.*;
import android.annotation.UiThread;
import android.annotation.WorkerThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.Credentials;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toolbar;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import static android.app.AppOpsManager.OP_ACTIVATE_VPN;
import android.os.SystemProperties;
import android.support.annotation.Keep;
import android.text.BidiFormatter;
import android.text.TextUtils;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import java.util.WeakHashMap;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;

@Keep
public class BluetoothFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceClickListener, BluetoothCallback{
    private static final String TAG = "BluetoothFragment";
    private static final String KEY_BLUETOOTH_ENABLE = "bluetooth_enable";
    private static final String KEY_BLUETOOTH_RENAME = "bluetooth_rename";
    private static final String KEY_BLUETOOTH_PAIRED = "bluetooth_paried";
    private static final String KEY_BLUETOOTH_AVAILABLE = "bluetooth_avaliable";
    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;;
    private Map<String, Preference> mPreferenceCache;
    private BluetoothDeviceFilter.Filter mFilter;
    private boolean mInitiateDiscoverable;
    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap =
            new WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference>();
    private boolean mAvailableDevicesCategoryIsPresent;
    private boolean mInitialScanStarted;
    private PreferenceGroup mDeviceListGroup;
    private Preference mPreferenceBluetoothRename;
    private SwitchPreference mPreferenceBluetoothEnable;
    private PreferenceCategory mCategoryBluetoothPaired;
    private PreferenceCategory mCategoryBluetoothAvailable;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
            final int state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName(context);
            }

            if (state == BluetoothAdapter.STATE_ON) {
                mInitiateDiscoverable = true;
            }
        }
        private void updateDeviceName(Context context) {
            if (mLocalAdapter.isEnabled() && mPreferenceBluetoothRename != null) {
                final Resources res = context.getResources();
                final Locale locale = res.getConfiguration().getLocales().get(0);
                final BidiFormatter bidiFormatter = BidiFormatter.getInstance(locale);
                mPreferenceBluetoothRename.setSummary(bidiFormatter.unicodeWrap(mLocalAdapter.getName()));
            }
        }
    };

    private final BroadcastReceiver mRenameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast receiver is always running on the UI thread here,
            // so we don't need consider thread synchronization.
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            handleStateChanged(state);
        }
    };
    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
    	IntentFilter bluetoothChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    	IntentFilter bluetoothRenameFilter = new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
    	getActivity().registerReceiver(mReceiver, bluetoothRenameFilter);
    	getActivity().registerReceiver(mRenameReceiver, bluetoothChangeFilter);
        super.onResume();
        if (mLocalManager == null /*|| isUiRestricted()*/) 
        	return;
        mLocalManager.setForegroundActivity(getActivity());
        //mLocalManager.getEventManager().registerCallback(this);
        updateBluetooth();
        mInitiateDiscoverable = true;
    }

    @Override
    public void onPause() {
    	getActivity().unregisterReceiver(mReceiver);
    	getActivity().unregisterReceiver(mRenameReceiver);
    	super.onPause();
    	 if (mLocalManager == null /*|| isUiRestricted()*/) {
             return;
         }
         removeAllDevices();
         mLocalManager.setForegroundActivity(null);
         mLocalManager.getEventManager().unregisterCallback(this);
         mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bluetooth, null);
        mLocalManager = BluetoothUtils.getLocalBtManager(getContext());
        if (mLocalManager == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
            mPreferenceBluetoothEnable.setEnabled(false);
        } else {
            mLocalAdapter = mLocalManager.getBluetoothAdapter();
        }
        mInitiateDiscoverable = true;
        mPreferenceBluetoothEnable = (SwitchPreference)findPreference(KEY_BLUETOOTH_ENABLE);
        mPreferenceBluetoothRename = (Preference) findPreference(KEY_BLUETOOTH_RENAME);
        mCategoryBluetoothAvailable = (PreferenceCategory)findPreference(KEY_BLUETOOTH_AVAILABLE);
        mCategoryBluetoothPaired = (PreferenceCategory)findPreference(KEY_BLUETOOTH_PAIRED);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey() == null) {
            return super.onPreferenceTreeClick(preference);
        }
		if (preference == mPreferenceBluetoothRename) {
			// MetricsLogger.action(getActivity(),
			// MetricsEvent.ACTION_BLUETOOTH_RENAME);
			new BluetoothNameDialogFragment().show(getFragmentManager(),
					"rename device");
			return true;
		}else if(preference == mPreferenceBluetoothEnable){
			refreshSwitchView();
			return true;
		}
        return super.onPreferenceTreeClick(preference);

    }



    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    private void updateBluetooth(){
    	if(mLocalAdapter == null)
    		return;
    	handleStateChanged(mLocalAdapter.getBluetoothState());
    	 if (mLocalAdapter != null) {
             updateContent(mLocalAdapter.getBluetoothState());
         }
    }
    
    private void refreshSwitchView(){
    	boolean isChecked = mPreferenceBluetoothEnable.isChecked();
        if (isChecked && !WirelessUtils.isRadioAllowed(getContext(), Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(getContext(), R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            mPreferenceBluetoothEnable.setChecked(false);
        }

        //MetricsLogger.action(mContext, MetricsEvent.ACTION_BLUETOOTH_TOGGLE, isChecked);
        if (mLocalAdapter != null) {
            boolean status = mLocalAdapter.setBluetoothEnabled(isChecked);
            if (isChecked && !status) {
                mPreferenceBluetoothEnable.setChecked(false);
                mPreferenceBluetoothEnable.setEnabled(true);
               // mSwitchBar.setTextViewLabel(false);
                return;
            }
        }
        mPreferenceBluetoothEnable.setEnabled(false);
    }
    
    private void handleStateChanged(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
            	mPreferenceBluetoothEnable.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
            	mPreferenceBluetoothEnable.setEnabled(true);
            	mPreferenceBluetoothEnable.setChecked(true);
                //updateSearchIndex(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mPreferenceBluetoothEnable.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
            	mPreferenceBluetoothEnable.setEnabled(true);
            	mPreferenceBluetoothEnable.setChecked(false);
                //updateSearchIndex(false);
                break;
            default:
            	mPreferenceBluetoothEnable.setEnabled(true);
            	mPreferenceBluetoothEnable.setChecked(false);
                //updateSearchIndex(false);
        }
    }

    private void updateContent(int bluetoothState) {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        int messageId = 0;

        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                mDevicePreferenceMap.clear();

              /*  if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                    break;
                }*/
             /*   getPreferenceScreen().removeAll();
                getPreferenceScreen().addPreference(mPairedDevicesCategory);
                getPreferenceScreen().addPreference(mAvailableDevicesCategory);
                getPreferenceScreen().addPreference(mMyDevicePreference);*/

                // Paired devices category
                addDeviceCategory(mCategoryBluetoothPaired,
                        R.string.bluetooth_preference_paired_devices,
                        BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
                int numberOfPairedDevices = mCategoryBluetoothPaired.getPreferenceCount();

                if (/*isUiRestricted() ||*/ numberOfPairedDevices <= 0) {
                    if (mCategoryBluetoothPaired != null) {
                        preferenceScreen.removePreference(mCategoryBluetoothPaired);
                    }
                } else {
                    if (preferenceScreen.findPreference(KEY_BLUETOOTH_PAIRED) == null) {
                        preferenceScreen.addPreference(mCategoryBluetoothPaired);
                    }
                }

                addDeviceCategory(mCategoryBluetoothAvailable,
                        R.string.bluetooth_preference_found_devices,
                        BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);

                if (!mInitialScanStarted) {
                    startScanning();
                }

                final Resources res = getResources();
                final Locale locale = res.getConfiguration().getLocales().get(0);
                final BidiFormatter bidiFormatter = BidiFormatter.getInstance(locale);
                mPreferenceBluetoothRename.setSummary(bidiFormatter.unicodeWrap(mLocalAdapter.getName()));

                //getActivity().invalidateOptionsMenu();

                // mLocalAdapter.setScanMode is internally synchronized so it is okay for multiple
                // threads to execute.
                if (mInitiateDiscoverable) {
                    // Make the device visible to other devices.
                    mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mInitiateDiscoverable = false;
                }
                return; // not break

            case BluetoothAdapter.STATE_TURNING_OFF:
                messageId = R.string.bluetooth_turning_off;
                break;

            case BluetoothAdapter.STATE_OFF:
                /*setOffMessage();
                if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                }*/
                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                messageId = R.string.bluetooth_turning_on;
                mInitialScanStarted = false;
                break;
        }

        setDeviceListGroup(preferenceScreen);
        removeAllDevices();
        /*if (messageId != 0) {
            getEmptyTextView().setText(messageId);
        }
        if (!isUiRestricted()) {
            getActivity().invalidateOptionsMenu();
        }*/
    }

    private void addDeviceCategory(PreferenceGroup preferenceGroup, int titleId,
            BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
        cacheRemoveAllPrefs(preferenceGroup);
        preferenceGroup.setTitle(titleId);
        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        preferenceGroup.setEnabled(true);
        removeCachedPrefs(preferenceGroup);
    }

    private void cacheRemoveAllPrefs(PreferenceGroup group) {
        mPreferenceCache = new ArrayMap<String, Preference>();
        final int N = group.getPreferenceCount();
        for (int i = 0; i < N; i++) {
            Preference p = group.getPreference(i);
            if (TextUtils.isEmpty(p.getKey())) {
                continue;
            }
            mPreferenceCache.put(p.getKey(), p);
        }
    }

    private void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }

    private void setFilter(int filterType) {
        mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    private void setDeviceListGroup(PreferenceGroup preferenceGroup) {
        mDeviceListGroup = preferenceGroup;
    }

    protected void removeCachedPrefs(PreferenceGroup group) {
        for (Preference p : mPreferenceCache.values()) {
            group.removePreference(p);
        }
        mPreferenceCache = null;
    }

    void removeAllDevices() {
        mLocalAdapter.stopScanning();
        mDevicePreferenceMap.clear();
        //mDeviceListGroup.removeAll();
    }

    private void startScanning() {
       /* if (isUiRestricted()) {
            return;
        }*/

        if (!mAvailableDevicesCategoryIsPresent) {
            getPreferenceScreen().addPreference(mCategoryBluetoothAvailable);
            mAvailableDevicesCategoryIsPresent = true;
        }

        if (mCategoryBluetoothAvailable != null) {
            setDeviceListGroup(mCategoryBluetoothAvailable);
            removeAllDevices();
        }

        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
        mCategoryBluetoothAvailable.removeAll();
        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }
    
    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        //super.onBluetoothStateChanged(bluetoothState);
    	
        // If BT is turned off/on staying in the same BT Settings screen
        // discoverability to be set again
        if (BluetoothAdapter.STATE_ON == bluetoothState)
            mInitiateDiscoverable = true;
        updateContent(bluetoothState);
    }
    
    @Override
    public void onScanningStateChanged(boolean started) {
        //super.onScanningStateChanged(started);
        // Update options' enabled state
        /*if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }*/
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (mDevicePreferenceMap.get(cachedDevice) != null) {
            return;
        }

        // Prevent updates while the list shows one of the state messages
        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;

        if (mFilter.matches(cachedDevice.getDevice())) {
            //createDevicePreference(cachedDevice);
        }
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = mDevicePreferenceMap.remove(cachedDevice);
        if (preference != null) {
            mDeviceListGroup.removePreference(preference);
        }
    }
    
    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        setDeviceListGroup(getPreferenceScreen());
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState());
    }
    
    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) { }
}

