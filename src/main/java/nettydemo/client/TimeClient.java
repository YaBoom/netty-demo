package nettydemo.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import nettydemo.client.TimeClientHandler;

public class TimeClient {
    public void connect(int port,String host)throws Exception{
        //配置客户端
        System.out.println(port+"--"+host);
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        try {
            Bootstrap b=new Bootstrap();
            b.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ByteBuf byteBuf= Unpooled.copiedBuffer("$".getBytes());
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,byteBuf));
                            socketChannel.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture f = b.connect(host,port).sync();
            //等待服务监听端口关闭
            f.channel().closeFuture().sync();
        }finally {
            //优雅退出，释放线程资源
            eventLoopGroup.shutdownGracefully();
        }
    }
    class TimeClientHandler extends ChannelHandlerAdapter {
        private byte[] req;
        public TimeClientHandler(){
            req="$tmb00035ET3318/08/22 11:5704026.75,027.31,20.00,20.00$".getBytes();
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf message=null;
            for(int i=0;i<100;i++){
                message= Unpooled.buffer(req.length);
                message.writeBytes(req);
                ctx.writeAndFlush(message);
            }
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                ByteBuf in = (ByteBuf) msg;
                System.out.println(in.toString(CharsetUtil.UTF_8));
            }  finally {
                ReferenceCountUtil.release(msg);
            }
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 出现异常就关闭
            cause.printStackTrace();
            ctx.close();
        }
    }
    public static void main(String[] args) throws Exception {
        new TimeClient().connect(8080,"localhost");
    }
}

