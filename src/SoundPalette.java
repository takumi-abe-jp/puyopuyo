// SoundPalette.java
// written by mnagaku

import java.applet.Applet;
import java.applet.AudioClip;
import java.util.Hashtable;

/**
 * サウンドを管理するSoundPaletteクラス<br>
 * BGMとSEを登録(読み込み)しておき、必要な時に鳴らせるよう準備しておく。
 * AudioClipでサポートされる形式のみ鳴らせる。
 * @author mnagaku
 */
public class SoundPalette {

/** BGMを読み込んで置いておくプール */
	Hashtable bgms;

/** SEを読み込んで置いておくプール */
	Hashtable ses;

/** JVMのバーション */
	int javaVersion;

/** 演奏中のBGM */
	int nowBgm;

/** 再生中のSE */
	AudioClip nowSe = null;

/** プログラム実行状態、アプレット動作か否か */
	boolean isApplet;

/** SoundPaletteを呼び出すアプレット */
	Applet owner;


/**
 * コンストラクタ。
 * プールを生成。引数なしの場合は非アプレットとして考える。
*/
	public SoundPalette() {
		this(null);
	}


/**
 * コンストラクタ。
 * プールを生成。実行環境の版、Appletかどうかも判定しておく。
 * @param owner SoundPaletteを管理するオーナーとなるアプレット
*/
	public SoundPalette(Applet owner) {
		this.owner = null;
		this.isApplet = false;
		if(owner instanceof Applet) {
			this.owner = owner;
			this.isApplet = true;
		}

		bgms = new Hashtable();
		ses = new Hashtable();

		String javaVersionStr = System.getProperty("java.version");
		if(javaVersionStr.compareTo("1.1.0") < 0)
			javaVersion = 10;
		else if(javaVersionStr.compareTo("1.2.0") < 0)
			javaVersion = 11;
		else
			javaVersion = 12;
	}

/**
 * プールにdataを読み込む。
 * プールの何番目の位置に、何と言う名前のdataを読み込むか指定する。
 * @param no 読み込んだdataを格納する、プールの場所(インデックス)
 * @param file 読み込むdataファイル名
 * @param pool 対象となるpool
 * @return 読み込みが正常に終了した場合はtrue
*/
	boolean loadData(int no, String file, Hashtable pool) {
		AudioClip ac = null;
		try {
			switch(javaVersion) {
				case 11:
					if(isApplet)
						ac = owner.getAudioClip(getClass().getResource(file));
	//				else
	//					ac = newAudioClip4Sun(file);
					break;
				case 12:
					ac = Applet.newAudioClip(getClass().getResource(file));
					break;
			}
		} catch(Exception e) {
			System.out.println("Warning : SoundPalette is unplayable.");
			System.out.println("java.version : "
				+ System.getProperty("java.version"));
			System.out.println("java.vendor : "
				+ System.getProperty("java.vendor"));
			e.printStackTrace();
		}
		if(ac == null)
			return false;
		pool.put(new Integer(no), ac);
		return true;
	}

/**
 * sunの内部クラスを用いて音dataを読み込む。
 * Java1.1.x版のJVM上でapplet以外のゲームから利用する場合の処理を行う。
 * @param file 読み込むdataファイル名
 * @return 読み込んだAudioClip
*/
/*	AudioClip newAudioClip4Sun(String file) {
		AudioClip ret;
		try {
			Class.forName("sun.applet.AppletAudioClip");
			ret = new sun.applet.AppletAudioClip(getClass().getResource(file));
		} catch(Exception e) {
			ret = null;
		}
		return ret;
	}
*/

/**
 * BGMプールにBGMデータを読み込む。
 * プールの何番目の位置に、何と言う名前のBGMを読み込むか指定する。
 * @param no 読み込んだBGMを格納する、プールの場所(インデックス)
 * @param file 読み込むBGMファイル名
 * @return 読み込みが正常に終了した場合はtrue
*/
	public boolean addBgm(int no, String file) {
		return loadData(no, file, bgms);
	}


/**
 * SEプールにSEデータを読み込む。
 * プールの何番目の位置に、何と言う名前のSEを読み込むか指定する。
 * @param no 読み込んだSEを格納する、プールの場所(インデックス)
 * @param file 読み込むSEファイル名
 * @return 読み込みが正常に終了した場合はtrue
*/
	public boolean addSe(int no, String file) {
		return loadData(no, file, ses);
	}


/** poolからacを取得 */
	AudioClip getAc(int no, Hashtable pool) {
		AudioClip ac = null;
		ac = (AudioClip)pool.get(new Integer(no));
		return ac;
	}


/** 音を一時停止 */
	public boolean pause() {
		AudioClip ac = getAc(nowBgm, bgms);
		if(ac == null)
			return false;
		ac.stop();
		return true;
	}


/** 音を再開 */
	public boolean restart() {
		AudioClip ac = getAc(nowBgm, bgms);
		if(ac == null)
			return false;
		ac.loop();
		return true;
	}


/** BGMを鳴らす */
	public boolean playBgm(int no) {
		AudioClip ac = getAc(nowBgm, bgms);
		if(ac != null)
		ac.stop();
		nowBgm = no;
		ac = getAc(nowBgm, bgms);
		if(ac == null)
			return false;
		ac.loop();
		return true;
	}


/** SEを鳴らす */
	public boolean playSe(int no) {
		if(nowSe != null)
			nowSe.stop();
		nowSe = getAc(no, ses);
		if(nowSe == null)
			return false;
		nowSe.play();
		return true;
	}
}

