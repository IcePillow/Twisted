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
import com.twisted.local.game.state.ClientGameState;
import com.twisted.local.game.state.GamePlayer;
import com.twisted.local.lib.RectTextButton;
import com.twisted.local.lobby.Lobby;
import com.twisted.logic.Player;
import com.twisted.logic.descriptors.events.GameEvent;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
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
        shape.setColor(Main.SPACE);
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
        RectTextButton contButton = new RectTextButton("Continue", skin, "medium");
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
        parent.setPosition(100, 700);

        //line image
        Image lineImage = new Image(Asset.retrieve(Asset.Shape.PIXEL_LIGHTGRAY));
        lineImage.setSize(Main.WIDTH-200, 2);
        parent.addActor(lineImage);

        //left text
        Group leftText = new Group();
        parent.addActor(leftText);

        //result text
        Label resultLabel = new Label("", skin, "medium", Color.WHITE);
        resultLabel.setFontScale(1.5f);
        resultLabel.setAlignment(Align.bottomLeft);
        if(state.myId == end.winnerId){
            resultLabel.setText("VICTORY");
            resultLabel.setColor(Color.GREEN);
        }
        else {
            resultLabel.setText("DEFEAT");
            resultLabel.setColor(Color.RED);
        }
        leftText.addActor(resultLabel);

        //duration
        int durMin = ((int) end.timeElapsed) / 60;
        int durSec = ((int) end.timeElapsed) % 60;
        Label durationLabel = new Label(durMin + ":" + ((durSec<10?("0"+durSec):(durSec))),
                skin, "small", Color.WHITE);
        durationLabel.setColor(Color.LIGHT_GRAY);
        Main.glyph.setText(resultLabel.getStyle().font, resultLabel.getText());
        durationLabel.setPosition(resultLabel.getX() + Main.glyph.width*resultLabel.getFontScaleX() + 12,
                2);
        leftText.addActor(durationLabel);

        //right text
        HorizontalGroup rightText = new HorizontalGroup();
        float width = 0;
        rightText.setY(2);
        parent.addActor(rightText);

        //names of players
        Label nameLabel, versusLabel;
        for(GamePlayer p : state.players.values()){
            nameLabel = new Label(p.getName(), skin, "medium", Color.WHITE);
            nameLabel.setColor(p.getFile().color);
            rightText.addActor(nameLabel);

            Main.glyph.setText(nameLabel.getStyle().font, nameLabel.getText());
            width += Main.glyph.width;

            //add the versus
            if(rightText.getChildren().size < 2*state.players.size()-1){
                versusLabel = new Label(" vs ", skin, "medium", Color.WHITE);
                versusLabel.setColor(Color.LIGHT_GRAY);
                rightText.addActor(versusLabel);

                Main.glyph.setText(versusLabel.getStyle().font, versusLabel.getText());
                width += Main.glyph.width;
            }
        }

        //position the right text
        rightText.align(Align.bottomLeft);
        rightText.setX(lineImage.getWidth() - width);

        return parent;
    }

    private Group loadHistory(){
        Group parent = new Group();
        parent.setBounds(Main.WIDTH-100-400, 100, 400, 500);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight()-20);
        parent.addActor(decoration);

        Image ribbon = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);

        //title text
        Label titleLabel = new Label("Timeline", skin, "medium", Color.WHITE);
        titleLabel.setColor(Color.LIGHT_GRAY);
        Main.glyph.setText(titleLabel.getStyle().font, titleLabel.getText());
        titleLabel.setPosition(parent.getWidth()/2-Main.glyph.width/2, decoration.getHeight());
        parent.addActor(titleLabel);

        //create the pane's child
        VerticalGroup vertical = new VerticalGroup();
        vertical.top().left();
        vertical.columnAlign(Align.left);

        //create the pane
        ScrollPane pane = new ScrollPane(vertical, skin);
        pane.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6-20);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);
        parent.addActor(pane);

        //add to the pane
        for(GameEvent e : end.eventHistory){
            HorizontalGroup g = e.displayForCurtain(state, skin);
            vertical.addActor(g);
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
            Label name = new Label(p.getName(), skin, "medium", Color.WHITE);
            name.setColor(p.getFile().color);
            child.addActor(name);

            //ship summaries
            Table table = new Table(skin);
            table.left().top();
            table.setPosition(80, 0);
            child.addActor(table);

            //prep for filling the table
            int kBound = 1+Ship.Type.values().length+ Station.Type.values().length;
            Cell<Label> cell;
            //fill the table
            for(int j=0; j<3; j++){
                //j is row index, k is column index
                for(int k=0; k<kBound; k++){
                    //first column
                    if(k==0){
                        if(j==0) cell = table.add("");
                        else if(j==1) cell = table.add("Built", "small", Color.WHITE);
                        else cell = table.add("Killed", "small", Color.WHITE);

                        cell.getActor().setColor(Color.GRAY);
                        cell.left().padRight(12);
                    }
                    //ship columns
                    else if(k<Ship.Type.values().length+1){
                        Ship.Type sh = Ship.Type.values()[k-1];
                        if(j==0){
                            Image img = new Image(Asset.retrieve(sh.getIcon()));
                            img.setColor(p.getFile().color);
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
                        Station.Type st = Station.Type.values()[k-Ship.Type.values().length-1];
                        if(j==0){
                            Image img = new Image(Asset.retrieve(st.getIcon()));
                            img.setColor(p.getFile().color);
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
