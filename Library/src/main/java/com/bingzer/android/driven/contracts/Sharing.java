package com.bingzer.android.driven.contracts;

import com.bingzer.android.driven.DrivenFile;

/**
 * Contracts for sharing
 */
public interface Sharing {

    /**
     * Default permission
     */
    int PERMISSION_DEFAULT = -1;

    /**
     * Read-only permission
     */
    int PERMISSION_READ = 0;

    /**
     * Full permission
     */
    int PERMISSION_FULL = 1;

    /**
     * True if Provider's implementation support "Sharing"
     */
    boolean isSupported();

    /**
     * Share {@link com.bingzer.android.driven.DrivenFile} to other user.
     * The term "user" is generic. Most of the time it is an email address.
     * Check with provider's implementation.
     */
    String share(DrivenFile drivenFile, String user);

    /**
     * Share {@link com.bingzer.android.driven.DrivenFile} to other user.
     * The term "user" is generic. Most of the time it is an email address.
     * Check with provider's implementation.
     */
    String share(DrivenFile drivenFile, String user, int kind);

    /**
     * Async call for {@link #share(com.bingzer.android.driven.DrivenFile, String)}
     */
    void shareAsync(DrivenFile drivenFile, String user, Task<String> result);

    /**
     * Async call for {@link #share(com.bingzer.android.driven.DrivenFile, String, int)}
     */
    void shareAsync(DrivenFile drivenFile, String user, int kind, Task<String> result);

}
