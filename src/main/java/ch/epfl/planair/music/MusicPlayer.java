package ch.epfl.planair.music;

import ddf.minim.*;
import processing.core.PApplet;

/**
 * Handles music & sounds
 */
public class MusicPlayer {
	private Minim minim;

	// Musics
	private AudioPlayer backgroundMusic;

	// Sounds
	private AudioSample rampage;
	private AudioSample firstBlood;
	private AudioSample headshot;
	private AudioSample doubleKill;

	public MusicPlayer(PApplet applet) {
		this.minim = new Minim(applet);

		backgroundMusic = minim.loadFile("music/Dont_Go_Way_Nobody.mp3");
		rampage = minim.loadSample("sound/rampage.mp3");
		firstBlood = minim.loadSample("sound/first_blood.mp3");
		headshot = minim.loadSample("sound/headshot.mp3");
		doubleKill = minim.loadSample("sound/double_kill.mp3");
	}

	public void playBackgroundMusic() {
		backgroundMusic.loop();
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
