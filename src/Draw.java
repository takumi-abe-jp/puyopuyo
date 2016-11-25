// Draw.java
// written by mnagaku

import java.awt.Graphics;

/**
 * スプライトに描画処理を行うDrawインターフェイス<br>
 * @author mnagaku
*/
interface Draw {

/**
 * 描画処理を実装。
 * プレーンの情報を使った描画処理を実装する。
 * @param g 描画面
 * @param plane プレーン
 * @return 描画が成功したらtrueを返すように実装する
*/
	public boolean drawing(Graphics g, Plane plane);
}