package com.twisted;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.twisted.local.lobby.Lobby;

import java.text.DecimalFormat;

public class Main extends Game {

	/* Constants */

	//graphical values
	public static final int WIDTH = 1440;
	public static final int HEIGHT = 800;

	//color values TODO refactor into enum tree with all useful colors

	//utility objects
	public static final DecimalFormat df1 = new DecimalFormat("0.0");
	public static final DecimalFormat df2 = new DecimalFormat("0.00");
	public static final GlyphLayout glyph = new GlyphLayout();


	/* State */

	private Screen scene;
	@Override
	public void setScreen(Screen screen){
		super.setScreen(screen);
		this.scene = screen;
	}


	/* Game Methods */

	@Override
	public void create () {
		scene = new Lobby(this);

		this.setScreen(scene);
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
