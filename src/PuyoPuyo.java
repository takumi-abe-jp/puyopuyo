// Block.java
// written by mnagaku

import java.awt.Color;
import java.awt.event.KeyEvent;


/**
 * Blockクラス<br>
 * ブロックの落ちる処理を実現してみる
 * @author mnagaku
 */
public class PuyoPuyo extends Game2D {
/**
 * コンストラクタ。
 * 画面サイズとメインループの速度、キーリピートの設定を行う。
 */
	public PuyoPuyo() {
		CANVAS_SIZE_W = 16*13;
		CANVAS_SIZE_H = 16*16;
		SPEED = 100;
		KEY_SPEED = 40;
		KEY_DELAY = 3;
	}



/**
 * アプリケーションとして動作する場合の開始位置。
 * Game2DクラスのstartGame()を呼ぶ。
 */
	public static void main(String args[]) {
		startGame("PuyoPuyo");
	}
/**
 * BlockMainクラス<br>
 * ゲーム本体の処理を行う。
 * @author mnagaku
 */
	public class PuyoPuyoMain extends Game2DMain {

/** 新規ブロック生成フラグ。trueの時、新規作成 */
		boolean newBlock = true;
/** ブロックを何個まで作ったか示す値 */
		int blockCount = 1;
/** ブロックを1段落とすまでの時間をカウントする */
		int blockWait = 0;
/** ブロックの積まれ具合を表す */
		int map[][] = new int[13][16];
		//再帰用。いった場所の記録しておく
		int board[][] = new int [13][16];
		//再帰用。4以上なら消す。そのためのカウンタ
		int eraseCount = 0;
		//スコア盤のblockCount番号
		int scoreboard = 0;
		int scoreboard2 = 0;
		//連鎖数
		int rensaCount = 0;
		//連鎖数による得点
		int point[] = {0,8,16,32,64,96,128,160,192,224,256,288,320,352,388,416,448,480,512};

		//得点
		int score = 0;
		//ゲーム終了時に表示
		int Maxscore = 0;
		int MaxrensaCount = 0;

/** ブロックの描画に使われる色を表す */

		//とりあえず適当な個数作成。長引くならもっといる
		Color[] colorCount = new Color[10000];
		//ぷよぷよは4色なので黄色の追加
		Color colorList[] = {Color.red, Color.green, Color.blue,Color.yellow};
/**
 * ブロックの描画に使われる
 * Drawインターフェイスを備えたDrawRectクラスのインスタンス
 */
		DrawRect nowBlock;

		//modeの初期化(STEP4)
		char mode = 's';

/**
 * コンストラクタ。
 * mapの初期化と背景設定。
 */
		public PuyoPuyoMain() {
			// blockの積み具合を管理するmap[][]の初期化
			for(int i = 0; i <13 ; i++)
				for(int j = 0; j < 16; j++){
					map[i][j] = 1;
				}
				for(int i = 2; i < 8; i++)
					for(int j = 1; j < 15; j++)
						map[i][j] = 0;
			//枠の画像
			sprite.addGrp(0, "w.gif");
			//色の画像
			sprite.addGrp(1, "r.gif");
			sprite.addGrp(2, "g.gif");
			sprite.addGrp(3, "b.gif");
			sprite.addGrp(4, "y.gif");
			
			//連鎖するときのボイス
			sp.addSe(0, "CH08VO00.wav");
			sp.addSe(1, "CH08VO01.wav");
			sp.addSe(2, "CH08VO07.wav");
			sp.addSe(3, "CH08VO08.wav");
			sp.addSe(4, "CH08VO06.wav");
			sp.addSe(5, "CH08VO09.wav");
			sp.addSe(6, "CH08VO10.wav");

			// 背景の設定
			for(int i=0; i<13; i++){
				for(int j=0; j<16; j++){
					//ぷよバック
					if((0<i && i<7 )&&(0<j && j <15)){
						sprite.setPlaneDraw(blockCount,new DrawRect(16, 16,Color.black));
						sprite.setPlanePos(blockCount,i*16,j*16);
					//ネクスト欄
					}else if((7<i && i<12 )&&(0<j && j <4)){
						sprite.setPlaneDraw(blockCount,new DrawRect(16, 16,Color.black));
						sprite.setPlanePos(blockCount,i*16,j*16);
					//スコア盤
					}else if((7<i && i<12 )&&(4<j && j <15)){
						sprite.setPlaneDraw(blockCount,new DrawRect(16, 16,Color.black));
						sprite.setPlanePos(blockCount,i*16,j*16);

					//その他は枠。画像の挿入
					}else{
						sprite.setPlaneDraw(blockCount,new DrawRect(16, 16,Color.black));
						sprite.setPlanePos(blockCount,i*16,j*16);
						sprite.setPlaneGrp(blockCount, 0);
					}
					blockCount++;
				}
			}
			//連鎖盤の初期化。文字とrensaCountの表示用の変数
			scoreboard = blockCount;
			blockCount++;
			blockCount++;

			//スコア盤の初期化。文字とscoreの表示用の変数
			scoreboard2 = blockCount;
			blockCount++;
			blockCount++;



			//開始ぷよの準備
			//4色なので、かける4
			//1回 c に色を保存して
			Color c = colorList[(int)(Math.random() * 4)];
			Color c1 = colorList[(int)(Math.random() * 4)];
			//開始ぷよの作成
			sprite.setPlaneDraw(blockCount, nowBlock = new DrawRect(16, 16,c));
			//blockCount番目のcolorCountに色を保存
			colorCount[blockCount] = c;
			//色に合った画像をセット
			if(colorCount[blockCount] == Color.red){
				sprite.setPlaneGrp(blockCount, 1);
			}else if(colorCount[blockCount] == Color.green){
				sprite.setPlaneGrp(blockCount, 2);
			}else if(colorCount[blockCount] == Color.blue){
				sprite.setPlaneGrp(blockCount, 3);
			}else if(colorCount[blockCount] == Color.yellow){
				sprite.setPlaneGrp(blockCount, 4);
			}

			sprite.setPlaneDraw(blockCount+1, nowBlock = new DrawRect(16, 16,c1));
			//blockCount番目のcolorCountに色を保存
			colorCount[blockCount+1] = c1;
			//色に合った画像をセット
			if(colorCount[blockCount+1] == Color.red){
				sprite.setPlaneGrp(blockCount+1, 1);
			}else if(colorCount[blockCount+1] == Color.green){
				sprite.setPlaneGrp(blockCount+1, 2);
			}else if(colorCount[blockCount+1] == Color.blue){
				sprite.setPlaneGrp(blockCount+1, 3);
			}else if(colorCount[blockCount+1] == Color.yellow){
				sprite.setPlaneGrp(blockCount+1, 4);
			}

			//開始位置にセット
			sprite.setPlanePos(blockCount,3*16,  32 );
			sprite.setPlanePos(blockCount+1,3*16,  16);

			//ネクストの作成
			c = colorList[(int)(Math.random() * 4)];
			c1 = colorList[(int)(Math.random() * 4)];
			//ネクストの生成
			sprite.setPlaneDraw(blockCount+2, nowBlock = new DrawRect(16, 16,c));
			//blockCount番目のcolorCountに色を保存
			colorCount[blockCount+2] = c;
			//色に合った画像をセット
			if(colorCount[blockCount+2] == Color.red){
				sprite.setPlaneGrp(blockCount+2, 1);
			}else if(colorCount[blockCount+2] == Color.green){
				sprite.setPlaneGrp(blockCount+2, 2);
			}else if(colorCount[blockCount+2] == Color.blue){
				sprite.setPlaneGrp(blockCount+2, 3);
			}else if(colorCount[blockCount+2] == Color.yellow){
				sprite.setPlaneGrp(blockCount+2, 4);
			}

			sprite.setPlaneDraw(blockCount+3, nowBlock = new DrawRect(16, 16,c1));
			//blockCount番目のcolorCountに色を保存
			colorCount[blockCount+3] = c1;
			//色に合った画像をセット
			if(colorCount[blockCount+3] == Color.red){
				sprite.setPlaneGrp(blockCount+3, 1);
			}else if(colorCount[blockCount+3] == Color.green){
				sprite.setPlaneGrp(blockCount+3, 2);
			}else if(colorCount[blockCount+3] == Color.blue){
				sprite.setPlaneGrp(blockCount+3, 3);
			}else if(colorCount[blockCount+3] == Color.yellow){
				sprite.setPlaneGrp(blockCount+3, 4);
			}

			//ネクスト欄にセット
			sprite.setPlanePos(blockCount+2,9 *16,  32 );
			sprite.setPlanePos(blockCount+3,9 *16,  16);

		}
		//モードを３つに分ける(STEP4)
		public boolean mainLoop(){
			if(mode == 's')
				startmode();
			else if(mode == 'g')
				gamemode();
			else if(mode == 'e')
				endmode();
			return true;
		}
		public boolean startmode(){
			InputEventTiny ket;

			while((ket = (InputEventTiny)(keyQ.dequeue())) != null) {
				if(ket.getID() != KeyEvent.KEY_PRESSED)
					continue;
				switch(ket.getKeyCode()) {
					case KeyEvent.VK_SPACE:
						mode = 'g';
						break;
				}
			}
			sprite.setPlaneString(scoreboard, "START");
			sprite.setPlanePos(scoreboard, 16*8, 16*7);
			sprite.setPlaneColor(scoreboard,255,255,255);

			sprite.setPlaneString(scoreboard+1, "SPACE");
			sprite.setPlanePos(scoreboard+1, 16*8, 16*8);
			sprite.setPlaneColor(scoreboard+1,255,255,255);


			sprite.setPlaneString(scoreboard2, "Z=左回転");
			sprite.setPlanePos(scoreboard2, 16*8, 16*10);
			sprite.setPlaneColor(scoreboard2,255,255,255);


			sprite.setPlaneString(scoreboard2+1, "X=右回転");
			sprite.setPlanePos(scoreboard2+1, 16*8, 16*11);
			sprite.setPlaneColor(scoreboard2+1,255,255,255);

			//スペースが押されてゲーム開始するとき、始まる前に初期化
			if(mode == 'g'){
				//連鎖盤の初期化。文字とrensaCountの表示
				sprite.setPlaneString(scoreboard, "連鎖");
				sprite.setPlanePos(scoreboard, 16*8, 16*7);
				sprite.setPlaneColor(scoreboard,255,255,255);

				sprite.setPlaneString(scoreboard+1, String.valueOf(rensaCount));
				sprite.setPlanePos(scoreboard+1, 16*8, 16*8);
				sprite.setPlaneColor(scoreboard+1,255,255,255);

				//スコア盤の初期化。文字とscoreの表示
				sprite.setPlaneString(scoreboard2, "得点");
				sprite.setPlanePos(scoreboard2, 16*8, 16*10);
				sprite.setPlaneColor(scoreboard2,255,255,255);

				sprite.setPlaneString(scoreboard2+1, String.valueOf(score));
				sprite.setPlanePos(scoreboard2+1, 16*8, 16*11);
				sprite.setPlaneColor(scoreboard2+1,255,255,255);

			}

			return true;

		}

/**
 * メインループ1回分の処理。
 * コンストラクタ。mapの初期化と背景設定。
 */
		public boolean gamemode() {
// マウスイベントは捨てる
			mouseQ.removeAllElements();
			shift();
			//連鎖本体
			if(newBlock){
				//消えるのたびにループするため、delCountが必要
				int delCount = 1;
				//連鎖数
				rensaCount = 0;
				score = 0;
				//newBlockがtrueのときに消えるぷよがあるか、mapを全てeraseでみる
				//eraseは再帰的に隣の色をみるメソッド
				//eraseの中にeraseCountがあり、消えるぷよの数がわかる
				//消えるぷよが４つ以上あれば消してdelCountに++する
				//if文内で得点盤にスコアを出してshiftによって消えた後のぷよを整列
				//shiftの間にsleepを挟むことで消えた場所と整列後が見てわかる
				//ifの条件が消えるぷよがあるときなのは、無駄にsleepしてしまうのを避けるため
				//連鎖が終わるまでdelCountが0にならないためループ
				while(delCount != 0){
					delCount = 0;
					for(int i=2; i<8; i++){
						for(int j=14; j>2; j--){
							if(colorCount[map[i][j]] != null){
								//eraseCountとboardの初期化
								erase_board_syokika();
								erase(i,j,0,0);
								//4つ以上つながってたら、消す
								if(eraseCount >= 4){
									del_puyo();
									//消した数をdelCountに保存。eraseCountはforで他の値になるため
									delCount+= eraseCount;
								}
							}
						}
					}
					//ここでリペイントすることで、同時消しに見える
					//上のforで一回でも消してたら連鎖を++して整列
					if(delCount !=0){
						rensaCount++;
						//連鎖数
						sprite.setPlaneString(scoreboard+1, String.valueOf(rensaCount));
						sprite.setPlanePos(scoreboard+1, 16*8, 16*8);
						sprite.setPlaneColor(scoreboard+1,255,255,255);
						//その連鎖の得点
						//スコアにあらかじめ連鎖数によってきめられた点数*消した数
						//連鎖点数基礎点のみ。連結同時消し同色ボーナスなし
						score = score + (point[rensaCount-1] * (delCount*10));

						sprite.setPlaneString(scoreboard2+1, String.valueOf(score));
						sprite.setPlanePos(scoreboard2+1, 16*8, 16*11);
						sprite.setPlaneColor(scoreboard2+1,255,255,255);
						
						//連鎖ボイス。連鎖数によってボイスをかえる
						if(rensaCount == 1){
							sp.playSe(0);
						}else if(rensaCount == 2){
							sp.playSe(1);
						}else if(rensaCount == 3){
							sp.playSe(2);
						}else if(rensaCount == 4){
							sp.playSe(3);
						}else if(rensaCount == 5){
							sp.playSe(4);
						}else if(rensaCount == 6){
							sp.playSe(5);
						}else{
							sp.playSe(6);
						}
						repaint();

						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}

						shift();
						repaint();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
					}
				}
				//終了時に点数と連鎖数を表示するため記憶
				if(score > Maxscore){
					Maxscore = score;
				}
				if(rensaCount > MaxrensaCount){
					MaxrensaCount = rensaCount;
				}
			}
			//連鎖で消せるものを消し整列したあとに、一番上にぷよがあったら終了
			if(map[4][2] != 0){
				mode = 'e';
			}
// 新しいblockを作る
			//4色なので、かける4
			//1回 c に色を保存して
			Color c = colorList[(int)(Math.random() * 4)];
			Color c1 = colorList[(int)(Math.random() * 4)];
			if(newBlock) {
				//ネクネクの生成
				sprite.setPlaneDraw(blockCount+4, nowBlock = new DrawRect(16, 16,c));
				//blockCount番目のcolorCountに色を保存
				colorCount[blockCount+4] = c;
				//色に合った画像をセット
				if(colorCount[blockCount+4] == Color.red){
					sprite.setPlaneGrp(blockCount+4, 1);
				}else if(colorCount[blockCount+4] == Color.green){
					sprite.setPlaneGrp(blockCount+4, 2);
				}else if(colorCount[blockCount+4] == Color.blue){
					sprite.setPlaneGrp(blockCount+4, 3);
				}else if(colorCount[blockCount+4] == Color.yellow){
					sprite.setPlaneGrp(blockCount+4, 4);
				}

				sprite.setPlaneDraw(blockCount+5, nowBlock = new DrawRect(16, 16,c1));
				//blockCount番目のcolorCountに色を保存
				colorCount[blockCount+5] = c1;
				//色に合った画像をセット
				if(colorCount[blockCount+5] == Color.red){
					sprite.setPlaneGrp(blockCount+5, 1);
				}else if(colorCount[blockCount+5] == Color.green){
					sprite.setPlaneGrp(blockCount+5, 2);
				}else if(colorCount[blockCount+5] == Color.blue){
					sprite.setPlaneGrp(blockCount+5, 3);
				}else if(colorCount[blockCount+5] == Color.yellow){
					sprite.setPlaneGrp(blockCount+5, 4);
				}

				//ネクネク欄にセット
				sprite.setPlanePos(blockCount+4,11 *16,  32 );
				sprite.setPlanePos(blockCount+5,11 *16,  16);

				//ネクネクからネクストへセット
				sprite.setPlanePos(blockCount+2,9 *16,  32 );
				sprite.setPlanePos(blockCount+3,9 *16,  16);

				//ネクスト欄からとったぷよを3*16の位置から落とす
				sprite.setPlanePos(blockCount,3*16,  32 );
				sprite.setPlanePos(blockCount+1,3*16,  16);
				newBlock = false;

				blockWait = 0;
			}
// mainLoop()10回毎に1マス落とす
			blockWait++;
			if(blockWait % 10 == 0) {

				//今操作しているプレーンのどちらかが0以外が入っているmap座標にきた場合そのまま下まで落とす
				//blockCount番目のプレーン
				if(map[sprite.getPlanePosX(blockCount)/16+1]
					[sprite.getPlanePosY(blockCount)/16+1] != 0) {
					map[sprite.getPlanePosX(blockCount)/16+1]
						[sprite.getPlanePosY(blockCount)/16] = blockCount;
					map[sprite.getPlanePosX(blockCount+1)/16+1]
							[sprite.getPlanePosY(blockCount+1)/16] = blockCount+1;
					newBlock = true;
					blockCount = blockCount + 2;
					keyQ.removeAllElements();
					return true;
				//blockCount+1番目
				}else if(map[sprite.getPlanePosX(blockCount+1)/16+1]
						[sprite.getPlanePosY(blockCount+1)/16+1] != 0) {
					map[sprite.getPlanePosX(blockCount)/16+1]
							[sprite.getPlanePosY(blockCount)/16] = blockCount;
					map[sprite.getPlanePosX(blockCount+1)/16+1]
							[sprite.getPlanePosY(blockCount+1)/16] = blockCount+1;
					newBlock = true;
					blockCount = blockCount + 2;
					keyQ.removeAllElements();
					return true;
				}
				sprite.setPlaneMov(blockCount, 0, 16);
				sprite.setPlaneMov(blockCount+1, 0, 16);
			}
// キー入力処理
			InputEventTiny ket;
			while((ket = (InputEventTiny)(keyQ.dequeue())) != null) {
				if(ket.getID() != KeyEvent.KEY_PRESSED)
					continue;
				switch(ket.getKeyCode()) {
// 下が押されたら1マス落とす
					case KeyEvent.VK_DOWN:
					if(map[sprite.getPlanePosX(blockCount)/16+1]
						[sprite.getPlanePosY(blockCount)/16+1] == 0 &&
						map[sprite.getPlanePosX(blockCount+1)/16+1]
								[sprite.getPlanePosY(blockCount+1)/16+1] == 0){
						sprite.setPlaneMov(blockCount, 0, 16);
						sprite.setPlaneMov(blockCount+1, 0, 16);
						}
						break;
					case KeyEvent.VK_UP:
						break;
// 左右に移動
					case KeyEvent.VK_RIGHT:
						if(map[sprite.getPlanePosX(blockCount)/16+2]
							[sprite.getPlanePosY(blockCount)/16] == 0 &&
							map[sprite.getPlanePosX(blockCount+1)/16+2]
									[sprite.getPlanePosY(blockCount+1)/16] == 0){
							sprite.setPlaneMov(blockCount, 16, 0);
							sprite.setPlaneMov(blockCount+1, 16, 0);
						}
						break;
					case KeyEvent.VK_LEFT:
						if(map[sprite.getPlanePosX(blockCount)/16]
							[sprite.getPlanePosY(blockCount)/16] == 0 &&
							map[sprite.getPlanePosX(blockCount+1)/16]
									[sprite.getPlanePosY(blockCount+1)/16] == 0){
							sprite.setPlaneMov(blockCount, -16, 0);
							sprite.setPlaneMov(blockCount+1, -16, 0);
						}
						break;
					case KeyEvent.VK_X:
						//右回転
						//blockCount番目のプレーンを軸にblockCount+1番目のプレーンが回転
						//0度から90度回転 x座標は同じなのでy座標がblockCount+1番目のプレーンが上にあり、かつ
						//blockCount番目の右のmap座標が0なら回る
						if(sprite.getPlanePosY(blockCount+1) < sprite.getPlanePosY(blockCount)&&
								map[sprite.getPlanePosX(blockCount)/16+2]
										[sprite.getPlanePosY(blockCount)/16] == 0){
							sprite.setPlaneMov(blockCount+1, 16, 16);
						//90度から180度 y座標は同じなのでx座標がblockCount+1番目のプレーンが右にあり、かつ
						//blockCount番目の下のmap座標が0なら回る
						}else if(sprite.getPlanePosX(blockCount) < sprite.getPlanePosX(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16+1] == 0){
							sprite.setPlaneMov(blockCount+1, -16, 16);
						//180度から270度 x座標は同じなのでy座標がblockCount+1番目のプレーンが下にあり、かつ
						//blockCount番目の左のmap座標が0なら回る
						}else if(sprite.getPlanePosY(blockCount) < sprite.getPlanePosY(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16]
										[sprite.getPlanePosY(blockCount)/16] == 0){
							sprite.setPlaneMov(blockCount+1, -16, -16);
						//270度から360度 y座標は同じなのでx座標がblockCount+1番目のプレーンが左にあり、かつ
						//blockCount番目の上のmap座標が0なら回る
						}else if(sprite.getPlanePosX(blockCount+1) < sprite.getPlanePosX(blockCount)&&
								map[sprite.getPlanePosX(blockCount)/16]
										[sprite.getPlanePosY(blockCount)/16-1] == 0){
							sprite.setPlaneMov(blockCount+1, 16, -16);
						///両サイドにぷよか壁があったら180度回転するが、片方だけなら操作ぷよを2つ違う方向へ90度回転
						//0度から180度。右のmap座標に0以外がはいっている。かつ下のmap座標が0
						}else if(sprite.getPlanePosY(blockCount+1) < sprite.getPlanePosY(blockCount) &&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16+1] == 0 &&
								map[sprite.getPlanePosX(blockCount)/16+2]
										[sprite.getPlanePosY(blockCount)/16] != 0){
							//左のmap座標が0以外なら180度回転
							if(map[sprite.getPlanePosX(blockCount)/16]
									[sprite.getPlanePosY(blockCount)/16] != 0){
									sprite.setPlaneMov(blockCount+1, 0, 32);
							//左のmap座標が0なら１つが90度回転もう１つが逆回転
							}else{
								sprite.setPlaneMov(blockCount, -16, 0);
								sprite.setPlaneMov(blockCount+1, 0, 16);
							}
						///両サイドにぷよか壁があったら180度回転するが、片方だけなら操作ぷよを2つ違う方向へ90度回転
						//180度から360度。左のmap座標に0以外がはいっている。かつ上のmap座標が0
						}else if(sprite.getPlanePosY(blockCount) < sprite.getPlanePosY(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16-1] == 0 &&
								map[sprite.getPlanePosX(blockCount)/16]
										[sprite.getPlanePosY(blockCount)/16] != 0){
							//右のmap座標が0以外なら180度回転
							if(map[sprite.getPlanePosX(blockCount)/16+2]
									[sprite.getPlanePosY(blockCount)/16] != 0){
								sprite.setPlaneMov(blockCount+1, 0, -32);
								//右のmap座標が0なら１つが90度回転もう１つが逆回転
							}else{
								sprite.setPlaneMov(blockCount, 16, 0);
								sprite.setPlaneMov(blockCount+1, 0, -16);
							}
						}
						break;
					case KeyEvent.VK_Z:
						//左回転
						//blockCount番目のプレーンを軸にblockCount+1番目のプレーンが回転
						//0度から-90度回転 x座標は同じなのでy座標がblockCount+1番目のプレーンが上にあり、かつ
						//blockCount番目の左のmap座標が0なら回る
						if(sprite.getPlanePosY(blockCount+1) < sprite.getPlanePosY(blockCount)&&
								map[sprite.getPlanePosX(blockCount)/16]
										[sprite.getPlanePosY(blockCount)/16] == 0){
							sprite.setPlaneMov(blockCount+1, -16, +16);
						//-90度から-180度 y座標は同じなのでx座標がblockCount+1番目のプレーン左にあり、かつ
						//blockCount番目の下のmap座標が0なら回る
						}else if(sprite.getPlanePosX(blockCount+1) < sprite.getPlanePosX(blockCount)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16+1] == 0){
							sprite.setPlaneMov(blockCount+1, 16, 16);
						//-180度から-270度 x座標は同じなのでy座標がblockCount+1番目のプレーンが下にあり、かつ
						//blockCount番目の右のmap座標が0なら回る
						}else if(sprite.getPlanePosY(blockCount) < sprite.getPlanePosY(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16+2]
										[sprite.getPlanePosY(blockCount)/16] == 0){
							sprite.setPlaneMov(blockCount+1, 16, -16);
						//-270度から-360度 y座標は同じなのでx座標がblockCount+1番目のプレーンが右にあり、かつ
						//blockCount番目の上のmap座標が0なら回る
						}else if(sprite.getPlanePosX(blockCount) < sprite.getPlanePosX(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16-1] == 0){
							sprite.setPlaneMov(blockCount+1, -16, -16);
						///両サイドにぷよか壁があったら180度回転するが、片方だけなら操作ぷよを2つ違う方向へ90度回転
						//0度から-180度。左のmap座標に0以外がはいっている。かつ下のmap座標が0
						}else if(sprite.getPlanePosY(blockCount+1) < sprite.getPlanePosY(blockCount)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16+1] == 0 &&
								map[sprite.getPlanePosX(blockCount)/16]
										[sprite.getPlanePosY(blockCount)/16] != 0){
								//かつ右のmap座標が0以外なら180度回転
								if(	map[sprite.getPlanePosX(blockCount)/16+2]
											[sprite.getPlanePosY(blockCount)/16] != 0){
									sprite.setPlaneMov(blockCount+1, 0, 32);
								}else{
									//右のmap座標が0の場合は１つが-90度回転して１つが180度から-90度回転する
									sprite.setPlaneMov(blockCount, 16, 0);
									sprite.setPlaneMov(blockCount+1, 0, 16);
								}
						///両サイドにぷよか壁があったら180度回転するが、片方だけなら操作ぷよを2つ違う方向へ90度回転
						//-180度から-360度。右のmap座標に0以外がはいっている。
						}else if(sprite.getPlanePosY(blockCount) < sprite.getPlanePosY(blockCount+1)&&
								map[sprite.getPlanePosX(blockCount)/16+1]
										[sprite.getPlanePosY(blockCount)/16-1] == 0 &&
								map[sprite.getPlanePosX(blockCount)/16+2]
										[sprite.getPlanePosY(blockCount)/16] != 0){
								//上のmap座標が0、かつ左のmap座標が0以外なら180度回転
								if(	map[sprite.getPlanePosX(blockCount)/16]
											[sprite.getPlanePosY(blockCount)/16] != 0){
									sprite.setPlaneMov(blockCount+1, 0, -32);
								}else{
									//左のmap座標が0の場合は１つが回転して１つが逆に回転する
									sprite.setPlaneMov(blockCount, -16, 0);
									sprite.setPlaneMov(blockCount+1, 0, -16);
								}
						}
						break;

				}
			}

			return true;
		}
		//ゲーム画面のマスを詰める
		public void shift(){
			int count = 1;
			while(count != 0){
				count = 0;
				for(int i=2; i<8; i++){
					for(int j=14; j>1; j--){
						if(map[i][j] == 0 && map[i][j-1] != 0){
							sprite.setPlaneMov(map[i][j-1],0,16);
							map[i][j] = map[i][j-1];
							map[i][j-1] = 0;
							count = 1;
							repaint();
							//sleepすることで整列するときの流れが視認できる
							try {
								Thread.sleep(40);
							} catch (InterruptedException e) {}
						}
					}
				}
			}
		}
		//再帰的に消すプレーンを記憶
		public void erase(int current_x, int current_y, int from_x, int from_y){
				board[from_x][from_y] = 1;
				eraseCount++;
				// 上
			   if((current_y-1 >= 2) & (current_y-1 != from_y)){ // 行き先があるか？
				// 行き先に現在と同じ色があるか?かついったことがないか？
				   if(colorCount[map[current_x][current_y-1]] == colorCount[map[current_x][current_y]] && board[current_x][current_y-1] == 0)
					   erase(current_x, current_y-1, current_x, current_y);
			   }
			   // 下
			   if((current_y+1 < 16) & (current_y+1 != from_y)){ //行き先
				   if(colorCount[map[current_x][current_y+1]] == colorCount[map[current_x][current_y]]&& board[current_x][current_y+1] == 0)
					   erase(current_x,current_y+1,current_x,current_y);
			   }
			   // 左
			   if((current_x-1 >= 0) & (current_x-1 != from_x)){ //行き先
				   if(colorCount[map[current_x-1][current_y]] == colorCount[map[current_x][current_y]]&& board[current_x-1][current_y] == 0)
					   erase(current_x-1,current_y,current_x,current_y);
			   }

			   // 右
			   if((current_x+1 < 8) & (current_x+1 != from_x)){ //行き先
				   if(colorCount[map[current_x+1][current_y]] == colorCount[map[current_x][current_y]]&& board[current_x+1][current_y] == 0)
					   erase(current_x+1,current_y,current_x,current_y);
			   }
			   board[current_x][current_y] = 1;
		}
		public void erase_board_syokika(){
			//eraseCountとboardの初期化
			eraseCount = 0;
			for(int i=2; i<8; i++){
				for(int j=14; j>=2; j--){
					board[i][j] = 0;
				}
			}
		}
		public void del_puyo(){
			//4つ以上つながってたら、Viewをflaseに
			for(int i=2; i<8; i++){
				for(int j=14; j>=2; j--){
					if(board[i][j] == 1){
						sprite.setPlaneView(map[i][j], false);
						map[i][j] = 0;
						board[i][j] = 0;
					}
				}
			}
		}

		public boolean endmode (){
			//最大連鎖数と最大得点をスコアばんに表示
			sprite.setPlaneString(scoreboard, "最大連鎖");
			sprite.setPlanePos(scoreboard, 16*8, 16*7);
			sprite.setPlaneColor(scoreboard,255,255,255);

			sprite.setPlaneString(scoreboard+1, String.valueOf(MaxrensaCount));
			sprite.setPlanePos(scoreboard+1, 16*8, 16*8);
			sprite.setPlaneColor(scoreboard+1,255,255,255);


			sprite.setPlaneString(scoreboard2, "最大得点");
			sprite.setPlanePos(scoreboard2, 16*8, 16*10);
			sprite.setPlaneColor(scoreboard2,255,255,255);


			sprite.setPlaneString(scoreboard2+1, String.valueOf(Maxscore));
			sprite.setPlanePos(scoreboard2+1, 16*8, 16*11);
			sprite.setPlaneColor(scoreboard2+1,255,255,255);

	        return true;
		}
	}
}