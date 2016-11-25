// Queue.java
// written by mnagaku

import java.util.Vector;

/**
 * Queueクラス<br>
 * Vectorを拡張してQueueを実現。
 * MacOS9以前の環境MRJ2.2.6やMSVMでも動作するよう、Java1.1.x環境を対象に
 * Vectorクラスの古いメソッドを使って記述。
 * 生成時、特に初期化処理がいらないので、デフォルトコンストラクタを使用。
 * @author mnagaku
 */
public class Queue extends Vector {

/**
 * Queueに入れる
 * @param element 入れる要素
 * @return 入れた要素
*/
	public Object enqueue(Object element) {
		addElement(element);
		return element;
	}


/**
 * Queueから出す
 * @return 取り出せた要素。Queueに要素がなければnull
*/
	public Object dequeue() {
		Object ret;
		if(isEmpty())
			return null;
		ret = elementAt(0);
		removeElementAt(0);
		return ret;
	}
}

