# Sampling

​	**标签：** 

### 描述

​	对数据集进行采样。

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

​	抽样策略。支持absolute(绝对值)，relative(相对比例)，probability(抽样概率)。absolute指抽取数据集中前sampleSize条数据。relative指按照比例，从第一条开始，抽取数据集sampleRatio百分比的数据。probability指等概率抽取，每条数据被抽取到概率一样。

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

##### sampleProbability

​	抽样概率。 当抽样策略为probability时可填写

​	**数据类型**: double

​	**是否可选**: 否

​	**默认值**: 0.0