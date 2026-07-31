# TextExtractor

​	**标签：** 

### 描述

​	抽取数据流中的文本。如：可抽取txt、word(doc,docx,docm,dotx,dotm)、pdf、excel(xls, xlsx,xlsm,xlt)、ppt(ppt,pptx,potm,potx,ppa,ppam,ppsx,ppsm)、rtf、odf等各类文件中的文本内容。

### 输入端口

#### streamIn

​	数据流输入端口

​	**输入类型**：/dataStream

### 输出端口

#### streamOut

​	数据流输出端口。当无法从输入的数据流中抽取出文本时，将数据流从此端口输出。

​	**输出类型**：/dataStream

#### blockOut

​	数据块输出端口。将识别到的文本以数据块结构从此端口输出。

​	**输出类型**：/streamBlock

### 参数

##### textLimit

​	抽取出的文本的最大限制

​	**数据类型**: Uinteger

​	**是否可选**: 否

​	**缺省值**: 100000

##### ignorePageNo

​	是否忽略抽取文件中的页号

​	**数据类型**: Boolean

​	**是否可选**: 否

​	**缺省值**: false

##### ignoreSuperscript

​	是否忽略抽取文本中的上下标

​	**数据类型**: Boolean

​	**是否可选**: 否

​	**缺省值**: false

##### ignoreLink

​	是否忽略抽取超连接中的url

​	**数据类型**: Boolean

​	**是否可选**: 否

​	**缺省值**: false

##### ignoreEmbededFileName

​	是否忽略抽取嵌入文件的文件名字

​	**数据类型**: Boolean

​	**是否可选**: 否

​	**缺省值**: false
