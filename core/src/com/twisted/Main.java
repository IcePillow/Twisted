package com.twisted;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.twisted.local.lobby.Lobby;

import java.text.DecimalFormat;

public class Main extends Game {

	public static final int WIDTH = 1440;
	public static final int HEIGHT = 800;

	public static final DecimalFormat df1 = new DecimalFormat("0.0");

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
