# PrivacyAggregate

​	**标签：** 

### 描述

​	隐私聚合。采用差分隐私算法对数据进行隐私计算。差分隐私（英语：differential privacy）是密码学中的一种手段，旨在提供一种当从统计数据库查询时，最大化数据查询的准确性，同时最大限度减少识别其记录的机会。算子目前支持的隐私计算函数有：均值、总数、分位数、总和、方差等。

### 输入端口

#### dataIn

​	数据输入端口

​	**输入类型**：/

### 输出端口

#### dataOut

​	数据输出端口

​	**输出类型**：/

### 参数

##### columnSet

​	待聚合的列集合。

###### columnName

​	待聚合的列名。 

​	**数据类型**: String

​	**是否可选**: 否

###### function

​	统计函数。支持函数有：avg(平均值),count(计数),percentile(百分位),sum(总和),variance(方差)

​	**数据类型**: String

​	**是否可选**: 否

###### alias

​	列聚合后重命名。不可以是函数名(字段名)。不填默认以字段名命名。

​	**数据类型**: String

​	**是否可选**: 是

##### groupingParams

​	待分组列集合。

###### columnName

​	待分组的列名。

​	**数据类型**: String

​	**是否可选**: 否

##### confuseParams

​	混淆参数设置。

###### epsilon

​	epsilon变量。

​	**数据类型**: Double

​	**是否可选**: 否

​	**缺省值**：1.0

###### delta

​	delta变量。

​	**数据类型**: Double

​	**是否可选**: 否

​	**缺省值**：0.15

###### noise

​	混淆算法。系统支持的混淆算法有：高斯混淆(GAUSSIAN),离散拉普拉斯(DISCRETELAPLACE),拉普拉斯(LAPLACE)

​	**数据类型**: String

​	**是否可选**: 否

​	**缺省值**：GAUSSIAN

###### lower

​	值阈下界。

​	**数据类型**: UInteger

​	**是否可选**: 否

​	**缺省值**：0

###### upper

​	值阈上界。

​	**数据类型**: UInteger

​	**是否可选**: 否

​	**缺省值**：500

###### maxPartitionsContributed

​	最大分区贡献。

​	**数据类型**: UInteger

​	**是否可选**: 否

​	**缺省值**：1

###### maxContributionsPerPartition

​	每分区最大贡献。

​	**数据类型**: UInteger

​	**是否可选**: 否

​	**缺省值**：1
