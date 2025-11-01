package nettydemo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class DiscardServer {
    public void run(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup  = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChildChannelHandler());
            //绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();
            //等待服务监听端口关闭
            f.channel().closeFuture().sync();
        }finally {
            //退出，释放线程资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ByteBuf byteBuf= Unpooled.copiedBuffer("$".getBytes());
            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,byteBuf));
            socketChannel.pipeline().addLast(new DiscardServerHandler());
        }
    }

    class DiscardServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {

            try {
                ByteBuf in = (ByteBuf) msg;
                System.out.println("传输内容是");
                System.out.println(in.toString(CharsetUtil.UTF_8));
                ByteBuf resp= Unpooled.copiedBuffer("收到信息$".getBytes());
                ctx.writeAndFlush(resp);
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
        new DiscardServer().run(8080);
    }
}
