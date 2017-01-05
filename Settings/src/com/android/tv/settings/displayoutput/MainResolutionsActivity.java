package com.android.tv.settings.displayoutput;

import android.app.Fragment;

import com.android.tv.settings.TvSettingsActivity;

/**
 * @author GaoFei
 * 分辨率设置Activity
 */
public class MainResolutionsActivity extends TvSettingsActivity{

	@Override
	protected Fragment createSettingsFragment() {
		return new MainResolutionsFragment();
	}

}
