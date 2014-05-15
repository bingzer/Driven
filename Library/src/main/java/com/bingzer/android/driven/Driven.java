package com.bingzer.android.driven;

import com.bingzer.android.driven.providers.gdrive.GoogleDrive;

public class Driven {

    public static final int GOOGLE_DRIVE = 0;

    public static DrivenProvider getProvider(int provider){
        switch (provider){
            case GOOGLE_DRIVE:
                return new GoogleDrive();
        }

        throw new DrivenException("No DrivenProvider found for " + provider);
    }
}
