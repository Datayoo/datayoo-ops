# StratifiedSampling

​	**标签：** 

### 描述

​	对数据集进行分层采样。

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/

### 参数

##### sample

​	抽样策略。支持绝对值(absolute)及相对比例(relative)两种策略。absolute指抽取数据集中前sampleSize条数据。relative指按照比例，从第一条开始，抽取数据集sampleRatio百分比的数据。

​	**数据类型**: String

​	**是否可选**: 否

​	**默认值**: absolute

##### sampleSize

​	抽样数量。 当抽样策略为absolute时可填写。

​	**数据类型**: uinteger

​	**是否可选**: 否

​	**默认值**: 0

##### sampleRatio

​	抽样比例。 当抽样策略为relative时可填写。

​	**数据类型**: double

​	**是否可选**: 否

​	**默认值**: 0.0

##### stratifiedColumn

​	分层列。选择的分层列中每个不同的值作为一个分层进行数据抽取。当抽样策略为relative时可填写。

​	**数据类型**: String

​	**是否可选**: 否
