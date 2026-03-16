# 智能排课系统

基于 Spring Boot + Vue 3 的智能排课系统，支持贪心算法和遗传算法进行课程调度优化。

## 技术栈

### 后端
- Java 17+
- Spring Boot 3.4
- MyBatis Plus
- MySQL 8.0
- Redis
- JWT认证

### 前端
- Vue 3
- Vant 4 (移动端UI)
- Vite
- Pinia
- Axios

## 项目结构

```
course-scheduling/
├── course-scheduling-common/      # 公共模块
├── course-scheduling-algorithm/   # 算法模块
├── course-scheduling-adapter/     # 适配模块
├── course-scheduling-admin/       # 管理端模块
├── course-scheduling-web/         # 前端项目
├── docs/                          # 文档
│   └── sql/                       # 数据库脚本
├── scripts/                       # 启动脚本
└── docker-compose.yml             # Docker配置
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Docker Desktop (包含MySQL和Redis)
- Maven 3.8+

### 一键启动

```bash
# 启动所有服务（Docker + 数据库初始化 + 后端 + 前端）
scripts\start-all.bat

# 停止所有服务
scripts\stop-all.bat
```

如果之前启动过其他 MySQL 容器并保留了旧数据卷，导致 root 密码和脚本里的 `root123456` 不一致，先执行 `docker-compose down -v` 再重新启动。

启动成功后访问地址：

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:3000 |
| 后端API | http://localhost:8080 |
| API文档 | http://localhost:8080/doc.html |

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher001 | 123456 |
| 学生 | student001 | 123456 |

### Docker服务信息

| 服务 | 地址 | 用户名 | 密码 |
|------|------|--------|------|
| MySQL | localhost:3306 | root | root123456 |
| Redis | localhost:6379 | - | - |

## 手动启动（可选）

### 1. 启动Docker容器

```bash
docker-compose up -d
```

### 2. 启动后端

```bash
# 首次启动前先初始化数据库
scripts\init-database.bat

# 再启动后端
cd course-scheduling-admin
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd course-scheduling-web
npm install
npm run dev
```

## 功能特性

### 排课算法
- **贪心算法**：按优先级排序任务，依次分配最优时间槽和教室
- **遗传算法**：通过选择、交叉、变异操作迭代寻找最优解

### 约束处理
- 教师时间冲突检测
- 教室占用冲突检测
- 班级时间冲突检测

### 业务功能
- 课表生成与管理
- 教学任务管理
- 课表查询（按班级/教师/教室）
- 调课申请与审批
- 统计分析

## 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_scheduling
    username: root
    password: root123456
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-secret-key
  expiration: 86400000

algorithm:
  genetic:
    population-size: 100
    max-generations: 500
```

### 前端配置 (vite.config.js)

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## API接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /auth/login | 用户登录 |
| 课表 | POST /timetable/generate | 生成课表 |
| 课表 | GET /timetable/{id}/details | 查询课表明细 |
| 任务 | GET /task/page | 分页查询任务 |
| 排课 | POST /schedule/execute | 执行排课 |
| 调课 | POST /adjustment/execute | 执行调课 |
| 统计 | GET /statistics/conflict-report/{id} | 冲突报告 |

## 构建部署

### 后端构建

```bash
mvn clean package -DskipTests
java -jar course-scheduling-admin/target/course-scheduling-admin-1.0.0-SNAPSHOT.jar
```

### 前端构建

```bash
cd course-scheduling-web
npm run build
# 部署dist目录到Web服务器
```

## 许可证

MIT License
