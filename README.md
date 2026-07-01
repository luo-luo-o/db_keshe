# 箱式变压器监测系统

基于 Spring Boot 3、Vue 3 和 Oracle 21c 的箱式变压器监测与维保管理系统。

## 目录

```text
.
├── Core/    # Spring Boot 后端
├── Front/   # Vue 3 + TypeScript 前端
├── build/   # Windows 构建与启动脚本
├── docs/    # 需求、权限与说明文档
└── README.md
```

## 环境要求

- Java 17
- Maven
- Node.js / npm
- Oracle Database 21c

根目录 `.env` 可由 `.env.template` 复制得到，再按本机数据库环境填写账号、密码和连接串。

## 快速启动

前端开发：

```powershell
cd Front
npm run dev
```

前端构建：

```powershell
cd Front
npm run build
```

后端编译：

```powershell
cd Core
mvn -q -DskipTests compile
```

后端测试：

```powershell
cd Core
mvn test
```

Windows 一键构建：

```powershell
.\build\build.bat
```

Windows 一键启动：

```powershell
.\build\start.bat
```

后台模式：

```powershell
.\build\start.bat -b
```

如果使用 Docker 启动 Oracle：

```powershell
docker compose up -d
```

默认开发时区为 `Asia/Shanghai (UTC+8)`。如果重置数据库，请先用 `SYSTEM` 或 `SYSDBA` 执行 `Core/src/main/resources/db/oracle21c-admin-timezone.sql`，重启 Oracle 后，再执行 `Core/src/main/resources/db/oracle21c-init.sql`。应用默认数据库时区也已对齐到 `Asia/Shanghai`。

备份回溯使用 Oracle Data Pump。使用前请在 XEPDB1 中用 `SYSTEM` 或 `SYSDBA` 执行 `Core/src/main/resources/db/oracle21c-admin-datapump.sql`，它会创建 `PSM_BACKUP_DIR` 并授权应用用户读写。脚本提示应用数据库用户时，请输入 `.env` 中的 `DB_USERNAME`；直接回车会使用默认的 `psm_app`。

## 日志

后端文件日志默认写入根目录 `logs/`。

- `logs/access.log`：记录所有 `/api/**` 请求的单行访问日志，字段包含 `requestId`、`userId`、`roleCode`、`method`、`path`、`queryString`、`status`、`elapsedMs`、`remoteAddr`。
- `logs/application.log`：记录业务操作、异常和模拟任务失败日志，日志行会带上 MDC 中的 `requestId`，方便和访问日志关联。

可以通过环境变量 `APP_LOG_DIR` 或启动参数 `--app.log.dir=...` 覆盖目录。`.\build\start.bat` 会在启动前创建根目录 `logs/`，并显式传入该参数。

## 数据库初始化

数据库初始化入口脚本为 `Core/src/main/resources/db/oracle21c-init.sql`。
数据库时区管理员脚本为 `Core/src/main/resources/db/oracle21c-admin-timezone.sql`。
Data Pump 目录管理员脚本为 `Core/src/main/resources/db/oracle21c-admin-datapump.sql`。

执行初始化时请确保 SQL 客户端和脚本文件都按 UTF-8 处理；基础中文展示数据、权限文案和样例名称都依赖 UTF-8 编码，错误的会话编码会导致中文乱码。
