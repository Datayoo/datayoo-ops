# COO2Table

​	**标签：** 

### 描述

​	COO格式转为表。CSM格式为(row_index, column_index, value),转为指定header的表数据结构.系统内变体为(table_name,row_index, column_index, value)

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/csm

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/

### 参数

#### type

​	转换模式。分为行模式和列模式。行模式指的是以行作为数据维度，认为行号相同的数据是一个完整数据。列模式则认为列号相同的数据为一条完整数据。

​	**数据类型**: string

​	**是否可选**: 否

#### headerType

​	指定header的模式。值模式指的是指定一行或者一列作为header，与type联动。自定义模式指的是自行定义列头，并指定所在行或者列。

​	**数据类型**: string

​	**是否可选**: 否

#### rowHeaderIndex

​	行模式下header所在行号。

​	**数据类型**: int

​	**是否可选**: 否

#### colHeaderIndex

​	列模式下header所在列号。

​	**数据类型**: int

​	**是否可选**: 否

#### rowValueIndex

​	行模式下数据起始行号。

​	**数据类型**: int

​	**是否可选**: 否

#### colValueIndex

​	列模式下数据起始列号。

​	**数据类型**: int

​	**是否可选**: 否

#### columnSet

​	手动模式下header的列集合。

##### colName

​	header。

​	**数据类型**: String

​	**是否可选**: 是

##### colType

​	header对应的字段类型。

​	**数据类型**: String

​	**是否可选**: 否

##### index

​	header所在行号/列号 

​	**数据类型**: int

​	**是否可选**: 否