package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.twisted.Main;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Station;
import com.twisted.net.msg.gameRequest.MJobRequest;
import com.twisted.local.game.state.GameState;

import java.util.ArrayList;

public class SecIndustry extends Sector{

    //reference variables
    private Game game;
    private GameState state;
    @Override
    public void setState(GameState state) {
        this.state = state;
    }

    //graphics utilities
    private Skin skin;
    private Thread logFadeThread;

    //tree
    private Group parent;
    private VerticalGroup vertical, jobQueue;
    private Label focusStation, logLabel;

    //graphics state
    private int focusStationId = -1;


    /**
     * Constructor
     */
    public SecIndustry(Game game, Skin skin){
        this.game = game;
        this.skin = skin;
    }

    @Override
    Group init() {
        final int FOCUS_HEIGHT = 150;
        final int LOG_HEIGHT = 20;

        //initialize the top level group
        parent = new Group();
        parent.setBounds(Main.WIDTH-275, 260, 275, 410); //original height=395

        //main background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(main);

        /* primary scroll pane */

        //create the main scroll pane
        vertical = new VerticalGroup();
        vertical.top().left();
        vertical.columnAlign(Align.left);

        ScrollPane pane = new ScrollPane(vertical, skin);
        pane.setBounds(3, 3 + FOCUS_HEIGHT+3, parent.getWidth()-6,
                parent.getHeight()-6 - (3+FOCUS_HEIGHT) - (3+LOG_HEIGHT));
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);

        parent.addActor(pane);

        /* focus */

        //create the focus group
        Group focusGroup = new Group();
        focusGroup.setBounds(3, 3, parent.getWidth()-6, FOCUS_HEIGHT);
        parent.addActor(focusGroup);

        //make the background image
        Image focusBackground = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        focusBackground.setSize(focusGroup.getWidth(), focusGroup.getHeight());
        focusGroup.addActor(focusBackground);

        //name of the station currently in focus
        focusStation = new Label("[Station]", skin, "small", Color.WHITE);
        focusStation.setPosition(3, FOCUS_HEIGHT- focusStation.getHeight()-3);
        focusGroup.addActor(focusStation);

        //job queue title
        Label jobQueueTitle = new Label("Job Queue", skin, "small", Color.GRAY);
        jobQueueTitle.setPosition(focusGroup.getWidth()-3-150, focusStation.getY()-jobQueueTitle.getHeight());
        focusGroup.addActor(jobQueueTitle);

        //job queue pane
        jobQueue = new VerticalGroup();
        jobQueue.left();
        ScrollPane queuePane = new ScrollPane(jobQueue, skin);
        queuePane.setBounds(focusGroup.getWidth()-3-150, 3, 150, jobQueueTitle.getY()-3);
        queuePane.setColor(Color.GRAY);
        focusGroup.addActor(queuePane);

        //TODO add a flag/color for the current state (i.e. armored, reinforced)

        /* Log Group */
        Group logGroup = new Group();
        logGroup.setBounds(3, parent.getHeight()-3-LOG_HEIGHT, parent.getWidth()-6, LOG_HEIGHT);
        parent.addActor(logGroup);

        //make the image
        Image logBackground = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        logBackground.setSize(logGroup.getWidth(), logGroup.getHeight());
        logGroup.addActor(logBackground);

        //make the label
        logLabel = new Label("Log label", skin, "small", Color.GRAY);
        logLabel.setPosition(3, -1);
        logGroup.addActor(logLabel);


        return parent;
    }

    @Override
    void load() {
        //loop through the grids
        for(Grid g : state.grids){

            /* Top Level for Station */
            VerticalGroup stationGroup = new VerticalGroup();
            stationGroup.columnAlign(Align.left);
            vertical.addActor(stationGroup);

            /* Title bar for station */
            HorizontalGroup stationTitleBar = new HorizontalGroup();

            //create and add the dropdown icon
            Image dropdown = new Image(new Texture(Gdx.files.internal("images/ui/gray-arrow-3.png")));
            dropdown.setOrigin(dropdown.getWidth()/2f, dropdown.getHeight()/2f);
            stationTitleBar.addActor(dropdown);

            //create and add the name label
            Label stationNameLabel = new Label(g.station.name, skin, "small");
            stationNameLabel.setAlignment(Align.left);
            stationTitleBar.addActor(stationNameLabel);

            //add the group to the station group
            stationGroup.addActor(stationTitleBar);

            /* Expanded Section */
            VerticalGroup child = new VerticalGroup();
            child.columnAlign(Align.left);

            Table resourceBar = new Table();
            resourceBar.padLeft(16);

            //calculate widths for resource bar
            int allowedImageWidth = 18;
            int allowedLabelWidth = (int) Math.floor((vertical.getWidth()-10-16-18*4)/4);

            //loop through gem files
            g.station.industryResourceLabels = new Label[4];
            int index=0;
            for(String filename : new String[]{"calcite", "kernite", "pyrene", "crystal"}){

                Image image = new Image(new Texture(Gdx.files.internal("images/gems/" + filename + ".png")));
                Label label = new Label("0", skin, "small");

                resourceBar.add(image).minWidth(allowedImageWidth);
                resourceBar.add(label).minWidth(allowedLabelWidth);

                //add to the station object
                g.station.industryResourceLabels[index] = label;
                index++;
            }
            child.addActor(resourceBar);

            /* Jobs */
            //create costs groups
            Table jobTable = new Table();
            jobTable.align(Align.left);
            jobTable.padLeft(12).padBottom(5);
            child.addActor(jobTable);

            //loop through all the jobs
            for(Station.Job job : g.station.getPossibleJobs()){

                Label nameLabel = new Label(job.name(), skin, "small", Color.WHITE);
                nameLabel.setColor(Color.GRAY);
                jobTable.add(nameLabel).align(Align.left).padRight(10);

                //create the cost labels
                Label[] costLabels = new Label[4];
                int i=0;
                for(Gem gem : Gem.orderedGems){
                    costLabels[i] = new Label(""+job.getGemCost(gem), skin, "small", Color.WHITE);
                    costLabels[i].setColor(Color.GRAY);
                    jobTable.add(costLabels[i]).width(40);
                    i++;
                }

                jobTable.row();

                //listener for color change
                nameLabel.addListener(new InputListener() {
                    /*
                    These extra complications were necessary because of what seems like a bug in
                    the library. Clicking on the actor without moving causes an enter event then an
                    exit event to occur. These two booleans track and account for this.
                     */
                    boolean entered = false;
                    boolean extraEnter = false;
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                        if(entered){
                            extraEnter = true;
                        }
                        else {
                            changeNodeColors(Color.LIGHT_GRAY);
                            entered = true;
                        }
                    }
                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                        if(extraEnter){
                            extraEnter = false;
                        }
                        else {
                            entered = false;
                            changeNodeColors(Color.GRAY);
                        }
                    }

                    //changes the color to light gray and back
                    private void changeNodeColors(Color color){
                        nameLabel.setColor(color);
                        for(Label label : costLabels){
                            label.setColor(color);
                        }
                    }
                });
                //listener for requesting jobs on clicks
                nameLabel.addListener(new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y){
                        industryJobRequest(g.station, job);

                        event.handle();
                    }
                });
            }

            /* Add listeners */
            dropdown.addListener(new ClickListener(Input.Buttons.LEFT){
                private boolean down = false;
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(down){
                        dropdown.rotateBy(90);

                        //remove the child
                        stationGroup.removeActor(child);
                    }
                    //not down
                    else {
                        dropdown.rotateBy(-90);

                        //add the child
                        stationGroup.addActorAfter(stationTitleBar, child);
                    }
                    down = !down;

                    event.handle();
                }
            });
            stationNameLabel.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    industryFocusStation(g.station);

                    event.handle();
                }
            });

            /* Visibility */
            if(g.station.owner != state.myId){
                stationGroup.getParent().removeActor(stationGroup);

                //TODO add them back to the parent when ownership is regained
            }

        }
    }

    @Override
    void render(float delta) {
        //update the times on the current jobs
        if(focusStationId != -1){
            ArrayList<CurrentJob> arr = state.grids[focusStationId].station.currentJobs;

            //add to have enough children
            for(int i=jobQueue.getChildren().size; i<arr.size(); i++){
                jobQueue.addActor(new Label("", skin, "small", Color.LIGHT_GRAY));
            }
            //remove to not have too many children
            for(int i=jobQueue.getChildren().size; i>arr.size(); i--){
                jobQueue.removeActorAt(i-1, false).clear();
            }

            for(int i=0; i<arr.size(); i++){
                CurrentJob job = arr.get(i);
                ((Label) jobQueue.getChild(i)).setText(job.jobType + "  " + (int) Math.ceil(job.timeLeft));
            }

        }
        //update the resources per station
        for(Grid g : state.grids){
                for(int i=0; i<g.station.industryResourceLabels.length; i++){
                    g.station.industryResourceLabels[i].setText(g.station.resources[i]);
                }
            }
    }

    @Override
    void dispose() {

    }


    /* Event Methods */

    /**
     * Called when the user clicks on a station in the industry menu.
     */
    void industryFocusStation(Station station){

        focusStationId = station.grid;
        focusStation.setText(station.name);

        //add to have enough children
        for(int i=jobQueue.getChildren().size; i<station.currentJobs.size(); i++){
            jobQueue.addActor(new Label("X", skin, "small", Color.LIGHT_GRAY));
        }
        //remove to not have too many children
        for(int i=jobQueue.getChildren().size; i>station.currentJobs.size(); i--){
            jobQueue.removeActorAt(i-1, false).clear();
        }

        //fill in the current data
        for(int i=0; i<jobQueue.getChildren().size; i++){
            ((Label) jobQueue.getChild(i)).setText(station.currentJobs.get(i).jobType + "  " +
                    (int) Math.ceil(station.currentJobs.get(i).timeLeft));
        }

    }

    /**
     * Called when the industry log window needs to have a new message displayed.
     * @param color Array of length 3 with the initial color in rgb form.
     */
    void updateIndustryLog(String string, float[] color){

        //deal with current thread if it exists
        if(logFadeThread != null) logFadeThread.interrupt();

        //set up the new string and initial color
        logLabel.setText(string);
        logLabel.setColor(color[0], color[1], color[2], 1);

        //create and start the new thread
        logFadeThread = new Thread(() -> {
            try {
                Thread.sleep(500);

                for(float i=1; i>0; i-=0.1f){
                    //check a new string hasn't overwritten
                    if(!(logLabel.getText().toString().equals(string))) break;

                    //update color then wait
                    logLabel.setColor(color[0], color[1], color[2], i);
                    Thread.sleep(80);
                }
            } catch (InterruptedException e) {
                //exit
            }

        });
        logFadeThread.start();
    }

    /**
     * Called when the user attempts to start a job at a station.
     */
    void industryJobRequest(Station station, Station.Job job){
        updateIndustryLog("Build " + job.name() + " @ " + station.name,
                new float[]{0.7f, 0.7f, 0.7f});

        game.client.send(new MJobRequest(station.grid, job));
    }

    @Override
    void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos,
                            SecViewport.ClickType type, int typeId) {
    }
}
