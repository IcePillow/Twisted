package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.twisted.Main;
import com.twisted.Asset;
import com.twisted.local.game.util.IndPackedStationRow;
import com.twisted.local.game.util.IndShipRow;
import com.twisted.local.game.util.IndustryRow;
import com.twisted.local.game.util.JobRow;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.net.msg.gameReq.MJobReq;
import com.twisted.net.msg.gameReq.MShipUndockReq;

import java.util.ArrayList;
import java.util.HashMap;

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
    private VerticalGroup dockInvGroup; //should only have IndustryRow children
    private Label focusStationName;
    private HashMap<Integer, VerticalGroup> stationGroups;
    private HashMap<Integer, Label[]> resourceLabels; //indices in label are for each resource

    //graphics state
    private int focusStationId = -1;

    //jobs
    private final ArrayList<CurrentJob> jobMappings; //should stay in sync with children of jobQueue


    /**
     * Constructor
     */
    SecIndustry(Game game){
        this.game = game;
        this.skin = game.skin;

        jobMappings = new ArrayList<>();
    }

    @Override
    Group init() {
        final int FOCUS_HEIGHT = 150;

        //initialize the top level group
        parent = super.init();
        parent.setBounds(Main.WIDTH-275, 260, 275, 410); //original height=395

        //main background
        Image main = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
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
        pane.setScrollbarsVisible(false);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);

        parent.addActor(pane);

        /* Focus */

        //create the focus group
        Group focusGroup = new Group();
        focusGroup.setBounds(3, 3, parent.getWidth()-6, FOCUS_HEIGHT);
        parent.addActor(focusGroup);

        //make the background image
        Image focusBackground = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        focusBackground.setSize(focusGroup.getWidth(), focusGroup.getHeight());
        focusGroup.addActor(focusBackground);

        //name of the station currently in focus
        focusStationName = new Label("[Station]", skin, "small", Color.WHITE);
        focusStationName.setPosition(3, FOCUS_HEIGHT- focusStationName.getHeight()-3);
        focusGroup.addActor(focusStationName);

        //title of scroll panes
        Label jobQueueTitle = new Label("Job Queue", skin, "small", Color.GRAY);
        jobQueueTitle.setPosition(focusGroup.getWidth()-3-QUEUE_WIDTHS, focusStationName.getY()-jobQueueTitle.getHeight());
        focusGroup.addActor(jobQueueTitle);
        Label dockedShipsTitle = new Label("Docked", skin, "small", Color.GRAY);
        dockedShipsTitle.setPosition(3, focusStationName.getY()-dockedShipsTitle.getHeight());
        focusGroup.addActor(dockedShipsTitle);

        //cosmetic squares
        Image dockingBox = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
        dockingBox.setBounds(2, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        focusGroup.addActor(dockingBox);
        Image jobBox = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
        jobBox.setBounds(focusGroup.getWidth()-3-QUEUE_WIDTHS-1, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        focusGroup.addActor(jobBox);

        //docking pane
        dockInvGroup = new VerticalGroup();
        dockInvGroup.left();
        dockInvGroup.columnAlign(Align.left);
        ScrollPane dockPane = new ScrollPane(dockInvGroup, skin);
        dockPane.setBounds(3, 3, QUEUE_WIDTHS, dockedShipsTitle.getY()-3);
        dockPane.setColor(Color.BLACK);
        focusGroup.addActor(dockPane);

        //job queue pane
        jobQueue = new VerticalGroup();
        jobQueue.left();
        jobQueue.columnAlign(Align.left);
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
        dockPane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                game.scrollFocus(dockPane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                game.scrollFocus(null);
            }
            return true;
        });
        queuePane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                game.scrollFocus(queuePane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                game.scrollFocus(queuePane);
            }
            return true;
        });

        //TODO add a flag/color for the current state (i.e. armored, reinforced)

        return parent;
    }

    @Override
    void load() {
        stationGroups = new HashMap<>();
        resourceLabels = new HashMap<>();

        //loop through the grids
        for(Grid g : state.grids){

            // Top Level for Station \\
            VerticalGroup stationGroup = new VerticalGroup();
            stationGroup.columnAlign(Align.left);
            vertical.addActor(stationGroup);
            stationGroups.put(g.station.getId(), stationGroup);

            // Title bar for station \\
            HorizontalGroup stationTitleBar = new HorizontalGroup();

            //create and add the dropdown icon
            Image dropdown = new Image(Asset.retrieve(Asset.UiBasic.GRAY_ARROW_3));
            dropdown.setOrigin(dropdown.getWidth()/2f, dropdown.getHeight()/2f);
            stationTitleBar.addActor(dropdown);

            //create and add the name label
            Label stationNameLabel = new Label(g.station.getFullName(), skin, "small", Color.LIGHT_GRAY);
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
            Label[] arr = new Label[4];
            resourceLabels.put(g.station.getId(), arr);
            int index=0;
            for(Asset.Gem asset : Asset.Gem.values()){
                Image image = new Image(Asset.retrieve(asset));
                Label label = new Label("0", skin, "small");

                resourceBar.add(image).minWidth(allowedImageWidth);
                resourceBar.add(label).minWidth(allowedLabelWidth);

                //add to the station object
                arr[index] = label;
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
                for(Gem gem : com.twisted.logic.descriptors.Gem.orderedGems){
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


    /* Updates */

    /**
     * Update the resources in a given station.
     */
    void stationResourceUpdate(Station s){
        for(int i=0; i<4; i++){
            resourceLabels.get(s.getId())[i].setText(s.resources[i]);
        }
    }

    /**
     * Called when a station's stage changes.
     */
    void stationStageUpdate(Station s){
        VerticalGroup g = stationGroups.get(s.getId());

        if(s.owner == state.myId && !g.hasParent()){
            vertical.addActor(g);
        }
        else if(s.owner != state.myId && g.hasParent()){
            vertical.removeActor(g);

            //deselecting
            if(focusStationId == s.getId()){
                industryFocusStation(null);
            }
        }
    }


    /* Adding and Removing */

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
            JobRow row = new JobRow(skin, Main.glyph);
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
     * Add a docked ship.
     */
    void addDockedShip(Ship ship){
        if(!ship.docked || ship.grid != focusStationId) return;

        Gdx.app.postRunnable(() -> {
            IndShipRow row = new IndShipRow(this, skin, Main.glyph, QUEUE_WIDTHS, ship);
            dockInvGroup.addActor(row);
        });
    }

    /**
     * Removes a docked ship.
     */
    void removeDockedShip(int stationId, Ship ship){
        if(stationId != focusStationId) return;

        //remove it if it exists
        Actor toRemove = null;
        for(Actor child : dockInvGroup.getChildren()){
            if(((IndustryRow) child).matches(ship)){
                toRemove = child;
            }
        }
        if(toRemove != null){
            dockInvGroup.removeActor(toRemove);
        }
    }

    /**
     * Adds a packed station.
     */
    void checkAddPackedStation(int stationId, Station.Type type){
        if(stationId != focusStationId) return;

        Gdx.app.postRunnable(() -> {
            IndPackedStationRow row = new IndPackedStationRow(this, skin, Main.glyph, QUEUE_WIDTHS,
                    type);
            dockInvGroup.addActor(row);
        });
    }

    /**
     * Removes a packed station
     */
    void checkRemovePackedStation(int stationId, Station.Type type){
        if(stationId != focusStationId) return;

        //remove it if it exists
        Actor toRemove = null;
        for(Actor child : dockInvGroup.getChildren()){
            if(((IndustryRow) child).matches(type)){
                toRemove = child;
            }
        }
        if(toRemove != null){
            dockInvGroup.removeActor(toRemove);
        }
    }


    /* Internal Event Methods */

    /**
     * Called when a ship's undock button is clicked.
     */
    public void undockButtonClicked(Ship ship){
        if(ship.docked){
            game.sendGameRequest(new MShipUndockReq(ship.id, ship.grid));
        }
        else {
            System.out.println("Unexpected non-docked ship in SecIndustry.undockButtonClicked()");
            new Exception().printStackTrace();
        }
    }

    /**
     * Called when a ship is clicked on for focus.
     */
    public void focusShipRequest(Ship ship){
        game.industryClickEvent(ship);
    }

    /**
     * Called when the user clicks on a station in the industry menu.
     * @param station Set to null to unfocus a station without focusing another station.
     */
    private void industryFocusStation(Station station){
        if(station == null){
            focusStationId = -1;

            //reset actors
            focusStationName.setText("[Station]");
            dockInvGroup.clearChildren();
            jobQueue.clearChildren();
            jobMappings.clear();

        }
        else {
            focusStationId = station.grid;

            //top text
            focusStationName.setText(station.getFullName());

            //docked ships
            dockInvGroup.clearChildren();
            for(Ship s : station.dockedShips.values()){
                addDockedShip(s);
            }
            for(Station.Type t : station.packedStations){
                if(t != null) checkAddPackedStation(focusStationId, t);
            }

            //job queue
            jobQueue.clearChildren();
            jobMappings.clear();
            for(int i=0; i<station.currentJobs.size(); i++){
                upsertStationJob(station.getId(), station.currentJobs.get(i), i);
            }
        }
    }

    /**
     * Called when the user attempts to start a job at a station.
     */
    private void industryJobRequest(Station station, Station.Job job){
        game.client.send(new MJobReq(station.grid, job));
    }
}
