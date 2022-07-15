package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.state.GameState;
import com.twisted.local.game.util.FleetTab;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;

import java.util.Arrays;
import java.util.HashMap;

public class SecFleet extends Sector {

    //reference variables
    private Game game;

    //tree
    private Group parent;
    private ScrollPane bodyPane;
    private HashMap<TabType, FleetTab> tabs;
    private TabType selected;

    //rendering
    private float timeSinceSort;

    //fleet grid index tracking
    private int[] fleetGridEntCt;
    private int fleetWarpEntCt;


    /**
     * Constructor
     */
    SecFleet(Game game){
        this.game = game;
        this.selected = TabType.Fleet;
    }

    @Override
    Group init() {
        //initialize the top level group
        parent = super.init();
        parent.setBounds(0, 230, 300, 450);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        Image ribbon = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);
        Image embeddedBot = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embeddedBot.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-9-24);
        decoration.addActor(embeddedBot);
        Image embeddedTop = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embeddedTop.setBounds(3, 6+embeddedBot.getHeight(), parent.getWidth()-6, 24);
        decoration.addActor(embeddedTop);

        //initialize tabs
        tabs = new HashMap<>();

        //add subsections
        parent.addActor(initTabs());
        parent.addActor(bodyPane = initScrollPane(parent.getWidth()-6, parent.getHeight()-9-24));
        return parent;
    }

    private Group initTabs(){
        Group group = new Group();
        group.setPosition(3, 421);

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

        return group;
    }

    private ScrollPane initScrollPane(float width, float height){

        ScrollPane pane = new ScrollPane(tabs.get(TabType.Fleet).getVertical());
        pane.setBounds(3, 3, width, height);
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.GRAY);

        return pane;
    }

    @Override
    public void setState(GameState state){
        super.setState(state);

        //entity tracking
        fleetGridEntCt = new int[state.grids.length];
    }

    @Override
    void load() {
        //set the currently selected one
        switchSelectedTabs(TabType.Fleet);
    }

    @Override
    void render(float delta) {
        timeSinceSort += delta;

        if(timeSinceSort > 0.5f){
            //perform the sort
            sortTab();

            //reset the timer
            timeSinceSort = 0;
        }
    }

    @Override
    void dispose() {
    }


    /* Event Methods */

    /**
     * Switches which tab is currently selected and displaying.
     */
    void switchSelectedTabs(TabType switchTo){
        //unselect the current one
        tabs.get(selected).selectTopTab(false);
        tabs.get(selected).clearEntities();

        //reset the fleet counter
        fleetWarpEntCt = 0;
        Arrays.fill(fleetGridEntCt, 0);

        //select the new one
        selected = switchTo;
        tabs.get(selected).selectTopTab(true);
        bodyPane.setActor(tabs.get(selected).getVertical());
        loadEntitiesForSelectedTab();

        //sort correctly
        sortTab();
    }

    /**
     * Reloads all entities on the given tab.
     */
    void reloadTabEntities(){
        //reset the fleet counter
        fleetWarpEntCt = 0;
        Arrays.fill(fleetGridEntCt, 0);

        //clear all entities
        tabs.get(selected).clearEntities();
        //load the entities
        loadEntitiesForSelectedTab();
    }

    /**
     * Tells this sector to update the entity with new information from the state.
     * Can add/remove entity if needed.
     * @param entGrid The grid the entity is now on.
     */
    void upsertEntity(Entity entity, int entGrid){
        //updates the graphics
        entity.fleetRow.updateDisplay(state, entGrid);

        //remove from non-fleet tab  TODO remove from fleet tab
        if(tabs.get(selected).hasEntityRow(entity.fleetRow) &&
                entGrid != game.getGrid() &&
                (selected==TabType.Grid || selected==TabType.Ally || selected==TabType.Enemy)){
            tabs.get(selected).removeEntity(entity.fleetRow);
        }
        //add to fleet tab
        if(!tabs.get(selected).hasEntityRow(entity.fleetRow) &&
                (entity.owner == state.myId && selected==TabType.Fleet)){
            tabs.get(selected).addEntityRowAt(entity, selected, fleetTabIndexBasedOnGrid(entGrid));
            if(entGrid != -1){
                fleetGridEntCt[entGrid]++;
            }
            else {
                fleetWarpEntCt++;
            }
        }
        //add to non-fleet tab
        else if(!tabs.get(selected).hasEntityRow(entity.fleetRow) &&
                (entGrid == game.getGrid() &&
                (selected==TabType.Grid ||
                (selected==TabType.Ally && entity.owner == state.myId) ||
                (selected==TabType.Enemy && entity.owner != state.myId)))){
            tabs.get(selected).addEntityRow(entity, selected);
        }
    }

    public void updateEntityEnterWarp(Entity entity, int originGridId){
        entity.fleetRowDisplayingWarp = true;

        if(selected==TabType.Fleet){
            tabs.get(selected).moveEntityRow(entity.fleetRow, fleetTabIndexBasedOnGrid(-1));

            fleetWarpEntCt++;
            fleetGridEntCt[originGridId]--;
        }
    }

    public void updateEntityExitWarp(Entity entity, int destGridId){
        entity.fleetRowDisplayingWarp = false;

        if(selected==TabType.Fleet){
            //the -1 accounts for this ship leaving warp
            tabs.get(selected).moveEntityRow(entity.fleetRow, fleetTabIndexBasedOnGrid(destGridId)-1);

            fleetWarpEntCt--;
            fleetGridEntCt[destGridId]++;
        }
    }

    /**
     * Called when an entity's name is clicked on.
     */
    public void entityNameClicked(int button, Entity entity){
        game.fleetClickEvent(button, entity);
    }


    /* Utility Methods */

    /**
     * Loads all entities for the given tab.
     *
     * TODO stop this from running twice when the game is first starting
     */
    private void loadEntitiesForSelectedTab() {
        FleetTab t = tabs.get(selected);

        switch (selected) {
            case Fleet: {
                //don't need to insert at specific indices because going in grid order
                for (Ship s : state.inWarp.values()) {
                    if (s.owner == state.myId) {
                        t.addEntityRow(s, selected);
                        fleetWarpEntCt++;
                    }
                }
                for (Grid g : state.grids) {
                    //stations
                    if (g.station.owner == state.myId) {
                        t.addEntityRow(g.station, selected);
                        fleetGridEntCt[g.id]++;
                    }
                    //ships
                    for (Ship s : g.ships.values()) {
                        if (s.owner == state.myId) {
                            t.addEntityRow(s, selected);
                            fleetGridEntCt[g.id]++;
                        }
                    }
                }
                break;
            }
            case Grid: {
                Grid g = state.grids[game.getGrid()];
                //station
                t.addEntityRow(g.station, selected);
                for (Ship s : g.ships.values()) {
                    t.addEntityRow(s, selected);
                }
                break;
            }
            case Ally: {
                Grid g = state.grids[game.getGrid()];
                //station
                if (g.station.owner == state.myId) {
                    t.addEntityRow(g.station, selected);
                }
                //ships
                for (Ship s : g.ships.values()) {
                    if (s.owner == state.myId) {
                        t.addEntityRow(s, selected);
                    }
                }
                break;
            }
            case Enemy: {
                Grid g = state.grids[game.getGrid()];
                //station
                if (g.station.owner != state.myId) {
                    t.addEntityRow(g.station, selected);
                }
                //ships
                for (Ship s : g.ships.values()) {
                    if (s.owner != state.myId) {
                        t.addEntityRow(s, selected);
                    }
                }
                break;
            }
        }
    }

    /**
     * Checks if a sort should be performed, then performs it if it should.
     */
    private void sortTab(){
        if(selected != TabType.Fleet){
            tabs.get(selected).sortByPosOrigin();
        }
    }

    /**
     * Returns the index that an entity on the given grid should be placed.
     */
    private int fleetTabIndexBasedOnGrid(int entGrid){
        int count = fleetWarpEntCt;

        //loop through grids until reaching the correct one
        if(entGrid != -1){
            int g = 0;
            while (g <= entGrid) {
                count += fleetGridEntCt[g];
                g++;
            }
        }

        return count;
    }


    /* Enums */

    public enum TabType {
        Fleet, Grid, Ally, Enemy
    }
}
