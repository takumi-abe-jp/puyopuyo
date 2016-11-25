// ScrollSpace.java
// written by mnagaku

import java.awt.Color;
import java.awt.Graphics;

/**
 * ScrollSpaceクラス<br>
 * 背景用にスクロールする星空を描く。
 * @author mnagaku
 */
class GameBack implements Draw {

/** 描画面の大きさ */
	int w,w2, h,h2;


/**
 * コンストラクタ。
 * 描画準備。
 * @param w 描画面の幅
 * @param h 描画面の高さ
 */
	GameBack(int w,int w2, int h,int h2) {
		this.w = w;
		this.h = h - 1;
		this.w2 = w2;
		this.h2 = h2 - 1;
	}


	public boolean drawing(Graphics g, Plane pln) {
		g.setColor(Color.yellow);
		g.fillRect(w, h, w2, h2);
		
		return true;
	}
}