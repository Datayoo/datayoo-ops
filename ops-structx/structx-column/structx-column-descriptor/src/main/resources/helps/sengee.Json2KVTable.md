# Json2KVTable

​	**标签：** 

### 描述

​	将Json对象转换为键值对表格。 若无法预先知道Json数据的格式，可使用此算子做基础的Json数据的结构话处理。若Json数据是一个Object元素，那么格式化后的数据为一个以Json对象的字段名为Key，字段值为Value的KV表格，并输出。

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/

### 输出端口

#### dataOut

​	KV表输出端口

​	**输出类型**：/kvTable

### 参数

#### dataColumn

​	Json数据列名

​	**数据类型**: String

​	**是否可选**: 否
