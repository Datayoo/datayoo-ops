# RowTransposition

​	**标签：**

### 描述

​	行列转换

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/

### 参数

#### headerColumn

​	作为行转列后列头的列。

​	**数据类型**: string

​	**是否可选**: 否

#### columnSet

​	转换为行数据的列集合。

##### columnName

​	转换为行数据的列。

​	**数据类型**: String

​	**是否可选**: 否

#### groupColumnSet

​	作为分组数据的列集合。

##### columnName

​	作为分组数据的列。

​	**数据类型**: String

​	**是否可选**: 是

#### outputColumnSet

​	自定义输出列。

##### columnName

​	列名，必须和headerColumn列的实际值一致。

​	**数据类型**: String

​	**是否可选**: 否

##### columnType

​	列数据类型

​	**数据类型**: DataType

​	**是否可选**: 否