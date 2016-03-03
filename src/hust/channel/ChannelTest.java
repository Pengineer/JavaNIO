package hust.channel;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 基于文件通道的缓冲区读写
 * 
 * @author liangjian
 * @since 2016-02-27
 *
 */
public class ChannelTest {

	public static void main(String[] args) throws Exception {
		//获取一个FileChannel
		RandomAccessFile aFile = new RandomAccessFile("note.txt", "rw");
		FileChannel inChannel = aFile.getChannel();

		//声明一个字节缓冲区
		ByteBuffer buf = ByteBuffer.allocate(48);

		//将通道中的内容读到缓冲区，返回读入到缓冲区的字节数
		int bytesRead = inChannel.read(buf);
		while (bytesRead != -1) {
			System.out.println("Read " + bytesRead);
			//使缓冲区为一系列新的通道写入或相对获取 操作做好准备：它将限制设置为当前位置，然后将位置设置为 0
			//为了保证从缓冲区的起始位置开始读取数据，必须将position归0，同时将读取的最高位置设置为当前position的位置
			buf.flip();
		
			//读取缓冲区的数据
			while(buf.hasRemaining()){
				System.out.print((char)buf.get());
			}
		
			//使缓冲区为一系列新的通道读取或相对放置 操作做好准备：它将限制设置为容量大小，将位置设置为 0
			//在下次写入之前position的位置必须归0，因为上面get方法会使得position不断递增
			buf.clear();
			bytesRead = inChannel.read(buf);
		}
		aFile.close();
	}
}
