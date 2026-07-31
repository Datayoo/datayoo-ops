# SSDeepHash

​	**标签：** 

### 描述

​	SSDeep是一种基于内容分割的分片哈希算法（context triggered piecewise hashing, CTPH），也叫做模糊哈希。可用于匹配同源文档(相似文档)。同源文档中可能有一些顺序相同的字节，尽管这些字节可能在一个序列中长度和内容都不尽相同。

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/

### 参数

##### workingMode

​	编码信息的输出模式。列值覆盖(overwrite)模式表示将编码值写入columnSet集合配置的对应待编码列。添加列(addColumn)模式，表示将编码值写入新增列中，新增列的名字为columnSet集合配置的对应待编码列的名称加上后缀"_hash"。如编解码列名为col1，则新增的对应列名为col1_hash。

##### columnSet

​	待哈希集合。

###### columnName

​	待哈希列的列名。 支持的列类型为字符串与字节数组。

​	**数据类型**: String

​	**是否可选**: 否

