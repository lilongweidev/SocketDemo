# SocketDemo

## 具备功能

1. 服务端和客户端在同一个局域网中进行通讯。
2. 支持文字消息和表情消息。

## 使用过程

1. 同一个WIFI网络下，两台Android手机连接此WIFI。
2. 一台手机作为服务端，另一台手机作为客户端。
3. 服务端页面中可以看到IP地址，然后开启服务，等待客户端连接。
4. 客户端连接服务，在弹窗中输入服务端的Ip地址（Ip地址会根据不同的WIFI网络而变化）。
5. 任意一端发送消息都可以，另一端收到后会显示。
6. 可以发送表情，或者表情 + 文字。

## 版本说明

查看Release下的版本，版本号由低到高，对应功能由少到多。想查看不同版本下的代码直接下载zip包即可，master分支下是当前最新的代码。

### Release1.2
1. 优化表情列表。
2. 增加消息返回时的ip地址。
3. 封装基类BaseSocketActivity并使用。

### Release1.1
1. 分离服务端和客户端。
2. 添加Emoji2使用，增加表情消息。

### Release1.0
1. 同一个页面实现服务端和客户端。
2. 完成基本的Socket通讯。



