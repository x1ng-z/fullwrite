# 工程简介
收发数据格式

 +---------+------------+-------------+-------------+---------------------------------------+      
| Header   |   comand   |   nodeid    |  Length     | Actual Content                        |
|  0x8818  |     0x01   |   FFFFFFFF  | 0x00000C    | {"aa.pv":12,"bb.pv":22}               |     
+----------+------------+-------------+-------------+---------------------------------------+
| Header   |    comand  |  nodeid     |paramer      |    context                            |
|(2 bytes) |    (1byte) | (4bytes)    | length      |                                       |
|  0-1     |      2     |  3-6        |(3bytes) 7-9 |  (n bytes) 10-n
|          |            |             |实际内容的长度|
command:
    code:0x01 RESULT
     code:0x02 PARAM
      code:0x03 HEART
       code:0x04 ACK
       code:0x05 STOP
# 延伸阅读
