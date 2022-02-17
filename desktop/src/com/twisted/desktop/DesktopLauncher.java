package com.twisted.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.twisted.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.forceExit = true;
		config.title = "Twisted";
		config.width = Main.WIDTH;
		config.height = Main.HEIGHT;

		new LwjglApplication(new Main(), config);
	}
}
