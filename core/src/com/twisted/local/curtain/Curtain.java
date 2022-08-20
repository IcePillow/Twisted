package com.twisted.local.curtain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.state.GamePlayer;
import com.twisted.local.game.state.GameState;
import com.twisted.net.msg.gameUpdate.MGameEnd;

import java.util.HashMap;

public class Curtain implements Screen {

    //high level reference
    private final Main main;

    //graphics references
    private Skin skin;
    private Stage stage;

    //game info storage
    private final GameState state;
    private final MGameEnd end;


    /* Creation */

    public Curtain(Main main, MGameEnd end, GameState state){
        this.main = main;
        this.end = end;
        this.state = state;

        loadGui();
    }


    /* Screen Methods */

    @Override
    public void show() {

    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override
    public void pause() {

    }
    @Override
    public void resume() {

    }
    @Override
    public void hide() {

    }
    @Override
    public void dispose() {
        skin.dispose();
        stage.dispose();
    }


    /* Graphics Methods */

    private void loadGui(){
        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //create the stage
        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);

        //set the background
        Image image = new Image(Asset.retrieve(Asset.Shape.PIXEL_MAGENTA));
        image.setBounds(0, 0, Main.WIDTH, Main.HEIGHT);
        stage.addActor(image);

    }
}
