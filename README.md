# netty-demo

#### 介绍
采用 client 和 server 端进行通道传输
client 利用 EventLoopGroup使用NioSocketChannel初始化channel,并配置端口，同步等待连接服务端响应，在client端中自定义ChannelHandlerAdapter的子类重写channelActive方法，从Buffer里把byte[]数组的信息写入进ChannelHandlerContext上下文里。

server 利用 ServerBootstrap 初始化化子ChildChannelHandler并加入了自定义ChannelHandlerAdapter的子类，子类对channelRead 和 exceptionCaught两个方法进行重写。

#### 依赖包

采用io.netty.netty-all 5.0.0 Alphal版本