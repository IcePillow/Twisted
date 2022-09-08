package com.twisted.local.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.Game;
import com.twisted.logic.entities.*;

public class SecDecor extends Sector {

    //graphics utilities
    private final Stage stage;
    private OrthographicCamera camera;
    SpriteBatch sprite;
    ShapeRenderer shape;

    //render state
    private float[][] stars;


    /**
     * Constructor
     */
    SecDecor(Lobby lobby, Stage stage){
        super(lobby);
        this.stage = stage;
    }


    /* Standard Graphics */

    @Override
    public Group init(){
        Group parent = super.init();

        parent.addActor(initTree());
        initRender();

        return parent;
    }
    @Override
    void render(float delta) {
        //must be at the beginning
        camera.update();
        sprite.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        //draw background
        shape.setColor(Main.SPACE);
        shape.rect(0, 0, stage.getWidth(), stage.getHeight());

        //draw stars
        for(float[] s : stars){
            shape.setColor(s[3], s[3], s[3], 1f);
            shape.circle(s[0], s[1], s[2]);
        }

        shape.end();
    }
    @Override
    void dispose() {

    }


    /* Graphics Utility */

    private Group initTree(){
        Group group = new Group();

        //title text
        Label titleText = new Label("ARMADA", Asset.labelStyle(Asset.Avenir.BLACK_48));
        titleText.setColor(Color.LIGHT_GRAY);
        titleText.setPosition(720-titleText.getWidth()/2, 650);
        group.addActor(titleText);

        return group;
    }

    private void initRender(){
        //create objects
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camera.translate(stage.getWidth()/2, stage.getHeight()/2);
        shape = new ShapeRenderer();
        sprite = new SpriteBatch();

        //prepare stars
        stars = new float[150][4];
        for(float[] s : stars){
            s[0] = (float) (Math.random()) * Main.WIDTH;
            s[1] = (float) (Math.random()) * Main.HEIGHT;
            s[2] = (float) Math.floor((Math.random()) * 1.99f) + 1;
            s[3] = (float) (Math.random()*0.4f + 0.2f);
        }
    }

}
