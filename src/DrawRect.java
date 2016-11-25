import java.awt.Color;
import java.awt.Graphics;

/**
 * DrawRectクラス<br>
 * 四角を描く。
 * @author mnagaku
 */
class DrawRect implements Draw {

/** 四角の大きさ */
	int w, h;
/** 四角の色 */
	Color color;


/**
 * コンストラクタ。
 * 描画準備。
 * @param w 四角の幅
 * @param h 四角の高さ
 * @param color 四角の色
 */
	DrawRect(int w, int h, Color color) {
		this.w = w;
		this.h = h;
		this.color = color;
	}


/**
 * 描画。
 * 保持している情報に基づいて、プレーンの位置に四角を描画する。
 * @param g 描画面
 * @param pln プレーン
 */
	public boolean drawing(Graphics g, Plane pln) {
		g.setColor(color);
		g.fillRect(pln.posX, pln.posY, w, h);
		return true;
	}
}