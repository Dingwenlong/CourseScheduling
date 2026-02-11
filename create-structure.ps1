# 创建项目根目录结构
$baseDir = "d:\Devs\PaiKe\course-scheduling-system"

# 1. 创建后端模块目录结构
$backendDirs = @(
    # common模块
    "course-scheduling-common/src/main/java/com/paike/common/constants",
    "course-scheduling-common/src/main/java/com/paike/common/utils",
    "course-scheduling-common/src/main/java/com/paike/common/result",
    "course-scheduling-common/src/main/java/com/paike/common/exception",
    "course-scheduling-common/src/main/java/com/paike/common/config",
    "course-scheduling-common/src/main/java/com/paike/common/entity",
    "course-scheduling-common/src/main/java/com/paike/common/enums",
    "course-scheduling-common/src/main/resources",
    "course-scheduling-common/src/test/java",

    # admin模块
    "course-scheduling-admin/src/main/java/com/paike/admin/controller",
    "course-scheduling-admin/src/main/java/com/paike/admin/service",
    "course-scheduling-admin/src/main/java/com/paike/admin/service/impl",
    "course-scheduling-admin/src/main/java/com/paike/admin/mapper",
    "course-scheduling-admin/src/main/java/com/paike/admin/dto",
    "course-scheduling-admin/src/main/java/com/paike/admin/vo",
    "course-scheduling-admin/src/main/resources",
    "course-scheduling-admin/src/test/java",

    # algorithm模块
    "course-scheduling-algorithm/src/main/java/com/paike/algorithm/greedy",
    "course-scheduling-algorithm/src/main/java/com/paike/algorithm/genetic",
    "course-scheduling-algorithm/src/main/java/com/paike/algorithm/model",
    "course-scheduling-algorithm/src/main/java/com/paike/algorithm/utils",
    "course-scheduling-algorithm/src/main/resources",
    "course-scheduling-algorithm/src/test/java",

    # adapter模块
    "course-scheduling-adapter/src/main/java/com/paike/adapter/config",
    "course-scheduling-adapter/src/main/java/com/paike/adapter/parser",
    "course-scheduling-adapter/src/main/java/com/paike/adapter/sync",
    "course-scheduling-adapter/src/main/resources",
    "course-scheduling-adapter/src/test/java"
)

foreach ($dir in $backendDirs) {
    $path = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $path | Out-Null
    Write-Host "Created: $path"
}

# 2. 创建前端Vue3项目结构
$frontendDirs = @(
    # 源代码目录
    "course-scheduling-web/src/api",
    "course-scheduling-web/src/assets",
    "course-scheduling-web/src/components/TimetableGrid",
    "course-scheduling-web/src/components/ConflictMarker",
    "course-scheduling-web/src/components/StatisticChart",
    "course-scheduling-web/src/components/Layout",
    "course-scheduling-web/src/views/login",
    "course-scheduling-web/src/views/admin/dashboard",
    "course-scheduling-web/src/views/admin/schedule",
    "course-scheduling-web/src/views/admin/adjustment",
    "course-scheduling-web/src/views/admin/statistics",
    "course-scheduling-web/src/views/admin/settings",
    "course-scheduling-web/src/views/teacher/timetable",
    "course-scheduling-web/src/views/teacher/adjustment",
    "course-scheduling-web/src/views/student/timetable",
    "course-scheduling-web/src/router",
    "course-scheduling-web/src/store",
    "course-scheduling-web/src/utils",
    "course-scheduling-web/src/styles",
    "course-scheduling-web/public"
)

foreach ($dir in $frontendDirs) {
    $path = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $path | Out-Null
    Write-Host "Created: $path"
}

# 3. 创建数据库脚本目录
$dbDirs = @(
    "database/mysql",
    "database/redis",
    "database/migration"
)

foreach ($dir in $dbDirs) {
    $path = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $path | Out-Null
    Write-Host "Created: $path"
}

# 4. 创建文档目录
$docDirs = @(
    "docs/requirements",
    "docs/design",
    "docs/api",
    "docs/deployment",
    "docs/manual"
)

foreach ($dir in $docDirs) {
    $path = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $path | Out-Null
    Write-Host "Created: $path"
}

# 5. 创建部署配置目录
$deployDirs = @(
    "deploy/docker",
    "deploy/nginx",
    "deploy/scripts"
)

foreach ($dir in $deployDirs) {
    $path = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $path | Out-Null
    Write-Host "Created: $path"
}

Write-Host "`n项目结构创建完成！"
