# 单次开发任务 Prompt 模板

复制本模板后，将方括号内容替换为你的具体目标。

## Prompt

你是 PSM-Smart 项目的开发 Agent。请先阅读：

- `agent.md`
- `docs/prompts/00-project-context.md`
- `[按任务选择：10-backend-agent.md / 20-frontend-agent.md / 30-database-agent.md]`

然后完成以下任务。

## 任务目标

`[写清楚本次要实现的功能，例如：实现动态采样模拟任务和 TS_RAW_DATA 批量写入]`

## 修改范围

允许修改：

- `[例如：Core/src/main/java/pers/luoluo/databasekeshe/acquisition/**]`
- `[例如：Core/src/main/resources/mapper/**]`
- `[例如：Core/src/main/resources/application.yml]`

避免修改：

- `[例如：Front/**]`
- `[例如：与本任务无关的 README 或依赖版本]`

## 业务要求

- `[要求 1]`
- `[要求 2]`
- `[要求 3]`

## 技术要求

- 保持 Java 17 / Spring Boot 3 / MyBatis / Oracle 21c XE 兼容。
- 后端保持 Controller、Service、Mapper 分层。
- 前端保持 Vue 3 `<script setup lang="ts">`。
- 数据库 SQL 需可在 Oracle 21c XE 执行。

## 验收方式

请至少执行：

```powershell
[例如：cd Core; .\mvnw.cmd test]
[例如：cd Front; npm run build]
```

完成后说明：

- 修改了哪些文件。
- 如何验证。
- 是否有未完成项或环境限制。
