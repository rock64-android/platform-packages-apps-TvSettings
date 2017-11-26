package com.android.tv.settings.device.sound;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.tv.settings.R;
/**
 * @author GaoFei
 *
 */
public class VolumeSettingsActivity extends Activity{


    /** Called when the activity is first created. */
    private MediaPlayer mediaPlayer01;
    public AudioManager audiomanage;
    private TextView mVolume ;  //显示当前音量
    public SeekBar soundBar;
    private int maxVolume, currentVolume;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volume_settings);
        mediaPlayer01 = new MediaPlayer();

        //imageButton_white1=(ImageButton)findViewById(R.id.volume_test);
        final SeekBar soundBar=(SeekBar)findViewById(R.id.volume_seekbar);  //音量设置
        mVolume = (TextView)findViewById(R.id.volume_test);
        audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        soundBar.setMax(maxVolume);   //拖动条最高值与系统最大声匹配
        currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
        soundBar.setProgress(currentVolume);
        mVolume.setText(currentVolume*100/maxVolume + " %");

        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() //调音监听器
        {
            public void onProgressChanged(SeekBar arg0,int progress,boolean fromUser)
            {
                audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
                soundBar.setProgress(currentVolume);
                mVolume.setText(currentVolume*100/maxVolume + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


}
