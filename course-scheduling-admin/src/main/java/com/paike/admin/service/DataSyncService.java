package com.paike.admin.service;

import com.paike.admin.dto.DataSyncApplyResponse;
import com.paike.admin.dto.DataSyncPreviewResponse;
import com.paike.admin.dto.DataSyncRequest;

public interface DataSyncService {

    DataSyncPreviewResponse preview(DataSyncRequest request);

    DataSyncApplyResponse apply(DataSyncRequest request);
}
