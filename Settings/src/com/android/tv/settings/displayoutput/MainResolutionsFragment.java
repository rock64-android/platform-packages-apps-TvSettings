package com.android.tv.settings.displayoutput;

import java.util.Arrays;

import com.android.tv.settings.device.sound.SoundFragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Display.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.hardware.display.*;
import android.hardware.display.DisplayManager.DisplayListener;
import com.android.tv.settings.R;
import android.os.ServiceManager;
/**
 * @author GaoFei 分辨率设置
 */
public class MainResolutionsFragment extends Fragment implements OnItemClickListener,DisplayListener{

	private static final String TAG = "MainResolutionsFragment";
	private ListView mDeviceListView;
	private DisplayManager mDisplayManager;
	private Display mSelectDisplay;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initData();
		mDeviceListView = getRootView();
		mDeviceListView.setPadding(0, 40, 0, 0);
		return mDeviceListView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initEvent();
	}

	@Override
	public void onResume() {
		super.onResume();
		rebulidView();
	}

	private void rebulidView() {
		//mDeviceListView.removeAllViews();
		Display[] displays = mDisplayManager.getDisplays();
		String[] names = new String[displays.length];
		for (int i = 0; i != displays.length; ++i) {
			Display itemDisplay = displays[i];
			names[i] = itemDisplay.getName();
		}

		ArrayAdapter<String> deviceListAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, names);
		mDeviceListView.setAdapter(deviceListAdapter);
	}


	private void initData(){
		mDisplayManager = (DisplayManager)getContext().getSystemService(Context.DISPLAY_SERVICE);
	}

	private void initEvent(){
		mDeviceListView.setOnItemClickListener(this);
		mDisplayManager.registerDisplayListener(this, null);
	}

	public ListView getRootView(){
		return new ListView(getContext());
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Display[] displays = mDisplayManager.getDisplays();
		for (int i = 0; i != displays.length; ++i) {
			Display itemDisplay = displays[i];
			if(itemDisplay.getName().equals(parent.getAdapter().getItem(position))){
				mSelectDisplay = itemDisplay;
				break;
			}
		}

		if(mSelectDisplay != null){
			final Mode[] supportModes = mSelectDisplay.getSupportedModes();
			int currentModeId = mSelectDisplay.getMode().getModeId();
			Log.i(TAG, "currentModeId:" + currentModeId);
			String[] strModes = new String[supportModes.length];
			int selectIndex = -1;
			for(int i = 0; i != supportModes.length; ++i){
				Mode itemMode = supportModes[i];
				if(itemMode.getModeId() == currentModeId)
					selectIndex = i;
				StringBuilder modeBuilder = new StringBuilder();
				modeBuilder.append(itemMode.getPhysicalWidth())
				.append("x").append(itemMode.getPhysicalHeight())
				.append("-").append(itemMode.getRefreshRate());
				strModes[i] = modeBuilder.toString();
			}

			new AlertDialog.Builder(getContext()).setTitle("Setting Resoultion")
			.setSingleChoiceItems(strModes, selectIndex, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						IDisplayManager manager = IDisplayManager.Stub.asInterface(ServiceManager.getService(Context.DISPLAY_SERVICE));
						manager.requestMode(mSelectDisplay.getDisplayId(), supportModes[which].getModeId());
					}catch (Exception e){
						Log.i(TAG, "onclick exception:" + e);
					}

				}
			}).show();
		}
	}

	@Override
	public void onDisplayAdded(int displayId) {
		Log.i(TAG, "onDisplayAdded:" + displayId);
		rebulidView();
	}

	@Override
	public void onDisplayRemoved(int displayId) {
		Log.i(TAG, "onDisplayRemoved:" + displayId);
		rebulidView();
	}

	@Override
	public void onDisplayChanged(int displayId) {
		Log.i(TAG, "onDisplayChanged:" + displayId);
		rebulidView();
	}
}
