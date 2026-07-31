# StreamAggregate

​    **标签：**

### 描述

​ 对数据集根据水位线进行分组统计。水位线变化后，直接对前置数据进行聚合，不再等到读取所有数据。

### 输入端口

#### dataIn

​ 数据输入端口

​    **输入类型**：/

### 输出端口

#### dataOut

​ 数据输出端口

​    **输出类型**：/

### 参数

#### columnSet

​ 待聚合的列集合。

##### columnName

​ 待聚合的列名。

​    **数据类型**: String

​    **是否可选**: 否

##### function

​ 统计函数。支持函数有：avg(平均值),count(计数),first(首值),kurtosis(峰度),last(
末值),max(最大值),median(中位数),min(最小值),mode(Mode),notNull(非空)
,percentile(百分位),range(范围),semiVariance(半方差),skewness(偏度)
,standardDeviation(标准差),sum(总和),variance(方差),joint(字符串连接)
,jointByComma(字符串连接,将一列的值拼接成一个字符串，用逗号隔开),group2Array(
列向量化,将一列数据拼接到一个数组)

​    **数据类型**: String

​    **是否可选**: 否

##### alias

​ 列聚合后重命名。不可以是函数名(字段名)。不填默认以字段名命名。

​    **数据类型**: String

​    **是否可选**: 是

#### groupingParams

​ 待分组列集合。

##### columnName

​ 待分组的列名。

​    **数据类型**: String

​    **是否可选**: 否
