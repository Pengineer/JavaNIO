package hust.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * buffer是一个用于特定基本类型数据的容器。 
 * 
 * 缓冲区是特定基本类型元素的线性有限序列。除内容外，缓冲区的基本属性还包括容量、限制和位置：
 * 		缓冲区的容量: 是它所包含的元素的数量。缓冲区的容量不能为负并且不能更改。 
 * 		缓冲区的限制: 是第一个不应该读取或写入的元素的索引。缓冲区的限制不能为负，并且不能大于其容量。 
 * 		缓冲区的位置: 是下一个要读取或写入的元素的索引。缓冲区的位置不能为负，并且不能大于其限制。 
 * 对于每个非 boolean 基本类型，此类都有一个子类与之对应。
 * 
 * 做标记和重置 
 * 		缓冲区的标记 是一个索引，在调用 reset 方法时会将缓冲区的位置重置为该索引。并非总是需要定义标记，但在定义标记时，不能将其定义为负数，
 * 并且不能让它大于位置。如果定义了标记，则在将位置或限制调整为小于该标记的值时，该标记将被丢弃。如果未定义标记，那么调用 reset 方法将导致
 * 抛出 InvalidMarkException。 
 * 
 * 不变式 
 * 		标记、位置、限制和容量值遵守以下不变式： 0 <= 标记 <= 位置 <= 限制 <= 容量 
 * 		新创建的缓冲区总有一个 0 位置和一个未定义的标记。初始限制可以为 0，也可以为其他值，这取决于缓冲区类型及其构建方式。一般情况下，缓冲区
 * 的初始内容是未定义的。 
 * 
 * 清除、反转和重绕 
 * 		除了访问位置、限制、容量值的方法以及做标记和重置的方法外，此类还定义了以下可对缓冲区进行的操作： 
 * 		clear() 使缓冲区为一系列新的通道读取或相对放置 操作做好准备：它将限制设置为容量大小，将位置设置为 0。 
 * 		flip() 使缓冲区为一系列新的通道写入或相对获取 操作做好准备：它将限制设置为当前位置，然后将位置设置为 0。 
 * 		rewind() 使缓冲区为重新读取已包含的数据做好准备：它使限制保持不变，将位置设置为 0。
 * 
 * 缓冲区的实现是线程不安全，即多个当前线程使用缓冲区是不安全的。如果一个缓冲区由不止一个线程使用，则应该通过适当的同步来控制对该缓冲区的访问。 
 *  
 * @author liangjian
 * @since 2016-02-27
 * 
 */
public class BufferTest {
	public static void main(String[] args) {
		//分配一个48字节capacity的ByteBuffer
		ByteBuffer buf1 = ByteBuffer.allocate(48);
		
		//分配一个可存储1024个字符的CharBuffer
		CharBuffer buf2 = CharBuffer.allocate(1024);
	}
}
