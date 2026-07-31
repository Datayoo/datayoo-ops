# HmacHash

​	**标签：** 

### 描述

​	HMAC是密钥相关的哈希运算[消息认证码]（Hash-based Message Authentication Code）的缩写，由H.Krawezyk，M.Bellare，R.Canetti于1996年提出的一种基于Hash函数和密钥进行消息认证的方法，并于1997年作为RFC2104被公布，并在[IPSec]和其他网络协议（如[SSL]）中得以广泛应用，现在已经成为事实上的Internet安全标准。它可以与任何迭代散列函数捆绑使用。

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

​	待哈希集合。

###### columnName

​	待哈希列的列名。 支持的列类型为字符串与字节数组。

​	**数据类型**: String

​	**是否可选**: 否

##### algorithm

​	支持的算法有HMAC_MD5， HMAC_SHA_1， HMAC_SHA_224，HMAC_SHA_256，HMAC_SHA_384，HMAC_SHA_512等。

​	**数据类型**: String

​	**是否可选**: 否
