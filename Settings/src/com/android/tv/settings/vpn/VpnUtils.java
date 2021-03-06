/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tv.settings.vpn;

import android.content.Context;
import android.net.ConnectivityManager;
import android.security.Credentials;
import android.security.KeyStore;

/**
 * Utility functions for vpn.
 *
 * Keystore methods should only be called in system user
 */
public class VpnUtils {

    public static String getLockdownVpn() {
        final byte[] value = KeyStore.getInstance().get(Credentials.LOCKDOWN_VPN);
        return value == null ? null : new String(value);
    }

    public static void clearLockdownVpn(Context context) {
        KeyStore.getInstance().delete(Credentials.LOCKDOWN_VPN);
        // Always notify ConnectivityManager after keystore update
        context.getSystemService(ConnectivityManager.class).updateLockdownVpn();
    }

    public static void setLockdownVpn(Context context, String lockdownKey) {
        KeyStore.getInstance().put(Credentials.LOCKDOWN_VPN, lockdownKey.getBytes(),
                KeyStore.UID_SELF, /* flags */ 0);
        // Always notify ConnectivityManager after keystore update
        context.getSystemService(ConnectivityManager.class).updateLockdownVpn();
    }

    public static boolean isVpnLockdown(String key) {
        return key.equals(getLockdownVpn());
    }
}