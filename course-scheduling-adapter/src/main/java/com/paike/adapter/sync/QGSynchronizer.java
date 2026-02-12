package com.paike.adapter.sync;

import com.paike.adapter.config.SyncConfig;
import org.springframework.stereotype.Component;

@Component
public class QGSynchronizer implements DataSynchronizer {

    @Override
    public SyncResult sync(SyncConfig config) {
        return SyncResult.success(0, 0, 0);
    }

    @Override
    public SyncResult syncIncremental(SyncConfig config, String lastSyncTime) {
        return SyncResult.success(0, 0, 0);
    }

    @Override
    public boolean supports(String systemType) {
        return "QG".equalsIgnoreCase(systemType);
    }

    @Override
    public String getSystemName() {
        return "青果教务系统";
    }
}
