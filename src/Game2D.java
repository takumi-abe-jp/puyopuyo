// Game2D.java
// written by mnagaku

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;




/**
 * 2Dゲームフレームワーククラス Game2D<br>
 * 2Dゲーム作成に必要な、スプライト、音、入力を扱うプログラムのフレームワーク。
 * これをextendsして2Dゲームを作る。
 * MacOS9以前の環境MRJ2.2.6やMSVMでも動作するよう、Java1.1.x環境を対象に
 * スレッドによるタイマー割り込みを実装。
 * <br>
 * このクラスをextendsしたGameクラスは、
 * 次の様に記述されたhtmlファイルから呼び出される。
 * (アプレットがjarファイル化されている場合)
 * <br>
 * <pre>
 * &lt;html&gt;
 * &lt;head&gt;
 * &lt;title&gt;Game&lt;/title&gt;
 * &lt;/head&gt;
 * &lt;applet code=Game archive=Game.jar width=200 height=200&gt;
 * &lt;/applet&gt;
 * &lt;/html&gt;
 * </pre>
 * @author mnagaku
 */
abstract public class Game2D extends Applet {

/** ゲームの名前。Game2Dクラスを継承して作られたクラス名から生成される */
	static String GAME_NAME;

/** Game2DMainクラスを継承して作られたクラス名 */
	static String GAME_MAIN_NAME;

/** ウィンドウの描画面の幅 */
	int CANVAS_SIZE_W = 320;
/** ウィンドウの描画面の高さ */
	int CANVAS_SIZE_H = 240;

/** 画面再描画、メインループ処理を発生させる間隔。単位はミリ秒 */
	int SPEED = 100;

/** カーソルキーのキーリピートを取得する間隔。単位はミリ秒 */
	int KEY_SPEED = 50;
/** カーソルキーのキーリピート開始の遅れ。単位はGET_KEY_SPEEDの回数 */
	int KEY_DELAY = 3;

/** キーの押し下げ状態を保持 */
	boolean pressUp = false, pressDown = false,
		pressLeft = false, pressRight = false;
/** マウスが領域内にあるかどうかの状態を保持 */
	boolean mouseOnFrame = false;

/** アプレット起動か、アプリケーション起動か */
	boolean appletFlag = true;

/** Spriteクラス。画像に関する処理はSpriteクラスに任せる */
	Sprite sprite;

/** SoundPaletteクラス。BGM、SEに関する処理はSoundPaletteクラスに任せる */
	SoundPalette sp;

/** Game2DMainクラス。ゲーム本体の処理はGame2DMainクラスに任せる */
	Game2DMain gm;

/** キーボードイベントのQueue。カーソルキーはキーリピートされる */
	Queue keyQ;
/** マウスイベントのQueue */
	Queue mouseQ;

/** 再描画・メインループを実現するMainLoopクラスのオブジェクトを保持 */
	MainLoop timerTask;
/** キーリピートを実現するKeyRepeaterクラスのオブジェクトを保持 */
	KeyRepeater keyTimerTask;


/**
 * コンストラクタ。
 * GAME_MAIN_NAMEにGame2DMainを継承して作られたクラスの名前を設定する。
 * 基本的には自動的に検索するが、Java1.1.xでは検索できないので、
 * GAME_MAIN_NAME = GAME_NAME + "$" + GAME_NAME + "Main";
 * とする。
 */

	public Game2D() {
		try {
			GAME_NAME = getClass().getName();
			Class[] mbrs = getClass().getClasses();
			if(mbrs.length == 0) {
				GAME_MAIN_NAME = GAME_NAME + "$" + GAME_NAME + "Main";
				infomation("Warning : I can not getClasses().", null);
			}
			for(int i = 0; i < mbrs.length; i++)
				if(mbrs[i].getSuperclass().getName()
					.compareTo("Game2D$Game2DMain") == 0)
					GAME_MAIN_NAME = mbrs[i].getName();
		} catch (Exception e) {
			infomation("Error : I can not finish Game2D constructor.", e);
		}
	}


/**
 * エラー、警告の表示。
 * @param info 表示する文字列。「Error」で始まる文字列が渡されると強制終了する
 * @param e 例外が発生している場合は、例外も渡すと情報を表示する。
 * 例外を渡さない場合はnullにする
 */
	static void infomation(String info, Exception e) {
		System.out.println(info);
		System.out.println("java.version : "
			+ System.getProperty("java.version"));
		System.out.println("java.vendor : "
			+ System.getProperty("java.vendor"));
		if(e != null) {
			if(e.getClass().getName().compareTo(
				"java.lang.reflect.InvocationTargetException") == 0)
				((InvocationTargetException)e).getTargetException()
					.printStackTrace();
			else
				e.printStackTrace();
		}
		if(info.indexOf("Error") == 0)
			System.exit(0);
	}


/**
 * 非applet時、main()から呼び出すコンストラクタのラッパ。
 * @param game2dClassName 生成対象となるGame2Dクラスの派生クラスの名前
 */
	static void startGame(String game2dClassName) {
		GAME_NAME = game2dClassName;
		try {
			Game2D game2D = (Game2D)(Class
				.forName(game2dClassName).newInstance());
			game2D.newGame2D();
		} catch (Exception e) {
			infomation("Error : I can not create Game2D or newGame2D().", e);
		}
	}


/**
 * Game2Dから呼び出すGame2DMainのコンストラクタのラッパ。
 * 実際にインスタンスが生成される
 * Game2DMainを継承したクラスの名前が、
 * GAME_MAIN_NAMEに設定されているものとして、コンストラクタを呼び出す。
 * @return 生成されたGame2DMainを継承したクラスのオブジェクト
 */
	Game2DMain newGame2DMain() {
		try {
			Class argClass[] = {getClass()};
			Constructor g2dmCon
				= Class.forName(GAME_MAIN_NAME).getConstructor(argClass);
			Object initArgs[] = {this};
			return (Game2DMain)(g2dmCon.newInstance(initArgs));
		} catch(Exception e) {
			infomation("Error : I can not create Game2DMain.", e);
		}
		return null;
	}


/**
 * アプリケーションの開始用メソッドの実体。
 * main()メソッド内で自身のクラスを生成した後で実行。
 * ウィンドウを生成し、内部にアプレットを表示。
 */
	public void newGame2D() {
// アプレットを実行開始
		appletFlag = false;
		init();

// アプリケーションとしてのウィンドウを作成
		Frame frame = new Frame(GAME_NAME);
		frame.pack();
		frame.setVisible(true);
		frame.setVisible(false);
		frame.pack();

// ウィンドウの表示位置を、ほぼ画面中央に
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width-CANVAS_SIZE_W)/2,
			(d.height-CANVAS_SIZE_H)/2);

// ウィンドウの大きさを設定
		int left, right, top, bottom;
		left = frame.getInsets().left;
		right = frame.getInsets().right;
		top = frame.getInsets().top;
		bottom = frame.getInsets().bottom;
		frame.setSize(CANVAS_SIZE_W + left + right,
			CANVAS_SIZE_H + top + bottom);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
// ウィンドウがアイコン化されたら音を停止
			public void windowIconified(WindowEvent e) {stop();}
// ウィンドウがアイコンから復帰したらBGMを再開
			public void windowDeiconified(WindowEvent e) {start();}
		});
		frame.setResizable(false);

// ウィンドウにアプレットを張り込む
		frame.add(this);


// ウィンドウを表示
		frame.setVisible(true);
// ウィンドウの大きさを確認
		if(left != frame.getInsets().left
			|| right != frame.getInsets().right
			|| top != frame.getInsets().top
			|| bottom != frame.getInsets().bottom) {
			left = frame.getInsets().left;
			right = frame.getInsets().right;
			top = frame.getInsets().top;
			bottom = frame.getInsets().bottom;
			frame.setSize(CANVAS_SIZE_W + left + right,
				CANVAS_SIZE_H + top + bottom);
		}
	}


/**
 * アプレットの開始。
 * ゲーム本体の処理を準備。
 * 再描画・メインループ、キーボードリピータの開始。
 */
	public void init() {
// キーイベント取得を設定
		enableEvents(AWTEvent.MOUSE_EVENT_MASK |
			AWTEvent.MOUSE_MOTION_EVENT_MASK |
			AWTEvent.KEY_EVENT_MASK);
// スプライト、音、入力を管理するクラスを生成
		keyQ = new Queue();
		mouseQ = new Queue();
		if(appletFlag)
			sp = new SoundPalette(this);
		else
			sp = new SoundPalette();
		sprite = new Sprite(CANVAS_SIZE_W, CANVAS_SIZE_H, this);
// ゲーム本体を処理するクラスを生成
		gm = newGame2DMain();
// 再描画・メインループの生成、開始
		timerTask = new MainLoop();
		timerTask.start();
// キーボードリピータ生成、開始
		keyTimerTask = new KeyRepeater();
		keyTimerTask.start();
// フォーカス取得
		requestFocus();
	}


/** アプレット復帰時、BGMを再生 */
	public void start() {
		sp.restart();
		timerTask.threadSuspended = false;
		keyTimerTask.threadSuspended = false;
	}


/** アプレット停止時、音を停止 */
	public void stop() {
		sp.pause();
		timerTask.threadSuspended = true;
		keyTimerTask.threadSuspended = true;
	}


/** アプレット終了時、スレッドを終了 */
	public void destroy() {
		timerTask.threadStoped = true;
		keyTimerTask.threadStoped = true;
		try {
			timerTask.join();
			keyTimerTask.join();
		} catch (Exception e) {
			infomation("Error : I can not finish destroy().", e);
		}
	}


/**
 * マウスイベント(ウィンドウ上か、押されているか)を処理。
 * ボタンが押された時、離された時、Queueに入れる。
 * @param e マウスイベント
 */
	public void processMouseEvent(MouseEvent e) {
		switch(e.getID()) {
			case MouseEvent.MOUSE_ENTERED:
				mouseOnFrame = true;
				break;
			case MouseEvent.MOUSE_EXITED:
				mouseOnFrame = false;
				break;
			case MouseEvent.MOUSE_PRESSED:
			case MouseEvent.MOUSE_RELEASED:
				if(mouseOnFrame)
					mouseQ.enqueue(new InputEventTiny(e.getID(),
						 e.getX() - getInsets().left,
						 e.getY() - getInsets().top));
				break;
		}
	}


/**
 * マウスイベント(移動)を処理。
 * マウスの座標が変化した時、Queueに入れる。
 * @param e マウスイベント
 */
	public void processMouseMotionEvent(MouseEvent e) {
		if(!mouseOnFrame || e.getID() != MouseEvent.MOUSE_MOVED
			|| e.getX() < getInsets().left
			|| e.getX() > CANVAS_SIZE_W + getInsets().left
			|| e.getY() < getInsets().top
			|| e.getY() > CANVAS_SIZE_H + getInsets().top)
			return;
		mouseQ.enqueue(new InputEventTiny(e.getID(),
			e.getX() - getInsets().left, e.getY() - getInsets().top));
	}


/**
 * キーボードイベントを処理。
 * キーボードイベントをQueueに入れる。
 * カーソルキーだけはKeyRepeaterと連携して、キーリピートを実現。
 * @param e キーイベント
 */
	public void processKeyEvent(KeyEvent e) {
		switch(e.getID()) {
			case KeyEvent.KEY_PRESSED:
				switch(e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						if(!pressDown)
							keyQ.enqueue(new InputEventTiny(e.getID(),
								e.getKeyCode()));
						pressDown = true;
						break;
					case KeyEvent.VK_UP:
						if(!pressUp)
							keyQ.enqueue(new InputEventTiny(e.getID(),
								e.getKeyCode()));
						pressUp = true;
						break;
					case KeyEvent.VK_RIGHT:
						if(!pressRight)
							keyQ.enqueue(new InputEventTiny(e.getID(),
								e.getKeyCode()));
						pressRight = true;
						break;
					case KeyEvent.VK_LEFT:
						if(!pressLeft)
							keyQ.enqueue(new InputEventTiny(e.getID(),
								e.getKeyCode()));
						pressLeft = true;
						break;
					default:
						keyQ.enqueue(new InputEventTiny(e.getID(),
							e.getKeyCode()));
						break;
				}
				break;
			case KeyEvent.KEY_RELEASED:
				switch(e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						pressDown = false;
						break;
					case KeyEvent.VK_UP:
						pressUp = false;
						break;
					case KeyEvent.VK_RIGHT:
						pressRight = false;
						break;
					case KeyEvent.VK_LEFT:
						pressLeft = false;
						break;
				}
				keyQ.enqueue(new InputEventTiny(e.getID(), e.getKeyCode()));
				break;
		}
	}


/**
 * 描画。
 * Spriteクラスに描いてもらう
 * 割り込み処理からのrepaint()以外の描画時、つまり最小化からの復帰時や、
 * 隠れていた状態から有効なウィンドウとして表にピックアップされた時に
 * フォーカスを取得し、キー入力を受け付けられるようにする。
 * フォーカスを強制的に取得するのは、アプレットをマウスクリックしてからでないと
 * キー入力が受け付けられないため。
 * @param g ペイント先となるGraphicsコンテキスト
 */
	public void paint(Graphics g) {
		sprite.paintScreen(g);
		requestFocus();
	}


/**
 * 画面更新。
 * repaint()で呼ばれるので、Spriteクラスに描いてもらう。
 * ここではフォーカスの取得は行わない。
 * @param g ペイント先となるGraphicsコンテキスト
 */
	public void update(Graphics g) {
		sprite.paintScreen(g);
	}


/**
 * キーリピートを実現するKeyRepeaterクラス<br>
 * カーソルキーの適切なキーリピートを実現。
 * キーリピートの時間間隔や、開始の遅れは、
 * Game2DクラスのKEY_SPEED、KEY_DELAYを使う。
 * @author mnagaku
 */
	class KeyRepeater extends Thread {
/** trueに設定されるとキーリピートの一時停止が行われる */
		boolean threadSuspended = false;
/** trueに設定されるとキーリピートが停止される */
		boolean threadStoped = false;

/**
 * タイマー割り込み処理本体。
 * 一定時間毎にキーリピートを発生させる処理が行われる。
 */
		public void run() {
			long processTime, pressDownCount = 0, pressUpCount = 0,
				pressRightCount = 0, pressLeftCount = 0;
			while(!threadStoped) {
				try {
					processTime = System.currentTimeMillis();
// 条件を満たしていればキーリピートを発生させる。Queueに入れる
					if(pressDown) {
					 	if(pressDownCount > KEY_DELAY)
							keyQ.enqueue(new InputEventTiny(
								KeyEvent.KEY_PRESSED, KeyEvent.VK_DOWN));
						else
							pressDownCount++;
					}
					else
						pressDownCount = 0;
					if(pressUp) {
					 	if(pressUpCount > KEY_DELAY)
							keyQ.enqueue(new InputEventTiny(
								KeyEvent.KEY_PRESSED, KeyEvent.VK_UP));
						else
							pressUpCount++;
					}
					else
						pressUpCount = 0;
					if(pressRight) {
					 	if(pressRightCount > KEY_DELAY)
							keyQ.enqueue(new InputEventTiny(
								KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT));
						else
							pressRightCount++;
					}
					else
						pressRightCount = 0;
					if(pressLeft) {
					 	if(pressLeftCount > KEY_DELAY)
							keyQ.enqueue(new InputEventTiny(
								KeyEvent.KEY_PRESSED, KeyEvent.VK_LEFT));
						else
							pressLeftCount++;
					}
					else
						pressLeftCount = 0;
// KEY_SPEEDの時間待つ
					processTime = System.currentTimeMillis() - processTime;
					if(KEY_SPEED - processTime < 0)
						infomation("Warning : Processing delay in KeyRepeater.",
							null);
					else
						sleep(KEY_SPEED - processTime);

					while(threadSuspended && !threadStoped)
						yield();

				} catch (Exception e) {
					infomation("Error : Problem occurred in KeyRepeater.", e);
				}
			}
		}
	}


/**
 * 再描画・メインループを実現するMainLoopクラス<br>
 * run()内部のループを一定の時間間隔で回すことで、タイマー割り込みを実現。
 * @author mnagaku
 */
	class MainLoop extends Thread {
/** trueに設定されるとメインループの一時停止が行われる */
		boolean threadSuspended = false;
/** trueに設定されるとメインループが停止される */
		boolean threadStoped = false;

/**
 * タイマー割り込み処理本体。
 * 一定時間毎に呼び出され、
 * Game2DMainクラス(の派生クラス)のmainLoop()を呼び出して
 * メインループの処理を行い、
 * スプライトに描画リクエストを出す。
 */
		public void run() {
			long processTime = 0;
			while(!threadStoped) {
				try {
					processTime = System.currentTimeMillis();
// メインループ1回分を処理
					gm.mainLoop();
// 更新した情報に基づいて描画
					repaint();
// SPEEDの時間待つ
					processTime = System.currentTimeMillis() - processTime;
					if(SPEED - processTime < 0)
						infomation("Warning : Processing delay in MainLoop.",
							null);
					else
						sleep(SPEED - processTime);

					while(threadSuspended && !threadStoped)
						yield();

				} catch (Exception e) {
					infomation("Error : Problem occurred in MainLoop.", e);
					System.exit(0);
				}
			}
		}
	}


/**
 * Game2DMainクラス<br>
 * ゲーム本体の処理を行う。
 * @author mnagaku
 */
	abstract public class Game2DMain {

/**
 * コンストラクタ。
 * ゲーム本体の処理の初期化を行います。
*/
//		Game2DMain() {}


/**
 * メインループ1回分の処理。
 * 一定時間毎に呼び出される処理を記述する。
 * 画面のレンダリングはこのメソッドが実行された後、自動的に行われる。
 */
		abstract public boolean mainLoop();
	}
}
