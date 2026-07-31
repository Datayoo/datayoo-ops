# datayoo-ops

HuggingFists 平台算子开源工程。将 HuggingFists 低代码数据平台的算子组件以 **Descriptor + Oyez** 的 SPI 架构逐步开放，每个算子由描述符（Descriptor）定义元数据与参数，由执行器（Oyez）提供运行时实现。

## 目录

- [快速开始](#快速开始)
- [环境说明](#环境说明)
- [模块说明](#模块说明)

## 快速开始

`lib/` 目录存放平台基础依赖 jar 包（Footstone、Sengee、Oyez 等框架），不在 Maven 中央仓库中，**本地编译前需先执行脚本安装到本地 Maven 仓库，否则工程无法编译。**

| 脚本 | 平台 |
|------|------|
| `install-lib.sh` | Linux / macOS |
| `install-lib.ps1` | Windows PowerShell |
| `install-lib.bat` | Windows 批处理（内部调用 `install-lib.ps1`） |

### 使用方式

```bash
# Linux / macOS — 跳过已安装的
./install-lib.sh

# Linux / macOS — 强制覆盖
./install-lib.sh --force

# Windows PowerShell
.\install-lib.ps1
.\install-lib.ps1 -Force

# Windows CMD
install-lib.bat
install-lib.bat --force
```

### 算子打包

1. 执行 `./install-lib.sh` 安装依赖
2. 在 IDEA 右侧 Maven 面板中，依次对每个目标模块执行 `Lifecycle` → `clean`、`package`
3. 对 descriptor 模块：双击 `Plugins` → `descriptor-plugin` → `descriptorPack`，生成 descriptor zip
4. 对 oyez 模块：双击 `Plugins` → `impl-plugin` → `oyezPack`，生成 oyez zip

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
