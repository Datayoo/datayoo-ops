# AttachmentAggregate

​	**标签：** 

### 描述

​	将数据流按照给定的分组列，打包成附件列表。

### 输入端口

#### streamIn

​	数据流输入端口

​	**输入类型**：/dataStream，非标准数据流，是扩展了可分组列的数据流

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/attachmented

### 参数

##### groupingColumn

​	附件待打包分组列。

​	**数据类型**: String

​	**是否可选**: 否
