package com.paike.adapter.sync;

import com.paike.adapter.config.SyncConfig;
import org.springframework.stereotype.Component;

@Component
public class ZFSynchronizer implements DataSynchronizer {

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
        return "ZF".equalsIgnoreCase(systemType);
    }

    @Override
    public String getSystemName() {
        return "正方教务系统";
    }
}
