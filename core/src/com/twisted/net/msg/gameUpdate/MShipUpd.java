package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.entities.attach.StationTrans;
import com.twisted.net.msg.summary.WeaponSum;

public class MShipUpd implements MGameUpd {

    //metadata
    public int shipId;

    //state data
    public int grid; //-1 if in warp
    public boolean docked;
    private Vector2 position;
    private Vector2 velocity;
    private float rotation; //stored in degrees

    //command data
    public String moveDescription;

    //weapons
    private WeaponSum[] weapons;

    //warping
    private Ship.Warping warping;
    private float warpCharge;
    public Vector2 warpPos;
    public EntPtr warpTarget;

    //combat
    private float health;


    /* Exterior Facing Methods */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship s){
        //physics
        s.grid = grid;
        s.docked = docked;
        s.pos.set(position);
        s.vel.set(velocity);
        s.rot = rotation;

        //movement
        s.moveDescription = moveDescription;

        //warping
        s.warping = warping;
        s.warpCharge = warpCharge;
        s.warpPos.set(warpPos);
        if(warpTarget != null) s.warpTarget = warpTarget;

        //combat
        s.health = health;

        //weapons
        for(int i=0; i<s.weapons.length; i++){
            weapons[i].copyToWeapon(s.weapons[i]);
        }
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s){
        MShipUpd upd = new MShipUpd();

        //id
        upd.shipId = s.id;

        //physics
        upd.grid = s.grid;
        upd.docked = s.docked;
        upd.position = s.pos.cpy();
        upd.velocity = s.vel.cpy();
        upd.rotation = s.rot;

        //movement
        upd.moveDescription = s.moveDescription;

        //warping
        upd.warping = s.warping;
        upd.warpCharge = s.warpCharge;
        upd.warpPos = s.warpPos.cpy();
        if(upd.warpTarget != null) upd.warpTarget = s.warpTarget.cpy();

        //combat
        upd.health = s.health;

        //weapons
        upd.weapons = new WeaponSum[s.weapons.length];
        for(int i=0; i<s.weapons.length; i++){
            upd.weapons[i] = WeaponSum.createFromWeapon(s.weapons[i]);
        }

        return upd;
    }

}
