package com.twisted.local.lobby;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.twisted.util.Asset;
import com.twisted.Main;
import com.twisted.util.Paint;

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
        shape.setColor(Paint.SPACE.c);
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
        group.setPosition(720, 650);

        //title text
        HorizontalGroup text = new HorizontalGroup();
        group.addActor(text);
        Label firstText = new Label("ARM", Asset.labelStyle(Asset.Avenir.BLACK_48));
        firstText.setColor(Paint.TITLE_PURPLE.c);
        text.addActor(firstText);
        Label midText = new Label("A", Asset.labelStyle(Asset.Avenir.BLACK_48));
        midText.setRotation(90);
        midText.setColor(Paint.TITLE_PURPLE.c);
        Container<Label> midTextCont = new Container<>(midText);
        midTextCont.setTransform(true);
        midTextCont.setOrigin(midText.getWidth()/2f, midText.getHeight()/2f);
        midTextCont.setSize(midText.getWidth(), midText.getHeight());
        midTextCont.setRotation(180);
        text.addActor(midTextCont);
        Label lastText = new Label("DA", Asset.labelStyle(Asset.Avenir.BLACK_48));
        lastText.setColor(Paint.TITLE_PURPLE.c);
        text.addActor(lastText);

        //positioning
        float textWid = firstText.getWidth()+midText.getWidth()+lastText.getWidth();
        text.setX(-textWid/2f);

        Image botBar = new Image(Asset.retrieve(Asset.Pixel.WHITE));
        botBar.setBounds(text.getX()-5, -firstText.getHeight()/2f, textWid+10, 5);
        botBar.setColor(Paint.TITLE_PURPLE.c);
        group.addActor(botBar);
        Image topBar = new Image(Asset.retrieve(Asset.Pixel.WHITE));
        topBar.setBounds(text.getX()-5, botBar.getY()+firstText.getHeight()-5, textWid+10, 5);
        topBar.setColor(Paint.TITLE_PURPLE.c);
        group.addActor(topBar);

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
