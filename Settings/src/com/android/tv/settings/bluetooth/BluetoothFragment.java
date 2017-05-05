

package com.android.tv.settings.bluetooth;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
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
import java.util.Set;
import android.util.Log;
import android.security.Credentials;
import android.security.KeyStore;
import android.net.IConnectivityManager;
import android.os.ServiceManager;
import android.util.ArraySet;
import java.util.Map;

import com.android.tv.settings.data.ConstData;
import com.android.tv.settings.vpn.*;
import android.annotation.UiThread;
import android.annotation.WorkerThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

@Keep
public class BluetoothFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceClickListener{
    private static final String TAG = "BluetoothFragment";
    private static final String KEY_WIFI_ENABLE = "wifi_enable";
    private static final String KEY_WIFI_LIST = "wifi_list";
    private static final String KEY_WIFI_COLLAPSE = "wifi_collapse";
    private static final String KEY_WIFI_OTHER = "wifi_other";
    private static final String KEY_WIFI_WPS = "wifi_wps";
    private static final String KEY_WIFI_ADD = "wifi_add";
    private static final String KEY_WIFI_ALWAYS_SCAN = "wifi_always_scan";
    private static final String KEY_ETHERNET = "ethernet";
    private static final String KEY_ETHERNET_STATUS = "ethernet_status";
    private static final String KEY_ETHERNET_PROXY = "ethernet_proxy";
    private static final String KEY_ETHERNET_DHCP = "ethernet_dhcp";
    private static final String KEY_ETHERNET_PPPOE = "ethernet_pppoe";
    private static final String KEY_VPN = "avaliable_vpns";
    private static final String KEY_EDIT_VPN = "edit_vpn";
    private static final int INITIAL_UPDATE_DELAY = 500;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private AccessPointPreference.UserBadgeCache mUserBadgeCache;
    private TwoStatePreference mEnableWifiPref;
    private Preference mCollapsePref;
    private Preference mWpsPref;
    private Preference mAddPref;
    private TwoStatePreference mAlwaysScan;
    private PreferenceCategory mEthernetCategory;
    private PreferenceCategory mVpnCategory;
    private Preference mEthernetStatusPref;
    private Preference mEthernetProxyPref;
    private Preference mEthernetDhcpPref;
    private Preference mEthernetPppoePref;
    private Preference mVpnCreatePref;
    private final Handler mHandler = new Handler();
    private long mNoWifiUpdateBeforeMillis;
    private LegacyVpnInfo mConnectedLegacyVpn;
    private Map<String, LegacyVpnPreference> mLegacyVpnPreferences = new ArrayMap<>();
    
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
        super.onResume();
        // There doesn't seem to be an API to listen to everything this could cover, so
        // tickle it here and hope for the best.
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bluetooth, null);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }



    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
}

