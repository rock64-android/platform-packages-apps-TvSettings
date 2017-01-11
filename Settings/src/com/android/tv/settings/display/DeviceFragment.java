/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tv.settings.display;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Display.Mode;
import android.view.View;
import android.widget.TextView;
import android.os.DisplayOutputManager;
import android.os.SystemProperties;
import android.support.annotation.Keep;
import com.android.tv.settings.R;
import com.android.tv.settings.data.ConstData;
import com.android.tv.settings.display.DisplayFragment.DisplayInfo;
import android.os.DisplayOutputManager;
import android.os.SystemProperties;
import android.hardware.display.*;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.ServiceManager;
@Keep
public class DeviceFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener, 
Preference.OnPreferenceClickListener{
	private static final String TAG = "DeviceFragment";
	public static final String KEY_RESOLUTION = "resolution";
	public static final String KEY_ZOOM = "zoom";
	private PreferenceScreen mPreferenceScreen;
	/**
	 * 分辨率设置
	 */
	private ListPreference mResolutionPreference;
	/**
	 * 缩放设置
	 */
	private Preference mZoomPreference;
	/**
	 * 当前显示设备对应的信息
	 */
	private DisplayInfo mDisplayInfo;
	/**
	 * 标题
	 */
	private TextView mTextTitle;
	/**
	 * 标识平台
	 */
	private String mStrPlatform;
	/**
	 * 显示管理
	 */
	private DisplayManager mDisplayManager;
    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display_device, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	initData();
        initView();
        initEvent();
        restoreResolutionValue();
    }



    @Override
    public void onResume() {
    	super.onResume();
    }


    @Override
    public void onPause() {
    	super.onPause();
    }


    private void initData(){
    	mStrPlatform = SystemProperties.get("ro.board.platform");
    	mDisplayManager = (DisplayManager)getActivity().getSystemService(Context.DISPLAY_SERVICE);
    	mPreferenceScreen = getPreferenceScreen();
    	mResolutionPreference = (ListPreference)findPreference(KEY_RESOLUTION);
    	mZoomPreference = findPreference(KEY_ZOOM);
    	mDisplayInfo = (DisplayInfo)getActivity().getIntent().getExtras().getSerializable(ConstData.IntentKey.DISPLAY_INFO);
    	mTextTitle = (TextView)getActivity().findViewById(android.support.v7.preference.R.id.decor_title);
    }

    private void initView(){
    	mResolutionPreference.setEntries(mDisplayInfo.getModes());
    	mResolutionPreference.setEntryValues(mDisplayInfo.getModes());
    	mTextTitle.setText(mDisplayInfo.getDescription());
    }


    private void initEvent(){
    	mResolutionPreference.setOnPreferenceChangeListener(this);
    	mZoomPreference.setOnPreferenceClickListener(this);
    }


    /**
     * 还原分辨率值
     */
    public void restoreResolutionValue(){
    	String resolutionValue = null;
    	if(mStrPlatform.contains("3399")){
    		Display currDisplay = mDisplayManager.getDisplay(mDisplayInfo.getDisplayId());
    		resolutionValue = getStrMode(currDisplay.getMode());
    		Log.i(TAG, "3399 resolutionValue:" + resolutionValue);
    		if(resolutionValue != null)
                mResolutionPreference.setValue(resolutionValue);
    	}else{
    		DisplayOutputManager displayOutputManager = null;
    		try{
    			displayOutputManager = new DisplayOutputManager();
    			resolutionValue = displayOutputManager.getCurrentMode(mDisplayInfo.getDisplayId() == 0 ? 0 : 1, mDisplayInfo.getType());
    		}catch (Exception e){
    			Log.i(TAG, "restoreResolutionValue->exception:" + e);
    		}
    		if(resolutionValue != null)
    			mResolutionPreference.setValue(resolutionValue);
    	}
    }

    
   /**
    * 获取对应index的ModeId，针对DRM
    * @param index
    * @return
    */
    private int getModeId(int index){
    	return mDisplayManager.getDisplay(mDisplayInfo.getDisplayId()).getSupportedModes()[index].getModeId();
    }
    
    /**
     * 从Display.Mode获取字符串
     * @param mode
     * @return
     */
    private String getStrMode(Mode mode){
    	StringBuilder builder = new StringBuilder();
    	builder.append(mode.getPhysicalWidth()).append("x")
    	.append(mode.getPhysicalHeight()).append("-").append(mode.getRefreshRate());
    	return builder.toString();
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
    	Log.i(TAG, "onPreferenceChange:" + obj);
    	if(preference == mResolutionPreference){
    		if(mStrPlatform.contains("3399")){
    			try{
					IDisplayManager manager = IDisplayManager.Stub.asInterface(ServiceManager.getService(Context.DISPLAY_SERVICE));
					Log.i(TAG, "onPreferenceChange->3399->modeId:" + getModeId(mResolutionPreference.findIndexOfValue((String)obj)));
					Log.i(TAG, "onPreferenceChange->3399->displayId:" + mDisplayInfo.getDisplayId());
					manager.requestMode(mDisplayInfo.getDisplayId(), getModeId(mResolutionPreference.findIndexOfValue((String)obj)));
				}catch (Exception e){
					Log.i(TAG, "onclick exception:" + e);
				}
    		}else{
    			DisplayOutputManager displayOutputManager = null;
        		try{
        			displayOutputManager = new DisplayOutputManager();
        		}catch (Exception e){
        			Log.i(TAG, "onPreferenceChange->exception:" + e);
        		}

        		if(displayOutputManager != null)
        			displayOutputManager.setMode(mDisplayInfo.getDisplayId(), mDisplayInfo.getType(), (String)obj);
    		}

    	}
    	return true;
    }

    
    @Override
    public boolean onPreferenceClick(Preference preference) {
    	if(preference == mZoomPreference && !mStrPlatform.contains("3399"))
    		startActivity(new Intent(getActivity(), ScreenScaleActivity.class));
    	return true;
    }

}
