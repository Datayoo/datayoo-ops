# UnixCryptHash

​	**标签：** 

### 描述

​	基于UnixCrypt的加密哈希算法。

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

##### key

​	哈希加密密钥，一般被称之为salt。哈希密钥需要满足正则表达式："^[./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]{2,}$", 如：1234等

​	**数据类型**: String

​	**是否可选**: 否

