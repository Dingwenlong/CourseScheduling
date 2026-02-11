# 高校智能排课系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue-3.4.0-brightgreen.svg" alt="Vue"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Redis-7.0-red.svg" alt="Redis"/>
  <img src="https://img.shields.io/badge/JDK-17-orange.svg" alt="JDK"/>
</p>

## 项目简介

高校智能排课系统是一款基于 **Spring Boot + Vue3** 开发的智能排课管理平台，采用 **贪心算法 + 遗传算法** 混合优化策略，实现高校课程表的自动化编排。系统支持多校区、多约束条件的复杂排课场景，可大幅提升排课效率，降低冲突率。

### 核心特性

- 🤖 **智能算法**: 贪心算法快速初始化 + 遗传算法迭代优化
- 🏫 **多校区支持**: 支持跨校区排课，自动计算通勤时间
- 📊 **可视化展示**: 课表可视化、资源利用率统计图表
- 🔄 **动态调整**: 支持一键调课、拖拽调整、实时冲突检测
- 🔐 **权限管理**: 基于RBAC的细粒度权限控制
- 🔌 **数据对接**: 支持正方、青果等主流教务系统数据导入

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 主框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| JWT | 0.12.x | Token认证 |
| Redis | 7.x | 缓存/分布式锁 |
| MySQL | 8.0 | 主数据库 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Element Plus | 2.5.x | UI组件库 |
| Pinia | 2.1.x | 状态管理 |
| ECharts | 5.4.x | 数据可视化 |
| Axios | 1.6.x | HTTP请求 |

## 项目结构

```
course-scheduling-system/
├── course-scheduling-common/          # 公共模块
│   ├── constants/                     # 常量定义
│   ├── utils/                         # 工具类
│   ├── result/                        # 统一响应
│   └── exception/                     # 全局异常
│
├── course-scheduling-admin/           # 管理端模块
│   ├── controller/                    # 控制器
│   ├── service/                       # 业务逻辑
│   ├── mapper/                        # 数据访问
│   └── dto/                           # 数据传输对象
│
├── course-scheduling-algorithm/       # 算法引擎模块
│   ├── greedy/                        # 贪心算法
│   ├── genetic/                       # 遗传算法
│   ├── model/                         # 算法模型
│   └── utils/                         # 算法工具
│
├── course-scheduling-adapter/         # 数据适配模块
│   ├── config/                        # 适配配置
│   ├── parser/                        # 数据解析
│   └── sync/                          # 数据同步
│
├── course-scheduling-web/             # Web前端
│   ├── src/
│   │   ├── views/                     # 页面视图
│   │   ├── components/                # 组件
│   │   ├── api/                       # API接口
│   │   └── store/                     # 状态管理
│   └── package.json
│
├── database/                          # 数据库脚本
│   ├── mysql/                         # MySQL脚本
│   └── redis/                         # Redis配置
│
├── docs/                              # 项目文档
│   ├── requirements/                  # 需求文档
│   ├── design/                        # 设计文档
│   └── api/                           # API文档
│
└── deploy/                            # 部署配置
    ├── docker/                        # Docker配置
    └── nginx/                         # Nginx配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+

### 后端部署

1. **创建数据库**

```bash
mysql -u root -p < database/mysql/init-schema.sql
```

2. **修改配置**

编辑 `course-scheduling-admin/src/main/resources/application-dev.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_scheduling?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

3. **编译运行**

```bash
# 编译项目
mvn clean install

# 运行应用
cd course-scheduling-admin
mvn spring-boot:run
```

后端服务将启动在 `http://localhost:8080`

### 前端部署

1. **安装依赖**

```bash
cd course-scheduling-web
npm install
```

2. **启动开发服务器**

```bash
npm run dev
```

前端服务将启动在 `http://localhost:3000`

3. **构建生产环境**

```bash
npm run build
```

## 系统功能

### 教务管理员功能

- 📅 **智能排课**: 配置约束条件，一键生成课表
- 🔄 **排课调整**: 手动拖拽调整、一键调课
- 📊 **数据统计**: 教室利用率、课程分布可视化
- ⚙️ **系统设置**: 基础数据管理、权限配置

### 教师功能

- 📋 **个人课表**: 查看个人授课安排
- 📝 **调课申请**: 提交调课申请、查看审核状态

### 学生功能

- 📚 **班级课表**: 查看班级课程安排
- ⭐ **课程反馈**: 对课程时段进行评分反馈

## 算法说明

### 混合排课算法

系统采用 **贪心算法 + 遗传算法** 的混合策略：

1. **贪心算法阶段**: 快速生成满足硬性约束的初始可行解
2. **遗传算法阶段**: 对初始解进行迭代优化，提升整体质量

### 约束条件

**硬性约束** (必须满足):
- 教师时间不冲突
- 教室不冲突
- 班级时间不冲突
- 教室容量匹配

**软性约束** (尽量满足):
- 核心课程优先上午
- 减少跨校区通勤
- 教师时间偏好
- 课程分布均匀

## 开发计划

| 阶段 | 周期 | 主要任务 |
|------|------|---------|
| 第一阶段 | 第1-2周 | 需求分析、架构设计 |
| 第二阶段 | 第3-4周 | 后端基础框架搭建 |
| 第三阶段 | 第5-7周 | 核心算法开发 |
| 第四阶段 | 第8-10周 | 排课业务功能开发 |
| 第五阶段 | 第11-12周 | 前端界面开发 |
| 第六阶段 | 第13-14周 | 数据对接、统计功能 |
| 第七阶段 | 第15-16周 | 系统测试、性能优化 |
| 第八阶段 | 第17-18周 | 部署上线、文档编写 |

## 预期效果

| 指标 | 目标值 |
|------|--------|
| 排课周期 | ≤2天 |
| 调课响应 | ≤10秒 |
| 冲突率 | ≤5% |
| 教室利用率 | ≥85% |
| 系统可用性 | ≥99.5% |

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。

## 许可证

本项目采用 [MIT](LICENSE) 许可证。

## 联系方式

如有问题或建议，请联系项目维护团队。

---

<p align="center">Made with ❤️ for Education</p>
