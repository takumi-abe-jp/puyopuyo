// Sprite.java
// written by mnagaku

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * スプライトシステムを実現するSpriteクラス<br>
 * 画像を部品として、描画、アニメーションを行う為の機能を提供します。
 * 読み込んだ画像をプールしておき、必要な時に必要な場所に配置します。
 * 画像と同じように、文字列も表示出来るようにします。
 * 画像や文字は、スプライトプレーンに乗せられ、
 * プレーンが全て重なった形で描画されます
 * @author mnagaku
 */
public class Sprite {
/** スプライトプレーンのモード */
	static final int NULL_MODE = 0, GRP_MODE = 1, STR_MODE = 2, DRW_MODE = 8,
		CENTER_STR_MODE = 6;
/** 描画対象となる面の大きさ */
	int canvasWidth, canvasHeight;
/** 描画対象となる生成元を記憶しておく場所 */
	Container owner;
/** 画像を読み込んで置いておく画像プール */
	Hashtable grp;
/** バックバッファ */
	Image backGrp = null;
/** 画像の読み込み状態を監視する */
	MediaTracker tracker;
/** スプライトプレーン */
	Hashtable planes;
/** スプライト描画時の描画順 */
	Integer spriteList[];


/**
 * コンストラクタ。
 * 画像プール、スプライトプレーンを生成し、描画面情報を記憶。
 * @param canvasWidth 描画対象面の幅
 * @param canvasHeight 描画対象面の高さ
 * @param owner 描画対象となる生成元
*/
	public Sprite(int canvasWidth, int canvasHeight, Container owner) {
		int i, j;
// 画像プールの生成
		grp = new Hashtable();
// スプライトプレーンの生成
		planes = new Hashtable();
// 描画面情報の記憶
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.owner = owner;
// 画像読込状態を管理するMediaTrackerの生成
		tracker = new MediaTracker(owner);
	}


/**
 * エラー、警告の表示。
*/
	void infomation(String info, Exception e) {
		System.out.println(info);
		System.out.println("java.version : "
			+ System.getProperty("java.version"));
		System.out.println("java.vendor : "
			+ System.getProperty("java.vendor"));
		if(e != null)
			e.printStackTrace();
	}


/**
 * 画像プールに画像を読み込む。
 * 画像プールの何番目の位置に、何と言う名前の画像を読み込むか指定する。
 * 読み込んだ画像は、スプライトプレーンに乗せて使うことができるようになる。
 * @param no 読み込んだ画像を格納する、画像プールの場所(インデックス、0～)
 * @param file 読み込む画像ファイル名
 * @return 読み込みが正常に終了した場合はtrue
*/
	public boolean addGrp(int no, String file) {
		if(getClass().getResource(file) == null) {
			infomation("Error : Do not find [" + file + "].", null);
			return false;
		}
		try {
			grp.put(new Integer(no),
				Toolkit.getDefaultToolkit()
					.getImage(getClass().getResource(file)));
		} catch (Exception e) {
			infomation("Warning : Do not create image data.", e);
			return false;
		}
		tracker.addImage((Image)(grp.get(new Integer(no))), 1);
		return true;
	}


/**
 * 画像の読み込みを待つ。
 * 画像プールに画像を読み込んでも、読み込みが完了しないと画像が使えないので
 * 読み込み中の画像が全て読み込み終わるまで待つ。
 * @return 読み込み中に例外が発生したらfalse
*/
	public boolean waitLoad() {
		try {
			tracker.waitForID(1);
		} catch (InterruptedException e) {
			infomation("Warning : Problem occurred in waitLoad().", e);
			return false;
		}
		return true;
	}


/**
 * 画像の読み込み状態を調べる。
 * 画像プールに画像を読み込んでも、読み込みが完了しないと画像が使えないので
 * 読み込み中の画像が全て読み込み終わったか調べる。
 * 画像の裏読み込みを行う場合に使う。
 * @return 読み込み正常終了なら1、読み込み中なら0、エラーが発生したら-1
*/
	public int isLoaded() {
		if(tracker.checkID(1) == false)
			return 0;
		if(tracker.isErrorID(1) == false)
			return 1;
		return -1;
	}


/**
 * スプライトプレーンに画像を登録。
 * 画像プール内の画像を、実際に表示する対象としてプレーンに登録する。
 * プレーンにはアニメーションを行わせるために、複数の画像を登録できる。
 * 画像を登録すると、プレーンは画像表示用に設定される。
 * @param planeNo 登録するプレーンの番号(0～)
 * @param animeNo アニメーションさせる場合、何番目の画像か。させないなら0。
 * @param grpNo 登録する画像。画像プール内での番号(0～)
 * @return 登録が成功したらtrue
*/
	public boolean setPlaneGrp(int planeNo, int animeNo, int grpNo) {
		Integer pno = new Integer(planeNo);
		Plane pln;
		if((pln = (Plane)(planes.get(pno))) == null) {
			pln = new Plane();
			planes.put(pno, pln);
		}
		pln.animeNo = new Integer(animeNo);
		pln.grp.put(pln.animeNo, grp.get(new Integer(grpNo)));
		pln.planeMode = GRP_MODE;
		pln.view = true;
		pln.str = null;
		pln.font = null;
		pln.color = null;
		pln.draw = null;
		return true;
	}


/**
 * スプライトプレーンに画像を登録。
 * 画像プール内の画像を、実際に表示する対象としてプレーンに登録する。
 * 画像を登録すると、プレーンは画像表示用に設定される。
 * アニメを利用しない場合に使用する。
 * @param planeNo 登録するプレーンの番号(0～)
 * @param grpNo 登録する画像。画像プール内での番号(0～)
 * @return 登録が成功したらtrue
*/
	public boolean setPlaneGrp(int planeNo, int grpNo) {
		Integer pno = new Integer(planeNo);
		Plane pln;
		if((pln = (Plane)(planes.get(pno))) == null) {
			pln = new Plane();
			planes.put(pno, pln);
		}
		pln.animeNo = new Integer(0);
		pln.grp.put(pln.animeNo, grp.get(new Integer(grpNo)));
		pln.planeMode = GRP_MODE;
		pln.view = true;
		pln.str = null;
		pln.font = null;
		pln.color = null;
		pln.draw = null;
		return true;
	}


/**
 * スプライトプレーンの座標を設定。
 * スプライトプレーンの画面上での表示位置を指定する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param x x座標
 * @param y y座標
 * @return 成功したらtrue
*/
	public boolean setPlanePos(int planeNo, int x, int y) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		pln.posX = x;
		pln.posY = y;
		return true;
	}


/**
 * スプライトプレーンの座標に加算。
 * スプライトプレーンの画面上での表示位置を、現在の位置から加算して移動する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param x x方向の移動量
 * @param y y方向の移動量
 * @return 成功したらtrue
*/
	public boolean setPlaneMov(int planeNo, int x, int y) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		pln.posX += x;
		pln.posY += y;
		return true;
	}


/**
 * スプライトプレーンのx座標を返す。
 * スプライトプレーンの画面上での表示位置を返す。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @return x方向の表示位置
*/
	public int getPlanePosX(int planeNo) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return 0xffff;
		return pln.posX;
	}


/**
 * スプライトプレーンのy座標を返す。
 * スプライトプレーンの画面上での表示位置を返す。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @return y方向の表示位置
*/
	public int getPlanePosY(int planeNo) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return 0xffff;
		return pln.posY;
	}


/**
 * スプライトプレーンのアニメモードを設定。
 * 対象となるプレーンをアニメーションさせるかどうか設定する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param mode アニメーションさせるならtrue、させないならfalse
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneAnime(int planeNo, boolean mode) {
		int i, j;
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		if(pln.planeMode != GRP_MODE)
			return false;
		if((pln.anime = mode) == true) {
			pln.animeList = new Integer[pln.grp.size()];
			Enumeration enumeration = pln.grp.keys();
			for(i = 0; enumeration.hasMoreElements(); i++)
				pln.animeList[i] = (Integer)(enumeration.nextElement());
// Java1.1.xにはsort()がないので、自分で並べ替える
//			Arrays.sort(pln.animeList);
			Integer tmp;
			for(i = 0; i < pln.animeList.length - 1; i++)
				for(j = i + 1; j < pln.animeList.length; j++)
					if(pln.animeList[i].intValue()
						> pln.animeList[j].intValue()) {
						tmp = pln.animeList[i];
						pln.animeList[i] = pln.animeList[j];
						pln.animeList[j] = tmp;
					}
		}
		else
			pln.animeList = null;
		return true;
	}


/**
 * スプライトプレーンに文字列を設定。
 * 対象となるプレーンを文字列表示用に設定する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param str このプレーンで表示させる文字列
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneString(int planeNo, String str) {
		Integer pno = new Integer(planeNo);
		Plane pln;
		if((pln = (Plane)(planes.get(pno))) == null) {
			pln = new Plane();
			planes.put(pno, pln);
		}
		pln.font = new Font("Monospaced", Font.PLAIN, 16);
		pln.color = new Color(0, 0, 0);
		pln.str = str;
		pln.planeMode = STR_MODE;
		pln.view = true;
		pln.grp.clear();
		pln.anime = false;
		pln.animeNo = null;
		pln.draw = null;
		return true;
	}


/**
 * スプライトプレーンにセンタリング表示文字列を設定。
 * 対象となるプレーンを文字列表示用に設定する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param str このプレーンで表示させる文字列
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneCenterString(int planeNo, String str) {
		Integer pno = new Integer(planeNo);
		Plane pln;
		if((pln = (Plane)(planes.get(pno))) == null) {
			pln = new Plane();
			planes.put(pno, pln);
		}
		pln.font = new Font("Monospaced", Font.PLAIN, 16);
		pln.color = new Color(0, 0, 0);
		pln.str = str;
		pln.planeMode = CENTER_STR_MODE;
		pln.view = true;
		pln.grp.clear();
		pln.anime = false;
		pln.animeNo = null;
		pln.draw = null;
		return true;
	}


/**
 * スプライトプレーンのFont属性を設定。
 * プレーンの文字表示に使うフォントを設定する。
 * 与える情報はFontクラスの生成に使われる。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param name フォント名
 * @param style スタイル
 * @param size サイズ
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneFont(int planeNo,String name,int style,int size) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		if((pln.planeMode & STR_MODE) == 0)
			return false;
		if(name == null)
			name = "Monospaced";
		if(style < 0)
			style = Font.PLAIN;
		if(size < 0)
			size = 16;
		pln.font = new Font(name, style, size);
		return true;
	}


/**
 * スプライトプレーンの色属性を設定。
 * RGB値をプレーンに記憶する。色は文字の描画に使われる。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param r 赤(0～255)
 * @param g 青(0～255)
 * @param b 緑(0～255)
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneColor(int planeNo, int r, int g, int b) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		if((pln.planeMode & STR_MODE) == 0)
			return false;
		pln.color = new Color(r, g, b);
		return true;
	}

/**
 * スプライトプレーンに描画ルーチンを設定。
 * 対象となるプレーンを描画ルーチン表示用に設定する。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param draw このプレーンで描画するルーチンを表すDrawクラスの実体
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneDraw(int planeNo, Draw draw) {
		Integer pno = new Integer(planeNo);
		Plane pln;
		if((pln = (Plane)(planes.get(pno))) == null) {
			pln = new Plane();
			planes.put(pno, pln);
		}
		pln.font = null;
		pln.color = null;
		pln.str = null;
		pln.planeMode = DRW_MODE;
		pln.view = true;
		pln.grp.clear();
		pln.anime = false;
		pln.animeNo = null;
		pln.draw = draw;
		return true;
	}


/**
 * スプライトプレーンの表示をオン/オフ。
 * 表示状態をオンにしたりオフにしたりする。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @param view オンならtrue、オフならfalse
 * @return 設定に成功したらtrue
*/
	public boolean setPlaneView(int planeNo, boolean view) {
		Plane pln;
		if((pln = (Plane)(planes.get(new Integer(planeNo)))) == null)
			return false;
		pln.view = view;
		return true;
	}


/**
 * スプライトプレーンの保持する情報を消去。
 * 使わなくなったスプライトプレーンを消去し、新しい用途に利用できるようにする。
 * @param planeNo 対象となるプレーンの番号(0～)
 * @return 消去に成功したらtrue
*/
	public boolean delPlane(int planeNo) {
		Integer pno = new Integer(planeNo);
		planes.remove(pno);
		return true;
	}


/**
 * 全てのスプライトプレーンの保持する情報を消去。
 * 全てのスプライトプレーンを消去し、まっさらな状態で利用できるようにする。
 * @return 消去に成功したらtrue
*/
	public boolean delPlaneAll() {
		planes.clear();
		return true;
	}


/**
 * 保持した内容に基づき、画面に描画。
 * Spriteクラスに保持された情報に基づき、描画を行う。
 * @param g 描画対象のGraphicsコンテキスト
 * @return 描画に成功したらtrue
*/
	public boolean paintScreen(Graphics g) {
		int i, j;
		Graphics gbg;
		Plane pln;

		if(backGrp == null) {
			backGrp = owner.createImage(canvasWidth, canvasHeight);
		}
		gbg = backGrp.getGraphics();

		spriteList = new Integer[planes.size()];
		Enumeration enumeration = planes.keys();
		for(i = 0; enumeration.hasMoreElements(); i++)
			spriteList[i] = (Integer)(enumeration.nextElement());
// Java1.1.xにはsort()がないので、自分で並べ替える
//		Arrays.sort(spriteList);
		Integer tmp;
		for(i = 0; i < spriteList.length - 1; i++)
			for(j = i + 1; j < spriteList.length; j++)
				if(spriteList[i].intValue() > spriteList[j].intValue()) {
					tmp = spriteList[i];
					spriteList[i] = spriteList[j];
					spriteList[j] = tmp;
				}

		for(i = 0; i < spriteList.length; i++) {
			pln = (Plane)(planes.get(spriteList[i]));
			if(pln.view == false)
				continue;
			if(pln.planeMode == GRP_MODE) {
				gbg.drawImage((Image)(pln.grp.get(pln.animeNo)),
					pln.posX, pln.posY, owner);
				if(pln.anime == true) {
					for(j = 0; pln.animeList[j] != pln.animeNo; j++);
					j = (j + 1) % pln.animeList.length;
					pln.animeNo = pln.animeList[j];
				}
// 左上基準で文字列表示
			} else if(pln.planeMode == STR_MODE) {
				gbg.setFont(pln.font);
				gbg.setColor(pln.color);
				gbg.drawString(pln.str, pln.posX, pln.posY+pln.font.getSize());
// 上辺中央基準で文字列表示
			} else if(pln.planeMode == CENTER_STR_MODE) {
				gbg.setFont(pln.font);
				gbg.setColor(pln.color);
				gbg.drawString(pln.str,
					pln.posX - gbg.getFontMetrics().stringWidth(pln.str) / 2,
					pln.posY + pln.font.getSize());
			} else if(pln.planeMode == DRW_MODE)
				pln.draw.drawing(gbg, pln);
		}
		gbg.dispose();
		g.drawImage(backGrp, owner.getInsets().left, owner.getInsets().top,
			owner);
		return true;
	}
}


/**
 * スプライトプレーン1枚分の情報を保持するPlaneクラス<br>
 * 生成時、特に初期化処理がいらないので、デフォルトコンストラクタを使用。
 * @author mnagaku
 */
class Plane {
/** 表示させるか否かのフラグ */
	boolean view = false;
/** アニメーションさせるか否かのフラグ */
	boolean anime = false;
/** プレーンの座標 */
	int posX = 0, posY = 0;
/** アニメーション時、何番目の画像を表示中なのかを保持 */
	Integer animeNo = null;
/** アニメーション用の画像リスト */
	Integer animeList[] = null;
/** プレーンのモード */
	int planeMode = 0;
/** プレーンに関連付けられた画像の番号 */
	Hashtable grp = new Hashtable();
/** 表示に使用する文字列を保持 */
	String str = null;
/** 表示に使用するフォント */
	Font font = null;
/** 文字表示に使用する色 */
	Color color = null;
/** drawモード時に描画処理を行うDrawインスタンスを保持 */
	Draw draw = null;
}

