/**
 * 
 */
package com.android.tv.settings.display;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.DisplayOutputManager;
import com.android.tv.settings.BaseInputActivity;
import com.android.tv.settings.R;
import com.android.tv.settings.data.ConstData;
import com.android.tv.settings.util.JniCall;

import android.view.View;
/**
 * @author GaoFei
 * 
 */
public class AdvancedDisplaySettingsActivity extends BaseInputActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener{
	private static final String TAG = "AdvancedDisplaySettingsActivity";
	private DisplayOutputManager mDisplayOutputManager;
	private int mOldBcshBrightness;
	private int mOldBcshContrast;
	private int mOldBcshStauration;
	private int mOldBcshTone;
	private double mOldHdrMaxBrightness;
	private double mOldHdrMinBrightness;
	private double mOldHdrBrightnessNum;
	private double mOldHdrSaturationNum;
	/**
	 * BCSH亮度
	 */
	private SeekBar mSeekBarBcshBrightness;
	/**
	 * BCSH对比度
	 */
	private SeekBar mSeekBarBcshContrast;
	/**
	 * BCSH饱和度
	 */
	private SeekBar mSeekBarBcshSaturation;
	/**
	 * BCSH色调
	 */
	private SeekBar mSeekBarBcshTone;
	/**
	 * BCSH亮度值
	 */
	private TextView mTextBcshBrightnessNum;
	/**
	 * BCSH对比度值
	 */
	private TextView mTextBcshContrastNum;
	/**
	 * BCSH饱和度值
	 */
	private TextView mTextBcshStaurationNum;
	/**
	 * BCSH色调值
	 */
	private TextView mTextBcshToneNum;
	/**
	 * 确定按钮
	 */
	private Button mBtnOK;
	/**
	 * 取消按钮
	 */
	private Button mBtnCancel;
	/**
	 * HDR布局
	 */
	private LinearLayout mLayoutHdr;
	
	/**最大亮度基数*/
	public static final int MAX_BRIGHTNESS_BASE = 400;
	/**亮度基数*/
	public static final int BRIGHTNESS_BASE = 500;
	/**饱和度基数*/
	public static final int STATURATION_BASE = 2000;
	/**最大亮度，最小亮度生成曲线路径*/
	public static final String HDR_BRIGHTNESS_PATH = "/sys/class/graphics/fb0/hdr_bt1886eotf";
	/**亮度-饱和度曲线路径*/
	public static final String BRIGHTNESS_STATURATION_PATH = "/sys/class/graphics/fb0/hdr_st2084oetf";
	/**Hdr最大亮度滑动条*/
	private SeekBar mMaxBrightnessBar;
	/**Hdr最小亮度滑动条*/
	private SeekBar mMinBrightnessBar;
	/**Hdr亮度拖动条*/
	private SeekBar mSeekBarBrightness;
	/**Hdr饱和度拖动条*/
	private SeekBar mSeekBarStatustion;
	/**当前最大亮度进度值*/
	private TextView mTextMaxBrightness;
	/**当前最小亮度进度值*/
	private TextView mTextMinBrightness;
	/**亮度值*/
	private TextView mTextBrightnessNum;
	/**饱和度*/
	private TextView mTextStatustionNum;
	/**最大亮度*/
	private double mMaxBrightness;
	/**最小亮度*/
	private double mMinBrightness;
	/**亮度数值*/
	private double mBrightnessNum;
	/**饱和度数值*/
	private double mSaturationNum;
	@Override
	public void init() {
		try{
			mDisplayOutputManager = new DisplayOutputManager();
		}catch (Exception e){
			
		}
		mSeekBarBcshBrightness = (SeekBar)findViewById(R.id.brightness);
		mSeekBarBcshContrast = (SeekBar)findViewById(R.id.contrast);
		mSeekBarBcshSaturation = (SeekBar)findViewById(R.id.saturation);
		mSeekBarBcshTone = (SeekBar)findViewById(R.id.tone);
		mTextBcshBrightnessNum = (TextView)findViewById(R.id.text_bcsh_brightness_num);
		mTextBcshContrastNum = (TextView)findViewById(R.id.text_bcsh_contrast_num);
		mTextBcshStaurationNum = (TextView)findViewById(R.id.text_bcsh_saturation_num);
		mTextBcshToneNum = (TextView)findViewById(R.id.text_bcsh_tone_num);
		mBtnOK = (Button)findViewById(R.id.btn_ok);
		mBtnCancel = (Button)findViewById(R.id.btn_cancel);
		mLayoutHdr = (LinearLayout)findViewById(R.id.layout_hdr);
		mBtnOK.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mSeekBarBcshContrast.setKeyProgressIncrement(20);
		mSeekBarBcshSaturation.setKeyProgressIncrement(20);
		mSeekBarBcshBrightness.setOnSeekBarChangeListener(this);
		mSeekBarBcshContrast.setOnSeekBarChangeListener(this);
		mSeekBarBcshSaturation.setOnSeekBarChangeListener(this);
		mSeekBarBcshTone.setOnSeekBarChangeListener(this);
		SharedPreferences bcshPreferences = getSharedPreferences(ConstData.SharedKey.BCSH_VALUES, Context.MODE_PRIVATE);
		mOldBcshBrightness = bcshPreferences.getInt(ConstData.SharedKey.BCSH_BRIGHTNESS, 32);
		mSeekBarBcshBrightness.setProgress(mOldBcshBrightness);
		mOldBcshContrast = bcshPreferences.getInt(ConstData.SharedKey.BCSH_CONTRAST, 1000);
		mSeekBarBcshContrast.setProgress(mOldBcshContrast);
		mOldBcshStauration = bcshPreferences.getInt(ConstData.SharedKey.BCSH_STAURATION, 1000);
		mSeekBarBcshSaturation.setProgress(mOldBcshStauration);
		mOldBcshTone = bcshPreferences.getInt(ConstData.SharedKey.BCSH_TONE, 30);
		mSeekBarBcshTone.setProgress(mOldBcshTone);
		updateBcshValue();
		mMaxBrightnessBar = (SeekBar)findViewById(R.id.seekbar_max_brightness);
		mMinBrightnessBar = (SeekBar)findViewById(R.id.seekbar_min_brightness);
		mMaxBrightnessBar.setOnSeekBarChangeListener(this);
		mMinBrightnessBar.setOnSeekBarChangeListener(this);
		mMaxBrightnessBar.setKeyProgressIncrement(14);
		mMinBrightnessBar.setKeyProgressIncrement(17);
		mTextMaxBrightness = (TextView)findViewById(R.id.text_progress_max_brightness);
		mTextMinBrightness = (TextView)findViewById(R.id.text_progress_min_progress);
		mSeekBarBrightness = (SeekBar)findViewById(R.id.seekbar_brightness);
		mSeekBarStatustion = (SeekBar)findViewById(R.id.seekbar_saturation);
		mSeekBarBrightness.setKeyProgressIncrement(17);
		mSeekBarStatustion.setKeyProgressIncrement(17);
		mSeekBarBrightness.setOnSeekBarChangeListener(this);
		mSeekBarStatustion.setOnSeekBarChangeListener(this);
		mTextBrightnessNum = (TextView)findViewById(R.id.text_brightness_num);
		mTextStatustionNum = (TextView)findViewById(R.id.text_saturation_num);
		try{
			mMaxBrightness = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.MAX_BRIGHTNESS));
		}catch (Exception e){
			//发生异常，此时恢复默认值
			mMaxBrightness = 800;
		}
		mOldHdrMaxBrightness = mMaxBrightness;
		mMaxBrightnessBar.setProgress((int)(mMaxBrightness - MAX_BRIGHTNESS_BASE));
		mTextMaxBrightness.setText("" + mMaxBrightness);
		try{
			mMinBrightness = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.MIN_BRIGHTNESS));
		}catch(Exception e){
			mMinBrightness = 5;
		}
		mOldHdrMinBrightness = mMinBrightness;
		mMinBrightnessBar.setProgress((int)(mMinBrightness * 100));
		mTextMinBrightness.setText("" + mMinBrightness);
		try{
			mBrightnessNum = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.BRIGHTNESS));
		}catch (Exception e){
			mBrightnessNum = 1;
		}
		mOldHdrBrightnessNum = mBrightnessNum;
		mSeekBarBrightness.setProgress((int)(mBrightnessNum * 1000) - BRIGHTNESS_BASE);
		mTextBrightnessNum.setText("" + mBrightnessNum);
		try{
			mSaturationNum = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.STATURATION));
		}catch(Exception e){
			mSaturationNum = 2.5;
		}
		mOldHdrSaturationNum = mSaturationNum;
		mSeekBarStatustion.setProgress((int)(mSaturationNum * 1000) - STATURATION_BASE);
		mTextStatustionNum.setText("" + mSaturationNum);
		try{
			mMaxBrightness = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.MAX_BRIGHTNESS));
		}catch (Exception e){
			//发生异常，此时恢复默认值
			mMaxBrightness = 800;
		}
		mMaxBrightnessBar.setProgress((int)(mMaxBrightness - MAX_BRIGHTNESS_BASE));
		mTextMaxBrightness.setText("" + mMaxBrightness);
		
		try{
			mMinBrightness = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.MIN_BRIGHTNESS));
		}catch(Exception e){
			mMinBrightness = 5;
		}
		
		mMinBrightnessBar.setProgress((int)(mMinBrightness * 100));
		mTextMinBrightness.setText("" + mMinBrightness);
		
		try{
			mBrightnessNum = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.BRIGHTNESS));
		}catch (Exception e){
			mBrightnessNum = 1;
		}
		
		mSeekBarBrightness.setProgress((int)(mBrightnessNum * 1000) - BRIGHTNESS_BASE);
		mTextBrightnessNum.setText("" + mBrightnessNum);
		try{
			mSaturationNum = Double.parseDouble(getValueFromPreference(ConstData.SharedKey.STATURATION));
		}catch(Exception e){
			mSaturationNum = 2.5;
		}
		mSeekBarStatustion.setProgress((int)(mSaturationNum * 1000) - STATURATION_BASE);
		mTextStatustionNum.setText("" + mSaturationNum);
	}

	@Override
	public int getContentLayoutRes() {
		return R.layout.activity_advanced_display_settings;
	}

	@Override
	public String getInputTitle() {
		return getString(R.string.advanced_settings);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(seekBar == mSeekBarBcshBrightness){
			updateBcshValue();
		}else if(seekBar == mSeekBarBcshContrast){
			updateBcshValue();
		}else if(seekBar == mSeekBarBcshSaturation){
			updateBcshValue();
		}else if(seekBar == mSeekBarBcshTone){
			updateBcshValue();
		}else if(seekBar == mMaxBrightnessBar){
			mMaxBrightness = (progress + MAX_BRIGHTNESS_BASE) * 1.0;
			mTextMaxBrightness.setText("" + mMaxBrightness);
			updateHdrBrightness();
		}else if(seekBar == mMinBrightnessBar){
			mMinBrightness = progress * 1.0 / 100;
			mTextMinBrightness.setText("" + mMinBrightness);
			updateHdrBrightness();
		}else if(seekBar == mSeekBarBrightness){
			mBrightnessNum = (progress + BRIGHTNESS_BASE) * 1.0 / 1000;
			mTextBrightnessNum.setText("" + mBrightnessNum);
			updateBrightnessSaturation();
		}else if(seekBar == mSeekBarStatustion){
			mSaturationNum = (progress + STATURATION_BASE) * 1.0 / 1000;
			mTextStatustionNum.setText("" + mSaturationNum);
			updateBrightnessSaturation();
		}
		Log.i(TAG, "onProgressChanged->progress:" + progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.i(TAG, "onStartTrackingTouch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.i(TAG, "onStopTrackingTouch");
	}

    private void updateBcshValue() {
    	if(mDisplayOutputManager == null)
    		return;
        try {
            //调整亮度
            mDisplayOutputManager.setBrightness(mDisplayOutputManager.MAIN_DISPLAY, -32 + mSeekBarBcshBrightness.getProgress());
            //调整对比度
            mDisplayOutputManager.setContrast(mDisplayOutputManager.MAIN_DISPLAY, mSeekBarBcshContrast.getProgress() * 1.0f / 1000);
            //调整饱和度
            mDisplayOutputManager.setSaturation(mDisplayOutputManager.MAIN_DISPLAY, mSeekBarBcshSaturation.getProgress() * 1.0f / 1000);
            //调整色调
            mDisplayOutputManager.setHue(mDisplayOutputManager.MAIN_DISPLAY, -30 + mSeekBarBcshTone.getProgress());
            mTextBcshBrightnessNum.setText("" +  (-32 + mSeekBarBcshBrightness.getProgress()));
            mTextBcshContrastNum.setText("" + mSeekBarBcshContrast.getProgress() * 1.0f / 1000);
            mTextBcshStaurationNum.setText("" +  mSeekBarBcshSaturation.getProgress() * 1.0f / 1000);
            mTextBcshToneNum.setText("" + (-30 + mSeekBarBcshTone.getProgress()));
        } catch (Exception e) {
        }
    }
    
	/**更新最大亮度-最小亮度生成曲线*/
	private void updateHdrBrightness(){
		int[] brightnessArray = JniCall.get(mMaxBrightness, mMinBrightness);
		//Log.i(TAG, "updateHdrBrightness->brightnessArray:" + Arrays.toString(brightnessArray));
		updateFileContent(brightnessArray, HDR_BRIGHTNESS_PATH);
	}
	/**更新亮度-饱和度曲线*/
	private void updateBrightnessSaturation(){
		int[] staturationArray = JniCall.getOther(mBrightnessNum, mSaturationNum);
		updateFileContent(staturationArray, BRIGHTNESS_STATURATION_PATH);
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		recoveryOldValue();
    		finish();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }

    private void recoveryOldValue(){
    	try{
    		//调整亮度
            mDisplayOutputManager.setBrightness(mDisplayOutputManager.MAIN_DISPLAY, -32 + mOldBcshBrightness);
            //调整对比度
            mDisplayOutputManager.setContrast(mDisplayOutputManager.MAIN_DISPLAY, mOldBcshContrast * 1.0f / 1000);
            //调整饱和度
            mDisplayOutputManager.setSaturation(mDisplayOutputManager.MAIN_DISPLAY, mOldBcshStauration * 1.0f / 1000);
            //调整色调
            mDisplayOutputManager.setHue(mDisplayOutputManager.MAIN_DISPLAY, -30 + mOldBcshTone);
    	}catch (Exception e){
    		
    	}
    }

    private void saveNewValue(){
    	SharedPreferences bcshPreferences = getSharedPreferences(ConstData.SharedKey.BCSH_VALUES, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = bcshPreferences.edit();
    	editor.putInt(ConstData.SharedKey.BCSH_BRIGHTNESS, mSeekBarBcshBrightness.getProgress());
    	editor.putInt(ConstData.SharedKey.BCSH_CONTRAST, mSeekBarBcshContrast.getProgress());
    	editor.putInt(ConstData.SharedKey.BCSH_STAURATION, mSeekBarBcshSaturation.getProgress());
    	editor.putInt(ConstData.SharedKey.BCSH_TONE, mSeekBarBcshTone.getProgress());
    	editor.commit();
    	saveValueToPreference(ConstData.SharedKey.MAX_BRIGHTNESS, "" + mMaxBrightness);
    	saveValueToPreference(ConstData.SharedKey.MIN_BRIGHTNESS, "" + mMinBrightness);
    	saveValueToPreference(ConstData.SharedKey.BRIGHTNESS, "" + mBrightnessNum);
    	saveValueToPreference(ConstData.SharedKey.STATURATION, "" + mSaturationNum);
    }

	@Override
	public void onClick(View v) {
		if(v == mBtnCancel){
			recoveryOldValue();
			finish();
		}else if(v == mBtnOK){
			saveNewValue();
			finish();
		}
	}
	
	/**初始化HDR设置*/
	public void initHDR(){
		boolean isSupport= JniCall.isSupportHDR();
		if(!isSupport)
			mLayoutHdr.setVisibility(View.GONE);
		else{
			//设置电视支持HDR模式
			JniCall.setHDREnable(1);
			//恢复HDR曲线值
			updateHdrBrightness();
			updateBrightnessSaturation();
		}
	}
	
	/**移除HDR*/
	public void removeHDR(){
		boolean isSupport= JniCall.isSupportHDR();
		if(isSupport){
			//取消HDR模式
			JniCall.setHDREnable(0);
		}
	}

	/**更新文件内容*/
	private void updateFileContent(int[] contents, String path){
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path)));
			StringBuffer strBuffer = new StringBuffer();
			for(int item : contents){
				strBuffer.append(item).append(" ");
			}
			//strBuffer.delete(strBuffer.length() - 1, strBuffer.length());
			bufferedWriter.write(strBuffer.toString());
			Log.i(TAG, "updateFileContent->strBuffer:" + strBuffer.toString());
			bufferedWriter.flush();
			bufferedWriter.close();
		}catch (Exception e){
			Log.i(TAG, "updateFileContent->exception:" + e);
		}
		
	}
	private String getValueFromPreference(String key){
    	return getSharedPreferences(ConstData.SharedKey.HDR_VALUES, Context.MODE_PRIVATE).getString(key, "");
    }
	private void saveValueToPreference(String key, String value){
    	SharedPreferences sharedPreferences = getSharedPreferences(ConstData.SharedKey.HDR_VALUES, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(key, value);
    	editor.commit();
    }
	@Override
	protected void onResume() {
		super.onResume();
		initHDR();
	}
	@Override
	protected void onPause() {
		super.onPause();
		removeHDR();
	}
}
