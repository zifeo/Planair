package ch.epfl.planair.music;

import ddf.minim.*;
import processing.core.PApplet;

public class MusicPlayer {
	private Minim minim;
	private AudioPlayer player;


	public MusicPlayer(PApplet applet) {
		this.minim = new Minim(applet);
	}

	public void playBackgroundMusic() {
		player = minim.loadFile("music/Dont_Go_Way_Nobody.mp3");
		player.play();
	}
}
