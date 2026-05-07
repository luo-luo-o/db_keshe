# PSM-Smart Agent 提示文档索引

本目录存放面向后续开发 Agent 的项目提示文档。推荐使用顺序如下：

1. 先阅读根目录 `agent.md`。
2. 再阅读 `00-project-context.md` 获取业务闭环。
3. 按任务类型选择专项文档：
   - 后端开发：`10-backend-agent.md`
   - 前端开发：`20-frontend-agent.md`
   - 数据库开发：`30-database-agent.md`
   - 阶段规划：`40-implementation-roadmap.md`
   - 验收检查：`50-acceptance-checklist.md`
   - 单次任务：`60-task-template.md`

## 推荐 Prompt 组合

后端任务：

```text
请先阅读 agent.md、docs/prompts/00-project-context.md、docs/prompts/10-backend-agent.md。
然后完成以下任务：...
```

前端任务：

```text
请先阅读 agent.md、docs/prompts/00-project-context.md、docs/prompts/20-frontend-agent.md。
然后完成以下任务：...
```

数据库任务：

```text
请先阅读 agent.md、docs/prompts/00-project-context.md、docs/prompts/30-database-agent.md。
然后完成以下任务：...
```

全链路任务：

```text
请先阅读 agent.md 和 docs/prompts/ 下所有提示文档。
按 40-implementation-roadmap.md 分阶段推进，并用 50-acceptance-checklist.md 验收。
```

