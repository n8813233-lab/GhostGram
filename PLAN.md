
## 十八、GitHub Actions 云端构建（2026-11-07，用户拍板公开仓库 + 云端出包）

- **仓库**：https://github.com/n8813233-lab/GhostGram（公开），发布副本 ~/workspace/ghostgram_publish；google-services.json 已换真配置（Firebase 项目 ghostgram-6a2e0，包名 com.Ghost.wed）。
- **第 1 轮（run 42337711603，commit c1bb9ffa，2026-11-07 12:16–12:43 CST）**：首次进入真实编译（25m44s），死于 `:TMessagesProj:packageReleaseResources`——`drawable/filter_all.xml` 重复资源：C1 批为 QRCodeSheet 补的单文件（res-cherrygram/drawable）与 C4 批 14.1 整目录导入的 res-tabs/drawable 里同名文件字节完全相同（md5 ed1668c40dd73a74871d2cdb3c1ffe8a），四 res 目录全量核对仅此一处重复。处置：删 res-cherrygram 副本、保留 res-tabs 正本（恢复 PLAN 9 节「res-cherrygram 均为独立命名、无同名冲突」约定；QRCodeSheet/FolderIconHelper 的 R.drawable.filter_all 引用不变）。已同步改 project 与 publish 两棵树，推送后触发第 2 轮。
