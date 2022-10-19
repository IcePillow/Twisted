package com.twisted.local.curtain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.Paint;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.local.game.state.GamePlayer;
import com.twisted.local.lib.RectTextButton;
import com.twisted.local.lib.Ribbon;
import com.twisted.local.lobby.Lobby;
import com.twisted.logic.descriptors.events.GameEvent;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.net.msg.gameUpdate.MGameEnd;

public class Curtain implements Screen {

    //high level reference
    private final Main main;

    //graphics references
    private Skin skin;
    private Stage stage;
    private OrthographicCamera camera;
    private ShapeRenderer shape;

    //game info storage
    private final ClientGameState state;
    private final MGameEnd end;

    //render storage
    private float[][] stars;


    /* Creation */

    public Curtain(Main main, MGameEnd end, ClientGameState state){
        this.main = main;
        this.end = end;
        this.state = state;

        loadGui();
        loadRender();
    }


    /* Screen Methods */

    @Override
    public void show() {

    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //rendering
        camera.update();
        shape.setProjectionMatrix(camera.combined);
        frameRender();

        //scene2d stuff
        Main.glyph.reset();
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


    /* Rendering Methods */

    private void loadRender(){
        //create objects
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camera.translate(stage.getWidth()/2, stage.getHeight()/2);
        shape = new ShapeRenderer();

        //prepare stars
        stars = new float[150][4];
        for(float[] s : stars){
            s[0] = (float) (Math.random()) * Main.WIDTH;
            s[1] = (float) (Math.random()) * Main.HEIGHT;
            s[2] = (float) Math.floor((Math.random()) * 1.99f) + 1;
            s[3] = (float) (Math.random()*0.4f + 0.2f);
        }
    }

    private void frameRender(){
        shape.begin(ShapeRenderer.ShapeType.Filled);

        //draw background
        shape.setColor(Paint.SPACE.col);
        shape.rect(0, 0, stage.getWidth(), stage.getHeight());

        for(float[] s : stars){
            shape.setColor(s[3], s[3], s[3], 1f);
            shape.circle(s[0], s[1], s[2]);
        }

        shape.end();
    }


    /* Gui Methods */

    private void loadGui(){
        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //create the stage and root
        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);

        //create root and children
        Group root = new Group();
        stage.addActor(root);
        root.addActor(loadMainButtons());
        root.addActor(loadTopBar());
        root.addActor(loadHistory());
        root.addActor(loadPlayerSummaries());
    }

    private Group loadMainButtons(){
        Group parent = new Group();

        //continue button
        RectTextButton contButton = new RectTextButton("Continue", Asset.labelStyle(Asset.Avenir.HEAVY_16),
                Asset.retrieve(Asset.Pixel.BLACK));
        contButton.setPosition(720, 50);
        contButton.setPadding(32, 24, 3);
        parent.addActor(contButton);

        //listeners
        contButton.setOnLeftClick(() -> Gdx.app.postRunnable(() -> {
            //create the new curtain
            Lobby lobby = new Lobby(main);

            //reset the graphics
            Asset.clear();

            //change screen
            main.setScreen(lobby);
            this.dispose();
        }));

        return parent;
    }

    private Group loadTopBar(){
        Group parent = new Group();
        parent.setPosition(100, 720);

        //create the main table
        Table table = new Table();
        table.setSize(Main.WIDTH-200, 0);
        parent.addActor(table);

        //result text
        Label resultLabel = new Label("", Asset.labelStyle(Asset.Avenir.BLACK_24));
        resultLabel.setAlignment(Align.bottomLeft);
        if(state.myId == end.winnerId){
            resultLabel.setText("VICTORY");
            resultLabel.setColor(Color.GREEN);
        }
        else {
            resultLabel.setText("DEFEAT");
            resultLabel.setColor(Color.RED);
        }
        table.add(resultLabel);

        //duration
        int durMin = ((int) end.timeElapsed) / 60;
        int durSec = ((int) end.timeElapsed) % 60;
        Label durationLabel = new Label(durMin + ":" + ((durSec<10?("0"+durSec):(durSec))),
                Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        durationLabel.setColor(Color.LIGHT_GRAY);
        table.add(durationLabel).expandX().left().bottom();

        //names of players
        Label nameLabel, versusLabel;
        int ct = 0;
        for(GamePlayer p : state.players.values()){
            nameLabel = new Label(p.getName(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
            nameLabel.setColor(p.getPaint().col);
            table.add(nameLabel).bottom();

            //add the versus
            ct += 1;
            if(ct < state.players.size()){
                versusLabel = new Label(" vs ", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
                versusLabel.setColor(Color.LIGHT_GRAY);
                table.add(versusLabel).bottom();
            }
        }

        //create the image
        Image lineImage = new Image(Asset.retrieve(Asset.Pixel.LIGHTGRAY));
        lineImage.setBounds(0, -15, Main.WIDTH-200, 2);
        parent.addActor(lineImage);

        //line image
        return parent;
    }

    private Group loadHistory(){
        Group parent = new Group();
        parent.setBounds(Main.WIDTH-100-400, 100, 400, 500);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight()-20);
        parent.addActor(decoration);

        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);

        //title text
        Label titleLabel = new Label("Timeline", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        titleLabel.setColor(Color.LIGHT_GRAY);
        Main.glyph.setText(titleLabel.getStyle().font, titleLabel.getText());
        titleLabel.setPosition(parent.getWidth()/2-Main.glyph.width/2, decoration.getHeight());
        parent.addActor(titleLabel);

        //create the pane's child
        Table table = new Table();
        table.top().left();
        table.pad(4f);

        //create the pane
        ScrollPane pane = new ScrollPane(table, skin);
        pane.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6-20);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);
        parent.addActor(pane);

        //add to the pane
        for(GameEvent e : end.eventHistory){
            table.add(e.timeForCurtain()).bottom().left().minWidth(32);
            table.add(e.describeForCurtain(state)).bottom().left();
            table.row();
        }

        return parent;
    }

    private Group loadPlayerSummaries(){
        Group parent = new Group();
        parent.setPosition(120, 550);

        GamePlayer[] arr = state.players.values().toArray(new GamePlayer[0]);
        for(int i=0; i<arr.length; i++){
            GamePlayer p = arr[i];

            //create the group for this player summary
            Group child = new Group();
            child.setPosition(0, -200*i);
            parent.addActor(child);

            //player name
            Label name = new Label(p.getName(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
            name.setColor(p.getPaint().col);
            child.addActor(name);

            //ship summaries
            Table table = new Table(skin);
            table.left().top();
            table.setPosition(80, 0);
            child.addActor(table);

            //prep for filling the table
            int kBound = 1+Ship.Tier.values().length+Station.Tier.values().length;
            Cell<Label> cell;
            //fill the table
            for(int j=0; j<3; j++){
                //j is row index, k is column index
                for(int k=0; k<kBound; k++){
                    //first column
                    if(k==0){
                        if(j==0) cell = table.add("");
                        else if(j==1) cell = table.add(new Label("Built", Asset.labelStyle(Asset.Avenir.LIGHT_16)));
                        else cell = table.add(new Label("Killed", Asset.labelStyle(Asset.Avenir.LIGHT_16)));

                        cell.getActor().setColor(Color.GRAY);
                        cell.left().padRight(12);
                    }
                    //ship columns
                    else if(k<Ship.Tier.values().length+1){
                        Ship.Tier sh = Ship.Tier.values()[k-1];
                        if(j==0){
                            Image img = new Image(Asset.retrieveEntityIcon(sh));
                            img.setColor(p.getPaint().col);
                            table.add(img).width(16).padLeft(12).padRight(12);
                        }
                        else if(j==1) {
                            cell = table.add("" + end.tracking.get(p.getId()).entitiesBuilt.get(sh),
                                    "small", Color.WHITE);
                            cell.getActor().setColor(Color.LIGHT_GRAY);
                        }
                        else {
                            cell = table.add("" + end.tracking.get(p.getId()).entitiesKilled.get(sh),
                                    "small", Color.WHITE);
                            cell.getActor().setColor(Color.LIGHT_GRAY);
                        }
                    }
                    //station columns
                    else {
                        Station.Tier st = Station.Tier.values()[k-Ship.Tier.values().length-1];
                        if(j==0){
                            Image img = new Image(Asset.retrieveEntityIcon(st));
                            img.setColor(p.getPaint().col);
                            table.add(img).width(16).padLeft(12).padRight(12);
                        }
                        else if(j==1){
                            cell = table.add("" + end.tracking.get(p.getId()).entitiesBuilt.get(st),
                                    "small", Color.WHITE);
                            cell.getActor().setColor(Color.LIGHT_GRAY);
                        }
                        else {
                            cell = table.add("" + end.tracking.get(p.getId()).entitiesKilled.get(st),
                                    "small", Color.WHITE);
                            cell.getActor().setColor(Color.LIGHT_GRAY);
                        }
                    }
                }
                table.row();
            }
        }

        return parent;
    }

}
