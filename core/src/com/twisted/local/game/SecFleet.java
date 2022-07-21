package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
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
    private final static int TOT_HEIGHT =450, MID_HEIGHT=14, TOP_HEIGHT =24;

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
    private final HashMap<Entity, HorizontalGroup> entityToRow; //caches created rows

    //state
    private float timeWithoutSorting;


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
        //set variables

        //create the group
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        //load texture
        Texture blackPixel = new Texture(Gdx.files.internal("images/pixels/black.png"));

        //create the images
        Image ribbon = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
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
            tabs.put(types[i], new FleetTab(types[i].toString(), game.skin, game.glyph,
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


        if(entity instanceof Ship){
            if(selectedType == TabType.Fleet){
                if(entity.grid != -1) ((Label) group.getChild(0)).setText("[" + state.grids[entity.grid].nickname  + "]");
                else ((Label) group.getChild(0)).setText("[W]");
                group.getChild(1).setColor(state.findColorForOwner(entity.owner));
            }
            else {
                group.getChild(0).setColor(state.findColorForOwner(entity.owner));
            }
        }
        else if(entity instanceof Station){
            group.getChild(0).setColor(state.findColorForOwner(entity.owner));
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
        posLab.setX(90);
        headerBar.addActor(posLab);

        //velocity child
        Label spdLab = new Label("Spd", skin, "small", Color.GRAY);
        spdLab.setFontScale(0.8f);
        spdLab.setX(135);
        headerBar.addActor(spdLab);
    }

    /**
     * TODO make more detailed
     */
    private HorizontalGroup createShipRow(Ship ship, TabType type){
        HorizontalGroup group = new HorizontalGroup();

        //actors
        if(type == TabType.Fleet){
            Label tagLabel = new Label("[X]", skin, "small", Color.LIGHT_GRAY);
            group.addActor(tagLabel);
        }
        Label nameLabel = new Label(ship.getType().toString(), skin, "small", Color.WHITE);
        group.addActor(nameLabel);

        //listeners
        nameLabel.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                entityClicked(Input.Buttons.LEFT, ship);
                event.handle();
            }
        });

        return group;
    }

    /**
     * TODO make more detailed
     */
    private HorizontalGroup createStationRow(Station station, TabType type){
        HorizontalGroup group = new HorizontalGroup();

        Label nameLabel = new Label(station.shortNickname, skin, "small", Color.WHITE);
        group.addActor(nameLabel);

        //listeners
        nameLabel.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                entityClicked(Input.Buttons.LEFT, station);
                event.handle();
            }
        });

        return group;
    }


    /* Enums */

    public enum TabType {
        Fleet, Grid, Ally, Enemy
    }
}
