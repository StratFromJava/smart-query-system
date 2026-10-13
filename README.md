# SmartQuery 智能问答系统

基于 **Spring Boot 3 + Spring AI Alibaba（通义千问）** 与 **Vue 3 + Element Plus** 构建的前后端分离智能对话系统。

参照通义千问的交互体验：SSE 流式打字机输出、Markdown 富文本渲染、会话云端留存与多模型切换；后端采用 Gradle 多模块分层，并通过 Spring Security + JWT 完成用户注册登录与数据隔离；同时为知识库 RAG、联网搜索、文件/图片理解三类扩展能力预留了完整接入点。

---

## 一、功能特性

### 已实现

| 能力 | 说明 |
| --- | --- |
| 流式对话 | `POST /api/chat/stream` 返回 `text/event-stream`，前端逐字打字机渲染，可随时「停止生成」 |
| Markdown 渲染 | 代码块语法高亮 + 一键复制，输出经 DOMPurify 净化，禁用原始 HTML（防 XSS） |
| 多模型切换 | `qwen-plus` / `qwen-max` / `qwen-turbo` 下拉切换，模型清单由配置驱动，可热增删 |
| 会话管理 | 新建、重命名、删除、按标题搜索；首条提问自动生成会话标题 |
| 历史上下文 | 每次请求携带最近 N 条历史消息（默认 20 条），保证多轮对话连贯 |
| 用户体系 | 注册 + 登录，BCrypt 加密存储，无状态 JWT 鉴权 |
| 数据隔离 | 会话与消息均绑定 `user_id`，越权访问统一返回 404 |
| 主题切换 | 浅色 / 深色主题，Element Plus 与自定义样式同步切换 |

### 预留（接入点已就绪，默认关闭）

| 能力 | 状态 | 说明 |
| --- | --- | --- |
| 知识库 RAG | 预留 | 开启后自动检索向量库并注入 System Prompt，默认内存 `SimpleVectorStore` |
| 联网搜索 | 预留 | `@Tool` 工具已定义，接入真实搜索服务即可被模型调用 |
| 文件/图片理解 | 预留 | 多模态消息构造入口已就绪，需配合 `qwen-vl-max` 等视觉模型 |

前端对未实现的能力统一展示「即将开放」，不影响主链路使用。

---

## 二、技术栈

| 层级 | 选型 |
| --- | --- |
| 构建 | Gradle 8.7（Groovy DSL，多模块） |
| JDK | Java 21（Gradle Toolchain 强制指定） |
| 后端 | Spring Boot 3.4.5 · Spring AI 1.0.0 · Spring AI Alibaba 1.0.0.2 |
| 鉴权 | Spring Security 6 + JJWT 0.12.6（BCrypt + 无状态 Token） |
| 数据 | MySQL 8 + MyBatis-Plus 3.5.9（逻辑删除 + 时间自动填充） |
| 前端 | Vue 3.4 + Vite 5 + Element Plus 2.8 + Pinia + Vue Router 4 |
| 渲染 | markdown-it + highlight.js + DOMPurify + github-markdown-css |

---

## 三、架构设计

### 3.1 模块依赖

模块依赖严格单向，禁止反向依赖与循环依赖：

```
                        ┌──────────────────────────────────┐
                        │   sqs-web   （启动模块 / 成品 jar） │
                        │  Controller · 全局异常 · CORS · yml │
                        └───────────────┬──────────────────┘
                                        │
                 ┌──────────────────────┴──────────────────────┐
                 ▼                                             ▼
   ┌────────────────────────────┐            ┌────────────────────────────┐
   │  sqs-security              │            │  sqs-ai                    │
   │  SecurityConfig            │            │  ChatService（对话编排）     │
   │  JwtTokenProvider / Filter │            │  ModelRegistry             │
   │  AuthService / Controller  │            │  RAG · WebSearch · 多模态   │
   └─────────────┬──────────────┘            └─────────────┬──────────────┘
                 │                                         │
                 └──────────────────┬──────────────────────┘
                                    ▼
                 ┌──────────────────────────────────┐
                 │  sqs-core                        │
                 │  Entity · Mapper · Service · VO   │
                 └──────────────────┬───────────────┘
                                    ▼
                 ┌──────────────────────────────────┐
                 │  sqs-common                      │
                 │  R · ResultCode · BizException    │
                 │  FeatureType（能力枚举）            │
                 └──────────────────────────────────┘
```

即：`sqs-web → {sqs-ai, sqs-security} → sqs-core → sqs-common`

### 3.2 请求链路（以流式对话为例）

```
浏览器 (Vue3)
   │  ① POST /api/chat/stream  {sessionId, content, model, features}
   │     Authorization: Bearer <jwt>   Accept: text/event-stream
   ▼
Vite Dev Server (:5173)
   │  ② /api 代理转发（compress:false，保证分片实时到达）
   ▼
JwtAuthenticationFilter          → 解析 Token，装载 LoginUser 到 SecurityContext
   ▼
ChatController                   → 取当前 userId，调用 ChatService
   ▼
ChatService                      ③ 解析/创建会话 → 校验归属
   │                             ④ 落库用户消息
   │                             ⑤ 加载最近 N 条历史（ConversationHistoryLoader）
   │                             ⑥ 可选：RAG 检索注入 System Prompt
   │                             ⑦ ChatClient.stream() → DashScope（通义千问）
   │                             ⑧ 增量文本 → Flux<String> → SSE 帧
   │                             ⑨ 流结束后整体落库助手消息 + 自动生成会话标题
   ▼
MySQL (chat_session / chat_message)
```

### 3.3 前端结构

```
ChatView
├── AppSidebar    ← 折叠侧边栏，底色品牌渐变
│   ├── SessionList   ← 会话列表 / 搜索 / 新建 / 重命名 / 删除
│   └── FeaturePanel  ← 预留能力入口（标注「即将开放」）
├── ChatHeader    ← 会话标题、模型切换下拉、主题切换、用户菜单
├── MessageList   ← 消息流、空状态推荐卡片、自动滚动 / 回到最新
│   └── MessageBubble → MarkdownRenderer（高亮 + 复制 + 重新生成）
└── ChatInput     ← 自适应高度、Enter 发送 / Shift+Enter 换行、停止生成
```

---

## 四、目录结构

```
smart-query-system/
├── build.gradle                    # 根工程：插件、BOM、版本统一管理
├── settings.gradle                 # 声明 5 个子模块
├── gradle.properties               # JDK 21 Toolchain、Gradle 内存与并行参数
├── sql/
│   └── schema.sql                  # 建库建表脚本（含预置 admin 账号）
│
├── sqs-common/                     # 基础层：无业务依赖
│   └── .../common/
│       ├── result/          R · ResultCode        统一响应封装与状态码
│       ├── exception/       BizException          业务异常
│       └── enums/           FeatureType           能力枚举（驱动前端开关）
│
├── sqs-core/                       # 领域层：实体、Mapper、Service
│   └── .../core/
│       ├── entity/          SysUser · ChatSession · ChatMessage
│       ├── mapper/          SysUserMapper · ChatSessionMapper · ChatMessageMapper
│       ├── service/         接口 + impl（强制 userId 过滤）
│       ├── vo/              ChatRequest · SessionVO · MessageVO · ChatReplyVO · FeatureVO ...
│       ├── enums/           MessageRole（user / assistant / system）
│       └── config/          MybatisPlusConfig（逻辑删除、时间自动填充）
│
├── sqs-ai/                         # AI 层：对话编排与扩展能力
│   └── .../ai/
│       ├── config/          DashScopeConfig        ChatClient Bean
│       ├── service/         ChatService            流式/非流式对话编排
│       │                    ConversationHistoryLoader  历史上下文加载
│       ├── model/           ModelRegistry          模型清单与合法性校验
│       ├── properties/      AiProperties           sqs.ai.* 配置绑定
│       ├── rag/             VectorStoreConfig · RagService · DocumentIngestService
│       ├── tool/            WebSearchTool          @Tool 联网搜索
│       └── multimodal/      MultimodalService      多模态接入点
│
├── sqs-security/                   # 安全层：鉴权与账号
│   └── .../security/
│       ├── SecurityConfig          Security 过滤链、BCrypt、CORS
│       ├── JwtTokenProvider        Token 签发与解析
│       ├── JwtAuthenticationFilter Token → SecurityContext
│       ├── SecurityUtils           currentUserId() 全局取当前用户
│       ├── service/AuthService     注册 / 登录 / 查询当前用户
│       ├── controller/AuthController
│       ├── entity/LoginUser        UserDetails 实现
│       └── vo/              LoginRequest · RegisterRequest · AuthVO
│
├── sqs-web/                        # 启动层：唯一可执行模块
│   └── .../web/
│       ├── SmartQueryApplication   启动类
│       ├── controller/     ChatController · SessionController · MetaController
│       ├── config/         GlobalExceptionHandler · WebMvcConfig（CORS）
│       ├── dto/            StreamEvent（SSE 帧结构）
│       └── resources/      application.yml
│
└── web/                            # 前端工程（Vite 独立构建，不走 Gradle）
    ├── index.html
    ├── vite.config.js              # :5173，/api 代理到 8080
    ├── package.json
    └── src/
        ├── main.js                 入口，全局注册 Element Plus 与图标
        ├── App.vue                 主题初始化
        ├── router/                 路由 + 登录守卫
        ├── views/          LoginView · RegisterView · ChatView · PlaceholderView
        ├── components/
        │   ├── layout/     AppSidebar · ChatHeader · SessionList
        │   ├── chat/       MessageList · MessageBubble · MarkdownRenderer · ChatInput
        │   └── feature/    FeaturePanel
        ├── stores/         user · app（主题/模型/能力）· session · chat
        ├── api/            request（axios 封装）· auth · chat · session · meta
        ├── utils/          sse（流式读取）· markdown（渲染与净化）
        └── styles/         variables · index · dark
```

---

## 五、各模块职责

| 模块 | 职责 | 对外暴露 | 依赖 |
| --- | --- | --- | --- |
| `sqs-common` | 统一响应 `R`、状态码、业务异常、能力枚举 | `R`、`ResultCode`、`BizException`、`FeatureType` | 无 |
| `sqs-core` | 数据建模与持久化，所有查询强制带 `userId` | Entity / Mapper / Service / VO | `sqs-common` |
| `sqs-ai` | 模型调用与对话编排，扩展能力（RAG / 搜索 / 多模态） | `ChatService`、`ModelRegistry` | `sqs-core` |
| `sqs-security` | 注册登录、JWT 签发校验、Security 过滤链 | `SecurityUtils.currentUserId()`、`AuthController` | `sqs-core` |
| `sqs-web` | HTTP 入口、全局异常、CORS、启动类、配置 | REST 接口 | `sqs-ai` + `sqs-security` |

> 只有 `sqs-web` 应用了 `org.springframework.boot` 插件并产出 `bootJar`，其余模块均为 `java-library`。

---

## 六、环境准备

| 依赖 | 版本要求 | 备注 |
| --- | --- | --- |
| JDK | **21**（必需） | Spring Boot 3 与 Spring AI 均要求 17+；项目已在 `gradle.properties` 指定 Toolchain |
| Gradle | 8.x | 项目不含 wrapper，使用本机 `gradle` 命令即可 |
| Node.js | 18+ | 推荐 20 / 22 LTS |
| MySQL | 8.x | 需支持 `utf8mb4` |
| DashScope API Key | — | 阿里云百炼控制台申请 |

**关于 JDK**：若本机默认 `JAVA_HOME` 是 JDK 8（常见情况），本项目通过 `gradle.properties` 显式指定 Gradle 运行在 JDK 21 上，无需改动系统环境变量：

```properties
org.gradle.java.home=C:/Program Files/Java/jdk-21
org.gradle.java.installations.paths=C:/Program Files/Java/jdk-21
```

若你的 JDK 21 不在该路径，修改为实际路径即可。

---

## 七、快速开始

### 步骤 1：初始化数据库

```bash
mysql -u root -p < sql/schema.sql
```

脚本创建 `smart_query` 库及 `sys_user`、`chat_session`、`chat_message` 三张表，并预置账号：

| 用户名 | 密码 |
| --- | --- |
| `admin` | `admin123` |

### 步骤 2：配置环境变量

后端敏感信息全部通过环境变量注入，也可直接修改 `sqs-web/src/main/resources/application.yml`。

**必填：**

```powershell
$env:AI_DASHSCOPE_API_KEY = "sk-xxxxxxxxxxxxxxxx"
```

**可选（均有默认值）：**

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 数据库地址 | `localhost` / `3306` / `smart_query` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `root` / `root` |
| `SQS_JWT_SECRET` | JWT 密钥，长度需 ≥ 32 字节 | 开发用默认值（**上线必须覆盖**） |

### 步骤 3：启动后端

```bash
gradle :sqs-web:bootRun
```

或打包运行：

```bash
gradle :sqs-web:bootJar
java -jar sqs-web/build/libs/smart-query-system.jar
```

默认端口 `8080`，健康检查：<http://localhost:8080/actuator/health>

### 步骤 4：启动前端

```bash
cd web
npm install
npm run dev
```

访问 <http://localhost:5173>，开发期已配置 `/api` 代理到 `8080`，无需处理跨域。

用 `admin / admin123` 登录，或注册新账号开始对话。

---

## 八、配置项说明

### 8.1 业务配置（`sqs.*`）

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `sqs.security.jwt.secret` | 开发默认值 | JWT 密钥，生产必须通过 `SQS_JWT_SECRET` 覆盖，长度 ≥ 32 字节 |
| `sqs.security.jwt.expiration-hours` | `12` | Token 有效期（小时） |
| `sqs.ai.default-model` | `qwen-plus` | 默认模型，请求未指定或指定非法时回退到此值 |
| `sqs.ai.temperature` | `0.7` | 采样温度 |
| `sqs.ai.history-limit` | `20` | 每次请求携带的历史消息条数 |
| `sqs.ai.system-prompt` | 内置中文提示词 | 系统人设与输出格式约束 |
| `sqs.ai.models[].code` | qwen-plus / qwen-max / qwen-turbo | 模型清单，增删即生效（前端下拉自动同步） |
| `sqs.ai.models[].label` | 中文名 | 前端展示名 |
| `sqs.ai.models[].multimodal` | `false` | 是否支持多模态输入 |
| `sqs.ai.rag.enabled` | `false` | 知识库 RAG 总开关 |
| `sqs.ai.rag.top-k` | `4` | 向量检索返回条数 |
| `sqs.ai.web-search.enabled` | `false` | 联网搜索工具开关 |

示例：新增一个模型只需在 yml 中追加一项，无需改代码：

```yaml
sqs:
  ai:
    models:
      - code: qwen-plus
        label: 通义千问 Plus
        multimodal: false
      - code: qwen-vl-max
        label: 通义千问 VL Max
        multimodal: true
```

### 8.2 其他配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 服务端口 |
| `spring.ai.dashscope.api-key` | `${AI_DASHSCOPE_API_KEY:}` | DashScope Key |
| `spring.datasource.hikari.*` | 最大 20 连接 | 连接池 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | `deleted` | 全局逻辑删除字段 |
| `management.endpoints.web.exposure.include` | `health,info` | Actuator 暴露端点 |

### 8.3 前端配置

`web/vite.config.js`：

| 配置 | 值 | 说明 |
| --- | --- | --- |
| `server.port` | `5173` | 开发端口 |
| `server.proxy['/api'].target` | `http://localhost:8080` | 后端地址，改动后端端口时同步修改 |
| `server.proxy['/api'].compress` | `false` | **SSE 必需**，关闭压缩避免流式分片被缓冲 |

---

## 九、数据库表

三张表均使用 `deleted` 字段做逻辑删除（`0` 未删除 / `1` 已删除），由 MyBatis-Plus 全局配置自动处理。

### `sys_user` 用户表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 主键 |
| `username` | VARCHAR(64) | 用户名，唯一索引 `uk_username` |
| `password` | VARCHAR(128) | BCrypt 加密后的密码 |
| `nickname` | VARCHAR(64) | 昵称 |
| `avatar` | VARCHAR(255) | 头像地址 |
| `status` | TINYINT | 1-启用 / 0-禁用 |
| `create_time` / `update_time` | DATETIME | 创建 / 更新时间 |
| `deleted` | TINYINT | 逻辑删除 |

### `chat_session` 会话表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 主键 |
| `user_id` | BIGINT | 所属用户，索引 `idx_user_update`、`idx_user_title` |
| `title` | VARCHAR(200) | 会话标题，默认「新对话」 |
| `model` | VARCHAR(64) | 该会话最近使用的模型 |
| `create_time` / `update_time` | DATETIME | 创建 / 更新时间（列表按 `update_time` 倒序） |
| `deleted` | TINYINT | 逻辑删除 |

### `chat_message` 消息表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 主键 |
| `session_id` | BIGINT | 会话 ID，索引 `idx_session_create` |
| `user_id` | BIGINT | 冗余存储，便于统计与二次隔离，索引 `idx_user_session` |
| `role` | VARCHAR(16) | `user` / `assistant` / `system` |
| `content` | TEXT | 消息内容 |
| `model` | VARCHAR(64) | 生成该消息的模型 |
| `tokens` | INT | 消耗 token 数（当前未回填，预留） |
| `create_time` | DATETIME | 创建时间 |
| `deleted` | TINYINT | 逻辑删除 |

---

## 十、接口文档

### 10.1 统一响应体

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1728000000000
}
```

> SSE 流式接口**不套用**该封装，直接下发事件帧。

响应码（`ResultCode`）：

| code | 含义 | code | 含义 |
| --- | --- | --- | --- |
| `200` | 成功 | `1001` | 用户名已被注册 |
| `400` | 请求参数错误 | `1002` | 用户名或密码错误 |
| `401` | 未登录或登录已失效 | `1003` | 账号已被禁用 |
| `403` | 没有访问权限 | `2001` | AI 服务调用失败 |
| `404` | 资源不存在 | `2002` | 暂不支持该模型 |
| | | `2003` | 该功能正在建设中 |
| | | `5000` | 系统内部错误 |

### 10.2 认证接口（无需 Token）

| 方法 | 路径 | 请求体 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | `{username, password, nickname?}` | 注册，用户名 4-20 位字母/数字/下划线，密码 6-32 位，昵称 ≤ 20 字符 |
| POST | `/api/auth/login` | `{username, password}` | 登录，返回 `{token, tokenType, expiresIn, user}` |
| GET | `/api/auth/me` | — | 当前登录用户信息（**需 Token**） |

### 10.3 会话接口（需 Token）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/session?keyword=` | 会话列表；带 `keyword` 时按标题模糊搜索 |
| POST | `/api/session` | 新建会话，body 可选 `{title, model}` |
| GET | `/api/session/{sessionId}/messages` | 该会话的历史消息 |
| PATCH | `/api/session/{sessionId}/title` | 重命名，body `{title}` |
| DELETE | `/api/session/{sessionId}` | 删除会话（逻辑删除） |

### 10.4 对话接口（需 Token）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/chat/stream` | **SSE 流式对话**，主链路 |
| POST | `/api/chat` | 非流式对话（预留），一次性返回完整内容 |

请求体：

```json
{
  "sessionId": 12,
  "content": "用 Java 写一个单例",
  "model": "qwen-plus",
  "features": ["rag"]
}
```

- `sessionId` 为空时后端自动创建新会话
- `model` 为空或非法时回退到 `sqs.ai.default-model`
- `features` 取值见 `FeatureType.code`：`multi_model` / `rag` / `web_search` / `multimodal`

### 10.5 元数据接口

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/models` | 需 Token | 可选模型列表 `[{code,label,multimodal}]` |
| GET | `/api/models/default` | 需 Token | 默认模型 code |
| GET | `/api/features` | **公开** | 能力清单 `[{code,name,desc,implemented}]`，驱动前端开关显隐 |

### 10.6 SSE 协议

`POST /api/chat/stream` 返回 `text/event-stream`，每帧一个 JSON 对象：

```
data: {"type":"delta","content":"增量文本"}

data: {"type":"delta","content":"继续追加"}

data: {"type":"done"}

```

| `type` | 含义 |
| --- | --- |
| `delta` | 一段增量文本，前端追加到当前助手消息 |
| `done` | 流结束，此时助手消息已完整落库 |
| `error` | 调用失败，`content` 为错误描述 |

未登录或参数校验失败时，由 Spring Security / 全局异常处理器返回统一响应体 JSON（HTTP 200 + `code` 非 200），前端会识别并提示。

### 10.7 鉴权说明

放行（无需 Token）：`/api/auth/login`、`/api/auth/register`、`/api/features`、`/actuator/health`、`/actuator/info`。

其余 `/api/**` 均需携带：

```
Authorization: Bearer <token>
```

Token 由 `SecurityUtils.currentUserId()` 解析后注入业务层，所有会话与消息查询都强制带 `userId` 条件，越权访问返回 404（而非 403），避免会话 ID 枚举探测。

---

## 十一、预留能力接入指南

### 知识库 RAG

1. 开启开关：

   ```yaml
   sqs:
     ai:
       rag:
         enabled: true
         top-k: 4
   ```

2. 此时 `VectorStoreConfig` 生效，默认注册 Spring AI 内存实现 `SimpleVectorStore`。  
   生产环境替换为 Milvus / PGVector / AnalyticDB，只需提供一个新的 `VectorStore` Bean 覆盖即可。

3. 通过 `DocumentIngestService` 写入文档；请求携带 `features: ["rag"]` 时，`ChatService` 会自动检索并把资料片段拼进 System Prompt。

### 联网搜索

1. 开启开关：`sqs.ai.web-search.enabled=true`
2. `WebSearchTool`（`@Tool` 标注）自动注册为 Bean，在 `ChatService` 中通过 `ChatClient.prompt().tools(webSearchTool)` 挂载即可被模型自主调用。
3. 替换 `WebSearchTool#search` 的实现接入真实搜索服务（如阿里 IQS、Serper、Bing API）。

### 文件 / 图片理解

1. `MultimodalService` 提供校验与消息构造入口。
2. 在 yml 中注册支持视觉的模型（如 `qwen-vl-max`）并标记 `multimodal: true`。
3. 构造带 `Media` 的 `UserMessage`：

   ```java
   new UserMessage(text, List.of(new Media(MimeTypeUtils.IMAGE_PNG, url)))
   ```

### 多模型切换

已完全实现。模型清单由 `sqs.ai.models` 配置驱动，`ModelRegistry` 负责合法性校验与默认回退，前端下拉自动同步，新增模型无需改代码。

---

## 十二、常见问题

**Q1：Gradle 报 `Unsupported class file major version` 或插件加载失败？**

Spring Boot 3 的 Gradle 插件要求 JDK 17+。项目已在 `gradle.properties` 中指定 `org.gradle.java.home=C:/Program Files/Java/jdk-21`，若你的 JDK 21 不在该路径请修改此处，或在构建前执行：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
```

**Q2：启动报 `DashScope API key must be set`？**

未注入 `AI_DASHSCOPE_API_KEY`。该 Key 在 DashScope 模型 Bean 初始化时即被校验，属必填项，即使是本地冒烟测试也需填一个占位值。

**Q3：启动报 JWT 密钥长度不足 32 字节？**

设置 `SQS_JWT_SECRET` 为 32 字节以上的随机字符串。

**Q4：Gradle 无法下载插件（`plugins.gradle.org` 超时）？**

项目已改用 `buildscript` 从 Maven Central 加载插件，不依赖 Gradle 插件门户。若仍缓慢，可配置国内镜像源。

**Q5：流式输出卡顿或一次性返回？**

- 开发环境：确认走的是 Vite 代理（`:5173`），而非直连 `8080` 后跨域失败。
- 生产环境：Nginx 需关闭代理缓冲，见下节。

**Q6：登录后立刻跳回登录页？**

检查后端日志是否有 Token 解析异常。常见原因是重启后端后 `SQS_JWT_SECRET` 变化，导致旧 Token 失效，清除浏览器 `localStorage` 后重新登录即可。

**Q7：MySQL 连接失败 `Access denied` / 库不存在？**

确认已执行 `sql/schema.sql`，且 `DB_USERNAME` / `DB_PASSWORD` 与本机一致。

---

## 十三、部署说明

### 后端

```bash
gradle :sqs-web:bootJar
```

产物：`sqs-web/build/libs/smart-query-system.jar`

```bash
java -jar sqs-web/build/libs/smart-query-system.jar \
  --spring.profiles.active=prod
```

### 前端

```bash
cd web
npm run build
```

产物：`web/dist/`

### Nginx 反向代理（SSE 关键配置）

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;

    # SSE 必需：关闭缓冲与压缩，否则流式分片会被攒到一起再返回
    proxy_buffering off;
    proxy_cache off;
    gzip off;
    chunked_transfer_encoding on;
    proxy_read_timeout 3600s;
}

location / {
    root /var/www/smart-query-web;
    try_files $uri $uri/ /index.html;
}
```

### 生产环境检查清单

- [ ] 覆盖 `SQS_JWT_SECRET`（≥ 32 字节随机串）
- [ ] 使用独立数据库账号，最小权限
- [ ] API Key 仅通过环境变量注入，不写入仓库
- [ ] 关闭 Actuator 额外端点（默认仅暴露 `health` / `info`）
- [ ] 配置 HTTPS，避免 Token 明文传输

---

## 十四、安全说明

- **密码存储**：BCrypt 加密，明文密码不落库、不打印日志。
- **鉴权方式**：无状态 JWT，服务端不保存会话，密钥与有效期均可配。
- **数据隔离**：所有会话与消息查询强制带 `userId` 条件，越权访问统一返回 404，防止会话 ID 枚举探测。
- **XSS 防护**：AI 输出经 DOMPurify 净化后才渲染，Markdown 渲染禁用原始 HTML。
- **密钥管理**：API Key 与 JWT 密钥均通过环境变量注入，仓库中不含任何真实密钥。
- **CSRF**：无状态 Token 架构下已关闭 CSRF，Token 由前端在 `Authorization` 头中携带，不依赖 Cookie。

---

## 十五、后续规划

- [ ] 知识库 RAG 完整链路（文档上传、切片、向量化、按用户隔离知识库）
- [ ] 联网搜索接入真实搜索服务
- [ ] 多模态文件上传与图片理解
- [ ] Token 消耗统计与用量配额
- [ ] 消息重试 / 重新生成（前端入口已预留）
- [ ] 流式响应中断后的断点续传
- [ ] 用户个人中心与密码修改
