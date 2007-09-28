//
// $Id$

package com.threerings.msoy.game.data;

import com.threerings.crowd.data.OccupantInfo;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.OidList;

/**
 * A game config for an AVR game.
 */
public class AVRGameObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>state</code> field. */
    public static final String STATE = "state";

    /** The field name of the <code>playerOids</code> field. */
    public static final String PLAYER_OIDS = "playerOids";

    /** The field name of the <code>players</code> field. */
    public static final String PLAYERS = "players";
    // AUTO-GENERATED: FIELDS END

    /** Contains the game's memories. */
    public DSet<GameState> state = new DSet<GameState>();

    /**
     * Tracks the oid of the body objects of all of the active players of this game
     */
    public OidList playerOids = new OidList();

    /**
     * Contains an {@link OccupantInfo} record for each player of this game.
     */
    public DSet<OccupantInfo> players = new DSet<OccupantInfo>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>state</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToState (GameState elem)
    {
        requestEntryAdd(STATE, state, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>state</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromState (Comparable key)
    {
        requestEntryRemove(STATE, state, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>state</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateState (GameState elem)
    {
        requestEntryUpdate(STATE, state, elem);
    }

    /**
     * Requests that the <code>state</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setState (DSet<com.threerings.msoy.game.data.GameState> value)
    {
        requestAttributeChange(STATE, value, this.state);
        @SuppressWarnings("unchecked") DSet<com.threerings.msoy.game.data.GameState> clone =
            (value == null) ? null : value.typedClone();
        this.state = clone;
    }

    /**
     * Requests that <code>oid</code> be added to the <code>playerOids</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    public void addToPlayerOids (int oid)
    {
        requestOidAdd(PLAYER_OIDS, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>playerOids</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPlayerOids (int oid)
    {
        requestOidRemove(PLAYER_OIDS, oid);
    }

    /**
     * Requests that the specified entry be added to the
     * <code>players</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPlayers (OccupantInfo elem)
    {
        requestEntryAdd(PLAYERS, players, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>players</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPlayers (Comparable key)
    {
        requestEntryRemove(PLAYERS, players, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>players</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updatePlayers (OccupantInfo elem)
    {
        requestEntryUpdate(PLAYERS, players, elem);
    }

    /**
     * Requests that the <code>players</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPlayers (DSet<com.threerings.crowd.data.OccupantInfo> value)
    {
        requestAttributeChange(PLAYERS, value, this.players);
        @SuppressWarnings("unchecked") DSet<com.threerings.crowd.data.OccupantInfo> clone =
            (value == null) ? null : value.typedClone();
        this.players = clone;
    }
    // AUTO-GENERATED: METHODS END
}
