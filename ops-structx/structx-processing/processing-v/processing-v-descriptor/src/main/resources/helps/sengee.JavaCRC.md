# JavaCRC

​	**标签：** 

### 描述

​	Java循环冗余码。循环冗余校验（Cyclic Redundancy Check， CRC）是一种根据网络数据包或计算机文件等数据产生简短固定位数校验码的一种信道编码技术，主要用来检测或校验数据传输或者保存后可能出现的错误。它是利用除法及余数的原理来作错误侦测的。

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

​	待编码列集合。

###### columnName

​	待编码列的列名。 支持的列类型为字符串与字节数组。

​	**数据类型**: String

​	**是否可选**: 否

##### algorithm

​	支持的CRC算法有PureJavaCrc32和PureJavaCrc32C等。

​	**数据类型**: String

​	**是否可选**: 否
