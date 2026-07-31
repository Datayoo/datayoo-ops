# MurmurHash

​	**标签：** 

### 描述

​	Murmur哈希。MurmurHash 是一种非 加密型哈希函数，适用于一般的哈希检索操作。 由Austin Appleby在2008年发明， 并出现了多个变种，都已经发布到了 公有领域(public domain)。与其它流行的哈希函数相比，对于规律性较强的key，MurmurHash的随机分布特征表现更良好。

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

​	待哈希列集合。

###### columnName

​	待哈希列的列名。 支持的列类型为字符串与字节数组。

​	**数据类型**: String

​	**是否可选**: 否

##### algorithm

​	Murmur哈希支持的算法。如：MurmurHash2_32，MurmurHash2_64，MurmurHash3_32，MurmurHash3_32x86，MurmurHash3_128，MurmurHash3_128x64等。

​	**数据类型**: String

​	**是否可选**: 否
