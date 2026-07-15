# 像素集 PixelHub - Android 设计规范 UI 重构文档

## 设计原则
遵循 Android 官方设计规范 (https://android-docs.cn/design/ui/mobile)

## 应用结构
1. **系统栏** - 状态栏 + 导航栏，Edge-to-edge 全面屏适配
2. **导航区域** - Material 3 NavigationBar（底部导航栏）
3. **主体内容区** - Scaffold 管理，延伸到导航栏下方

## 首页
- TopAppBar: 标题 + 数据源选择 + 刷新按钮
- SearchBar: Material 3 搜索组件
- 瀑布流: LazyVerticalStaggeredGrid，8dp 间距
- FAB: 浮动刷新按钮

## 收藏页
- TopAppBar: 标题 + 批量操作
- TabRow: 分组标签
- 瀑布流: 同首页

## 设置页
- TopAppBar: 标题
- LazyColumn: 卡片分组设置项

## 底部导航
- Material 3 NavigationBar
- 3个标签: 首页 / 收藏 / 设置
- 使用 NavigationBar 而非自定义浮动栏

## 主题
- Material 3 Dynamic Color (Android 12+)
- 深色/浅色/跟随系统
- AMOLED 纯黑模式
