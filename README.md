# Meteor Client Forge 中文实验移植版

基于 [Meteor Client v0.5.4](https://github.com/MeteorDevelopment/meteor-client/tree/6d76982a87ed175642d4f2ba2703179ab816cde3)
的**非官方原生 Forge 移植**，不隶属于 Meteor Development，不是 Fabric Connector 包装。

**目前仅 Minecraft 1.20.1 有可用构建。1.14.4 至 26.3 的全部版本移植尚未完成。**

## 下载与安装

[GitHub Releases](https://github.com/Xunldjc/meteor-client-forge-zh/releases)
提供最新修复版，旧 Release 已由此版本替换。这是客户端模组：

| Minecraft | Forge | Java | 状态 |
| --- | --- | --- | --- |
| 1.20.1 | 47.4.10 | 17 | Shader 重复实体绘制修复版，整合包及游戏内回归通过 |

1. 建立独立的 Minecraft 1.20.1 / 对应 Forge / Java 17 实例。
2. 下载对应 Release 的完整安装 ZIP，把 `mods/` 中的 Meteor 和 Baritone 两个 JAR 放进实例。
3. 正常启动，按右 Shift 打开 Meteor 界面。

升级时只保留一份 Meteor JAR 和一份 `baritone-unoptimized-forge-1.10.1.jar`。
不要混入 Fabric Meteor、Fabric Baritone、Fabric Loader、Fabric API 或 Connector。
不要使用 `-all.jar` 开发产物；不要安装到专用服务器。先备份存档和配置。
请遵守服务器规则。

## 已实现

- 168 个运行时注册模块的中文名称和简介、分类、主要导航及常用设置汉化。
- 已有搜索入口支持全拼、首字母及部分拼音，保留英文名称和注册 ID 搜索。
  例如 `stts` 搜索“实体透视”，`shitou` 搜索“石头”，`zsj` 搜索“钻石剑”，
  `jiangshi` 搜索“僵尸”。方块、物品及实体按当前游戏显示名称匹配，中文拼音需中文游戏语言。
- Shader ESP 使用独立的已加载实体绘制流程，修复原版模型距离裁剪导致的远处实体漏绘。
  仍遵守实体选择、视距、自身过滤、NoRender 和视锥限制；未加载实体无法绘制。
- Shader 遮罩只写入独立轮廓缓冲区，修复完整实体模型被重复绘制、悬浮或位置错乱的问题。
- 保留 ATM9 的 FOV 方法选择、Packet Fixer/Connectivity 数据包限制协作和 EntityCulling
  自定义实体绘制兼容修复。

拼音库已包含在 JAR 中，无需额外安装。英文界面可使用 JVM 参数 `-Dmeteor.locale=en_us`。

## 验证与限制

最新 JAR 已通过 Java 17 构建、分发 metadata/SRG 检查、纯 Forge Mixin 审计、
Zombie Invade 100 Days v2.3 整包启动与新建世界、中文 GUI/设置、拼音筛选、
远处实体的 Shader 模型掩码及 EntityCulling 回归。缓冲区探针复现了旧版泄漏，
并验证修复版不向普通实体缓冲区写入顶点。
详见 [公开验证摘要](VERIFICATION_PUBLIC.md)。

这仍是实验移植，不是完全汉化，也不保证所有模块、联机、长时间运行、
附加模组或 Embeddium/Oculus/其他渲染器与光影包兼容。Fabric 附加模组不能直接兼容。
这里的 Shader 指 Meteor ESP 模式，不是第三方光影包兼容性承诺。
问题请在[本仓库](https://github.com/Xunldjc/meteor-client-forge-zh/issues)反馈，并脱敏日志中的个人信息。

## 多版本进度

按官方 Forge 清单，对 Minecraft 1.14.4 至 26.3 的正式版各选择一个推荐 Forge，
无推荐版时选择最新版，共 40 个目标。**已验证 1 个目标，1 个依赖检查失败，38 个未移植。**
本次 JAR 在 Forge 47.2.0 上编译，在 Forge 47.4.10 整包中验证；它们不是两个 Minecraft 目标。
没有为未构建的版本创建空 Release。完整列表见 [build-matrix.csv](build-matrix.csv)。

1.14.4 的 Java 8 依赖、Minecraft API/Mixin 及 Baritone Forge 集成仍需移植。
其拼音组件已通过 Java 8 测试，不能据此宣称整个模组兼容。
26.3 已核对为官方最新正式版；其 Java 25 / ForgeGradle 7 移植尚未开始。

## 构建

使用 JDK 17，检出所需 Release 的 tag，执行：

```sh
./gradlew build --no-daemon --console=plain
```

Windows 使用 `gradlew.bat`。默认编译目标为 Forge 47.2.0，与已测试 JAR 的源码一致。
使用 `-Pforge_version=47.4.10` 可选择另一编译目标。
正式产物为 `build/libs/meteor-client-0.5.4-forge-zh.jar`，不要使用 `-all.jar`。
首次构建需要下载 Gradle、Forge 和映射依赖。

CI 只编译 1.20.1 的两个 Forge 目标，不运行图形客户端，不代表其他 Minecraft 版本通过验证。
上游原 CI 保存于 `.github/upstream-workflows/`，不会执行上游 Maven/Discord 发布任务。

## 许可证与署名

Meteor Development 是原项目作者。本移植基于提交
`6d76982a87ed175642d4f2ba2703179ab816cde3`，保留原版权声明及 [GPL-3.0](LICENSE)。
上游 README 保存在 [README_UPSTREAM.md](README_UPSTREAM.md)。

Baritone v1.10.1 来自 [cabaletta/baritone](https://github.com/cabaletta/baritone/releases/tag/v1.10.1)，
原样分发，遵循 LGPL-3.0。完整安装包包含相应源码、许可证与精确修改源码 ZIP。
pinyin4j 2.5.1 的源文件标注 GPL-2.0-or-later，保留其版权说明，合并作品按 GPL-3.0 分发；
安装包同时包含对应源码 JAR。Maven POM 的 BSD 标注差异见包内 pinyin4j-NOTICE.txt。

仓库及 Release 不包含本机配置、认证信息或未经脱敏的运行日志。
