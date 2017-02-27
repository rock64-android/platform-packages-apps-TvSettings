package com.android.tv.settings.dialog;

import com.android.tv.settings.R;

import android.content.Context;
import android.os.Handler;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.Log;

public class UsbModeSettings
{
    private static final String TAG = "UsbModeSettings";
    public static final String	HOST_MODE = new String("1");
    public static final String	SLAVE_MODE = new String("2");

    private File file = null;

    private StorageManager mStorageManager = null;
    private String mMode = null;


    private Context mContext;

    private boolean mLock = false;

    public UsbModeSettings(Context context)
    {
        mContext = context;

        mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        file = new File("/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
    }

    public boolean getDefaultValue(){
        if(file.exists())
        {
            Log.d("UsbModeSelect","/data/otg.cfg not exist,but temp file exist");
            mMode = ReadFromFile(file);
            if(mMode.equals(HOST_MODE)){
                return false;
            }else{
                return true;
            }
        }
        else
        {
            mMode = HOST_MODE;
            return false;
        }
    }

    private String ReadFromFile(File file)
    {
        if((file != null) && file.exists())
        {
            try
            {
                FileInputStream fin= new FileInputStream(file);
                BufferedReader reader= new BufferedReader(new InputStreamReader(fin));
                String config = reader.readLine();
                fin.close();
                return config;
            }
            catch(IOException e)
            {
                Log.i(TAG, "ReadFromFile exception:" + e);
                e.printStackTrace();
            }
        }

        return null;
    }

    private void Write2File(File file,String mode)
    {
        Log.d("UsbModeSelect","Write2File,write mode = "+mode);
        if((file == null) || (!file.exists()) || (mode == null))
            return ;

        try
        {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(mode);
            pWriter.flush();
            pWriter.close();
            fout.close();
        }
        catch(IOException re)
        {
        }
    }

    public void onUsbModeClick(String mode)
    {
        if(mLock)
            return ;
        mLock = true;
        mMode = mode;
        synchronized (this)
        {
            new Thread(mUsbSwitch).start();
        }
    }

    private  Runnable mUsbSwitch = new Runnable()
    {
        public synchronized void run()
        {
            Log.d("UsbModeSettings","mUsbSwitch Runnable() in*******************");
            if(mStorageManager != null)
            {
                if(mMode == HOST_MODE)
                {
                    mStorageManager.disableUsbMassStorage();
                    Log.d("UsbModeSettings","mStorageManager.disableUsbMassStorage()*******************");
                    Write2File(file, mMode);
                }
                else
                {
                    Write2File(file, mMode);
                    Log.d("UsbModeSettings","mStorageManager.enableUsbMassStorage()  in *******************");
                    mStorageManager.enableUsbMassStorage();
                    Log.d("UsbModeSettings","mStorageManager.enableUsbMassStorage()   out*******************");
                }
            }
            Log.d("UsbModeSettings","mUsbSwitch Runnable() out*******************");
            mLock = false;
        }
    };
}
