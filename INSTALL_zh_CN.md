# Meteor Client 1.20.1 Forge 中文实验移植版

基于 Meteor Client **v0.5.4**，上游提交
`6d76982a87ed175642d4f2ba2703179ab816cde3`。这是非官方原生 Forge 移植，
不是最新上游版本，也不是 Fabric Connector 包装。

## 安装

1. 使用 Java 17，建立 Minecraft **1.20.1 / Forge 47.4.10** 客户端实例。
2. 把安装包内 `mods/` 下的唯一 JAR 放进该实例的 `mods` 文件夹：
   `meteor-client-0.5.4-mc1.20.1-embedded-baritone-zh.jar`。
3. 正常启动游戏，按右 Shift 打开 Meteor 界面。

不要安装 Fabric Loader、Fabric API 或 Connector，也不要同时放入旧版 Meteor、
Fabric Baritone 或重复的 Baritone。不要使用源码构建目录中的 `-all.jar`。
这是客户端模组，不要放到专用服务器。建议先备份存档和旧配置。
模组沿用实例内的 `meteor-client` 配置目录，不会自动迁移其他实例的配置。
升级本次修订时，移除旧 Meteor 和外置 Baritone，只保留新的 Meteor JAR。
Baritone Forge 1.10.1 已完整内嵌；Forge JarJar 保留其独立入口、Mixin 与 API。
在聊天框输入 `#mine minecraft:stone` 可挖掘石头，输入 `#stop` 停止任务。

Meteor 界面默认简体中文。需要英文时，在启动器 JVM 参数中添加
`-Dmeteor.locale=en_us`。游戏自身语言可在 Minecraft 语言菜单中调整。

## 汉化与测试范围

- 已翻译 168 个运行时注册模块的名称和简介、分类、主要导航、按钮及常用设置名。
- 中文字形和中文换行已修复，并检查了实际 GUI 截图。
- 仍有部分设置说明、聊天消息、动态标签及选项使用英文；不是全量汉化。
- 已验证正式 Forge 安装启动、Mixin 审计、模块/设置界面、创建单人超平坦世界、
  移动 40 个游戏刻，以及全亮模块的开关状态恢复和正常退出。
- 此前渲染修复版已通过 Zombie Invade 100 Days v2.3 整包、纯 Forge 和 EntityCulling 回归。
- 本版内嵌 Baritone，并适配 Farsight 的区块快照；当前构建的实际测试结果见 `VERIFY_RELEASE.txt`。
- 这些检查不代表每个模块的效果都正确。联机、长时间运行、其他附加模组、
  外部光影包以及 Forge 实验性光照管线尚未验证。
  Fabric 附加模组不能直接兼容。

## 2026-10-06 搜索与实体透视修订

已有搜索/筛选入口统一支持中文全拼、首字母及部分拼音，例如模块搜索
`shititoushi` / `stts` 对应“实体透视”，方块 `shitou` 对应“石头”，
物品 `zsj` 对应“钻石剑”，实体 `jiangshi` / `js` 对应“僵尸”。
设置搜索、列表选择器、方块数据、效果、字体、歌曲及 HUD 搜索也接入同一匹配器。
保留中文、英文内部名及注册 ID 搜索；拼音只用于匹配，不更改保存的 ID 或配置值。
方块/物品/实体按游戏当前显示名称生成拼音，需显示中文名称才能按该中文名的拼音查找。
支持字库内简繁体及多音字的各读音；不承诺生僻扩展汉字均有拼音映射。

实体透视的 **Shader 模式**新增独立实体绘制，不再因原版模型距离裁剪而漏掉
已加载的远处/小型实体。仍遵守已选实体类型、视距、自身过滤和视锥限制。
客户端未加载或服务器没有发送的生物无法绘制。此处 Shader 指 Meteor 的绘制模式，
不是第三方光影包兼容性承诺；用户整合包及额外实体模型仍需实测。

拼音库已打入 Meteor JAR，无需另装依赖。随包提供 pinyin4j 2.5.1 对应源码与版权说明。

## Shader 重复实体绘制修复

Shader 遮罩现在只写入独立轮廓缓冲区，修复完整实体模型被额外绘制成悬浮副本的问题。
保留远距离轮廓、拼音搜索以及 ATM9 的 FOV、数据包与 EntityCulling 兼容修复。
这次测试中外部光影包未启用。

请先在独立实例和测试存档中使用，并遵守服务器规则。遇到问题保留
`logs/latest.log` 与 `crash-reports`。

## 源码与许可证

`source/MODIFIED_FILE.zip` 是已验证 JAR 的完整构建源码。Java 17 下执行
`gradlew.bat build` 或 `./gradlew build`；首次构建需要下载依赖。
默认编译目标 Forge 47.2.0，生产 JAR 在 Forge 47.4.10 上验证。
Release 的 `DIFF-from-previous-github.patch` 对应本仓库旧提交
`8ddee9062a5bbf3604fd29055f7ea586c4b2ad23` 到本次 Baritone 内嵌版的源码变更。
`VERIFY_RELEASE.txt` 提供公开测试摘要，`SHA256SUMS.txt` 提供下载校验值。

Meteor 及修改源码遵循 **GPL-3.0**。Baritone v1.10.1 来自
https://github.com/cabaletta/baritone/releases/tag/v1.10.1 ，随包原样分发，
遵循 **LGPL-3.0**，其源码见上述上游。保留所有版权与许可证文件。
