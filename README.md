# datayoo-ops

HuggingFists 平台算子开源工程。将 HuggingFists 低代码数据平台的算子组件以 **Descriptor + Oyez** 的 SPI 架构逐步开放，每个算子由描述符（Descriptor）定义元数据与参数，由执行器（Oyez）提供运行时实现。

## 目录

- [快速开始](#快速开始)
- [环境说明](#环境说明)
- [模块说明](#模块说明)

## 快速开始

`lib/` 目录存放平台私有依赖（Footstone、Sengee、Oyez、算子打包插件等），中央仓库没有。**本地编译前先执行安装脚本**，否则工程无法解析这些坐标。

| 脚本 | 平台 |
|------|------|
| `install-lib.bat` | Windows CMD（推荐入口，内部调用 `install-lib.ps1`） |
| `install-lib.ps1` | Windows PowerShell |
| `install-lib.sh` | Linux / macOS / Git Bash |

脚本会：

1. 把 `lib/*.jar` 按内嵌 GAV 装进本地 Maven 仓库，并带上完整 POM，让公共依赖（jackson、httpclient、commons-\* 等）仍由 Maven 从远程拉。
2. 识别 `META-INF/maven/plugin.xml`，把 `descriptor-plugin` / `i18n-maven-plugin` / `impl-maven-plugin` 按 `maven-plugin` 安装，并写入组级前缀元数据（`descriptor` / `i18n` / `impl`）。
3. 递归补齐 parent：私有 parent 没有就生成占位 POM；公共 parent（如 `maven-shared-components`）留给 Maven 自己下载，不 stub。
4. 重装那些"曾经从内网 Nexus 下载"的缓存包。这类包在 `_remote.repositories` 里被标成来自某个远程仓库，本工程没有配置该仓库，Maven 会拒绝使用，报 `Could not find artifact`——尽管文件就在仓库里。
5. 扫一遍私有依赖，列出 `lib/` 里还缺的包，方便补齐。

没有内嵌 Maven 元数据的 jar（例如重新打包过的 `sigar`），需要在旁边放一个同名 `.pom` 声明坐标，如 `lib/sigar-1.6.5.132-7.pom`。

### 使用方式

仓库路径由用户指定。无参数时脚本会提示输入；也可以通过命令行参数或 `MAVEN_REPO` 环境变量传入。脚本不会自动选择或写死仓库目录。

注意 IDEA 可以在 `Settings → Build Tools → Maven → Local repository` 单独指定仓库。应把该路径传给安装脚本；如果还要用命令行 Maven 构建，再按需安装到命令行 Maven 使用的仓库。

```bash
# Windows CMD（推荐）
install-lib.bat
install-lib.bat C:\path\to\repository
install-lib.bat C:\path\to\repository --force

# Windows PowerShell
.\install-lib.ps1
.\install-lib.ps1 -Repo C:\path\to\repository
.\install-lib.ps1 -Repo C:\path\to\repository -Force

# Linux / macOS / Git Bash
./install-lib.sh
./install-lib.sh /path/to/maven/repo
./install-lib.sh /path/to/maven/repo --force
export MAVEN_REPO=/path/to/maven/repo && ./install-lib.sh
```

Git Bash 下 Windows 盘符路径使用正斜杠，例如 `D:/path/to/repository`；CMD / PowerShell 使用 `D:\path\to\repository`。

### 算子打包

descriptor 和 oyez 的打包插件（`descriptor-plugin` / `impl-plugin`）均依赖同模块的 `target/*.jar`，需先编译：

```bash
./install-lib.sh          # 安装 lib 依赖到本地 Maven 仓库
mvn clean package          # 编译打包所有模块
```

#### 打包 descriptor

在 IDEA 右侧 Maven 面板中，对目标 descriptor 模块双击 `Plugins` → `descriptor-plugin` → `descriptorPack`，生成 descriptor zip。

#### 打包 oyez

oyez 模块依赖对应的 descriptor 模块 jar，打包前需先将 descriptor 安装到本地仓库：

注意：如果oyezPack报错缺失对应descriptor的包，就把父级module install到本地。
```bash
mvn install -pl descriptor模块路径
```

然后在 IDEA 右侧 Maven 面板中，对目标 oyez 模块双击 `Plugins` → `impl-plugin` → `oyezPack`，生成 oyez zip。

## 环境说明

**JDK 1.8**（Java 8），Maven 编译配置为 `source/target = 1.8`。

## 模块说明

```
ops-structx/
├── structx-column/               # 列级算子
│   ├── structx-column-descriptor
│   └── structx-column-oyez
├── structx-row/                  # 行级/结构转换算子
│   ├── structx-row-descriptor
│   └── structx-row-oyez
└── structx-processing/           # 数据处理算子
    ├── processing-row/           # 行集处理
    ├── processing-stream/        # 流式处理
    ├── processing-v/             # 列值处理
    └── processing-generator/     # 数据生成
```

### 模块结构

每个算子模块按 `descriptor` + `oyez` 两层组织：

| 层次 | 说明 |
|------|------|
| **Descriptor** | 算子元数据定义层。声明算子名称、版本、输入/输出端口、参数 schema 等，运行在 `sengee` 框架上。`src/main/resources` 下包含三类资源：`helps/`（算子帮助文档 .md）、`i18ns/`（国际化文案 .json）、`portraits/`（算子图标 .svg） |
| **Oyez** | 算子运行时实现层。实现算子的实际数据处理逻辑，运行在 `oyez` 运行时引擎上。`src/main/resources` 下可按需放置运行时配置文件（如 Tika 配置等） |

---

## 许可证

待定

---

> 本项目逐步开源中，更多算子模块将持续加入。
