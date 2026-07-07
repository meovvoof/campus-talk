# CampusTalk 校园社区内容互动平台

CampusTalk 是一个面向校园社区场景的前后端分离内容互动平台，核心业务覆盖用户注册登录、发帖评论、点赞收藏、内容搜索、后台治理、图片存储、实时天气和校园 AI 问答。项目分为客户端和管理端：客户端面向学生社区交流，管理端面向用户管理、帖子管理和内容审核。

## 功能模块

### 客户端

- 用户注册、登录、退出登录、密码重置、邮箱修改
- 用户资料、头像上传、隐私信息设置
- 帖子列表、帖子详情、发帖编辑、评论回复
- 点赞、收藏、我的收藏、帖子搜索
- 图片上传与代理访问
- 实时天气展示
- AI 助手流式问答

### 管理端

- 用户列表、用户详情、封禁和禁言管理
- 帖子列表、帖子置顶、锁定、隐藏和删除
- 帖子分类管理
- 违禁词管理
- 邮件发送记录和失败补发

## 技术栈

### 后端

- Spring Boot 3
- Spring Security
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- MinIO
- Elasticsearch
- Canal
- Spring AI
- SSE

### 前端

- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

## 项目结构

```text
.
├── database.sql                 # 数据库初始化脚本
├── my-project-backend/          # Spring Boot 后端服务
│   ├── src/main/java/com/example
│   │   ├── config/              # 安全、消息队列、对象存储等配置
│   │   ├── controller/          # 客户端与管理端接口
│   │   ├── entity/              # DTO、VO、请求响应对象
│   │   ├── filter/              # 鉴权、限流、跨域、日志过滤器
│   │   ├── listener/            # RabbitMQ 消息监听
│   │   ├── mapper/              # MyBatis-Plus Mapper
│   │   ├── service/             # 业务接口与实现
│   │   └── utils/               # JWT、限流、雪花 ID、违禁词等工具
│   └── src/main/resources
└── my-project-frontend/         # Vue 前端项目
    └── src
        ├── router/              # 前端路由
        ├── stores/              # Pinia 状态管理
        ├── utils/               # 请求封装与 Delta 内容工具
        └── views/               # 登录、论坛、后台、设置页面
```

## 本地运行

### 环境要求

- JDK 17
- Maven 3.8+
- Node.js 16+
- MySQL 8+
- Redis
- RabbitMQ
- MinIO
- Elasticsearch
- Canal

### 初始化数据库

先创建数据库，然后执行根目录的 `database.sql`：

```sql
source database.sql;
```

### 后端配置

后端使用 Maven profile 加载环境配置，默认激活 `dev`：

```yaml
spring:
  profiles:
    active: '@environment@'
```

本地运行前需要准备：

- `my-project-backend/src/main/resources/application-dev.yml`，可参考`my-project-backend/src/main/resources/application-test.yml`的内容
- MySQL、Redis、RabbitMQ、MinIO、Elasticsearch、邮件服务、天气服务、AI 服务相关配置

### 启动后端

```bash
mvn -f my-project-backend/pom.xml spring-boot:run
```

### 启动前端

```bash
cd my-project-frontend
npm install
npm run dev
```
