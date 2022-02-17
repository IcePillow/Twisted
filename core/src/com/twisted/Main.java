package com.twisted;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.twisted.vis.Lobby;

public class Main extends Game {

	public static final int WIDTH = 1440;
	public static final int HEIGHT = 800;

	private Screen scene;
	@Override
	public void setScreen(Screen screen){
		super.setScreen(screen);
		this.scene = screen;
	}

	//graphics thread
	public Thread thread;


	/* Game Methods */

	@Override
	public void create () {
		scene = new Lobby(this);

		this.setScreen(scene);

		thread = Thread.currentThread();
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		scene.dispose();
	}
}
