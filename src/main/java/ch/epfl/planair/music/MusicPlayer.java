package ch.epfl.planair.music;

import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import processing.core.PApplet;

/**
 * Handles music & sounds
 */
public final class MusicPlayer {
	private final Minim minim;

	// Musics
	private final AudioPlayer backgroundMusic;

	// Sounds
	private final AudioSample rampage;
	private final AudioSample firstBlood;
	private final AudioSample headshot;
	private final AudioSample doubleKill;

	public MusicPlayer(PApplet applet) {
		this.minim = new Minim(applet);
		this.backgroundMusic = minim.loadFile("music/Dont_Go_Way_Nobody.mp3");
		this.rampage = minim.loadSample("sound/rampage.mp3");
		this.firstBlood = minim.loadSample("sound/first_blood.mp3");
		this.headshot = minim.loadSample("sound/headshot.mp3");
		this.doubleKill = minim.loadSample("sound/double_kill.mp3");
	}

	public void playBackgroundMusic() {
		//backgroundMusic.loop();
	}

	public void triggerRampage() {
		rampage.trigger();
	}

	public void triggerFirstBlood() {
		firstBlood.trigger();
	}

	public void triggerHeadshot() {
		headshot.trigger();
	}

	public void triggerDoubleKill() {
		doubleKill.trigger();
	}
}
