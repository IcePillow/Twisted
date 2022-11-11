package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.twisted.Main;
import com.twisted.util.Asset;
import com.twisted.local.game.util.IndPackedStationRow;
import com.twisted.local.game.util.IndShipRow;
import com.twisted.local.game.util.IndustryRow;
import com.twisted.local.game.util.JobRow;
import com.twisted.local.lib.Ribbon;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.descriptors.JobType;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.net.msg.gameReq.MJobReq;
import com.twisted.net.msg.gameReq.MShipUndockReq;
import com.twisted.util.Quirk;

import java.util.ArrayList;
import java.util.HashMap;

public class SecIndustry extends Sector {

    //constants
    private final static float QUEUE_WIDTHS = 130f;
    private final static int FOCUS_HEIGHT = 150;

    //reference variables
    private final Game game;

    //graphics utilities
    private final Skin skin;

    //tree
    private Group parent;
    private VerticalGroup vertical;
    private Table jobQueueTable, dockInvTable; //should only have JobRow & IndustryRow children respectively
    private Label focusStationName, focusStationTimer;
    private Image focusStationStage;
    private HashMap<Integer, VerticalGroup> stationGroups;
    private HashMap<Integer, Label[]> resourceLabels; //indices in label are for each resource
    private HashMap<Integer, IndShipRow> dockedShipRows;

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
        //initialize the top level group
        parent = super.init();
        parent.setBounds(Main.WIDTH-275, 260, 275, 410); //original height=395

        //main background
        parent.addActor(initDecor());
        //primary scroll pane
        parent.addActor(initPrimaryScroll());
        //focus group
        parent.addActor(initFocusGroup());

        return parent;
    }
    private Group initDecor(){
        Group group = new Group();

        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        group.addActor(ribbon);
        Image band = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        band.setBounds(0, 3+FOCUS_HEIGHT, parent.getWidth(), 3);
        group.addActor(band);

        return group;
    }
    private ScrollPane initPrimaryScroll(){
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

        //listeners
        pane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                game.scrollFocus(pane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                game.scrollFocus(null);
            }
            return true;
        });

        return pane;
    }
    private Group initFocusGroup(){
        Group group = new Group();
        group.setBounds(3, 3, parent.getWidth()-6, FOCUS_HEIGHT);

        //make the background image
        Image focusBackground = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        focusBackground.setSize(group.getWidth(), group.getHeight());
        group.addActor(focusBackground);

        //top title
        Table topTable = new Table();
        topTable.setBounds(3, FOCUS_HEIGHT-14, group.getWidth()-6, 0);
        group.addActor(topTable);

        //name of the station currently in focus
        Label.LabelStyle focusStationNameStyle = Asset.labelStyle(Asset.Avenir.HEAVY_16);
        focusStationNameStyle.fontColor = Color.LIGHT_GRAY;
        focusStationName = new Label("[Station]", focusStationNameStyle);
        focusStationName.setPosition(3, FOCUS_HEIGHT-focusStationName.getHeight()-3);
        topTable.add(focusStationName).growX().left();

        //station stage and stage timer
        focusStationTimer = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        focusStationTimer.setVisible(false);
        topTable.add(focusStationTimer).padRight(2);
        focusStationStage = new Image(Asset.retrieve(Asset.UiIcon.STATION_DEPLOYMENT));
        focusStationStage.setColor(Color.GRAY);
        focusStationStage.setVisible(false);
        topTable.add(focusStationStage);

        //title of scroll panes
        Label jobQueueTitle = new Label("Job Queue", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        jobQueueTitle.setColor(Color.LIGHT_GRAY);
        jobQueueTitle.setPosition(group.getWidth()-3-QUEUE_WIDTHS, focusStationName.getY()-jobQueueTitle.getHeight());
        group.addActor(jobQueueTitle);
        Label dockedShipsTitle = new Label("Docked", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        dockedShipsTitle.setColor(Color.LIGHT_GRAY);
        dockedShipsTitle.setPosition(3, focusStationName.getY()-dockedShipsTitle.getHeight());
        group.addActor(dockedShipsTitle);

        //cosmetic squares
        Ribbon dockingBox = new Ribbon(Asset.retrieve(Asset.Pixel.DARKGRAY), 1);
        dockingBox.setBounds(2, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        group.addActor(dockingBox);
        Ribbon jobBox = new Ribbon(Asset.retrieve(Asset.Pixel.DARKGRAY), 1);
        jobBox.setBounds(group.getWidth()-3-QUEUE_WIDTHS-1, 2, QUEUE_WIDTHS+2, dockedShipsTitle.getY()-3+2);
        group.addActor(jobBox);

        //docking pane
        dockedShipRows = new HashMap<>();
        dockInvTable = new Table().top().left();
        ScrollPane dockPane = new ScrollPane(dockInvTable, skin);
        dockPane.setBounds(3, 3, QUEUE_WIDTHS, dockedShipsTitle.getY()-3);
        dockPane.setColor(Color.BLACK);
        group.addActor(dockPane);

        //job queue pane
        jobQueueTable = new Table().top().left();
        ScrollPane queuePane = new ScrollPane(jobQueueTable, skin);
        queuePane.setBounds(group.getWidth()-3-QUEUE_WIDTHS, 3, QUEUE_WIDTHS, jobQueueTitle.getY()-3);
        queuePane.setColor(Color.BLACK);
        group.addActor(queuePane);

        //add listeners
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

        return group;
    }

    @Override
    void load() {
        //initialize hashmaps
        stationGroups = new HashMap<>();
        resourceLabels = new HashMap<>();

        //update ui with state info
        focusStationName.setColor(state.findBaseColorForOwner(state.myId));

        //loop through the grids
        for(Grid g : state.grids){

            // Top Level for Station \\
            VerticalGroup stationGroup = new VerticalGroup();
            stationGroup.columnAlign(Align.left);
            if(g.station.owner == state.myId) vertical.addActor(stationGroup);
            stationGroups.put(g.station.getId(), stationGroup);

            // Title bar for station \\
            HorizontalGroup stationTitleBar = new HorizontalGroup();

            //create and add the dropdown icon
            Image dropdown = new Image(Asset.retrieve(Asset.UiBasic.ARROW_3));
            dropdown.setColor(Color.GRAY);
            dropdown.setOrigin(dropdown.getWidth()/2f, dropdown.getHeight()/2f);
            stationTitleBar.addActor(dropdown);

            //create and add the name label
            Label stationNameLabel = new Label(g.station.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
            stationNameLabel.setColor(Color.LIGHT_GRAY);
            stationNameLabel.setAlignment(Align.left);
            stationTitleBar.addActor(stationNameLabel);

            //add the group to the station group
            stationGroup.addActor(stationTitleBar);

            // Expanded Section \\
            VerticalGroup child = new VerticalGroup();
            child.columnAlign(Align.left);

            Table resourceBar = new Table();
            resourceBar.padLeft(18);

            //calculate widths for resource bar
            int allowedImageWidth = 16;
            int allowedLabelWidth = (int) Math.floor((vertical.getWidth()-10-16-18*4)/4);

            //loop through gem files
            Label[] arr = new Label[4];
            resourceLabels.put(g.station.getId(), arr);
            int index=0;
            for(Asset.Gem asset : Asset.Gem.values()){
                Image image = new Image(Asset.retrieve(asset));
                Actor filler = new Actor();
                filler.setWidth(6);
                Label label = new Label("0", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
                label.setColor(Color.LIGHT_GRAY);

                resourceBar.add(image).minWidth(allowedImageWidth);
                resourceBar.add(filler);
                resourceBar.add(label).minWidth(allowedLabelWidth);

                //add to the station object
                arr[index] = label;
                index++;
            }
            child.addActor(resourceBar);

            // Jobs \\
            Table jobTable = new Table();
            jobTable.align(Align.left);
            jobTable.padLeft(2).padBottom(5);
            child.addActor(jobTable);

            //loop through all the jobs
            for(JobType job : g.station.model.possibleJobs){
                Image tierIcon = new Image(Asset.retrieveEntityIcon(job.getModel().getTier()));
                tierIcon.setColor(Color.GRAY);
                jobTable.add(tierIcon).padRight(8);

                Label nameLabel = new Label(job.name(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
                nameLabel.setColor(Color.GRAY);
                jobTable.add(nameLabel).align(Align.left).padRight(10);

                //create the cost labels
                Label[] costLabels = new Label[4];
                int i=0;
                for(Gem gem : com.twisted.logic.descriptors.Gem.orderedGems){
                    costLabels[i] = new Label(""+job.getGemCost(gem), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
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
                        tierIcon.setColor(color);
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
        }
    }
    @Override
    void render(float delta, ShapeRenderer shape, SpriteBatch sprite) {}
    @Override
    void dispose() {

    }


    /* Updates */

    /**
     * Update the resources in a given station.
     */
    void stationResourceUpdate(Station s){
        if(resourceLabels != null && resourceLabels.get(s.getId()) != null){
            for(int i=0; i<4; i++){
                resourceLabels.get(s.getId())[i].setText(s.resources[i]);
            }
        }
    }

    /**
     * Update the station's stage or stage timer.
     */
    void stationStageUpdate(Station s){
        //update the vertical groups
        if(stationGroups != null && state != null){
            VerticalGroup g = stationGroups.get(s.getId());
            if(s.owner == state.myId && g != null && !g.hasParent()){
                vertical.addActor(g);
            }
            else if(s.owner != state.myId && g!=null && g.hasParent()){
                vertical.removeActor(g);

                //deselecting
                if(focusStationId == s.getId()){
                    industryFocusStation(null);
                }
            }
        }

        //update focus
        if(focusStationId == s.getId()){
            //stage and timer
            focusStationStage.setVisible(true);
            focusStationStage.setDrawable(Asset.retrieve(Station.getStageIcon(s.stage)));
            focusStationTimer.setVisible(true);
            switch(s.stage){
                case VULNERABLE:
                case ARMORED:
                    focusStationTimer.setText("" + (int) s.stageTimer);
                    break;
                case SHIELDED:
                case RUBBLE:
                    focusStationTimer.setText("");
                    break;
            }
        }
    }

    void shipUpdate(Ship s){
        IndShipRow row = dockedShipRows.get(s.id);
        if(row != null) row.update();
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
            JobRow row = (JobRow) jobQueueTable.getChildren().get(index);
            row.updateTimer(Integer.toString( Math.round(job.timeLeft) ));
        }
        //add the job
        else {
            JobRow row = new JobRow();
            row.updateName(job.jobType.name());
            row.updateTimer(Integer.toString( Math.round(job.timeLeft) ));

            jobMappings.add(position, job);
            jobQueueTable.add(row).growX();
            jobQueueTable.row();
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
            jobQueueTable.removeActorAt(index, true);
        }
    }

    /**
     * Add a docked ship.
     */
    void addDockedShip(Ship ship){
        if(!ship.docked || ship.grid != focusStationId) return;

        Gdx.app.postRunnable(() -> {
            IndShipRow row = new IndShipRow(this, QUEUE_WIDTHS, ship);
            dockInvTable.add(row).growX();
            dockInvTable.row();
            dockedShipRows.put(ship.id, row);
        });
    }

    /**
     * Removes a docked ship.
     */
    void removeDockedShip(int stationId, Ship ship){
        if(stationId != focusStationId) return;

        //remove it if it exists
        IndShipRow toRemove = dockedShipRows.remove(ship.id);
        if(toRemove != null) dockInvTable.removeActor(toRemove);
    }

    /**
     * Adds a packed station.
     */
    void checkAddPackedStation(int stationId, Station.Model type){
        if(stationId != focusStationId) return;

        Gdx.app.postRunnable(() -> {
            IndPackedStationRow row = new IndPackedStationRow(this, QUEUE_WIDTHS, type);
            dockInvTable.add(row).growX();
            dockInvTable.row();
        });
    }

    /**
     * Removes a packed station
     */
    void checkRemovePackedStation(int stationId, Station.Model type){
        if(stationId != focusStationId) return;

        //remove it if it exists
        Actor toRemove = null;
        for(Actor child : dockInvTable.getChildren()){
            if(((IndustryRow) child).matches(type)){
                toRemove = child;
            }
        }
        if(toRemove != null){
            dockInvTable.removeActor(toRemove);
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
            new Quirk(Quirk.Q.MissingDataAfterInput).print();
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
            focusStationStage.setVisible(false);
            focusStationTimer.setVisible(false);
            dockInvTable.clearChildren();
            jobQueueTable.clearChildren();
            jobMappings.clear();
        }
        else {
            focusStationId = station.grid;

            //top text
            focusStationName.setText(station.getFullName());

            //stage and timer
            focusStationStage.setVisible(true);
            focusStationStage.setDrawable(Asset.retrieve(Station.getStageIcon(station.stage)));
            focusStationTimer.setVisible(true);
            switch(station.stage){
                case VULNERABLE:
                case ARMORED:
                    focusStationTimer.setText("" + (int) station.stageTimer);
                    break;
                case SHIELDED:
                case RUBBLE:
                    focusStationTimer.setText("");
                    break;
            }

            //docked ships
            dockInvTable.clearChildren();
            for(Ship s : station.dockedShips.values()){
                addDockedShip(s);
            }
            for(Station.Model t : station.packedStations){
                if(t != null) checkAddPackedStation(focusStationId, t);
            }

            //job queue
            jobQueueTable.clearChildren();
            jobMappings.clear();
            for(int i=0; i<station.currentJobs.size(); i++){
                upsertStationJob(station.getId(), station.currentJobs.get(i), i);
            }
        }
    }

    /**
     * Called when the user attempts to start a job at a station.
     */
    private void industryJobRequest(Station station, JobType job){
        game.client.send(new MJobReq(station.grid, job));
    }
}
