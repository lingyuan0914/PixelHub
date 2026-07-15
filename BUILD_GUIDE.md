# PixelHub Miuix迁移构建指南

## 📋 前提条件

1. **Android Studio** - 最新稳定版本（建议2024.2.1或更高）
2. **Android SDK** - 安装Android SDK 37平台
3. **JDK 17** - Android Studio自带或单独安装
4. **Git** - 用于克隆项目

## 🛠️ 构建步骤

### 第一步：克隆项目
```bash
# 如果是从远程仓库克隆
git clone <your-repo-url>
cd PixelHub

# 如果已有项目，跳过此步骤
```

### 第二步：安装Android SDK 37平台
1. 打开Android Studio
2. 进入 **Tools > SDK Manager**
3. 在 **SDK Platforms** 选项卡中，勾选 **Android API 37**
4. 点击 **Apply** 安装

或者使用命令行：
```bash
# 使用sdkmanager安装
sdkmanager "platforms;android-37"
```

### 第三步：配置miuix仓库（如果需要）
miuix库可能发布在GitHub Packages上。在项目根目录创建或编辑 `local.properties` 文件：

```properties
# GitHub Packages认证（如果需要）
gpr.user=your_github_username
gpr.key=your_github_personal_access_token
```

**注意**：如果miuix已发布到Maven Central，则不需要此步骤。

### 第四步：更新项目配置

#### 1. 更新 `app/build.gradle.kts`
确保以下配置正确：

```kotlin
android {
    compileSdk = 37  // 使用37而不是36
    
    // ... 其他配置
}

dependencies {
    // ... 其他依赖
    
    // Miuix UI - 使用最新版本
    implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.0")
    implementation("top.yukonga.miuix.kmp:miuix-blur:0.9.0")
    
    // ... 其他依赖
}
```

#### 2. 更新 `gradle.properties`
```properties
# 抑制compileSdk警告
android.suppressUnsupportedCompileSdk=37,37.0
```

### 第五步：同步和编译

1. **同步Gradle**：点击Android Studio中的 **Sync Now** 按钮
2. **清理项目**：**Build > Clean Project**
3. **重新构建**：**Build > Rebuild Project**

### 第六步：解决常见问题

#### 问题1：miuix依赖找不到
**解决方案A**：检查miuix的实际仓库
1. 访问 [miuix GitHub仓库](https://github.com/compose-miuix-ui/miuix)
2. 查看README中的依赖配置
3. 可能需要添加额外的Maven仓库

**解决方案B**：使用本地构建
```bash
# 克隆miuix仓库
git clone https://github.com/compose-miuix-ui/miuix.git
cd miuix

# 发布到本地Maven仓库
./gradlew publishToMavenLocal

# 在项目中使用本地仓库
```

然后在 `settings.gradle.kts` 中添加：
```kotlin
repositories {
    mavenLocal()
    // ... 其他仓库
}
```

#### 问题2：compileSdk版本不兼容
1. 确保Android SDK 37已安装
2. 检查Android Gradle Plugin版本是否支持37
3. 更新Android Gradle Plugin：
   ```kotlin
   // 在根目录 build.gradle.kts 中
   plugins {
       id("com.android.application") version "8.13.2" apply false
       // 或更高版本
   }
   ```

#### 问题3：API不兼容错误
miuix的API可能与Material3不同。常见修复：

1. **TopAppBar**：参数可能不同
2. **AlertDialog**：按钮参数可能需要调整
3. **NavigationBar**：结构可能不同

### 第七步：测试和调试

1. **运行应用**：选择设备或模拟器，点击 **Run**
2. **检查UI**：确保所有组件正确显示
3. **检查日志**：查看是否有运行时错误

## 📁 已修改的文件清单

### 核心配置文件：
- `app/build.gradle.kts` - 依赖和编译配置
- `settings.gradle.kts` - 仓库配置
- `gradle.properties` - 构建属性

### 主题和UI文件：
- `PixelHubTheme.kt` - 主题系统迁移到MiuixTheme
- `MainActivity.kt` - 主要UI组件
- `ExploreScreen.kt` - 探索页面
- `FavoritesScreen.kt` - 收藏页面
- `SettingsScreen.kt` - 设置页面
- `ImageCard.kt` - 图片卡片组件
- `ImageDetailSheet.kt` - 图片详情弹窗
- `FloatingBottomBar.kt` - 浮动底部栏
- `ImageWaterfallGrid.kt` - 瀑布流网格

## 🎯 迁移完成度

- ✅ **主题系统**：100%迁移到MiuixTheme
- ✅ **核心组件**：90%迁移到miuix组件
- ⚠️ **构建配置**：需要解决依赖和compileSdk问题
- ⚠️ **API兼容性**：部分组件可能需要调整参数

## 🔄 回滚方案

如果迁移遇到严重问题，可以回滚到原始版本：

1. **使用Git回滚**：
   ```bash
   git checkout HEAD -- app/build.gradle.kts
   git checkout HEAD -- settings.gradle.kts
   git checkout HEAD -- app/src/main/java/com/pixelhub/app/ui/theme/PixelHubTheme.kt
   ```

2. **恢复Material3组件**：
   - 移除miuix导入
   - 恢复Material3组件

## 📞 获取帮助

1. **miuix文档**：https://compose-miuix-ui.github.io/miuix/zh_CN/
2. **GitHub Issues**：https://github.com/compose-miuix-ui/miuix/issues
3. **Android开发者文档**：https://developer.android.com/

## ✅ 验证清单

- [ ] Android Studio 2024.2.1或更高版本
- [ ] Android SDK 37平台已安装
- [ ] JDK 17已配置
- [ ] miuix依赖可解析
- [ ] 项目成功编译
- [ ] 应用可正常运行
- [ ] UI组件正确显示
- [ ] 所有功能正常工作

---

**注意**：这是一个实验性迁移，miuix库仍在开发中。API可能会变化，请关注官方更新。