//
// $Id$

package com.threerings.msoy.world.client {

import flash.geom.Rectangle;

import flash.events.Event;
import flash.external.ExternalInterface;
import flash.net.URLRequest;
import flash.system.Capabilities;

import mx.controls.Button;
import mx.core.Application;
import mx.core.UIComponent;

import com.threerings.flex.CommandMenu;
import com.threerings.util.Log;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.StringUtil;

import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.ResultWrapper;
import com.threerings.presents.net.Credentials;

import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneObject;

import com.threerings.msoy.chat.client.ChatChannelController;
import com.threerings.msoy.chat.client.IMRegisterDialog;
import com.threerings.msoy.chat.client.ReportingListener;
import com.threerings.msoy.group.data.all.GroupMembership;
import com.threerings.msoy.item.client.ItemService;
import com.threerings.msoy.item.data.ItemMarshaller;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.ItemIdent;

import com.threerings.msoy.party.client.PartyPopup;

import com.threerings.msoy.avrg.client.AVRGamePanel;
import com.threerings.msoy.game.client.MsoyGamePanel;
import com.threerings.msoy.game.data.MsoyGameConfig;

import com.threerings.msoy.client.BootablePlaceController;
import com.threerings.msoy.client.ChatPrefsDialog;
import com.threerings.msoy.client.ControlBar;
import com.threerings.msoy.client.DeploymentConfig;
import com.threerings.msoy.client.HeaderBar;
import com.threerings.msoy.client.LogonPanel;
import com.threerings.msoy.client.MemberService;
import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.client.MsoyController;
import com.threerings.msoy.client.MsoyParameters;
import com.threerings.msoy.client.NoPlaceView;
import com.threerings.msoy.client.PlaceBox;
import com.threerings.msoy.client.Prefs;
import com.threerings.msoy.client.TopPanel;
import com.threerings.msoy.client.UberClient;

import com.threerings.msoy.chat.data.ChatChannel;

import com.threerings.msoy.data.MemberLocation;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.MsoyCodes;

import com.threerings.msoy.data.all.ContactEntry;
import com.threerings.msoy.data.all.FriendEntry;
import com.threerings.msoy.data.all.GatewayEntry;
import com.threerings.msoy.data.all.MemberName;
import com.threerings.msoy.data.all.RoomName;
// import com.threerings.msoy.data.all.SceneBookmarkEntry;

import com.threerings.msoy.room.client.RoomObjectView;
import com.threerings.msoy.room.data.MsoyScene;
import com.threerings.msoy.room.data.MsoySceneModel;

/**
 * Extends the MsoyController with World specific bits.
 */
public class WorldController extends MsoyController
{
//    /** Command to display the recent scenes list. */
//    public static const POP_ROOMS_MENU :String = "PopRoomsMenu";

    /** Command to display the chat channel menu. */
    public static const POP_CHANNEL_MENU :String = "PopChannelMenu";

    /** Opens up a new toolbar and a new room editor. */
    public static const ROOM_EDIT :String = "RoomEdit";

    /** Command to go to a particular place (by Oid). */
    public static const GO_LOCATION :String = "GoLocation";

    /** Command to go to a particular scene. */
    public static const GO_SCENE :String = "GoScene";

    /** Command to go to a member's home scene. */
    public static const GO_MEMBER_HOME :String = "GoMemberHome";

    /** Command to go to a group's home scene. */
    public static const GO_GROUP_HOME :String = "GoGroupHome";

    /** Command to go to a member's current scene. */
    public static const GO_MEMBER_LOCATION :String = "GoMemberLocation";

    /** Command to join a member's current game. */
    public static const JOIN_PLAYER_GAME :String = "JoinPlayerGame";

    /** Command to join a member's currently pending game table. */
    public static const JOIN_PLAYER_TABLE :String = "JoinPlayerTable";

    /** Command to join a game lobby. */
    public static const JOIN_GAME_LOBBY :String = "JoinGameLobby";

    /** Command to join an in-world game. */
    public static const JOIN_AVR_GAME :String = "JoinAVRGame";

    /** Command to leave the in-world game. */
    public static const LEAVE_AVR_GAME :String = "LeaveAVRGame";

    /** Command to invite someone to be a friend. */
    public static const INVITE_FRIEND :String = "InviteFriend";

    /** Command to open the chat interface for a particular chat channel. */
    public static const OPEN_CHANNEL :String = "OpenChannel";

    /** Command to logon to an im account. */
    public static const REGISTER_IM :String = "RegisterIM";

    /** Command to logoff an im account. */
    public static const UNREGISTER_IM :String = "UnregisterIM";

    /** Command to view a member's profile, arg is [ memberId ] */
    public static const VIEW_MEMBER :String = "ViewMember";

    /** Command to view a game, arg is [ gameId ] */
    public static const VIEW_GAME :String = "ViewGame";

    /** Command to visit a member's current location */
    public static const VISIT_MEMBER :String = "VisitMember";

    /** Command to view a groups's page, arg is [ groupId ] */
    public static const VIEW_GROUP :String = "ViewGroup";

    /** Command to view the "my avatars" page. */
    public static const VIEW_MY_AVATARS :String= "ViewMyAvatars";

    /** Command to view the "my avatars" page. */
    public static const VIEW_MY_FURNITURE :String= "ViewMyFurniture";

    /** Command to view the "mail" page. */
    public static const VIEW_MAIL :String= "ViewMail";

    /** Command to issue an invite to a current guest. */
    public static const INVITE_GUEST :String = "InviteGuest";

    /** Command to respond to a request to follow another player. */
    public static const RESPOND_FOLLOW :String = "RespondFollow";

    /** Command to open the account creation UI. */
    public static const CREATE_ACCOUNT :String = "CreateAccount";

    /** Command to complain about a member. */
    public static const COMPLAIN_MEMBER :String = "ComplainMember";

    /** Command to toggle the client to full browser height. */
    public static const TOGGLE_HEIGHT :String = "ToggleHeight";

    /** Command to boot a partymate from the party. */
    public static const BOOT_PARTYMATE :String = "BootPartymate";

    /** Command to promote a partymate to leader. */
    public static const PROMOTE_PARTYMATE :String = "PromotePartymate";

    /** Command to invoke when the featured place was clicked. */
    public static const FEATURED_PLACE_CLICKED :String = "FeaturedPlaceClicked";

    public function WorldController (ctx :WorldContext, topPanel :TopPanel)
    {
        super(ctx, topPanel);
        _wctx = ctx;

        // ensure that the compiler includes these necessary symbols
        var c :Class;
        c = ItemMarshaller;
    }

    /**
     * Sets (or clears) the current AVRG overlay.
     */
    public function setAVRGamePanel (panel :AVRGamePanel) :void
    {
        var container :PlaceBox = _wctx.getTopPanel().getPlaceContainer();

        if (_avrGamePanel) {
            if (_avrGamePanel == panel) {
                return;
            }
            container.removeOverlay(_avrGamePanel);
            _avrGamePanel = null;
        }
        if (panel) {
            container.addOverlay(panel, PlaceBox.LAYER_AVRG_PANEL);
            _avrGamePanel = panel;
        }
    }

    /**
     * Handles the OPEN_CHANNEL command.
     */
    public function handleOpenChannel (name :Name) :void
    {
        _wctx.getMsoyChatDirector().openChannel(name);
    }

    /**
     * Handles the REGISTER_IM command.
     */
    public function handleRegisterIM (gateway :String) :void
    {
        _topPanel.callLater(function () :void { new IMRegisterDialog(_wctx, gateway); });
    }

    /**
     * Handles the UNREGISTER_IM command;
     */
    public function handleUnregisterIM (gateway :String) :void
    {
        _wctx.getMsoyChatDirector().unregisterIM(gateway);
    }

    /**
     * Handles the POP_CHANNEL_MENU command.
     */
    public function handlePopChannelMenu (trigger :Button) :void
    {
        var menuData :Array = [];
        menuData.push({ label: Msgs.GENERAL.get("b.chatPrefs"), command: CHAT_PREFS });
        menuData.push({ label: Msgs.GENERAL.get("b.clearChat"),
            callback: _wctx.getChatDirector().clearDisplays });
        menuData.push({ type: "separator" });

        if (!(_wctx.getTopPanel().getPlaceView() is MsoyGamePanel)) {
            if (!Prefs.getSlidingChatHistory()) {
                menuData.push({ command: TOGGLE_CHAT_HIDE, label: Msgs.GENERAL.get(
                        Prefs.getShowingChatHistory() ? "b.hide_chat" : "b.show_chat") });
            }
            menuData.push({ command: TOGGLE_CHAT_SLIDE, label: Msgs.GENERAL.get(
                    Prefs.getSlidingChatHistory() ? "b.overlay_chat" : "b.slide_chat") });
            menuData.push({ command: TOGGLE_OCC_LIST, label: Msgs.GENERAL.get(
                    Prefs.getShowingOccupantList() ? "b.hide_occ_list" : "b.show_occ_list") });
            menuData.push({ type: "separator" });
        }

        var me :MemberObject = _wctx.getMemberObject();
        var scene :Scene = _wctx.getSceneDirector().getScene();
        if (scene == null) {
            // if scene is null, we're in a game, and need to add the friends to the chat menu.
            var friends :Array = new Array();
            for each (var fe :FriendEntry in me.getSortedEstablishedFriends()) {
                if (fe.online) {
                    var item :Object = {
                        label: fe.name.toString(), command: OPEN_CHANNEL, arg: fe.name }
                    checkChatChannelOpen(fe.name, item);
                    friends.push(item);
                }
            }
            if (friends.length == 0) {
                friends.push({ label: Msgs.GENERAL.get("m.no_friends"),
                            enabled: false });
            }
            menuData = menuData.concat(friends);
            menuData.push({ type: "separator" });
        }

        var groups :Array = (me.groups != null) ? me.groups.toArray() : [];
        groups = groups.map(function (gm :GroupMembership, index :int, array :Array) :Object {
            var item :Object = { label: gm.group.toString(), command: OPEN_CHANNEL, arg: gm.group };
            checkChatChannelOpen(gm.group, item);
            return item;
        });
        if (groups.length == 0) {
            groups.push({ label: Msgs.GENERAL.get("m.no_groups"),
                          enabled : false });
        } else if (groups.length > 4) {
            menuData.push({ label: Msgs.GENERAL.get("l.groups"), children: groups});
        } else {
            menuData = menuData.concat(groups);
        }

        var gateways :Array = me.getSortedGateways();
        if (gateways.length > 0) {
            menuData.push({ type: "separator"});
        }
        for each (var ge :GatewayEntry in gateways) {
            var subMenuData :Array = [];
            if (!ge.online) {
                subMenuData.push(
                    { label: Msgs.CHAT.get("m.im_login"), command: REGISTER_IM, arg: ge.gateway });
            } else {
                var contacts :Array = me.getSortedImContacts(ge.gateway);
                for each (var ce :ContactEntry in contacts) {
                    if (!ce.online) {
                        continue;
                    }
                    var aitem :Object = {
                        label: ce.name.toString(), command: OPEN_CHANNEL, arg: ce.name }
                    checkChatChannelOpen(ce.name, aitem);
                    subMenuData.push(aitem);
                }
                if (contacts.length == 0) {
                    subMenuData.push({ label: Msgs.CHAT.get("m.no_im_contacts"), enabled: false});
                }
                subMenuData.push({ type: "separator"});
                subMenuData.push({
                    label:Msgs.CHAT.get("m.im_logout"), command: UNREGISTER_IM, arg: ge.gateway });
            }
            menuData.push({ label: Msgs.CHAT.get("m." + ge.gateway), children: subMenuData});
        }

        var r :Rectangle = trigger.getBounds(trigger.stage);
        var menu :CommandMenu = CommandMenu.createMenu(menuData.reverse(), _topPanel);
        menu.variableRowHeight = true;
        menu.setBounds(_wctx.getTopPanel().getMainAreaBounds());
        menu.popUpAt(r.left, r.top, true);
    }

//    /**
//     * Handles the POP_ROOMS_MENU command.
//     */
//    public function handlePopRoomsMenu (trigger :Button) :void
//    {
//        var scene :Scene = _wctx.getSceneDirector().getScene();
//        var currentSceneId :int = (scene == null) ? -1 : scene.getId();
//        if (!(_wctx.getLocationDirector().getPlaceObject() is SceneObject)) {
//            currentSceneId = -1;
//        }
//
//        var memberObj :MemberObject = _wctx.getMemberObject();
//
//        var friends :Array = memberObj.getSortedEstablishedFriends();
//        friends = friends.map(function (fe :FriendEntry, index :int, array :Array) :Object {
//            return { label: fe.name.toString(), command: GO_MEMBER_HOME, arg: fe.getMemberId()
//            };
//        });
//
//        var recent :Array = memberObj.recentScenes.toArray();
//        recent.sort(function (sb1 :SceneBookmarkEntry, sb2 :SceneBookmarkEntry) :int {
//            return int(sb1.lastVisit - sb2.lastVisit);
//        });
//
//        var owned :Array = memberObj.ownedScenes.toArray();
//        // TODO: sort owned?
//
//        var bookmarkMapper :Function = function (
//            sb :SceneBookmarkEntry, index :int, array :Array) :Object {
//                return {
//                    label: sb.toString(),
//                    enabled: (sb.sceneId != currentSceneId),
//                    command: GO_SCENE,
//                    arg: sb.sceneId
//                };
//            };
//
//        recent = recent.map(bookmarkMapper);
//        owned = owned.map(bookmarkMapper);
//
//        var menuData :Array = [];
//
//        // add the friends if present
//        if (friends.length > 0) {
//            menuData.push({ label: Msgs.GENERAL.get("l.visit_friends"), children: friends });
//        }
//        // add owned scenes, if any
//        if (owned.length > 0) {
//            menuData.push({ label: Msgs.GENERAL.get("l.owned_scenes"), children: owned});
//        }
//        // always add recent scenes
//        menuData.push({ label: Msgs.GENERAL.get("l.recent_scenes"), children: recent });
//
//        if (!memberObj.isGuest()) {
//            menuData.push(
//                { type: "separator" },
//                { label: Msgs.GENERAL.get("l.go_home"),
//                  enabled: (memberObj.getHomeSceneId() != currentSceneId),
//                  command :GO_SCENE, arg: memberObj.getHomeSceneId()
//                });
//        }
//
//        CommandMenu.createMenu(menuData, _topPanel).popUp(trigger, true);
//    }

    /**
     * Handles the VIEW_COMMENTED_ITEM command.
     */
    public function handleViewCommentedItem (itemType :int, itemId :int) :void
    {
        // in this case we are looking for an item that we were told was commented, so we can
        // assume that it's listed in the shop
        displayPage("shop", "l_" + itemType + "_" + itemId);
    }

    /**
     * Handles the VIEW_ITEM command.
     */
    public function handleViewItem (ident :ItemIdent) :void
    {
        var isvc :ItemService = _wctx.getClient().requireService(ItemService) as ItemService;
        isvc.getCatalogId(_wctx.getClient(), ident, new ResultWrapper(
            function (cause :String) :void {
                _wctx.displayFeedback(MsoyCodes.GENERAL_MSGS, cause);
            },
            function (result :Object) :void {
                if (result == null) {
                    // it's an object we own, or it's not listed but we are support+
                    displayPage("stuff", "d_" + ident.type + "_" + ident.itemId);

                } else if (result == 0) {
                    _wctx.displayFeedback(MsoyCodes.ITEM_MSGS,
                        MessageBundle.compose("m.not_listed", Item.getTypeKey(ident.type)));

                } else {
                    displayPage("shop", "l_" + ident.type + "_" + result);
                }
            }));
    }

    /**
     * Handles the VIEW_MEMBER command.
     */
    public function handleViewMember (memberId :int) :void
    {
        displayPage("people", "" + memberId);
    }

    /**
     * Handles hte VISIT_MEMBER command.
     */
    public function handleVisitMember (memberId :int) :void
    {
        _wctx.getWorldDirector().goToMemberLocation(memberId);
    }

    /**
     * Handles the VIEW_GROUP command.
     */
    public function handleViewGroup (groupId :int) :void
    {
        displayPage("whirleds", "d_" + groupId);
    }

    /**
     * Handles the VIEW_ROOM command.
     */
    public function handleViewRoom (sceneId :int) :void
    {
        displayPage("room", "room_" + sceneId);
    }

    /**
     * Handles the VIEW_FULL_VERSION command, used in embedded clients.
     */
    public function handleViewFullVersion () :void
    {
        // log that the full version button was clicked
        _wctx.getMsoyClient().trackClientAction("flashFullVersionClicked", null);

        // then go to the appropriate place..
        const sceneId :int = getCurrentSceneId();
        if (sceneId != 0) {
            displayPage("world", "s" + sceneId);

        } else {
            const gameId :int = getCurrentGameId();
            if (gameId != 0) {
                displayPage("games", "d_" + gameId);

            } else {
                displayPage("", "");
            }
        }
    }

    /**
     * Handles the FEATURED_PLACE_CLICKED command.
     */
    public function handleFeaturedPlaceClicked () :void
    {
        if (_wctx.getMsoyClient().isEmbedded()) {
            handleViewFullVersion();
        } else {
            var sceneId :int = int(getSceneAndGame()[0]);
            if (sceneId == 0) {
                // TODO: before falling back to the initial scene, we should try
                // any pending scene...
                sceneId = int(MsoyParameters.get()["sceneId"]);
            }
            handleGoScene(sceneId);
        }
    }

    /**
     * Handles the VIEW_GAME command.
     */
    public function handleViewGame (gameId :int) :void
    {
        // when a player clicks a game in the whirled, we try to display that game's detail page,
        // but if we can't do that, then fall back to displaying the game lobby
        if (!inGWTApp() || !displayPage("games", "d_" + gameId)) {
            handleJoinGameLobby(gameId);
        }
    }

    /**
     * Handles the VIEW_GAMES command.
     */
    override public function handleViewGames () :void
    {
        displayPage("games", "");
    }

    /**
     * Handles the VIEW_GAME_COMMENTS command.
     */
    public function handleViewGameComments (gameId :int) :void
    {
        displayPage("games", "d_" + gameId + "_c");
    }

    /**
     * Handles the VIEW_GAME_INSTRUCTIONS command.
     */
    public function handleViewGameInstructions (gameId :int) :void
    {
        displayPage("games", "d_" + gameId + "_i");
    }

    /**
     * Handles the VIEW_MY_AVATARS command.
     */
    public function handleViewMyAvatars () :void
    {
        displayPage("stuff", ""+Item.AVATAR);
    }

    /**
     * Handles the VIEW_MY_FURNITURE command.
     */
    public function handleViewMyFurniture () :void
    {
        displayPage("stuff", "" + Item.FURNITURE);
    }

    /**
     * Handles the VIEW_AVATAR_CATALOG command.
     */
    public function handleViewAvatarCatalog () :void
    {
        displayPage("shop", ""+Item.AVATAR);
    }

    /**
     * Handles the VIEW_MAIL command.
     */
    public function handleViewMail () :void
    {
        displayPage("mail", "");
    }

    /**
     * Handles the SHOW_SIGN_UP command.
     */
    public function handleShowSignUp () :void
    {
        displayPage("account", "create");
    }

    /**
     * Handles the GO_SCENE command.
     */
    public function handleGoScene (sceneId :int) :void
    {
        if (!displayPageGWT("world", "s" + sceneId)) {
            // fall back to breaking the back button
            log.info("Can't go to scene via GWT. Going direct to " + sceneId + ".");
            _wctx.getSceneDirector().moveTo(sceneId);
        }
    }

    /**
     * Handles the GO_MEMBER_HOME command.
     */
    public function handleGoMemberHome (memberId :int, direct :Boolean = false) :void
    {
        _wctx.getWorldDirector().goToMemberHome(memberId);
    }

    /**
     * Handles the GO_MEMBER_LOCATION command.
     */
    public function handleGoMemberLocation (memberId :int) :void
    {
        // pass the buck to the world director
        _wctx.getWorldDirector().goToMemberLocation(memberId);
    }

    /**
     * Handles the JOIN_PLAYER_GAME command. (Generated by chat-based invites.)
     */
    public function handleJoinPlayerGame (gameId :int, playerId :int) :void
    {
        _wctx.getGameDirector().joinPlayer(gameId, playerId);
    }

    /**
     * Handles the JOIN_PLAYER_TABLE command.
     */
    public function handleJoinPlayerTable (memberId :int) :void
    {
        var msvc :MemberService =
            (_wctx.getClient().requireService(MemberService) as MemberService);
        msvc.getCurrentMemberLocation(_wctx.getClient(), memberId, new ResultWrapper(
            function (cause :String) :void {
                _wctx.displayFeedback(null, cause);
            },
            function (location :MemberLocation) :void {
                if (location.gameId == 0) {
                    _wctx.displayFeedback(MsoyCodes.GAME_MSGS, "e.no_longer_lobbying");
                } else if (location.sceneId == 0) {
                    // if the game already started, take them straight into it.
                    _wctx.getWorldDirector().goToMemberLocation(location.memberId, location);
                } else {
                    _wctx.getGameDirector().joinPlayerTable(location.gameId, location.memberId);
                }
            }));
        restoreSceneURL();
    }

    /**
     * Handles the GO_GROUP_HOME command.
     */
    public function handleGoGroupHome (groupId :int, direct :Boolean = false) :void
    {
        _wctx.getWorldDirector().goToGroupHome(groupId);
    }

    /**
     * Handles the GO_LOCATION command to go to a placeobject.
     */
    public function handleGoLocation (placeOid :int) :void
    {
        if (!displayPageGWT("world", "l" + placeOid)) {
            // fall back to breaking the back button
            _wctx.getLocationDirector().moveTo(placeOid);
        }
    }

    /**
     * Handles the GO_GAME command to go to a non-Flash game.
     */
    public function handleGoGame (gameId :int, placeOid :int) :void
    {
        // route our entry to the game through GWT so that we can handle non-Flash games
        if (!inGWTApp() || !displayPage("world", "game_g_" + gameId + "_" + placeOid)) {
            // fall back to breaking the back button
            log.info("Going straight into game [oid=" + placeOid + "].");
            _wctx.getGameDirector().enterGame(placeOid);
            // TODO: if this is a Java game and we're in embedded mode, try popping up a new
            // browser window
            // NetUtil.navigateToURL("/#game-" + gameId + "_" + placeOid, false);
        } else {
            log.info("Routed game ready through URL [oid=" + placeOid + "].");
        }
    }

    /**
     * Handles JOIN_GAME_LOBBY (and gameLobby=XX).
     */
    public function handleJoinGameLobby (gameId :int, ghost :String = null, gport :int = 0) :void
    {
//        // if we're not running in the GWT app, we need to display a page externally
//        if (!inGWTApp() && displayPage("world", "game_l_" + gameId)) {
//            return;
//        }

//         // if we're not in a scene, go to our home scene while we're displaying the lobby (but not
//         // if we're in the standalone client because it's just pointless slowdown)
//         if (Capabilities.playerType != "StandAlone") {
//             if (_wctx.getSceneDirector().getScene() == null) {
//                 _wctx.getSceneDirector().moveTo(_wctx.getMemberObject().getHomeSceneId());
//             }
//         }

        // now display the lobby interface
        _wctx.getGameDirector().displayLobby(gameId, ghost, gport);
    }

    /**
     * Handles JOIN_AVR_GAME.
     */
    public function handleJoinAVRGame (gameId :int) :void
    {
        _wctx.getGameDirector().activateAVRGame(gameId);
    }

    /**
     * Handles LEAVE_AVR_GAME.
     */
    public function handleLeaveAVRGame () :void
    {
        _wctx.getGameDirector().leaveAVRGame();
    }

    /**
     * Handles INVITE_FRIEND.
     */
    public function handleInviteFriend (memberId :int) :void
    {
        _wctx.getMemberDirector().inviteToBeFriend(memberId);
    }

    /**
     * Handles RESPOND_FOLLOW.
     */
    public function handleRespondFollow (memberId :int) :void
    {
        var msvc :MemberService = _wctx.getClient().requireService(MemberService) as MemberService;
        msvc.followMember(_wctx.getClient(), memberId,
                          new ReportingListener(_wctx, MsoyCodes.GENERAL_MSGS, null,
                                                "m.following"));
    }

    /**
     * Handle the ROOM_EDIT command.
     */
    public function handleRoomEdit () :void
    {
        (_topPanel.getPlaceView() as RoomObjectView).getRoomObjectController().handleRoomEdit();
    }

    /**
     * Handles the CREATE_ACCOUNT command (generated by the InviteOverlay).
     */
    public function handleCreateAccount (invite :String = null) :void
    {
        displayPage("account", (invite == null) ? "create" : ("create_" + invite));
    }

    /**
     * Handles the COMPLAIN_MEMBER command.
     */
    public function handleComplainMember (memberId :int, username :String) :void
    {
        _topPanel.callLater(function () :void { new ComplainDialog(_wctx, memberId, username); });
    }

    /**
     * Handles booting a user.
     */
    public function handleBootFromPlace (memberId :int) :void
    {
        var svc :MemberService = _wctx.getClient().requireService(MemberService) as MemberService;
        svc.bootFromPlace(_wctx.getClient(), memberId,
            new ReportingListener(_wctx, MsoyCodes.GENERAL_MSGS));
    }

    /**
     * Handles the TOGGLE_HEIGHT command.
     */
    public function handleToggleHeight () :void
    {
        if (inGWTApp()) {
            try {
                if (ExternalInterface.available) {
                    ExternalInterface.call("toggleClientHeight");
                    return;
                }
            } catch (e :Error) {
                log.warning("Unable to handleToggleHeight via Javascript: " + e);
            }
        } else {
            log.warning("Can't access GWT to handleToggleHeight");
        }
    }

    /**
     * Called by the scene director when we've traveled to a new scene.
     */
    public function wentToScene (sceneId :int) :void
    {
        if (UberClient.isFeaturedPlaceView()) {
            return;
        }
        // this will result in another request to move to the scene we're already in, but we'll
        // ignore it because we're already there
        if (_oldCrustyShit) {
            displayPageGWT("world", "s" + sceneId);
        }
        _oldCrustyShit = true;
    }

    /**
     * Convienience function to restore our GWT page URL for the current scene.
     */
    public function restoreSceneURL () :void
    {
        const sceneId :int = getCurrentSceneId();
        if (sceneId != 0) {
            displayPageGWT("world", "s" + sceneId);
        }
    }

    /**
     * If we're joining a game lobby and have not yet logged onto the world server, we start the
     * game lobby connection process immediately instead of waiting until we're connected to the
     * world server. This short-circuits the normal logon-go process.
     */
    public function preLogonGo (params :Object) :void
    {
        _didFirstLogonGo = true;
        goToPlace(params);
    }

    /**
     * Updates our availability state.
     */
    public function updateAvailability (availability :int) :void
    {
        var msvc :MemberService = _wctx.getClient().requireService(MemberService) as MemberService;
        msvc.updateAvailability(_wctx.getClient(), availability);
        _wctx.displayFeedback(MsoyCodes.GENERAL_MSGS, "m.avail_tip_" + availability);
    }

    /**
     * Figure out where we should be going, and go there.
     */
    public function goToPlace (params :Object) :void
    {
        // first, see if we should hit a specific scene
        if (null != params["memberHome"]) {
            _oldCrustyShit = false;
            handleGoMemberHome(int(params["memberHome"]), true);

        } else if (null != params["groupHome"]) {
            _oldCrustyShit = false;
            handleGoGroupHome(int(params["groupHome"]), true);

        } else if (null != params["memberScene"]) {
            _oldCrustyShit = false;
            handleGoMemberLocation(int(params["memberScene"]));

        } else if (null != params["playerTable"]) {
            _oldCrustyShit = false;
            handleJoinPlayerTable(int(params["playerTable"]));

        } else if (null != params["gameLocation"]) {
            _oldCrustyShit = false;
            _wctx.getGameDirector().enterGame(int(params["gameLocation"]));

        } else if (null != params["noplace"]) {
            // go to no place- we just want to chat with our friends
            _wctx.getTopPanel().setPlaceView(new NoPlaceView());

        } else if (null != params["gameLobby"]) {
            handleJoinGameLobby(
                int(params["gameLobby"]), String(params["ghost"]), int(params["gport"]));

        } else if (null != params["playNow"]) {
            _wctx.getGameDirector().playNow(int(params["playNow"]), params["gameMode"] as String,
                                            String(params["ghost"]), int(params["gport"]));

        } else if (null != params["worldGame"]) {
            handleJoinAVRGame(int(params["worldGame"]));

        } else if (null != params["groupChat"]) {
            var groupId :int = int(params["groupChat"]);
            var gm :GroupMembership =
                _wctx.getMemberObject().groups.get(groupId) as GroupMembership;
            if (gm != null) {
                handleOpenChannel(gm.group);
            }

            // fix the URL
            if (_wctx.getGameDirector().getGameConfig() != null) {
                // For now, leave the URL alone if we're in a game.
            } else if (_wctx.getSceneDirector().getScene() != null) {
                displayPageGWT("world", "s" + _wctx.getSceneDirector().getScene().getId());
            } else {
                displayPageGWT("world", "m" + _wctx.getMemberObject().getMemberId());
            }

        } else if (null != params["sceneId"]) {
            var sceneId :int = int(params["sceneId"]);
            if (sceneId == 0) {
                sceneId = _wctx.getMemberObject().getHomeSceneId();
            }
            _wctx.getSceneDirector().moveTo(sceneId);

            // if we have a redirect page we need to show, do that (we do this by hand to avoid
            // potential infinite loops if something goes awry with opening external pages)
            try {
                var redirect :String = params["page"];
                if (redirect != null && ExternalInterface.available) {
                    ExternalInterface.call("displayPage", redirect, "");
                }
            } catch (error :Error) {
                // nothing we can do here...
            }

        } else if (!_wctx.getMemberObject().isGuest()) {
            _wctx.getWorldDirector().goToMemberHome(_wctx.getMemberObject().getMemberId());

        } else {
            // this only happens in the standalone client when we have no credentials
            _wctx.getSceneDirector().moveTo(1);
        }
    }

    // from MsoyController
    override public function showExternalURL (url :String, top :Boolean = false) :Boolean
    {
        // if our page refers to a Whirled page...
        var gwtPrefix :String = DeploymentConfig.serverURL + "#";
        var gwtUrl :String;
        if (url.indexOf(gwtPrefix) == 0) {
            gwtUrl = url.substring(gwtPrefix.length);
        } else if (url.indexOf("#") == 0) {
            gwtUrl = url.substring(1);
        } else {
            return super.showExternalURL(url, top);
        }

        // ...extract the page and arguments and tell GWT to display them properly
        var didx :int = gwtUrl.indexOf("-");
        if (didx == -1) {
            return super.showExternalURL(url, top);
        } else {
            return displayPage(gwtUrl.substring(0, didx), gwtUrl.substring(didx+1));
        }
    }

    // from MsoyController
    override public function getSceneAndGame () :Array
    {
        return [ getCurrentSceneId(), getCurrentGameId() ];
    }

    // from MsoyController
    override public function addMemberMenuItems (member :MemberName, menuItems :Array) :void
    {
        const memId :int = member.getMemberId();
        const us :MemberObject = _wctx.getMemberObject();
        const isUs :Boolean = (memId == us.getMemberId());

        // if we're not a guest, populate availability menu.
        if (isUs && !MemberName.isGuest(memId)) {
            var availActions :Array = [];
            for (var ii :int = MemberObject.AVAILABLE; ii <= MemberObject.UNAVAILABLE; ii++) {
                availActions.push({
                    label: Msgs.GENERAL.get("l.avail_" + ii), callback: updateAvailability, arg: ii,
                    enabled: (ii != us.availability) });
            }
            menuItems.push({ label: Msgs.GENERAL.get("l.avail_menu"), children: availActions });

        } else if (!isUs) {
            menuItems.push({ label: Msgs.GENERAL.get("b.open_channel"),
                             command: OPEN_CHANNEL, arg: member });
            if (!MemberName.isGuest(memId)) {
                if (!_wctx.getMsoyClient().isEmbedded()) {
                    menuItems.push({ label: Msgs.GENERAL.get("b.view_member"),
                                     command: VIEW_MEMBER, arg: memId });
                }
                if (!us.isGuest() && !us.friends.containsKey(memId)) {
                    menuItems.push({ label: Msgs.GENERAL.get("l.add_as_friend"),
                                     command: INVITE_FRIEND, arg: [memId] });
                }
            }
            menuItems.push({ label: Msgs.GENERAL.get("b.complain"),
                             command: COMPLAIN_MEMBER, arg: [memId, member] });

            // possibly add a menu item for booting this user
            var placeCtrl :Object = _wctx.getLocationDirector().getPlaceController();
            if (placeCtrl == null) {
                // check the gamecontext's place
                placeCtrl = _wctx.getGameDirector().getGameController();
            }
            if ((placeCtrl is BootablePlaceController) &&
                    BootablePlaceController(placeCtrl).canBoot()) {
                menuItems.push({ label: Msgs.GENERAL.get("b.boot"),
                    callback: handleBootFromPlace, arg: memId });
            }
        }
    }

    // from MsoyController
    override public function addFriendMenuItems (member :MemberName, menuItems :Array) :void
    {
        var memId :int = member.getMemberId();
        menuItems.push({ label: Msgs.GENERAL.get("b.open_channel"),
                         command: OPEN_CHANNEL, arg: member });
        menuItems.push({ label: Msgs.GENERAL.get("b.view_member"),
                         command: VIEW_MEMBER, arg: memId });
        menuItems.push({ label: Msgs.GENERAL.get("b.visit_member"),
                         command: VISIT_MEMBER, arg: memId });
    }

    override public function addPartymateMenuItems (member :MemberName, menuItems :Array) :void
    {
        // TODO: i18n
        menuItems.push({ label: "Boot", command: BOOT_PARTYMATE, arg: member.getMemberId() });
        menuItems.push({ label: "Promote", command: PROMOTE_PARTYMATE, arg: member.getMemberId() });

        addFriendMenuItems(member, menuItems);
    }

    public function handleBootPartymate (memberId :int) :void
    {
    }

    public function handlePromotePartymate (memberId :int) :void
    {
    }

    // from MsoyController
    override public function handleClosePlaceView () :void
    {
        if (_wctx.getTopPanel().getPlaceView() is MsoyGamePanel) {
            // if we're in a game, closing means closing the game and going back to our place
            handleMoveBack(true);
        } else {
            // if we're in the whirled, closing means closing the flash client totally
            _wctx.getMsoyClient().closeClient();
        }
    }

    // from MsoyController
    override public function handleMoveBack (closeInsteadOfHome :Boolean = false) :void
    {
        // go to the first recent scene that's not the one we're in
        const curSceneId :int = getCurrentSceneId();
        for each (var entry :Object in _recentScenes) {
            if (entry.id != curSceneId) {
                handleGoScene(entry.id);
                return;
            }
        }

        // there are no recent scenes, so either close the client or go home
        if (closeInsteadOfHome && inGWTApp()) {
            _wctx.getWorldClient().closeClient();
        } else {
            handleGoScene(_wctx.getMemberObject().getHomeSceneId());
        }
    }

    // from MsoyController
    override public function canMoveBack () :Boolean
    {
        // you can only NOT move back if you are in your home room and there are no
        // other scenes in your history
        const curSceneId :int = getCurrentSceneId();
        if (_wctx.getMemberObject().getHomeSceneId() != curSceneId) {
            return true;
        }
        for each (var entry :Object in _recentScenes) {
            if (entry.id != curSceneId) {
                return true;
            }
        }
        return false;
    }

    // from MsoyController
    override public function handleLogon (creds :Credentials) :void
    {
        // if we're currently logged on, save our current scene so that we can go back there once
        // we're relogged on as a non-guest; otherwise go to Brave New Whirled
        const currentSceneId :int = getCurrentSceneId();
        _postLogonScene = (currentSceneId == 0) ? 1 : currentSceneId;
        _wctx.getClient().logoff(false);

        super.handleLogon(creds);
    }

    // from ClientObserver
    override public function clientDidLogon (event :ClientEvent) :void
    {
        super.clientDidLogon(event);

        var memberObj :MemberObject = _wctx.getMemberObject();
        // if not a guest, save the username that we logged in with
        if (!memberObj.isGuest()) {
            var name :Name = _wctx.getClient().getCredentials().getUsername();
            if (name != null) {
                Prefs.setUsername(name.toString());
            }
            _wctx.getTopPanel().getHeaderBar().getChatTabs().memberObjectUpdated(memberObj);

        } else {
            // if we are a guest, let the GWT application know the guest id as whom we're
            // authenticated so that it can pass that guest id along to the server if we register
            // and the server can transfer any flow we earn as this guest to our new account
            _wctx.getMsoyClient().gotGuestIdToGWT(memberObj.getMemberId());
        }

        if (!_didFirstLogonGo) {
            _didFirstLogonGo = true;
            goToPlace(MsoyParameters.get());
        } else if (_postLogonScene != 0) {
            // we gotta go somewhere
            _wctx.getSceneDirector().moveTo(_postLogonScene);
            _postLogonScene = 0;
        }

        _wctx.getGameDirector().checkMemberAVRGame();
    }

    // from ClientObserver
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);
        setAVRGamePanel(null);
    }

    /**
     * Indicate on the menu item whether or not we have the specified chat channel open
     * or not.
     */
    protected function checkChatChannelOpen (name :Name, menuItem :Object) :void
    {
        menuItem["enabled"] = !_wctx.getMsoyChatDirector().hasOpenChannel(name);
    }

    /**
     * Calls our GWT application and requests that the specified page be displayed.
     */
    protected function displayPageGWT (page :String, args :String) :Boolean
    {
        if (inGWTApp()) {
            try {
                if (ExternalInterface.available) {
                    ExternalInterface.call("displayPage", page, args);
                    return true;
                }
            } catch (e :Error) {
                log.warning("Unable to display page via Javascript [page=" + page +
                            ", args=" + args +"]: " + e);
            }
        }
        return false;
    }

    /**
     * Displays a new page either in our GWT application or by reloading the current web page with
     * the full GWT application, restoring our current location and then displaying the page.
     */
    protected function displayPage (page :String, args :String) :Boolean
    {
        if (inGWTApp()) {
            return displayPageGWT(page, args);

        } else {
            var fullURL :String = DeploymentConfig.serverURL;
            // if we have no current scene, or we're asking to display our current scene (because
            // we're in embed mode and are trying to get out) don't do the scene wrap business
            const sceneId :int = getCurrentSceneId();
            if (sceneId == 0 || (page == "world" && args == ("s"+sceneId))) {
                if (!StringUtil.isBlank(page)) {
                    fullURL += "#" + page + "-" + args;
                }
            } else {
                fullURL += "#world-s" + sceneId + "_" + page + "-" + args;
            }
            log.info("Showing external URL " + fullURL);
            return super.showExternalURL(fullURL, false);
        }
    }

    // from MsoyController
    override protected function updateTopPanel (headerBar :HeaderBar, controlBar :ControlBar) :void
    {
        super.updateTopPanel(headerBar, controlBar);

        // TODO: The way I think we should consider doing this is have PlaceView's dispatch
        // some sort of NewPlaceEvent when they're showing and have downloaded whatever data
        // needed, and then various components up the hierarchy can react to this event.

        // if we moved to a scene, set things up thusly
        var scene :Scene = _wctx.getSceneDirector().getScene();
        if (scene != null) {
            _wctx.getMsoyClient().setWindowTitle(scene.getName());
            headerBar.setLocationName(scene.getName());
            var roomChannel :ChatChannel =
                ChatChannel.makeRoomChannel(new RoomName(scene.getName(), scene.getId()));
            headerBar.getChatTabs().clearUncheckedRooms([roomChannel.toLocalType()]);

            // subscribe to the new scene's channel, if we haven't already
            var roomName :RoomName = new RoomName(scene.getName(), scene.getId());
            _wctx.getMsoyChatDirector().openChannel(roomName, true);

            // update the owner link
            var model :MsoySceneModel = scene.getSceneModel() as MsoySceneModel;
            if (model != null) {
                var svc :MemberService =
                    _wctx.getClient().requireService(MemberService) as MemberService;
                if (model.ownerType == MsoySceneModel.OWNER_TYPE_MEMBER) {
                    svc.getDisplayName(_wctx.getClient(), model.ownerId, new ResultWrapper(
                        function (cause :String) :void {
                            log.debug("failed to retrieve member owner name: " + cause);
                            headerBar.setOwnerLink("");
                        },
                        function (res :Object) :void {
                            headerBar.setOwnerLink(res as String, handleViewMember, model.ownerId);
                        }));
                } else if (model.ownerType == MsoySceneModel.OWNER_TYPE_GROUP) {
                    svc.getGroupName(_wctx.getClient(), model.ownerId, new ResultWrapper(
                        function (cause :String) :void {
                            log.debug("failed to retrieve group owner name: " + cause);
                            headerBar.setOwnerLink("");
                        },
                        function (res :Object) :void {
                            headerBar.setOwnerLink(res as String, handleViewGroup, model.ownerId);
                        }));

                } else {
                    headerBar.setOwnerLink("");
                }
                if (!_wctx.getMsoyClient().isEmbedded()) {
                    headerBar.setCommentLink(handleViewRoom, model.sceneId);
                }
                headerBar.setInstructionsLink(null);
            }

            addRecentScene(scene);
            return;
        }

        // if we're in a game, display the game name and activate the back button
        var cfg :MsoyGameConfig = _wctx.getGameDirector().getGameConfig();
        if (cfg != null) {
            _wctx.getMsoyClient().setWindowTitle(cfg.name);
            headerBar.setLocationName(cfg.name);
            headerBar.setOwnerLink("");
            headerBar.setCommentLink(handleViewGameComments, cfg.getGameId());
            headerBar.setInstructionsLink(handleViewGameInstructions, cfg.getGameId());
        }
    }

    /**
     * Returns the current sceneId, or 0 if none.
     */
    protected function getCurrentSceneId () :int
    {
        const scene :Scene = _wctx.getSceneDirector().getScene();
        return (scene == null) ? 0 : scene.getId();
    }

    /**
     * Returns the game id, or 0 if none.
     */
    protected function getCurrentGameId () :int
    {
        const cfg :MsoyGameConfig = _wctx.getGameDirector().getGameConfig();
        return (cfg == null) ? 0 : cfg.getGameId();
    }

    protected function addRecentScene (scene :Scene) :void
    {
        const id :int = scene.getId();

        // first, see if it's already in the list of recent scenes, and remove it if so
        for (var ii :int = _recentScenes.length - 1; ii >= 0; ii--) {
            if (_recentScenes[ii].id == id) {
                _recentScenes.splice(ii, 1);
                break;
            }
        }

        // now add it to the beginning of the list
        _recentScenes.unshift({ name: scene.getName(), id: id });

        // and make sure we're not tracking too many
        _recentScenes.length = Math.min(_recentScenes.length, MAX_RECENT_SCENES);
    }

    override protected function populateGoMenu (menuData :Array) :void
    {
        super.populateGoMenu(menuData);

        const curSceneId :int = getCurrentSceneId();
        var sceneSubmenu :Array = [];
        for each (var entry :Object in _recentScenes) {
            sceneSubmenu.push({ label: StringUtil.truncate(entry.name, 50, "..."),
                command: GO_SCENE, arg: entry.id, enabled: (entry.id != curSceneId) });
        }
        if (sceneSubmenu.length == 0) {
            sceneSubmenu.push({ label: Msgs.GENERAL.get("m.none"), enabled: false });
        }
        menuData.push({ label: Msgs.WORLD.get("l.recent_scenes"), children: sceneSubmenu });
    }

    /** Giver of life, context. */
    protected var _wctx :WorldContext;

    /** Tracks whether we've done our first-logon movement so that we avoid trying to redo it as we
     * subsequently move between servers (and log off and on in the process). */
    protected var _didFirstLogonGo :Boolean;

    /** A scene to which to go after we logon. */
    protected var _postLogonScene :int;

    // TODO: remove, but it seems to be doing something
    protected var _oldCrustyShit :Boolean;

    /** The current AVRG display, if any. */
    protected var _avrGamePanel :AVRGamePanel;

    /** Recently visited scenes, ordered from most-recent to least-recent */
    protected var _recentScenes :Array = [];

    /** The maximum number of recent scenes we track. */
    protected static const MAX_RECENT_SCENES :int = 11; 

    private static const log :Log = Log.getLog(WorldController);
}
}
