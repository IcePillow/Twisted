package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.twisted.Main;
import com.twisted.local.game.util.DockedShipRow;
import com.twisted.local.game.util.JobRow;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.net.msg.gameReq.MJobReq;
import com.twisted.net.msg.gameReq.MShipUndockReq;

import java.util.ArrayList;

public class SecIndustry extends Sector{

    //constants
    private final static float QUEUE_WIDTHS = 130f;

    //reference variables
    private final Game game;

    //graphics utilities
    private final Skin skin;

    //tree
    private Group parent;
    private VerticalGroup vertical;
    private VerticalGroup jobQueue; //should only have JobRow children
    private VerticalGroup dockGroup; //should only have DockedShipRow children
    private Label focusStation;

    //graphics state
    private int focusStationId = -1;

    //jobs
    private final ArrayList<CurrentJob> jobMappings; //should stay in sync with children of jobQueue
    private final ArrayList<Ship> dockedShips; //should stay in sync with dockGroup


    /**
     * Constructor
     */
    SecIndustry(Game game){
        this.game = game;
        this.skin = game.skin;

        jobMappings = new ArrayList<>();
        dockedShips = new ArrayList<>();
    }

    @Override
    Group init() {
        final int FOCUS_HEIGHT = 150;

        //initialize the top level group
        parent = super.init();
        parent.setBounds(Main.WIDTH-275, 260, 275, 410); //original height=395

        //main background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(main);

        /* Primary scroll pane */

        //create the main scroll pane
        vertical = new VerticalGroup();
        vertical.top().left();
        vertical.columnAlign(Align.left);

        ScrollPane pane = new ScrollPane(vertical, skin);
        pane.setBounds(3, 3 + FOCUS_HEIGHT+3, parent.getWidth()-6,
                parent.getHeight()-6 - (3+FOCUS_HEIGHT));
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);

        parent.addActor(pane);

        /* Focus */

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

        //title of scroll panes
        Label jobQueueTitle = new Label("Job Queue", skin, "small", Color.GRAY);
        jobQueueTitle.setPosition(focusGroup.getWidth()-3-QUEUE_WIDTHS, focusStation.getY()-jobQueueTitle.getHeight());
        focusGroup.addActor(jobQueueTitle);
        Label dockedShipsTitle = new Label("Docked", skin, "small", Color.GRAY);
        dockedShipsTitle.setPosition(3, focusStation.getY()-dockedShipsTitle.getHeight());
        focusGroup.addActor(dockedShipsTitle);

        //cosmetic squares
        Image dockingBox = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        dockingBox.setBounds(2, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        focusGroup.addActor(dockingBox);
        Image jobBox = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        jobBox.setBounds(focusGroup.getWidth()-3-QUEUE_WIDTHS-1, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        focusGroup.addActor(jobBox);

        //docking pane
        dockGroup = new VerticalGroup();
        dockGroup.left();
        ScrollPane dockPane = new ScrollPane(dockGroup, skin);
        dockPane.setBounds(3, 3, QUEUE_WIDTHS, dockedShipsTitle.getY()-3);
        dockPane.setColor(Color.BLACK);
        focusGroup.addActor(dockPane);

        //job queue pane
        jobQueue = new VerticalGroup();
        jobQueue.left();
        ScrollPane queuePane = new ScrollPane(jobQueue, skin);
        queuePane.setBounds(focusGroup.getWidth()-3-QUEUE_WIDTHS, 3, QUEUE_WIDTHS, jobQueueTitle.getY()-3);
        queuePane.setColor(Color.BLACK);
        focusGroup.addActor(queuePane);

        //add listeners
        pane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                game.scrollFocus(pane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                game.scrollFocus(null);
            }
            return true;
        });

        //TODO add a flag/color for the current state (i.e. armored, reinforced)

        return parent;
    }

    @Override
    void load() {
        //loop through the grids
        for(Grid g : state.grids){

            // Top Level for Station \\
            VerticalGroup stationGroup = new VerticalGroup();
            stationGroup.columnAlign(Align.left);
            vertical.addActor(stationGroup);

            // Title bar for station \\
            HorizontalGroup stationTitleBar = new HorizontalGroup();

            //create and add the dropdown icon
            Image dropdown = new Image(new Texture(Gdx.files.internal("images/ui/gray-arrow-3.png")));
            dropdown.setOrigin(dropdown.getWidth()/2f, dropdown.getHeight()/2f);
            stationTitleBar.addActor(dropdown);

            //create and add the name label
            Label stationNameLabel = new Label(g.station.nickname, skin, "small", Color.LIGHT_GRAY);
            stationNameLabel.setAlignment(Align.left);
            stationTitleBar.addActor(stationNameLabel);

            //add the group to the station group
            stationGroup.addActor(stationTitleBar);

            // Expanded Section \\
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

            // Jobs \\
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

            // Add listeners \\
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

            // Visibility \\
            if(g.station.owner != state.myId){
                stationGroup.getParent().removeActor(stationGroup);

                //TODO add them back to the parent when ownership is regained
            }

        }
    }

    @Override
    void render(float delta) {}

    @Override
    void dispose() {

    }


    /* External Event Methods */

    /**
     * Called when the user clicks on a station in the industry menu.
     */
    void industryFocusStation(Station station){
        focusStationId = station.grid;

        //top text
        focusStation.setText(station.nickname);

        //docked hips
        dockGroup.clearChildren();
        for(Ship s : station.dockedShips.values()){
            addDockedShip(s);
        }

        //job queue
        jobQueue.clearChildren();
        jobMappings.clear();
        for(int i=0; i<station.currentJobs.size(); i++){
            upsertStationJob(station.getId(), station.currentJobs.get(i), i);
        }
    }

    /**
     * Called when the user attempts to start a job at a station.
     */
    void industryJobRequest(Station station, Station.Job job){
        game.client.send(new MJobReq(station.grid, job));
    }

    /**
     * Update or insert a job into the job queue for the given station if that station is the focused
     * station.
     * @param position The job's position in the job queue.
     */
    void upsertStationJob(int stationId, CurrentJob job, int position){
        if(stationId != focusStationId) return;

        int index = jobMappings.indexOf(job);

        //already have the job
        if(index != -1){
            JobRow row = (JobRow) jobQueue.getChildren().get(index);

            row.updateTimer(Integer.toString( Math.round(job.timeLeft) ));
        }
        //add the job
        else {
            JobRow row = new JobRow(skin, game.glyph);
            row.updateName(job.jobType.name());
            row.updateTimer(Integer.toString( Math.round(job.timeLeft) ));

            jobMappings.add(position, job);
            jobQueue.addActor(row);
        }
    }

    /**
     * Remove a job from the queue view, if visible.
     */
    void removeStationJob(int stationId, CurrentJob job){
        if(stationId != focusStationId) return;

        int index = jobMappings.indexOf(job);

        //remove it if it exists
        if(index != -1){
            jobMappings.remove(job);
            jobQueue.removeActorAt(index, true);
        }
    }

    /**
     * Update the resources in a given station.
     */
    void stationResourceUpdate(Station s){
        for(int i=0; i<s.industryResourceLabels.length; i++){
            s.industryResourceLabels[i].setText(s.resources[i]);
        }
    }

    /**
     * Add a docked ship.
     */
    void addDockedShip(Ship ship){
        if(ship.docked != -1 && ship.docked != focusStationId) return;

        Gdx.app.postRunnable(() -> {
            DockedShipRow row = new DockedShipRow(this, skin, game.glyph, QUEUE_WIDTHS, ship);
            row.updateName(ship.getType().toString());

            dockGroup.addActor(row);
            dockedShips.add(ship);
        });
    }

    /**
     * Removes a docked ship.
     */
    void removeDockedShip(int stationId, Ship ship){
        if(stationId != focusStationId) return;

        int index = dockedShips.indexOf(ship);

        //remove it if it exists
        if(index != -1){
            dockedShips.remove(ship);
            dockGroup.removeActorAt(index, true);
        }
    }


    /* Internal Event Methods */

    public void undockButtonClicked(Ship ship){
        if(ship.docked != -1){
            game.sendGameRequest(new MShipUndockReq(ship.id, ship.docked));
        }
        else {
            System.out.println("Unexpected non-docked ship in SecIndustry.undockButtonClicked()");
            new Exception().printStackTrace();
        }
    }
}
