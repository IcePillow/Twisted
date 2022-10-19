package com.twisted.local.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.twisted.Asset;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.local.game.util.*;
import com.twisted.local.lib.Ribbon;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.net.msg.gameReq.*;

import java.util.HashMap;

/**
 * The details sector that shows the details of a particular entity when clicked on. Currently
 * displayed in the bottom left corner.
 *
 * Tree
    > parent
        > (0) decoration
        > (1) child parents (empty/ship/etc)
 */
public class SecDetails extends Sector {

    //high level references
    private final Game game;
    private final Skin skin;

    //tree
    private Group parent;

    //graphics state
    private final HashMap<Display, DetsGroup> displayGroups;
    private Display activeDisplay;

    //input state
    private ExternalWait externalWait;
    private EntPtr storeEntClick;
    private int storeIntClick;


    /**
     * Constructor
     */
    SecDetails(Game game){
        this.game = game;
        this.skin = game.skin;

        externalWait = ExternalWait.NONE;
        activeDisplay = Display.EMPTY;

        displayGroups = new HashMap<>();
    }

    /**
     * Initialize the group.
     */
    @Override
    Group init() {
        parent = super.init();
        Vector2 size = new Vector2(300, 125); //TODO use this when creating groups
        parent.setBounds(0, 100, 300, 125);

        //initialize stuff
        parent.addActor(initDecoration(size));
        initDisplayGroups(size);

        return parent;
    }
    private Group initDecoration(Vector2 size){
        //decoration group
        Group decoration = new Group();

        //add the main window background
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        decoration.addActor(ribbon);

        Image embedded = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        decoration.addActor(embedded);

        //return
        return decoration;
    }
    private void initDisplayGroups(Vector2 size){
        //create the display groups
        displayGroups.put(Display.EMPTY, new EmptyDets(this, skin, size));
        displayGroups.put(Display.SHIP_IN_SPACE, new DetsShipInSpace(this, skin, size));
        displayGroups.put(Display.SHIP_DOCKED, new DetsShipDocked(this, skin, size));
        displayGroups.put(Display.SHIP_IN_WARP, new DetsShipInWarp(this, skin, size));
        displayGroups.put(Display.STATION_BASIC, new DetsStation(this, skin, size));

        //add the default actor
        parent.addActor(displayGroups.get(Display.EMPTY));
    }

    @Override
    void setState(ClientGameState state){
        this.state = state;

        for(DetsGroup d : displayGroups.values()){
            d.setState(state);
        }
    }
    @Override
    void load() {

    }
    @Override
    void render(float delta) {}
    @Override
    void dispose() {

    }


    /* Event Methods */

    @Override
    void viewportClickEvent(Vector2 screenPos, Vector2 gamePos, EntPtr ptr) {
        Entity ent = displayGroups.get(activeDisplay).getSelectedEntity();

        if(ent == null){
            System.out.println("Unexpected state");
            new Exception().printStackTrace();
            return;
        }
        else if(ent instanceof Ship){
            //create the request
            MGameReq req = null;

            if(game.getGrid() == ent.grid){
                if(externalWait == ExternalWait.MOVE){
                    req = new MShipMoveReq(ent.grid, ent.getId(), gamePos);

                    game.updateCrossSectorListening(null, null);
                }
                else if(externalWait == ExternalWait.ALIGN){
                    Ship s = state.grids[ent.grid].ships.get(ent.getId());
                    req = new MShipAlignReq(ent.grid, ent.getId(),
                            (float) Math.atan2(gamePos.y-s.pos.y, gamePos.x-s.pos.x));

                    game.updateCrossSectorListening(null, null);
                }
                else if(externalWait == ExternalWait.ORBIT_WHO){
                    if(ptr != null){
                        storeEntClick = ptr;

                        game.updateCrossSectorListening(this, "Orbit radius command...");
                        externalWait = ExternalWait.ORBIT_DIST;
                        game.viewportSelection(SecViewport.Select.BASE_MOUSE_CIRCLE, true,
                                new EntPtr(ptr.type, ptr.id, ent.grid, ent.isDocked()), Color.WHITE,
                                0);
                    }
                    else {
                        game.updateCrossSectorListening(null, null);
                    }
                }
                else if(externalWait == ExternalWait.ORBIT_DIST){
                    Entity target = null;
                    if(storeEntClick.type == Entity.Type.Ship){
                        target = state.grids[ent.grid].ships.get(storeEntClick.id);
                    }
                    else if(storeEntClick.type == Entity.Type.Station){
                        target = state.grids[ent.grid].station;
                    }

                    if(target != null){
                        float radius = gamePos.dst(target.pos);

                        req = new MShipOrbitReq(ent.grid, ent.getId(), storeEntClick.type,
                                storeEntClick.id, radius);
                    }

                    game.updateCrossSectorListening(null, null);
                }
                else if(externalWait == ExternalWait.WEAPON_TARGET){
                    Entity target = state.findEntity(ptr);
                    //deny
                    if(target == null){
                        game.addToLog("Unexpectedly could not find target", SecLog.LogColor.GRAY);
                    }
                    else if(target.pos.dst(ent.pos) >= ((Ship) ent).weapons[storeIntClick].subtype().getRange()){
                        game.addToLog("Cannot target due to range", SecLog.LogColor.GRAY);
                    }
                    //accept
                    else {
                        req = new MWeaponActiveReq(ent.grid, ent.getId(), storeIntClick, true, ptr);
                    }

                    game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, false,
                            null, null, 0);
                    game.updateCrossSectorListening(null, null);
                }
                else {
                    game.updateCrossSectorListening(null, null);
                }
            }
            else if(externalWait == ExternalWait.WARP){
                if(state.findEntity(ptr).isValidBeacon()){
                    req = new MShipWarpReq(ent.grid, ent.getId(), ptr);
                }
                else {
                    game.addToLog("Entity is not a valid beacon for warping", SecLog.LogColor.GRAY);
                }

                game.updateCrossSectorListening(null, null);
            }
            else {
                game.addToLog("Can't cmd a ship on a dif grid", SecLog.LogColor.GRAY);
                game.updateCrossSectorListening(null, null);
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);
        }
    }
    @Override
    void minimapClickEvent(int grid){
        Entity ent = displayGroups.get(activeDisplay).getSelectedEntity();
        if(ent == null){
            System.out.println("Unexpected state");
            new Exception().printStackTrace();
            return;
        }
        else if(ent instanceof Ship){
            if(ent.grid != -1){
                //create the request
                MGameReq req = null;
                if(externalWait == ExternalWait.WARP){
                    req = new MShipWarpReq(ent.grid, ent.getId(), EntPtr.createFromEntity(state.grids[grid].station));
                }
                else if(externalWait == ExternalWait.ALIGN){
                    //calculate the angle
                    Vector2 g2 = state.grids[grid].pos;
                    Vector2 g1 = state.grids[ent.grid].pos;
                    float angle = (float) Math.atan2(g2.y-g1.y, g2.x-g1.x);

                    //create the request
                    req = new MShipAlignReq(ent.grid, ent.getId(), angle);
                }

                //send the message to the server
                if(req != null) game.sendGameRequest(req);
            }
            else {
                game.addToLog("Cannot command a ship that is currently in warp", SecLog.LogColor.GRAY);
            }
        }

        //release the cross sector listening
        game.updateCrossSectorListening(null, null);
    }
    @Override
    void fleetClickEvent(EntPtr ptr){
        //the entity that is currently selected in SecDetails
        Entity ent = displayGroups.get(activeDisplay).getSelectedEntity();

        if(ent == null){
            System.out.println("Unexpected state");
            new Exception().printStackTrace();
            return;
        }
        else if(ent instanceof Ship){
            //create the request
            MGameReq req = null;
            if(externalWait == ExternalWait.ORBIT_WHO){
                //valid
                if(!ptr.docked && ptr.grid==ent.grid && !ptr.matches(ent)){
                    storeEntClick = ptr;

                    game.updateCrossSectorListening(this, "Orbit radius command...");
                    externalWait = ExternalWait.ORBIT_DIST;
                    game.viewportSelection(SecViewport.Select.BASE_MOUSE_CIRCLE, true,
                            new EntPtr(ptr.type, ptr.id, ent.grid, ptr.docked), Color.WHITE, 0);
                }
                //not valid
                else {
                    game.updateCrossSectorListening(null, null);
                    game.addToLog("Cannot orbit that entity right now", SecLog.LogColor.GRAY);
                }
            }
            else if(externalWait == ExternalWait.WEAPON_TARGET){
                //valid
                if(!ptr.docked && ptr.grid==ent.grid && !ptr.matches(ent)){
                    Entity target = state.findEntity(ptr);
                    //deny
                    if(target == null){
                        game.addToLog("Unexpectedly could not find target", SecLog.LogColor.GRAY);
                    }
                    else if(target.pos.dst(ent.pos) >= ((Ship) ent).weapons[storeIntClick].subtype().getRange()){
                        game.addToLog("Cannot target due to range", SecLog.LogColor.GRAY);
                    }
                    //accept
                    else {
                        req = new MWeaponActiveReq(ent.grid, ent.getId(), storeIntClick, true, ptr);
                    }
                    game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, false,
                            null, null, 0);
                }
                //not valid
                else {
                    game.addToLog("Cannot target that entity", SecLog.LogColor.GRAY);
                }
                game.updateCrossSectorListening(null, null);
            }
            else if(externalWait == ExternalWait.WARP){
                //valid
                if(state.findEntity(ptr).isValidBeacon() && !ent.matches(ptr)){
                    req = new MShipWarpReq(ent.grid, ent.getId(), ptr);
                }
                //not valid
                else {
                    game.addToLog("Entity is not a valid beacon for warping", SecLog.LogColor.GRAY);
                }

                game.updateCrossSectorListening(null, null);
            }
            else {
                game.updateCrossSectorListening(null, null);
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);
        }
    }
    @Override
    void crossSectorListeningCancelled(){
        if(externalWait == ExternalWait.ORBIT_DIST){
            game.viewportSelection(SecViewport.Select.BASE_MOUSE_CIRCLE, false, null, null, 0);
        }
        else if(externalWait == ExternalWait.MOVE){
            game.viewportSelection(SecViewport.Select.BASE_MOUSE_LINE, false, null,  null, 0);
        }
        else if(externalWait == ExternalWait.WEAPON_TARGET){
            game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, false, null,  null, 0);
        }

        externalWait = ExternalWait.NONE;
    }


    /* Entity Event Methods */

    /**
     * Selects an entity.
     */
    void selectEntity(Entity entity){
        //make the old one invisible
        parent.removeActor(displayGroups.get(activeDisplay));

        //set the active display
        if(entity == null){
            activeDisplay = Display.EMPTY;
        }
        else if(entity instanceof Ship){
            if(entity.grid == -1) activeDisplay = Display.SHIP_IN_WARP;
            else if(!entity.isDocked()) activeDisplay = Display.SHIP_IN_SPACE;
            else activeDisplay = Display.SHIP_DOCKED;
        }
        else if(entity instanceof Station){
            activeDisplay = Display.STATION_BASIC;
        }

        //stop any listening
        externalWait = null;
        storeEntClick = null;

        //load and make the new one visible
        displayGroups.get(activeDisplay).selectEntity(entity);
        parent.addActor(displayGroups.get(activeDisplay));
        displayGroups.get(activeDisplay).updateEntity();
    }

    /**
     * Stop selecting the entity if it is the currently selected one.
     */
    void deselectEntity(EntPtr ptr){
        if(ptr.matches(displayGroups.get(activeDisplay).getSelectedEntity())){
            selectEntity(null);
        }
    }

    /**
     * Update the entity's ui to match its logical values.
     */
    void updateEntity(EntPtr ptr){
        if(ptr.matches(displayGroups.get(activeDisplay).getSelectedEntity())){
            displayGroups.get(activeDisplay).updateEntity();
        }
    }

    /**
     * Update the entity's ui to match its logical values.
     */
    void updateEntity(Entity ent){
        if(ent.matches(displayGroups.get(activeDisplay).getSelectedEntity())){
            displayGroups.get(activeDisplay).updateEntity();
        }
    }

    /**
     * If this entity is the currently selected one, then it will be deselected and reselected.
     */
    void reloadEntity(Entity entity){
        if(EntPtr.createFromEntity(entity).matches(displayGroups.get(activeDisplay).getSelectedEntity())){
            selectEntity(null);
            selectEntity(entity);
        }
    }



    /* Internal Event Methods */

    public void input(Entity ent, Input input){
        input(ent, input, 0);
    }
    public void input(Entity ent, Input input, int value){
        switch(input){
            //ship click listeners
            case SHIP_STOP: {
                game.sendGameRequest(new MShipStopReq(ent.getId(), ent.grid));
                break;
            }
            case SHIP_MOVE: {
                game.updateCrossSectorListening(this, "Move command...");
                game.viewportSelection(SecViewport.Select.BASE_MOUSE_LINE, true,
                        new EntPtr(Entity.Type.Ship, ent.getId(), ent.grid, ent.isDocked()), Color.WHITE,
                        0);
                this.externalWait = SecDetails.ExternalWait.MOVE;
                break;
            }
            case SHIP_ORBIT: {
                game.updateCrossSectorListening(this, "Orbit command...");
                this.externalWait = SecDetails.ExternalWait.ORBIT_WHO;
                break;
            }
            case SHIP_ALIGN: {
                game.updateCrossSectorListening(this, "Align command...");
                this.externalWait = SecDetails.ExternalWait.ALIGN;
                break;
            }
            case SHIP_WARP: {
                game.updateCrossSectorListening(this, "Warp command...");
                this.externalWait = SecDetails.ExternalWait.WARP;
                break;
            }
            case SHIP_DOCK: {
                game.sendGameRequest(new MShipDockReq(ent.getId(), ent.grid));
                break;
            }
            case SHIP_UNDOCK: {
                game.sendGameRequest(new MShipUndockReq(ent.getId(), ent.grid));
                break;
            }
            case SHIP_WEAPON_TOGGLE: {
                Ship sh = (Ship) ent;
                //turning off or doesn't require target
                if(sh.weapons[value].isActive() || !sh.weapons[value].requiresTarget()){
                    game.sendGameRequest(new MWeaponActiveReq(ent.grid, ent.getId(), value,
                            !((Ship) ent).weapons[value].isActive(), null));
                }
                //turning on and requires target
                else {
                    storeIntClick = value;
                    this.externalWait = ExternalWait.WEAPON_TARGET;
                    game.updateCrossSectorListening(this, "Weapon command...");
                }
                break;
            }

            //ship hover listeners
            case SHIP_DOCK_HOVER_ON: {
                if(ent.grid != -1 && !ent.isDocked()){
                    //get the station and do ownership checks
                    Station station = state.grids[ent.grid].station;
                    if(station.owner != ent.owner) break;

                    if(externalWait != ExternalWait.WEAPON_TARGET) {
                        game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, true,
                                EntPtr.createFromEntity(station), Color.GREEN, station.model.dockingRadius);
                    }
                }
                break;
            }
            case SHIP_WEAPON_HOVER_ON: {
                if(externalWait != ExternalWait.WEAPON_TARGET){
                    game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, true,
                            EntPtr.createFromEntity(ent), Color.YELLOW,
                            ((Ship)ent).weapons[value].subtype().getRange());
                }
                break;
            }
            case SHIP_DOCK_HOVER_OFF:
            case SHIP_WEAPON_HOVER_OFF: {
                if(externalWait != ExternalWait.WEAPON_TARGET){
                    game.viewportSelection(SecViewport.Select.CIRCLE_RANGE_IND_ROT, false,
                            null, null, 0);
                }
                break;
            }
        }
    }
    public void input(MGameReq request){
        game.sendGameRequest(request);
    }

    public void scrollFocus(Actor actor){
        game.scrollFocus(actor);
    }
    public void keyboardFocus(Actor actor){
        game.keyboardFocus(actor);
    }


    /* Enums */

    /**
     * What this sector is listening for from any given external sector.
     */
    public enum ExternalWait {
        //all
        NONE,
        //viewport
        MOVE,
        ORBIT_WHO,
        ORBIT_DIST,
        WEAPON_TARGET,
        //viewport & minimap
        ALIGN,
        //minimap
        WARP,
    }
    /**
     * Input types.
     */
    public enum Input {
        SHIP_STOP,
        SHIP_MOVE,
        SHIP_ORBIT,
        SHIP_ALIGN,
        SHIP_WARP,
        SHIP_DOCK,
        SHIP_UNDOCK,
        SHIP_WEAPON_TOGGLE,

        SHIP_WEAPON_HOVER_ON,
        SHIP_WEAPON_HOVER_OFF,
        SHIP_DOCK_HOVER_ON,
        SHIP_DOCK_HOVER_OFF,
    }
    /**
     * Types of display for this sector.
     */
    public enum Display {
        EMPTY,
        SHIP_IN_SPACE,
        SHIP_DOCKED,
        SHIP_IN_WARP,
        STATION_BASIC,
    }
}
