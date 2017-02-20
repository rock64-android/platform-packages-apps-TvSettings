/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.settings.connectivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.wifi.WifiConfiguration;
import android.net.EthernetManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;

import com.android.tv.settings.form.FormPage;
import com.android.tv.settings.form.FormPageResultListener;

/**
 * Allows the modification of advanced Wi-Fi settings
 */
public class EditPppoeSettingsActivity extends WifiMultiPagedFormActivity
        implements SaveWifiConfigurationFragment.Listener, TimedMessageWizardFragment.Listener{

    private static final String TAG = "EditPppoeSettingsActivity";

    public static final int NETWORK_ID_ETHERNET = WifiConfiguration.INVALID_NETWORK_ID;
    private static final String EXTRA_NETWORK_ID = "network_id";
    private static EthernetManager mEthernetManager;
    private static Context mContext;
    
    public static Intent createIntent(Context context, int networkId) {
        mContext = context;
        mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
        return new Intent(context, EditPppoeSettingsActivity.class)
                .putExtra(EXTRA_NETWORK_ID, networkId);
    }
    
    private NetworkConfiguration mConfiguration;
    private AdvancedWifiOptionsFlow mAdvancedWifiOptionsFlow;
    private FormPage mSavePage;
    private FormPage mSuccessPage;
    private IntentFilter intentFilter;
    private int mNetworkStatus = mEthernetManager.ETHER_STATE_CONNECTING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        int networkId = getIntent().getIntExtra(EXTRA_NETWORK_ID, NETWORK_ID_ETHERNET);
        if (networkId == NETWORK_ID_ETHERNET) {
            mConfiguration = NetworkConfigurationFactory.createNetworkConfiguration(this,
                    NetworkConfigurationFactory.TYPE_ETHERNET);
            ((EthernetConfig) mConfiguration).load();
        } else {
            mConfiguration = NetworkConfigurationFactory.createNetworkConfiguration(this,
                    NetworkConfigurationFactory.TYPE_WIFI);
            ((WifiConfig) mConfiguration).load(networkId);
        }
        if (mConfiguration != null) {
            mAdvancedWifiOptionsFlow = new AdvancedWifiOptionsFlow(this, this, mConfiguration);
            addPage(mAdvancedWifiOptionsFlow.getInitialPppoeSettingsPage());
        } else {
            Log.e(TAG, "Could not find existing configuration for network id: " + networkId);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveWifiConfigurationCompleted(int reason) {
        Bundle result = new Bundle();
        result.putString(FormPage.DATA_KEY_SUMMARY_STRING, Integer.toString(reason));
        onBundlePageResult(mSavePage, result);
    }

    @Override
    public void onTimedMessageCompleted() {
        Bundle result = new Bundle();
        result.putString(FormPage.DATA_KEY_SUMMARY_STRING, "");
        onBundlePageResult(mSuccessPage, result);
    }

    private void addResultWifiFormPage(int dataSummary) {
        switch (dataSummary) {
            case SaveWifiConfigurationFragment.RESULT_FAILURE:
                addPage(WifiFormPageType.SAVE_FAILED);
                break;
            case SaveWifiConfigurationFragment.RESULT_SUCCESS:
                addPage(WifiFormPageType.PPPOE_CONNECTING);
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onPageComplete(WifiFormPageType formPageType, FormPage formPage) {
        switch(formPageType) {
            case SAVE:
                registerReceiver();
                addResultWifiFormPage(Integer.valueOf(formPage.getDataSummary()));
                break;
            case SAVE_FAILED:
                break;
            case SAVE_SUCCESS:
                addPage(WifiFormPageType.PPPOE_CONNECTING);
                break;
            case PPPOE_CONNECTING:
                Log.d(TAG,"get network status"); 
                if (mNetworkStatus == mEthernetManager.ETHER_STATE_CONNECTED){
                    unRegisterReceiver();
                    break;
                }else if (mNetworkStatus == mEthernetManager.ETHER_STATE_DISCONNECTED){
                    unRegisterReceiver();
                    addPage(WifiFormPageType.PPPOE_CONNECT_FAILED);
                    break;
                }else{
                    removePage(formPage);
                    addPage(WifiFormPageType.PPPOE_CONNECTING);
                }                    
                break;
            default:
                if (mAdvancedWifiOptionsFlow.handlePageComplete(formPageType, formPage) ==
                        AdvancedWifiOptionsFlow.RESULT_ALL_PAGES_COMPLETE) {
                    save();
                }
                break;
        }
        return true;
    }

    @Override
    protected void displayPage(FormPage formPage, FormPageResultListener listener,
            boolean forward) {
        WifiFormPageType formPageType = getFormPageType(formPage);
        if (formPageType == WifiFormPageType.SAVE) {
            mSavePage = formPage;
            Fragment fragment = SaveWifiConfigurationFragment.newInstance(
                    getString(formPageType.getTitleResourceId(), mConfiguration.getPrintableName()),
                    mConfiguration);
            displayFragment(fragment, forward);
        } else if (formPageType == WifiFormPageType.SAVE_SUCCESS) {
            mSuccessPage = formPage;
            Fragment fragment = TimedMessageWizardFragment.newInstance(
                    getString(formPageType.getTitleResourceId()));
            displayFragment(fragment, forward);
        }else if (formPageType == WifiFormPageType.PPPOE_CONNECTING) {
            mSuccessPage = formPage;
            Fragment fragment = TimedMessageWizardFragment.newInstance(
                    getString(formPageType.getTitleResourceId()));
            displayFragment(fragment, forward);
        } else {
            displayPage(formPageType, mConfiguration.getPrintableName(), null, null,
                    mAdvancedWifiOptionsFlow.getPreviousPage(formPageType), null,
                    formPageType != WifiFormPageType.SAVE_SUCCESS, formPage, listener, forward,
                    mAdvancedWifiOptionsFlow.isEmptyTextAllowed(formPageType));
        }
    }

    private FormPage getPreviousPage(WifiFormPageType formPageType) {
        return mAdvancedWifiOptionsFlow.getPreviousPage(formPageType);
    }

    private void save() {
        mAdvancedWifiOptionsFlow.updateConfiguration(mConfiguration);    
        addPage(WifiFormPageType.SAVE);
    }
    
    private void registerReceiver(){
        Log.d(TAG,"registerReceiver");
	    if ( intentFilter == null ){
		    intentFilter = new IntentFilter();
		    intentFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
		    mContext.registerReceiver(mEthBroadRece, intentFilter);
	    }
    }
    
    private void unRegisterReceiver(){
        Log.d(TAG,"unRegisterReceiver");
		if ( intentFilter != null ){
			mContext.unregisterReceiver(mEthBroadRece);
			intentFilter = null;
		}

	}
    
    private BroadcastReceiver mEthBroadRece = new BroadcastReceiver(){
	    public void onReceive( Context context, Intent intent ){
            Log.d(TAG,"EthBroadRece");
		    String action = intent.getAction();
			if ( !action.equals("android.net.ethernet.ETHERNET_STATE_CHANGED") ){
				return;
			}
			int status = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE,
                                            EthernetManager.ETHER_STATE_CONNECTING);
            Log.d(TAG,"EthBroadRece :" + status);
                mNetworkStatus = status;
        }
    };
        
}
