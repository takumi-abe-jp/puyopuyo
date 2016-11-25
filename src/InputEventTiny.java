// InputEventTiny.java
// written by mnagaku

/**
 * InputEventTinyクラス<br>
 * キーボード、マウスの状態を保持。
 * @author mnagaku
 */
public class InputEventTiny {

/** 動作(マウスイベント、キーイベントの動作を表すIDを入れる) */
	int id;

/** マウスイベントの発生したx座標 */
	int x;

/** マウスイベントの発生したy座標 */
	int y;

/** キーイベントの発生したキーのコード */
	int keyCode;

/** コンストラクタ。
 * マウスイベント用
*/
	InputEventTiny(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.keyCode = 0;
	}


/** コンストラクタ。
 * キーボードイベント用
*/
	InputEventTiny(int id, int keyCode) {
		this.id = id;
		this.x = 0;
		this.y = 0;
		this.keyCode = keyCode;
	}


/** ID(動作)取得 */
	public int getID() {
		return id;
	}


/** X座標取得 */
	public int getX() {
		return x;
	}


/** Y座標取得 */
	public int getY() {
		return y;
	}


/** KeyCode取得 */
	public int getKeyCode() {
		return keyCode;
	}
}

