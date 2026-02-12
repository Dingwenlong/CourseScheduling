package com.paike.adapter.sync;

import com.paike.adapter.config.SyncConfig;

import java.util.List;
import java.util.Map;

public interface DataSynchronizer {

    SyncResult sync(SyncConfig config);

    SyncResult syncIncremental(SyncConfig config, String lastSyncTime);

    boolean supports(String systemType);

    String getSystemName();
}
