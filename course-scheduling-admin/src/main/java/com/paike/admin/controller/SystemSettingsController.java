package com.paike.admin.controller;

import com.paike.admin.dto.DataSyncApplyResponse;
import com.paike.admin.dto.DataSyncPreviewResponse;
import com.paike.admin.dto.DataSyncRequest;
import com.paike.admin.service.DataSyncService;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统设置", description = "系统设置与数据对接")
@RestController
@RequestMapping("/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingsController {

    @Autowired
    private DataSyncService dataSyncService;

    @Operation(summary = "预览数据对接结果")
    @PostMapping("/data-sync/preview")
    public Result<DataSyncPreviewResponse> preview(@RequestBody DataSyncRequest request) {
        return Result.success(dataSyncService.preview(request));
    }

    @Operation(summary = "执行数据对接同步")
    @PostMapping("/data-sync/apply")
    public Result<DataSyncApplyResponse> apply(@RequestBody DataSyncRequest request) {
        return Result.success(dataSyncService.apply(request));
    }
}
