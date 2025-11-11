# 可云 AI 代码生成平台 - 项目分析文档

## 📋 项目概述

**项目名称**：keyun-autocode-backend（可云零代码生成平台 — 后端）

**项目定位**：基于 AI 的智能代码生成平台，用户通过自然语言描述需求，系统自动生成可运行的前端项目代码

**技术栈**：
- 后端框架：Spring Boot 3.5.4
- AI 框架：LangChain4j 1.1.0
- AI 模型：DeepSeek、通义千问（DashScope）、豆包（DouBao）
- 数据库：MySQL 8.x + MyBatis-Plus 3.5.12
- 缓存：Redis + Redisson + Caffeine（本地缓存）
- 认证授权：Sa-Token 1.44.0
- 对象存储：腾讯云 COS
- 构建工具：Maven + Vite（前端项目构建）
- API 文档：Knife4j 4.4.0
- 其他：Hutool、Lombok、Selenium（网页截图）

---

## 🏗️ 项目架构

### 1. 整体架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                                │
│                    (前端 Vue3 应用)                           │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/SSE
┌────────────────────▼────────────────────────────────────────┐
│                    Controller 层                             │
│  AppController | UserController | ChatHistoryController     │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    Service 层                                │
│       AppService | UserService | ChatHistoryService         │
└─────┬───────────────────────────────────────────────────────┘
      │
┌─────▼──────────────────────────────────────────────────────┐
│                 核心业务层 (Core)                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │        AiCodeGeneratorFacade (门面模式)               │  │
│  │  统一协调 AI 生成、代码解析、文件保存、项目构建       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌───────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ CodeParser    │  │ FileSaver    │  │ ProjectBuilder  │ │
│  │ (代码解析器)   │  │ (文件保存器)  │  │ (项目构建器)     │ │
│  └───────────────┘  └──────────────┘  └─────────────────┘ │
└─────┬──────────────────────────────────────────────────────┘
      │
┌─────▼──────────────────────────────────────────────────────┐
│                   AI 层 (LangChain4j)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │        AiCodeGeneratorServiceFactory                  │  │
│  │  (工厂模式 + 缓存) 创建和管理 AI 服务实例              │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐ │
│  │        AiCodeGeneratorService (接口)                   │ │
│  │  - generateHtmlCodeStream()      (单文件 HTML)        │ │
│  │  - generateMultiFileCodeStream() (多文件静态网站)      │ │
│  │  - generateVueProjectCodeStream() (Vue3 工程项目)     │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐ │
│  │        SimpleAiTaskService (智能路由服务)              │ │
│  │  - routeCodeGenType()       (自动选择生成类型)         │ │
│  │  - getInitialPrompt()       (生成应用名称)             │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌──────────────────────── AI Tools ─────────────────────┐ │
│  │  FileWriteTool  | FileReadTool | FileModifyTool       │ │
│  │  FileDeleteTool | FileDirReadTool                     │ │
│  │  (供 AI 在生成 Vue 项目时使用的工具集)                  │ │
│  └───────────────────────────────────────────────────────┘ │
└─────┬──────────────────────────────────────────────────────┘
      │
┌─────▼──────────────────────────────────────────────────────┐
│                   外部服务层                                 │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ DeepSeek   │  │ DashScope   │  │  DouBao            │ │
│  │ (推理模型)  │  │ (通义千问)   │  │  (豆包)             │ │
│  └────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                              │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   MySQL    │  │    Redis    │  │  腾讯云 COS         │ │
│  │  (持久化)   │  │  (对话记忆)  │  │  (截图存储)          │ │
│  └────────────┘  └─────────────┘  └─────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 核心功能模块

### 1. 代码生成模块

#### 1.1 三种生成模式

**① HTML 单文件模式**
- **适用场景**：简单的展示页面
- **生成内容**：单个 HTML 文件（内联 CSS 和 JS）
- **技术要求**：纯原生 HTML/CSS/JavaScript
- **AI 提示词模板**：`prompt/codegen-html-system-prompt.txt`

**② MULTI_FILE 多文件模式**
- **适用场景**：中等复杂度的静态网站
- **生成内容**：分离的 HTML、CSS、JS 文件
- **技术要求**：原生技术栈，文件分离
- **AI 提示词模板**：`prompt/codegen-multi-file-system-prompt.txt`

**③ VUE_PROJECT 工程项目模式**
- **适用场景**：复杂的现代化前端项目
- **生成内容**：完整的 Vue3 + Vite 工程项目
- **技术栈**：Vue 3.x + Vue Router 4.x + Vite
- **AI 提示词模板**：`prompt/codegen-vue-project-system-prompt.txt`
- **特殊能力**：AI 通过工具调用（Tool Calling）方式创建文件

#### 1.2 智能路由机制

```java
public interface SimpleAiTaskService {
    // AI 自动判断用户需求，返回最合适的生成类型
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
    
    // AI 总结用户需求，生成应用名称
    String getInitialPrompt(String userMessage);
}
```

**路由规则**：
- 简单展示页面 → `HTML`
- 多页面静态网站 → `MULTI_FILE`
- 复杂交互项目 → `VUE_PROJECT`

---

### 2. AI 服务管理

#### 2.1 多模型支持

| 模型 | 用途 | 配置 Bean |
|------|------|----------|
| DeepSeek Chat | HTML/多文件生成 | `chatStreamingModel` |
| DeepSeek Reasoner | Vue 项目生成（推理模型） | `reasoningStreamingModel` |
| 通义千问 Flash | 快速生成 | `qwFlashStreamingModel` |
| 通义千问 Max | 复杂生成 | `qwMaxLatestStreamingModel` |
| 豆包 | 备选模型 | `doubaoStreamingModel` |

#### 2.2 服务缓存与记忆管理

```java
// Caffeine 本地缓存策略
Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
    .maximumSize(1000)           // 最多缓存 1000 个实例
    .expireAfterWrite(30分钟)     // 写入后 30 分钟过期
    .expireAfterAccess(10分钟)    // 访问后 10 分钟过期
    .build();

// Redis 对话记忆
MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
    .id("chatMemory:" + appId)    // 每个应用独立记忆
    .chatMemoryStore(redisChatMemoryStore)
    .maxMessages(20)              // 保留最近 20 条消息
    .build();
```

#### 2.3 AI 工具调用（Tool Calling）

在 Vue 项目生成模式下，AI 可以调用以下工具：

| 工具名称 | 功能 | 实现类 |
|---------|------|--------|
| writeFile | 写入文件 | `FileWriteTool` |
| readFile | 读取文件 | `FileReadTool` |
| modifyFile | 修改文件 | `FileModifyTool` |
| deleteFile | 删除文件 | `FileDeleteTool` |
| listDirectory | 列出目录 | `FileDirReadTool` |

**工具上下文传递**：
```java
@Tool("写入文件到指定路径")
public String writeFile(
    @P("文件的相对路径") String relativeFilePath,
    @P("要写入文件的内容") String content,
    @ToolMemoryId Long appId  // 框架自动注入应用 ID
)
```

---

### 3. 代码处理流程

#### 3.1 HTML/MULTI_FILE 模式流程

```
用户输入
   ↓
AI 生成代码（流式输出）
   ↓
实时返回给用户（SSE 推送）
   ↓
流式输出完成
   ↓
CodeParser 解析代码（正则提取）
   ↓
FileSaver 保存文件到本地
   ↓
持久化 AI 回复到数据库
```

**关键代码**：
```java
// AiCodeGeneratorFacade.java
Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
return parserAndSaveResult(result, codeGenTypeEnum, appId);

private static Flux<String> parserAndSaveResult(...) {
    StringBuilder codeBuilder = new StringBuilder();
    return result
        .doOnNext(codeBuilder::append)  // 实时收集
        .doOnComplete(() -> {
            String completeCode = codeBuilder.toString();
            Object parsedResult = CodeParserExecutor.executeParser(...);
            File savedDir = FileSaveExecutor.executeSave(...);
        });
}
```

#### 3.2 VUE_PROJECT 模式流程

```
用户输入
   ↓
AI 通过工具调用创建项目结构
   ↓
实时推送工具调用信息（SSE）
   ↓ [FileWriteTool]
创建 package.json
   ↓ [FileWriteTool]
创建 vite.config.js
   ↓ [FileWriteTool]
创建 Vue 组件
   ↓ [FileWriteTool]
创建路由配置
   ↓
AI 完成项目生成
   ↓
VueProjectBuilder 执行构建
   ↓ [npm install]
安装依赖
   ↓ [npm run build]
构建项目
   ↓
生成 dist 目录
```

---

### 4. 项目部署模块

#### 4.1 部署流程

```java
public String deployApp(Long appId) {
    // 1. 校验权限
    // 2. 检查 deployKey 是否存在
    // 3. 如果是 Vue 项目，执行构建
    if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
        vueProjectBuilder.buildProject(sourcePath);
        sourceDir = new File(sourcePath, "dist");
    }
    // 4. 复制文件到部署目录
    FileUtil.copyContent(sourceDir, new File(deployPath), true);
    // 5. 更新数据库（部署时间、deployKey）
    // 6. 返回可访问 URL
    return String.format("%s/%s", deployAppAccessibleUrl, deployKey);
}
```

#### 4.2 部署目录结构

```
tmp/
├── code_output/              # 代码生成目录
│   ├── html_1960168102052954113/
│   │   └── index.html
│   ├── multi_file_xxx/
│   │   ├── index.html
│   │   ├── styles.css
│   │   └── script.js
│   └── vue_project_1960527904016928770/
│       ├── package.json
│       ├── vite.config.js
│       ├── dist/              # 构建后的文件
│       └── src/
└── code_deploy/              # 部署目录（可被 Nginx 代理）
    ├── husinK/               # deployKey
    │   └── index.html
    └── tv8bNz/
        ├── index.html
        └── assets/
```

---

### 5. 用户管理模块

#### 5.1 用户认证（Sa-Token）

```java
@SaCheckRole("admin")  // 角色校验注解
public Result<?> deleteAppByAdmin(DeleteRequest request) {
    // ...
}

// 获取当前登录用户
User loginUser = userService.getLoginUser();
Long userId = StpUtil.getLoginIdAsLong();
```

#### 5.2 用户权限

| 角色 | 值 | 权限 |
|------|---|------|
| 超级管理员 | 0 | 全部权限 |
| 管理员 | 1 | 管理应用、用户 |
| 普通用户 | 2 | 创建和管理自己的应用 |

---

### 6. 限流与安全

#### 6.1 接口限流

```java
@RateLimit(
    rate = 5,              // 5 次请求
    rateInterval = 30,     // 30 秒窗口
    limitType = RateLimitType.USER,
    message = "请求过于频繁，请稍后再试"
)
public Result<Long> createApp(AppAddRequest request) {
    // ...
}
```

**限流类型**：
- `USER`：按用户 ID 限流
- `IP`：按 IP 地址限流
- `GLOBAL`：全局限流

#### 6.2 AI 输入安全

```java
// PromptSafetyInputGuardrail.java
@Component
public class PromptSafetyInputGuardrail implements Guardrail {
    @Override
    public GuardrailResult validate(Object input) {
        // 检查敏感词、恶意指令、注入攻击等
        // 防止用户输入恶意 Prompt
    }
}
```

---

### 7. 应用截图功能

#### 7.1 自动截图上传

```java
public interface ScreenshotService {
    /**
     * 生成网页截图并上传到 COS
     * @param webUrl 网页 URL
     * @return 截图的可访问 URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
```

**实现方式**：
1. 使用 Selenium WebDriver 打开网页
2. 等待页面加载完成
3. 截图并保存为临时文件
4. 上传到腾讯云 COS
5. 返回 CDN 访问链接

#### 7.2 WebDriverManager

```java
// 自动管理浏览器驱动
WebDriverManager.chromedriver().setup();
ChromeDriver driver = new ChromeDriver();
```

---

## 📊 数据库设计

### 核心表结构

#### 1. user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_account | varchar(50) | 账号（唯一） |
| password | varchar(100) | 密码 |
| username | varchar(50) | 用户昵称 |
| avatar | varchar(255) | 头像 URL |
| status | tinyint | 状态（0-禁用，1-正常） |
| role | tinyint | 角色（0-超管，1-管理员，2-普通用户） |
| create_time | datetime | 创建时间 |
| is_deleted | datetime | 逻辑删除标识 |

#### 2. app（应用表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| appName | varchar(256) | 应用名称 |
| cover | varchar(512) | 应用封面（截图 URL） |
| init_prompt | text | 初始化提示词 |
| code_gen_type | varchar(64) | 代码生成类型（html/multi_file/vue_project） |
| deploy_key | varchar(64) | 部署标识（访问密钥） |
| deployed_time | datetime | 部署时间 |
| priority | int | 优先级（精选应用） |
| user_id | bigint | 创建用户 ID |
| create_time | datetime | 创建时间 |
| is_delete | datetime | 逻辑删除标识 |

#### 3. chat_history（对话历史表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| message | text | 消息内容 |
| message_type | varchar(10) | 消息类型（user/ai） |
| app_id | bigint | 应用 ID |
| user_id | bigint | 用户 ID |
| create_time | datetime | 创建时间 |
| is_delete | datetime | 逻辑删除标识 |

---

## 🔧 配置管理

### 1. 多环境配置

```yaml
spring:
  profiles:
    active: local  # 本地环境
    # active: prod  # 生产环境
```

**环境配置文件**：
- `application.yml`：公共配置
- `application-local.yml`：本地开发配置
- `application-prod.yml`：生产环境配置

### 2. AI 模型配置

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com/v1
      api-key: ${DEEPSEEK_API_KEY}
      model-name: deepseek-chat
      max-tokens: 8192
      max-retries: 3
    dashscope:
      base-url: ${dashscope.base-url}
      api-key: ${dashscope.api-key}
      model-name: ${dashscope.model-name}
    doubao:
      base-url: ${doubao.base-url}
      api-key: ${doubao.api-key}
      model-name: ${doubao.model-name}
```

### 3. Redis 配置

```yaml
spring:
  data:
    redis:
      database: 0
      host: ${redis.host}
      port: ${redis.port}
      ttl: 3600  # 对话记忆过期时间（秒）
      lettuce:
        pool:
          max-active: 200
          max-idle: 10
          min-idle: 0
```

---

## 🚀 核心流程详解

### 1. 创建应用流程

```
前端：用户输入需求描述
   ↓
POST /api/app/add
   ↓
AppController.createApp()
   ↓
AppService.createApp()
   ↓
1. SimpleAiTaskService.getInitialPrompt()
   生成应用名称（AI 总结）
   ↓
2. SimpleAiTaskService.routeCodeGenType()
   智能选择生成类型
   ↓
3. 创建 App 实体，保存到数据库
   ↓
返回 appId
```

### 2. 生成代码流程（HTML/MULTI_FILE）

```
GET /api/app/chat/gen/code?appId=xxx&message=xxx
   ↓
AppController.genAppCode()
   ↓
AppService.genAppCode()
   ↓
1. 校验权限（是否为应用创建者）
   ↓
2. 保存用户消息到 chat_history
   ↓
3. 删除旧的部署文件
   ↓
4. AiCodeGeneratorFacade.generateAndSaveCodeStream()
   ↓
   4.1 获取 AI 服务实例（从缓存或创建）
   ↓
   4.2 调用 AI 生成代码（流式）
   ↓
   4.3 实时推送给前端（SSE）
   ↓
   4.4 流式完成后，解析代码
   ↓
   4.5 保存文件到 code_output/
   ↓
5. 保存 AI 回复到 chat_history
   ↓
返回流式响应（ServerSentEvent）
```

### 3. 生成代码流程（VUE_PROJECT）

```
GET /api/app/chat/gen/code?appId=xxx&message=xxx
   ↓
AppService.genAppCode()
   ↓
AiCodeGeneratorFacade.generateAndSaveCodeStream()
   ↓
AiCodeGeneratorService.generateVueProjectCodeStream(appId, message)
   ↓
AI 返回 TokenStream（包含工具调用）
   ↓
processTokenStream() 处理流
   ↓
监听事件：
   - onPartialResponse() → 推送文本消息
   - onPartialToolExecutionRequest() → 推送工具调用请求
   - onToolExecuted() → 推送工具执行结果
      ↓ [FileWriteTool]
      创建 package.json
      创建 vite.config.js
      创建 main.js
      创建 App.vue
      创建 router/index.js
      创建 pages/Home.vue
      ...
   - onCompleteResponse() → 构建 Vue 项目
      ↓ [VueProjectBuilder]
      npm install
      npm run build
      生成 dist/
   - onError() → 错误处理
   ↓
推送完成事件
```

### 4. 部署应用流程

```
POST /api/app/deploy
   ↓
AppController.deployApp()
   ↓
AppService.deployApp()
   ↓
1. 校验权限
   ↓
2. 检查是否已部署（deployKey 存在）
   ↓
3. 生成随机 deployKey（6位，排除 0oO）
   ↓
4. 获取源文件路径
   code_output/[codeGenType]_[appId]/
   ↓
5. 如果是 Vue 项目，执行构建
   VueProjectBuilder.buildProject()
      ↓ npm install
      ↓ npm run build
      ↓ 验证 dist/ 目录
   ↓
6. 复制文件到部署目录
   code_deploy/[deployKey]/
   ↓
7. 更新数据库
   - deploy_key
   - deployed_time
   ↓
8. 生成并上传截图
   ScreenshotService.generateAndUploadScreenshot()
      ↓ Selenium 打开网页
      ↓ 截图保存
      ↓ 上传到 COS
      ↓ 返回 CDN URL
   ↓
9. 更新 app.cover（截图 URL）
   ↓
返回可访问 URL：
http://{host}/{deployKey}
```

---

## 🔑 核心技术亮点

### 1. 流式输出（Server-Sent Events）

```java
@GetMapping("/chat/gen/code")
public Flux<ServerSentEvent<String>> genAppCode(...) {
    return appService.genAppCode(message, appId)
        .map(content -> {
            Map<String, String> data = Map.of("d", content);
            String dataJson = JSONUtil.toJsonStr(data);
            return ServerSentEvent.<String>builder()
                .data(dataJson)
                .build();
        })
        .concatWith(Mono.just(
            ServerSentEvent.<String>builder()
                .event("done")
                .data("")
                .build()
        ));
}
```

**优势**：
- 实时反馈 AI 生成进度
- 用户体验好，无需等待
- 降低感知延迟

### 2. 多例 + 缓存架构

```java
// 每个应用独立的 AI 服务实例
private final Cache<String, AiCodeGeneratorService> serviceCache;

// 每个应用独立的对话记忆
MessageWindowChatMemory chatMemory = MessageWindowChatMemory
    .builder()
    .id("chatMemory:" + appId)  // 独立命名空间
    .maxMessages(20)
    .build();
```

**优势**：
- 避免对话记忆混淆
- 支持高并发（多用户同时生成）
- 内存可控（自动过期清理）

### 3. 策略模式 + 工厂模式

```java
// 不同生成类型的处理策略
return switch (codeGenTypeEnum) {
    case HTML -> parserAndSaveResult(...);
    case MULTI_FILE -> parserAndSaveResult(...);
    case VUE_PROJECT -> processTokenStream(...);
    default -> throw new BusinessException(...);
};

// 工厂创建不同的 AI 服务实例
public AiCodeGeneratorService getAiCodeGeneratorService(
    long appId, 
    CodeGenTypeEnum codeGenTypeEnum
) {
    String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
    return serviceCache.get(cacheKey, key -> 
        createAiCodeGeneratorService(appId, codeGenTypeEnum)
    );
}
```

### 4. 责任链模式（执行器）

```java
// 代码解析执行器
public class CodeParserExecutor {
    public static Object executeParser(
        String code, 
        CodeGenTypeEnum typeEnum
    ) {
        return switch (typeEnum) {
            case HTML -> new HtmlCodeParser().parse(code);
            case MULTI_FILE -> new MultiFileCodeParser().parse(code);
            // ...
        };
    }
}

// 文件保存执行器
public class FileSaveExecutor {
    public static File executeSave(
        Object result, 
        CodeGenTypeEnum typeEnum, 
        Long appId
    ) {
        return switch (typeEnum) {
            case HTML -> new HtmlFileSaver().save(...);
            case MULTI_FILE -> new MultiFileSaver().save(...);
            // ...
        };
    }
}
```

### 5. LangChain4j 高级特性

#### 5.1 结构化输出

```java
// AI 直接返回枚举类型（框架自动解析）
CodeGenTypeEnum routeCodeGenType(String userPrompt);
```

#### 5.2 工具调用（Function Calling）

```java
@Tool("写入文件到指定路径")
public String writeFile(
    @P("文件的相对路径") String relativeFilePath,
    @P("要写入文件的内容") String content,
    @ToolMemoryId Long appId
) {
    // AI 自动调用此方法创建文件
}
```

#### 5.3 Prompt 模板

```java
@SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
TokenStream generateVueProjectCodeStream(
    @MemoryId Long appId, 
    @UserMessage String userMessage
);
```

---

## 📦 部署与运维

### 1. 部署要求

**环境依赖**：
- JDK 21+
- MySQL 8.0+
- Redis 5.0+
- Node.js 18+（构建 Vue 项目）
- Nginx（静态文件代理）

**外部服务**：
- DeepSeek API Key
- 通义千问 API Key（可选）
- 腾讯云 COS（截图存储）

### 2. Nginx 配置示例

```nginx
server {
    listen 8123;
    server_name your-domain.com;

    # 静态文件代理（部署的应用）
    location / {
        root /path/to/tmp/code_deploy;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 3. 构建与运行

```bash
# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/keyun-autocode-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DEEPSEEK_API_KEY=your-api-key
```

---

## 🎨 设计模式应用

| 模式 | 应用场景 | 实现位置 |
|------|---------|---------|
| 门面模式 | 统一代码生成入口 | `AiCodeGeneratorFacade` |
| 工厂模式 | 创建 AI 服务实例 | `AiCodeGeneratorServiceFactory` |
| 策略模式 | 不同生成类型的处理 | `AiCodeGeneratorFacade.generateAndSaveCodeStream()` |
| 责任链模式 | 代码解析、文件保存 | `CodeParserExecutor`, `FileSaveExecutor` |
| 单例模式 | 工具管理器 | `ToolManager` |
| 建造者模式 | AI 模型配置 | `OpenAiStreamingChatModel.builder()` |
| 观察者模式 | 流式输出监听 | `TokenStream.onPartialResponse()` |

---

## 🔒 安全机制

### 1. 认证与授权
- Sa-Token 实现 Session 管理
- 基于角色的访问控制（RBAC）
- 接口级权限校验

### 2. 输入验证
- 参数校验（`ThrowUtils.throwIf`）
- Prompt 注入防护（`PromptSafetyInputGuardrail`）
- SQL 注入防护（MyBatis-Plus）

### 3. 限流保护
- 基于 Redisson 的分布式限流
- 支持用户级、IP 级、全局限流
- 自定义限流策略（注解驱动）

### 4. 数据安全
- 逻辑删除（`is_deleted` 字段）
- 密码加密存储
- 敏感信息脱敏

---

## 🚧 可优化点

### 1. 性能优化
- [ ] AI 响应结果缓存（相同 Prompt）
- [ ] Vue 项目构建异步化（避免阻塞）
- [ ] CDN 加速静态资源访问
- [ ] 数据库读写分离

### 2. 功能增强
- [ ] 支持更多前端框架（React、Angular）
- [ ] 代码版本管理（Git 集成）
- [ ] 在线代码编辑器
- [ ] AI 代码审查与优化建议
- [ ] 应用模板市场

### 3. 可观测性
- [ ] 完善日志系统（ELK Stack）
- [ ] 监控告警（Prometheus + Grafana）
- [ ] 链路追踪（SkyWalking）
- [ ] 性能分析（Arthas）

### 4. 测试覆盖
- [ ] 单元测试覆盖率提升
- [ ] 集成测试自动化
- [ ] 压力测试与性能基准
- [ ] AI 输出质量评估

---

## 📚 依赖说明

### 核心依赖

```xml
<!-- Spring Boot 核心 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.5.4</version>
</dependency>

<!-- LangChain4j AI 框架 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
    <version>1.1.0-beta7</version>
</dependency>

<!-- Redis 对话记忆 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-community-redis-spring-boot-starter</artifactId>
    <version>1.1.0-beta7</version>
</dependency>

<!-- MyBatis-Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.12</version>
</dependency>

<!-- Sa-Token 认证授权 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>1.44.0</version>
</dependency>

<!-- Redisson 分布式锁 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.50.0</version>
</dependency>

<!-- Selenium 网页截图 -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.33.0</version>
</dependency>

<!-- 腾讯云 COS -->
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos_api</artifactId>
    <version>5.6.227</version>
</dependency>
```

---

## 📞 总结

### 项目特色

1. **智能路由**：AI 自动判断用户需求，选择最合适的生成模式
2. **多模型支持**：集成 DeepSeek、通义千问、豆包等多个 AI 模型
3. **工具调用**：AI 可主动调用工具创建文件（Vue 项目生成）
4. **流式输出**：实时反馈生成进度，优秀的用户体验
5. **完整工程化**：自动构建 Vue 项目（npm install + build）
6. **一键部署**：生成后即可访问，支持 URL 分享
7. **对话记忆**：支持多轮对话，持续优化生成结果
8. **权限隔离**：每个用户独立的应用和对话空间

### 技术亮点

- ✅ 门面模式简化复杂流程
- ✅ 工厂模式 + 缓存提升性能
- ✅ 策略模式支持多种生成类型
- ✅ 责任链模式解耦业务逻辑
- ✅ SSE 流式输出提升体验
- ✅ LangChain4j 高级特性应用
- ✅ 分布式限流保障系统稳定

### 适用场景

- 前端开发人员快速原型验证
- 产品经理演示 Demo
- 非技术人员创建简单网站
- 教育培训场景（学习前端开发）
- 低代码平台基础设施

