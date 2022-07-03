package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.twisted.local.game.SecFleet;
import com.twisted.logic.entities.Entity;

import java.util.ArrayList;
import java.util.Collections;

public class FleetTab {

    /**
     * Title of tab that can be clicked to switch tabs.
     */
    private Group header;
    public Group getHeader() {
        return header;
    }

    /**
     * Vertical group that goes in the scrollpane.
     */
    private VerticalGroup vertical;
    public VerticalGroup getVertical() {
        return vertical;
    }

    //graphical stuff
    private Label label;
    private Image black;
    private Skin skin;

    //logical
    private final ArrayList<FleetRow> rows;
    private Actor swappingOne, swappingTwo;
    private int countDisplayTop;


    /**
     * Creates a header object for a SecFleet object. Does not add any listeners.
     * Also creates a vertical group for the scroll pane.
     */
    public FleetTab(String name, Skin skin, GlyphLayout glyph, Vector2 headerPos, Vector2 headerSize) {
        //copy
        this.skin = skin;
        this.countDisplayTop = 0;

        //graphics
        createGraphics(name, glyph, headerPos, headerSize);

        //create the fleet row storage
        rows = new ArrayList<>();
    }

    private void createGraphics(String name, GlyphLayout glyph, Vector2 headerPos,
                                Vector2 headerSize) {
        //creates a group
        header = new Group();
        header.setPosition(headerPos.x, headerPos.y);

        //create the background images
        Image purple = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        purple.setSize(headerSize.x, headerSize.y);
        header.addActor(purple);
        black = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        black.setBounds(2, 0, headerSize.x - 4, headerSize.y - 2);
        header.addActor(black);

        //create the label
        label = new Label(name, skin, "small", Color.LIGHT_GRAY);
        glyph.setText(skin.getFont("small"), name);
        label.setPosition(headerSize.x / 2f - glyph.width / 2f, -2);
        header.addActor(label);

        //create the vertical
        vertical = new VerticalGroup();
        vertical.top().left();
        vertical.columnAlign(Align.left);
    }


    /* Exterior Methods */

    /**
     * Sort the rows of this tab by their proximity to origin.
     * Should only be used if the tab is on a single grid.
     */
    public void sortByPosOrigin() {

        for(int i=0; i<rows.size(); i++){
            for(int j=i+1; j<rows.size(); j++){
                if(rows.get(i).entity.pos.len() > rows.get(j).entity.pos.len()){
                    //perform swap on arraylist
                    Collections.swap(rows, i, j);

                    //swap on vertical (had to do manually, the method didn't swap them visually)
                    swappingOne = vertical.removeActorAt(j, false);
                    swappingTwo = vertical.removeActorAt(i, false);
                    vertical.addActorAt(i, swappingOne);
                    vertical.addActorAt(j, swappingTwo);
                }
            }
        }
    }

    /**
     * @param select True to select, false to deselect.
     */
    public void selectTopTab(boolean select) {
        black.setVisible(!select);
    }

    /**
     * Adds a fleet row.
     */
    public void addEntityRow(Entity entity, SecFleet.TabType type){
        entity.fleetRow.switchDisplayType(type);
        vertical.addActorAt(vertical.getChildren().size, entity.fleetRow.group);
        rows.add(entity.fleetRow);
    }

    /**
     * Removes an entity row from both the scrollpane's vertical group and the state tracking list.
     */
    public void removeEntity(FleetRow row){
        vertical.removeActor(row.group);
        rows.remove(row);
    }

    /**
     * Clears all entities.
     */
    public void clearEntities() {
        //it glitched when I tried to use vertical.clearChildren() so this is done manually
        while (vertical.getChildren().size > 0) {
            vertical.removeActorAt(0, true);
        }
        rows.clear();
    }

    /**
     * Returns whether this tab contains the passed in entity row.
     */
    public boolean hasEntityRow(FleetRow entityRow){
        return rows.contains(entityRow);
    }

    /**
     * Reorders an entity row that is currently displayed to display it at the top.
     * @param display True to show at top, false to show in default spot.
     */
    public void displayEntityRowAtTop(FleetRow row, SecFleet.TabType type, boolean display){
        //remove it to be replaced somewhere else
        removeEntity(row);

        //add to the top
        if(display){
            vertical.addActorAt(countDisplayTop, row.group);
            countDisplayTop++;

            rows.add(row);
        }
        //add to the default
        else {
            vertical.addActorAt(vertical.getChildren().size, row.group);

            countDisplayTop--;
        }
    }

}
