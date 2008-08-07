// 
// $Id$

package com.threerings.msoy.avrg.client {

import flash.events.TimerEvent;
import flash.utils.Timer;
import flash.utils.getTimer;

import com.threerings.msoy.avrg.data.AVRGameAgentObject;
import com.threerings.msoy.avrg.data.AVRGameObject;
import com.threerings.msoy.avrg.data.PlayerLocation;
import com.threerings.msoy.avrg.data.SceneInfo;
import com.threerings.msoy.bureau.client.ThaneWorldService;
import com.threerings.msoy.bureau.client.Window;
import com.threerings.msoy.bureau.util.MsoyBureauContext;
import com.threerings.msoy.room.data.RoomObject;
import com.threerings.presents.client.ResultAdapter;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.SetAdapter;
import com.threerings.presents.dobj.SubscriberAdapter;
import com.threerings.presents.util.SafeSubscriber;
import com.threerings.util.HashMap;
import com.threerings.util.Iterator;
import com.threerings.util.Log;

public class ThaneAVRGameController
{
    public var log :Log = Log.getLog(this);

    /** Initializes the game controller. */
    public function init (
        ctx :MsoyBureauContext, gameObj :AVRGameObject, gameAgentObj :AVRGameAgentObject) :void
    {
        _ctx = ctx;
        _gameObj = gameObj;
        _gameAgentObj = gameAgentObj;

        // create the backend
        _backend = new ThaneAVRGameBackend();

        // set up existing player locations
        if (_gameObj.playerLocs.size () > 0) {
            // This is an unexpected condition, but... if the game agent ever reloads the user code,
            // it may also recreate the controller?
            log.warning("Game object player locations already populated");
            var iter :Iterator = _gameObj.playerLocs.iterator();
            while (iter.hasNext()) {
                bindPlayer(iter.next() as PlayerLocation);
            }
        }

        // listen for player location changes
        _gameObj.addListener(new SetAdapter(entryAdded, entryUpdated, entryRemoved));

        // set up the flush timer
        _flushTimer.addEventListener(TimerEvent.TIMER, handleFlushTimer);
        _flushTimer.start();
    }

    /** Shuts down the AVRG controller. */
    public function shutdown () :void
    {
        // stop the flush timer
        _flushTimer.stop();

        // flush all player bindings
        var bindings :Array = _bindings.values();
        _bindings.clear();
        for each (var binding :PlayerBinding in bindings) {
            flushBinding(binding);
        }

        // shutdown the backend
        backend.shutdown();

        // null our init references
        _ctx = null;
        _gameObj = null;
        _gameAgentObj = null;
    }

    /** Accesses the game backend. */
    public function get backend () :ThaneAVRGameBackend
    {
        return _backend;
    }

    /** Inform the server that the agent is ready. */
    public function agentReady () :void
    {
        log.info("Reporting agent ready " + _gameObj.which() + ".");
        _gameObj.manager.invoke("agentReady");
    }

    protected function entryAdded (event :EntryAddedEvent) :void
    {
        if (event.getName() == AVRGameObject.PLAYER_LOCS) {
            bindPlayer(event.getEntry() as PlayerLocation);
        }
    }

    protected function entryUpdated (event :EntryUpdatedEvent) :void
    {
        if (event.getName() == AVRGameObject.PLAYER_LOCS) {
            bindPlayer(event.getEntry() as PlayerLocation);
        }
    }

    protected function entryRemoved (event :EntryRemovedEvent) :void
    {
        if (event.getName() == AVRGameObject.PLAYER_LOCS) {
            removeBinding((event.getOldEntry() as PlayerLocation).playerId);
        }
    }

    /** Sets up a new binding for the given player, removing the old one if it exists. */
    protected function bindPlayer (location :PlayerLocation) :void
    {
        // Get rid of the old PlayerBinding
        var binding :PlayerBinding = _bindings.get(location.playerId);
        if (binding != null) {
            removeBinding(location.playerId);
        }

        // TODO: should we try to revive a previously removed binding if the sceneId is the same?
        // The difficulty with this would be that a scene may suddenly change hosts for some
        // reason.

        // Get the scene info, if it isn't there this is a no go
        var sceneInfo :SceneInfo = _gameAgentObj.scenes.get(location.sceneId) as SceneInfo;
        if (sceneInfo == null) {
            log.warning("Scene not found for player: " + location);
            return;
        }

        // Create a binding and add it to the map
        binding = new PlayerBinding();
        binding.playerId = location.playerId;
        binding.sceneId = location.sceneId;
        _bindings.put(binding.playerId, binding);

        var info :String = "scene=" + sceneInfo + ", location=" + location;

        // Open the window to the server hosting the player's scene
        var resultListener :ResultAdapter = new ResultAdapter(
            function (cause :String) :void {
                log.warning("Failed to open window [" + info + ", cause=\"" + cause + "\"]");
            },
            function (wnd :Window) :void {
                gotWindow(binding, wnd);
            }
        );

        log.info("Opening window ["  + info + "]");
        _ctx.getWindowDirector().openWindow(sceneInfo.hostname, sceneInfo.port, resultListener);
    }

    protected function gotWindow (binding :PlayerBinding, window :Window) :void
    {
        var info :String = "binding=" + binding + ", window=" + window;

        // close the window immediately if this binding has been removed
        var check :PlayerBinding = _bindings.get(binding.playerId);
        if (check != binding) {
            log.warning("Window no longer needed [" + info + "]");
            _ctx.getWindowDirector().closeWindow(window);
            return;
        }

        log.info("Got window [" + info + "]");

        // set the window so it can be closed later
        binding.window = window;

        // locate the room oid
        var resultListener :ResultAdapter = new ResultAdapter(
            function (cause :String) :void {
                log.warning("Failed to get room oid [" + info + ", cause=\"" + cause + "\"]");
            },
            function (roomOid :int) :void {
                gotRoomOid(binding, roomOid);
            }
        );

        var thaneSvc :ThaneWorldService = 
            window.requireService(ThaneWorldService) as ThaneWorldService;

        thaneSvc.locateRoom(window.getClient(), binding.sceneId, resultListener);
    }

    protected function gotRoomOid (binding :PlayerBinding, oid :int) :void
    {
        var info :String = "binding=" + binding + ", roomOid=" + oid;

        // if this player has been removed, forget it
        var check :PlayerBinding = _bindings.get(binding.playerId);
        if (check != binding) {
            log.warning("Room oid no longer needed [" + info + "]");
            return;
        }
        
        log.info("Got room id ["  + info + "]");

        // subscribe to the rooom object
        var subscriber :SubscriberAdapter = new SubscriberAdapter(
            function (obj :RoomObject) :void {
                gotRoomObject(binding, obj);
            },
            function (oid :int, cause :ObjectAccessError) :void {
                log.warning("Failed to subscribe to room [" + info + ", cause=\"" + cause + "\"]");
            }
        );

        binding.subscriber = new SafeSubscriber(oid, subscriber);
        binding.subscriber.subscribe(binding.window.getDObjectManager());
    }

    protected function gotRoomObject (binding :PlayerBinding, roomObj :RoomObject) :void
    {
        var info :String = "binding=" + binding + ", roomOid=" + roomObj.getOid();

        // if this player has been removed, unsubscribe right away
        var check :PlayerBinding = _bindings.get(binding.playerId);
        if (check != binding) {
            log.warning("Room no longer needed [" + info + "]");
            binding.subscriber.unsubscribe(binding.window.getDObjectManager());
            return;
        }

        log.info("Got room [" + info + "]");

        binding.room = roomObj;

        // TODO: let the server know that we've ratified the room entry now
    }

    /** Removes the binding of the given player. */
    protected function removeBinding (playerId :int) :void
    {
        var binding :PlayerBinding = _bindings.remove(playerId) as PlayerBinding;
        if (binding == null) {
            log.warning("PlayerBinding not found to remove: " + playerId);
            return;
        }

        // Mark for flushing
        log.info("Removing binding: " + binding);
        binding.removalTime = getTimer();
        _removed.push(binding);
    }

    /** Checks for inactive bindings that need to be closed down. */
    protected function handleFlushTimer (event :TimerEvent) :void
    {
        var now :Number = getTimer();
        while (true) {
            if (_removed.length == 0) {
                break;
            }

            var binding :PlayerBinding = _removed[0] as PlayerBinding;

            if (now - binding.removalTime < REMOVED_PLAYER_TIMEOUT) {
                break;
            }

            flushBinding(binding);
            _removed.shift();
        }
    }

    /** Flushes a binding. */
    protected function flushBinding (binding :PlayerBinding) :void
    {
        log.info("Flushing: " + binding);

        if (binding.room != null) {
            binding.subscriber.unsubscribe(binding.window.getDObjectManager());
            binding.subscriber = null;
            binding.room = null;
        }

        if (binding.window != null) {
            _ctx.getWindowDirector().closeWindow(binding.window);
            binding.window = null;
        }
    }

    protected var _ctx :MsoyBureauContext;
    protected var _backend :ThaneAVRGameBackend;
    protected var _gameObj :AVRGameObject;
    protected var _gameAgentObj :AVRGameAgentObject;
    protected var _bindings :HashMap = new HashMap();
    protected var _removed :Array = new Array();
    protected var _flushTimer :Timer = new Timer(FLUSH_CHECK_INTERVAL);

    protected static const FLUSH_CHECK_INTERVAL :int = 60 * 1000;
    protected static const REMOVED_PLAYER_TIMEOUT :int = 60 * 1000;
}

}

import com.threerings.msoy.avrg.data.SceneInfo;
import com.threerings.msoy.bureau.client.Window;
import com.threerings.msoy.room.data.RoomObject;
import com.threerings.util.StringUtil;
import com.threerings.presents.util.SafeSubscriber;

/** Binds a player to its window, scene and room. */
class PlayerBinding
{
    public var playerId :int;
    public var sceneId :int;
    public var window :Window;
    public var subscriber :SafeSubscriber;
    public var room :RoomObject;
    public var removalTime :Number;

    // from Object
    public function toString () :String
    {
        return StringUtil.simpleToString(this);
    }
}
