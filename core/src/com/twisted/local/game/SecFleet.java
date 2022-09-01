package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.twisted.Main;
import com.twisted.Asset;
import com.twisted.local.game.util.FleetContainer;
import com.twisted.local.game.util.FleetTab;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SecFleet extends Sector {

    //constants
    private final static int TOT_HEIGHT =450, MID_HEIGHT=14, TOP_HEIGHT=24;
    private final static float PANEL_NAME_WID=95, GRID_TAG_WID=26, SHIP_NAME_WID=PANEL_NAME_WID-GRID_TAG_WID;
    private final static float PANEL_DIST_WID=35, PANEL_SPD_WID=35, PANEL_HP_WID=50;
    private final static float PANEL_STAGE_WID=16;

    //reference variables
    private final Game game;
    private final Skin skin;

    //tree
    private Group parent;
    private HashMap<TabType, FleetTab> tabs;
    private TabType selectedType;
    private VerticalGroup vertical;
    private Group headerBar;

    //row tracking
    private final ArrayList<Entity> entities;
    private final HashMap<Entity, HorizontalGroup> entityToRow; //stores created rows

    //state
    private float timeWithoutSorting;

    //texture storage (for updating off gdx thread)
    final HashMap<Station.Stage, TextureRegionDrawable> stationStageTextures =
            new HashMap<Station.Stage, TextureRegionDrawable>(){{
        put(Station.Stage.SHIELDED, Asset.retrieve(Station.getStageIcon(Station.Stage.SHIELDED)));
        put(Station.Stage.ARMORED, Asset.retrieve(Station.getStageIcon(Station.Stage.ARMORED)));
        put(Station.Stage.VULNERABLE, Asset.retrieve(Station.getStageIcon(Station.Stage.VULNERABLE)));
    }};


    /* Creation */

    SecFleet(Game game){
        this.game = game;
        this.skin = game.skin;
        this.selectedType = TabType.Fleet;

        entities = new ArrayList<>();
        entityToRow = new HashMap<>();

        timeWithoutSorting = 0;
    }

    @Override
    Group init() {
        //initialize the top level group
        parent = super.init();
        parent.setBounds(0, 230, 300, 450);

        //initialize subsections
        parent.addActor(initDecoration());
        parent.addActor(initTabs());
        parent.addActor(initHeaderBar());
        parent.addActor(initPane());

        return parent;
    }

    private Group initDecoration(){
        //create the group
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        //load texture
        TextureRegionDrawable blackPixel = Asset.retrieve(Asset.Shape.PIXEL_BLACK);

        //create the images
        Image ribbon = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);

        Image embeddedBot = new Image(blackPixel);
        embeddedBot.setBounds(3, 3, parent.getWidth()-6,
                TOT_HEIGHT-12-MID_HEIGHT-TOP_HEIGHT);
        decoration.addActor(embeddedBot);

        Image embeddedMid = new Image(blackPixel);
        embeddedMid.setBounds(3, 3+embeddedBot.getHeight()+3, parent.getWidth()-6,
                MID_HEIGHT);
        decoration.addActor(embeddedMid);

        Image embeddedTop = new Image(blackPixel);
        embeddedTop.setBounds(3, 3+embeddedBot.getHeight()+3+embeddedMid.getHeight()+3,
                parent.getWidth()-6, TOP_HEIGHT);
        decoration.addActor(embeddedTop);

        return decoration;
    }

    private Group initTabs(){
        Group group = new Group();
        group.setPosition(3, 421);

        tabs = new HashMap<>();

        //create the tabs
        TabType[] types = TabType.values();
        for(int i=0; i<types.length; i++){
            tabs.put(types[i], new FleetTab(types[i].toString(), game.skin, Main.glyph,
                    new Vector2(3+66*i, 2), new Vector2(64, 22)));

            //get the group, add it to the overall header group, add listener
            Group tabGroup = tabs.get(types[i]).getHeader();
            group.addActor(tabGroup);

            //add listener
            TabType listenerType = types[i];
            tabGroup.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(event.isHandled()) return;
                    switchSelectedTabs(listenerType);
                    event.handle();
                }
            });
        }

        //select tab
        tabs.get(TabType.Fleet).selectTab(true);

        return group;
    }

    private Group initHeaderBar(){
        headerBar = new Group();
        headerBar.setPosition(5, 401);

        loadHeaderBar(TabType.Fleet);

        return headerBar;
    }

    private ScrollPane initPane(){
        vertical = new VerticalGroup();
        vertical.top().left();
        vertical.columnAlign(Align.left);

        ScrollPane pane = new ScrollPane(vertical, skin);
        pane.setBounds(3, 3, 294, TOT_HEIGHT-12-MID_HEIGHT-TOP_HEIGHT);
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);

        return pane;
    }

    @Override
    void load() {
    }

    @Override
    void render(float delta) {
        timeWithoutSorting += delta;
        if(timeWithoutSorting > 0.5f){
            sortEntities();
            timeWithoutSorting = 0;
        }
    }

    @Override
    void dispose() {
    }


    /* External Event Methods */

    /**
     * Switches which grid data is being displayed.
     */
    void switchGridFocus(){
        if(selectedType != TabType.Fleet){
            //remove everything about the old grid
            vertical.clearChildren();
            entities.clear();
            entityToRow.clear();

            //generate the new grid stuff
            addAllEntities(selectedType);
        }
    }

    /**
     * Adds an entity to the correct spot and updates its values.
     */
    void checkAddEntity(Entity entity){
        //check if this entity should be added
        if(selectedType == TabType.Fleet){
            if(state.myId != entity.owner) return;
        }
        else {
            if(game.getGrid() != entity.grid ||
                    (entity instanceof Ship && ((Ship) entity).warpTimeToLand != 0)) return;
        }

        //remove it if it already exists
        forceRemoveEntity(entity);

        //find the correct position
        int index = 0;
        for(Entity e : entities){
            if(entityOrdering(entity, e) < 0) break;
            else index++;
        }

        //add it
        entities.add(index, entity);
        vertical.addActorAt(index, getEntityRow(entity, selectedType));
        updEntityValues(entity);
    }

    /**
     * Removes the entity from the display.
     */
    void checkRemoveEntity(Entity entity){
        //check if the entity should be removed
        if(state.findEntity(EntPtr.createFromEntity(entity)) != null){
            if(selectedType == TabType.Fleet && entity.owner == state.myId){
                return;
            }
            else {
                //check if it is still in space on the grid
                if(entity instanceof Ship &&
                        state.grids[game.getGrid()].ships.containsKey(entity.getId())){
                    //check ownership
                    switch(selectedType){
                        case Ally:
                            if(entity.owner == state.myId) return;
                            break;
                        case Enemy:
                            if(entity.owner != state.myId) return;
                            break;
                        default:
                            return;
                    }
                }
            }
        }

        forceRemoveEntity(entity);
    }

    /**
     * Reload entity.
     */
    void reloadEntity(Entity entity){
        //find index
        int index = entities.indexOf(entity);

        //remove it
        if(index > -1){
            entities.remove(entity);
            vertical.removeActorAt(index, true);
            entityToRow.remove(entity);
        }

        //add it back
        checkAddEntity(entity);
    }

    /**
     * Sorts the entities based on their proximity to the origin. Does not operate if type is fleet.
     * TODO add different comparing functions
     */
    void sortEntities(){
        Actor swappingOne, swappingTwo;

        if(selectedType != TabType.Fleet){
            //double loop
            for(int i=0; i<entities.size(); i++){
                for(int j=i+1; j<entities.size(); j++){
                    if(entities.get(i).pos.len() > entities.get(j).pos.len()){
                        //perform the swap
                        Collections.swap(entities, i, j);

                        //swap on vertical (had to do manually, the method didn't swap them visually)
                        swappingOne = vertical.removeActorAt(j, false);
                        swappingTwo = vertical.removeActorAt(i, false);
                        vertical.addActorAt(i, swappingOne);
                        vertical.addActorAt(j, swappingTwo);
                    }
                }
            }
        }
    }

    /**
     * Updates the UI to match the entity's current logical values.
     */
    void updEntityValues(Entity entity){
        //get the group, return if it doesn't already exist
        HorizontalGroup group = entityToRow.get(entity);
        if(group == null) return;

        for(Actor a : group.getChildren()){
            if(a instanceof FleetContainer<?>){
                ((FleetContainer<?>) a).updateValuesFromEntity(entity);
            }
            else {
                System.out.println("Unexpected actor type");
                new Exception().printStackTrace();
            }
        }
    }


    /* Internal Event Methods */

    /**
     * Switches which tab is selected.
     */
    private void switchSelectedTabs(TabType type){
        //do nothing if it's the same type
        if(type == selectedType) return;

        //update selected type
        tabs.get(selectedType).selectTab(false);
        selectedType = type;
        tabs.get(selectedType).selectTab(true);

        //clear entities
        vertical.clearChildren();
        entities.clear();
        entityToRow.clear();

        //add all the needed entities
        addAllEntities(type);
        loadHeaderBar(type);
    }

    /**
     * Called when an entity name is clicked on.
     */
    private void entityClicked(int button, Entity entity){
        game.fleetClickEvent(entity);
    }


    /* Utility Methods */

    /**
     * Same as checkRemoveEntity except it doesn't do the checks to see if the entity should be
     * removed.
     */
    private void forceRemoveEntity(Entity entity){
        //find index
        int index = entities.indexOf(entity);

        //remove it
        if(index > -1){
            entities.remove(entity);
            vertical.removeActorAt(index, true);
            entityToRow.remove(entity);
        }
    }

    /**
     * Used to decide the ordering of two entities.
     * @return -1 if entOne belongs higher up. 1 if entTwo belongs higher up. 0 if indifferent.
     */
    private int entityOrdering(Entity entOne, Entity entTwo){
        //check warp
        if(entOne.grid == -1 && entTwo.grid != -1) return -1;
        else if(entOne.grid != -1 && entTwo.grid == -1) return 1;
        //check grid
        else if(entOne.grid < entTwo.grid) return -1;
        else if(entOne.grid > entTwo.grid) return 1;
        //check type
        else if(entOne instanceof Station && entTwo instanceof Ship) return -1;
        else if(entOne instanceof Ship && entTwo instanceof Station) return 1;
        //check docked
        else if(!entOne.isDocked() && entTwo.isDocked()) return -1;
        else if(entOne.isDocked() && !entTwo.isDocked()) return 1;
        //compare positions
        else if(!entOne.isDocked() && !(entOne.grid==-1) && !entTwo.isDocked() && !(entTwo.grid==-1)){
            if(entOne.pos.len() < entTwo.pos.len()) return -1;
            else if(entOne.pos.len() > entTwo.pos.len()) return 1;
        }

        //return equal ordering
        return 0;
    }

    /**
     * Gets the row for a given entity. If none exists, then it creates one.
     */
    private HorizontalGroup getEntityRow(Entity entity, TabType type){

        //TODO cap the size of the caching hashmap
        HorizontalGroup actor = entityToRow.get(entity);

        if(actor == null){
            if(entity instanceof Ship) actor = createShipRow((Ship) entity, type);
            else if(entity instanceof Station) actor = createStationRow((Station) entity, type);

            entityToRow.put(entity, actor);
        }

        return actor;
    }

    /**
     * Adds all entities necessary for this tab type.
     */
    private void addAllEntities(TabType type){
        if(type == TabType.Fleet){
            //ships in warp
            for(Ship s : state.inWarp.values()){
                checkAddEntity(s);
            }
            //not in warp
            for(Grid g : state.grids){
                //station
                if(g.station.owner == state.myId){
                    checkAddEntity(g.station);
                }
                //ships in space
                for(Ship s : g.ships.values()){
                    checkAddEntity(s);
                }
                //docked ships
                for(Ship s : g.station.dockedShips.values()){
                    checkAddEntity(s);
                }
            }
        }
        else {
            Grid g = state.grids[game.getGrid()];
            //add station
            if(type == TabType.Grid ||
                    (type == TabType.Ally && g.station.owner == state.myId) ||
                    (type == TabType.Enemy && g.station.owner != state.myId)){
                checkAddEntity(g.station);
            }
            //add ships
            for(Ship s : g.ships.values()){
                if(type == TabType.Grid ||
                        (type == TabType.Ally && s.owner == state.myId) ||
                        (type == TabType.Enemy && s.owner != state.myId)){
                    checkAddEntity(s);
                }
            }

        }
    }

    /**
     * Loads the header bar for the given tab type.
     */
    private void loadHeaderBar(TabType type){
        headerBar.clearChildren();

        //name child
        Label nameLab = new Label("Name", skin, "small", Color.GRAY);
        nameLab.setFontScale(0.8f);
        headerBar.addActor(nameLab);

        //position child
        Label posLab = new Label("Dst", skin, "small", Color.GRAY);
        posLab.setFontScale(0.8f);
        posLab.setX(PANEL_NAME_WID);
        headerBar.addActor(posLab);

        //velocity child
        Label spdLab = new Label("Spd", skin, "small", Color.GRAY);
        spdLab.setFontScale(0.8f);
        spdLab.setX(PANEL_NAME_WID + PANEL_DIST_WID);
        headerBar.addActor(spdLab);

        //hp child
        Label hpLab = new Label("HP", skin, "small", Color.GRAY);
        hpLab.setFontScale(0.8f);
        hpLab.setX(PANEL_NAME_WID + PANEL_DIST_WID + PANEL_SPD_WID);
        headerBar.addActor(hpLab);
    }

    /**
     * Creates the container objects to be placed in the group for the row.
     */
    private HorizontalGroup createShipRow(Ship ship, TabType type){
        HorizontalGroup group = new HorizontalGroup();

        //grid tag
        if(type == TabType.Fleet){
            Label gridTagLabel = new Label("[X]", skin, "small", Color.LIGHT_GRAY);
            group.addActor(new FleetContainer<Label>(gridTagLabel, GRID_TAG_WID) {
                @Override
                public void updateValuesFromEntity(Entity entity) {
                    if(entity.grid != -1) actor().setText("[" + state.grids[entity.grid].nickname  + "]");
                    else actor().setText("[W]");
                }
            });
        }

        //name label
        Label nameLabel = new Label(ship.getSubtype().toString(), skin, "small", Color.WHITE);
        group.addActor(new FleetContainer<Label>(nameLabel, type==TabType.Fleet ? SHIP_NAME_WID : PANEL_NAME_WID,
                true, false) {
            @Override
            public void updateValuesFromEntity(Entity entity) {
                actor().setColor(state.findColorForOwner(entity.owner));
            }
            @Override
            public void eventLeftClick(){
                entityClicked(Input.Buttons.LEFT, ship);
            }
        });

        //position label
        Label posLabel = new Label("", skin, "small", Color.LIGHT_GRAY);
        group.addActor(new FleetContainer<Label>(posLabel, PANEL_DIST_WID){
            @Override
            public void updateValuesFromEntity(Entity entity){
                //default
                if(entity.grid != -1 && !entity.isDocked()){
                    actor().setText(Main.df1.format(entity.pos.len()));
                    actor().setFontScale(1);
                }
                //docked
                else if(entity.isDocked()){
                    actor().setText("(Docked)");
                    actor().setFontScale(0.75f);
                }
                //in warp
                else if(entity.grid == -1) {
                    actor().setText("");
                }
            }
        });

        //speed label
        Label spdLabel = new Label("", skin, "small", Color.LIGHT_GRAY);
        group.addActor(new FleetContainer<Label>(spdLabel, PANEL_SPD_WID) {
            @Override
            public void updateValuesFromEntity(Entity entity){
                //default
                if(entity.grid != -1 && !entity.isDocked()){
                    actor().setText(Main.df1.format(entity.vel.len()));
                }
                //in warp or docked
                else {
                    actor().setText("");
                }

            }
        });

        //hp graphic
        Group hpGroup = new Group();
        Gdx.app.postRunnable(() -> {
            Image hpOutline = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
            hpOutline.setBounds(0, -5, PANEL_HP_WID, 10);
            hpGroup.addActor(hpOutline);
            Image hpBground = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
            hpBground.setBounds(1, -4, PANEL_HP_WID-2, 8);
            hpGroup.addActor(hpBground);
            Image hpValue = new Image(Asset.retrieve(Asset.Shape.PIXEL_GREEN));
            hpValue.setBounds(1, -4, PANEL_HP_WID-2, 8);
            hpGroup.addActor(hpValue);
            group.addActor(new FleetContainer<Group>(hpGroup, PANEL_HP_WID) {
                @Override
                public void updateValuesFromEntity(Entity entity) {
                    Ship sh = (Ship) entity;
                    hpValue.setWidth((PANEL_HP_WID-2) * sh.health/sh.getMaxHealth());
                }
            });
        });

        return group;
    }

    /**
     * Creates the container objects to be placed in the group for the row.
     */
    private HorizontalGroup createStationRow(Station station, TabType type){
        HorizontalGroup group = new HorizontalGroup();

        //name label
        Label nameLabel = new Label(station.getFleetName(), skin, "small", Color.WHITE);
        group.addActor(new FleetContainer<Label>(nameLabel, PANEL_NAME_WID,
                true, false) {
            @Override
            public void updateValuesFromEntity(Entity entity) {
                actor().setColor(state.findColorForOwner(entity.owner));
            }

            @Override
            public void eventLeftClick(){
                entityClicked(Input.Buttons.LEFT, station);
            }
        });

        //filler
        float stageImgRightSpace = 4;
        group.addActor(new FleetContainer<Actor>(new Actor(),
                PANEL_DIST_WID+PANEL_SPD_WID-PANEL_STAGE_WID-stageImgRightSpace) {
            @Override
            public void updateValuesFromEntity(Entity entity) {

            }
        });

        //requires asset loading
        Gdx.app.postRunnable(() -> {
            //stage img
            Image stageImg = new Image(Asset.retrieve(Asset.UiIcon.STATION_SHIELDED));
            stageImg.setColor(Color.GRAY);
            group.addActor(new FleetContainer<Image>(stageImg, PANEL_STAGE_WID) {

                @Override
                public void updateValuesFromEntity(Entity entity) {
                    stageImg.setDrawable(stationStageTextures.get(((Station) entity).stage));
                }
            });

            //filler
            group.addActor(new FleetContainer<Actor>(new Actor(), stageImgRightSpace) {
                @Override
                public void updateValuesFromEntity(Entity entity) {

                }
            });

            //hp group
            Group hpGroup = new Group();
            Image hpOutline = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
            hpOutline.setBounds(0, -5, PANEL_HP_WID, 10);
            hpGroup.addActor(hpOutline);
            Image hpBground = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
            hpBground.setBounds(1, -4, PANEL_HP_WID-2, 8);
            hpGroup.addActor(hpBground);
            Image shieldValue = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLUE));
            shieldValue.setBounds(1, -4, PANEL_HP_WID-2, 8);
            hpGroup.addActor(shieldValue);
            Image hullValue = new Image(Asset.retrieve(Asset.Shape.PIXEL_GREEN));
            hullValue.setBounds(1, -4, PANEL_HP_WID-2, 8);
            hpGroup.addActor(hullValue);
            group.addActor(new FleetContainer<Group>(hpGroup, PANEL_HP_WID) {
                @Override
                public void updateValuesFromEntity(Entity entity) {
                    Station s = (Station) entity;
                    shieldValue.setVisible(s.stage == Station.Stage.SHIELDED);
                    hullValue.setVisible(s.stage != Station.Stage.SHIELDED);

                    switch(s.stage){
                        case SHIELDED:
                            shieldValue.setWidth((PANEL_HP_WID-2) * s.shieldHealth/s.getMaxShield());
                            break;
                        case ARMORED:
                        case VULNERABLE:
                        case RUBBLE:
                            hullValue.setWidth((PANEL_HP_WID-2) * s.hullHealth/s.getMaxHull());
                            break;
                    }
                }
            });
        });

        return group;
    }


    /* Enums */

    public enum TabType {
        Fleet, Grid, Ally, Enemy
    }
}
