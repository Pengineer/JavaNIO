package hust.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 可以被注册的通道才有register()方法，才能被注册到选择器。
 * 
 * while(一直等待, 直到有接收连接就绪事件, 读就绪事件或写就绪事件发生){  //阻塞（事件的等待仍然是一个阻塞的方式，但是事件的处理是非阻塞的）
 *      //开始轮询
 * 		if(有客户连接)
 * 			接收客户的连接;                                           //非阻塞
 * 		if(某个 Socket 的输入流中有可读数据)
 * 			从输入流中读数据;                                         //非阻塞
 * 		if(某个 Socket 的输出流可以写数据)
 * 			向输出流写数据;                                           //非阻塞
 * 	}
 * 
 * SelectableChannel通道可以工作在阻塞模式下，也可以工作在非阻塞模式下，默认工作在阻塞模式下。与Selector结合使用时，一般使用非阻塞模式。
 * 非SelectableChannel类型的通道（FileChannel）只能工作在阻塞模式下，因此不能与Selector配合使用。
 * 
 * ServerSocketChannel或SocketChannel通过register()方法向Selector注册事件时，register()方法会创建一个SelectionKey对象，这个SelectionKey
 * 对象是用来跟踪注册事件的句柄，在SelectionKey对象的有效期间，Selector会一直监控与SelectionKey对象相关的事件，SelectionKey一共定义了4种事件：
 * ● SelectionKey.OP_ACCEPT: 接收连接就绪事件, 表示服务器监听到了客户连接, 服务器可以接收这个连接了. 常量值为 16.(00010000)
 * ● SelectionKey.OP_CONNECT: 连接就绪事件, 表示客户与服务器的连接已经建立成功. 常量值为 8.(00001000)
 * ● SelectionKey.OP_WRITE: 写就绪事件, 表示已经可以向通道写数据了. 常量值为 4.(00000100)
 * ● SelectionKey.OP_READ: 读就绪事件, 表示通道中已经有了可读数据, 可以执行读操作了. 常量值为 1.(00000001)
 * 以上常量分别占据不同的二进制位, 因此可以通过二进制的或运算 "|", 来将它们进行任意组合.
 * 
 * @author liangjian
 * @see http://blog.csdn.net/xiaoxiaoniaoer1/article/details/7433361
 *
 */
public class Server {
	
	private Selector selector = null;
	private Charset charset = Charset.forName("UTF-8");
	
	public void init() throws IOException {
		// 通过自身静态方法开启一个通道选择器
		selector = Selector.open();
		// 通过open方法来打开一个未绑定的ServerSocketChannel实例
		ServerSocketChannel server = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 30009);
		// 获取一个与通道关联的Socket，并绑定主机和端口号
		server.socket().bind(isa);
		// 设置通道为非阻塞模式
		server.configureBlocking(false);
		// 将通道注册到选择器，并告诉选择器对该通道所需要监控的事件
		server.register(selector, SelectionKey.OP_ACCEPT);
		// select()方法以阻塞的方式返回你所感兴趣的事件（并不是监控的所有事件）
		while(selector.select() > 0) {
			// 依次处理selector上的每个已选择的SelectionKey
			for(SelectionKey key :selector.selectedKeys()) {
				// 从selector上的已选择Key集中删除正在处理的SelectionKey
				selector.selectedKeys().remove(key);
				// 如果key对应的通道包含客户端的连接请求（客户端请求建立连接）
				if(key.isAcceptable()) {
					// 接受客户端连接请求（每一个请求都对应一个Channel，不再是单个的线程）
					SocketChannel sc = server.accept();
					// 设置采用非阻塞模式
					sc.configureBlocking(false);
					// 将该SocketChannel也注册到selector，声明监听通道上的读事件
					sc.register(selector, SelectionKey.OP_READ);
					// 事件处理完毕，将key对应的Channel设置成准备接受其他请求（上面将key移除了连接监听事件，需要向Selector重新声明一下连接事件）
					key.interestOps(SelectionKey.OP_ACCEPT);
				}
				// 如果key对应的通道有数据需要读取（客户端发送数据）
				if(key.isReadable()) {
					// 获取该SelectionKey对应的Channel，该Channel中有可读的数据
					SocketChannel sc = (SocketChannel)key.channel();
					// 定义准备执行读取数据的字节缓冲区
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					String content = "";
					// 开始将通道中的数据读取到buffer
					try {
						while(sc.read(buffer) > 0) {
							buffer.flip();// 设置position和limit已准备读取buffer中的数据
							content += charset.decode(buffer);
							buffer.clear();
						}
						System.out.println("******服务端收到的数据：" + content);
						// 事件处理完毕，将key对应的Channel设置成准备下一次读取（因为上面将key移除了，需要向Selector重新声明一下）
						key.interestOps(SelectionKey.OP_READ);
					// 如果捕捉到该key对应的Channel出现了异常，即表明该Channel对应的Client出现了问题，所以从Selector中取消key的注册
					} catch (IOException e) {
						// 从Selector中删除指定的SelectionKey
						key.cancel();
						if (key.channel() != null) {
							key.channel().close();
						}
					}
					// 如果content的长度大于0，即聊天信息不为空
					if (content.length() > 0) {
						// 遍历该selector里注册的所有SelectKey
						for (SelectionKey k : selector.keys()) {
							// 获取该k对应的Channel
							Channel targetChannel = k.channel();
							// 如果该channel是SocketChannel对象
							if (targetChannel instanceof SocketChannel) {
								// 将读到的内容写入该Channel中
								SocketChannel dest = (SocketChannel) targetChannel;
								dest.write(charset.encode("******服务端给客户端回应：server 收到数据了！"));
							}
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Server().init();
	}
}
