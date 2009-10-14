//
// $Id$

package com.threerings.msoy.world.server;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.jdbc.RepositoryUnit;
import com.samskivert.jdbc.WriteOnlyUnit;
import com.samskivert.util.Invoker;
import com.samskivert.util.ObjectUtil;
import com.samskivert.util.ResultListener;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.util.PersistingUnit;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.BodyManager;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;
import com.threerings.msoy.data.MemberExperience;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.MsoyCodes;
import com.threerings.msoy.data.all.MemberName;

import com.threerings.msoy.money.data.all.Currency;
import com.threerings.msoy.notify.server.NotificationManager;
import com.threerings.msoy.room.data.EntityMemories;
import com.threerings.msoy.room.data.MemberInfo;
import com.threerings.msoy.room.data.MsoySceneModel;
import com.threerings.msoy.room.data.RoomObject;
import com.threerings.msoy.room.server.MsoySceneRegistry;
import com.threerings.msoy.room.server.persist.MemoriesRecord;
import com.threerings.msoy.room.server.persist.MemoryRepository;
import com.threerings.msoy.room.server.persist.MsoySceneRepository;
import com.threerings.msoy.room.server.persist.SceneRecord;

import com.threerings.msoy.server.MemberLocal;
import com.threerings.msoy.server.MemberLocator;
import com.threerings.msoy.server.MemberLogic;
import com.threerings.msoy.server.MemberNodeActions;
import com.threerings.msoy.server.ServerConfig;
import com.threerings.msoy.server.persist.MemberRepository;
import com.threerings.msoy.world.client.WorldService.HomeResultListener;

import com.threerings.msoy.group.server.ThemeLogic;
import com.threerings.msoy.group.server.persist.GroupRepository;
import com.threerings.msoy.group.server.persist.ThemeRepository;
import com.threerings.msoy.item.data.ItemCodes;
import com.threerings.msoy.item.data.all.Avatar;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.ItemIdent;
import com.threerings.msoy.item.server.ItemLogic;
import com.threerings.msoy.item.server.ItemManager;
import com.threerings.msoy.item.server.persist.AvatarRecord;
import com.threerings.msoy.item.server.persist.AvatarRepository;
import com.threerings.msoy.item.server.persist.CatalogRecord;
import static com.threerings.msoy.Log.log;

/**
 * Handles various global world services.
 */
@Singleton @EventThread
public class WorldManager
    implements WorldProvider
{
    /** A generic feedback interface for setting a player's avatar. */
    public interface SetAvatarListener
    {
        void success();
        void noSuchItemFailure() throws Exception;
        void accessDeniedFailure() throws Exception;
    }

    @Inject public WorldManager (InvocationManager invmgr)
    {
        // register our bootstrap invocation service
        invmgr.registerDispatcher(new WorldDispatcher(this), MsoyCodes.WORLD_GROUP);
    }

    @Override // from interface WorldProvider
    public void getHomePageGridItems (
        ClientObject caller, InvocationService.ResultListener listener)
        throws InvocationException
    {
        MemberObject memObj = (MemberObject) caller;
        final MemberExperience[] experiences = new MemberExperience[memObj.experiences.size()];
        memObj.experiences.toArray(experiences);
        final boolean onTour = memObj.onTour;
        final int memberId = memObj.getMemberId();
        final short badgesVersion = memObj.getLocal(MemberLocal.class).badgesVersion;

        _invoker.postUnit(new PersistingUnit("getHPGridItems", listener, "who", memObj.who()) {
            @Override public void invokePersistent () throws Exception {
                _result = _memberLogic.getHomePageGridItems(
                    memberId, experiences, onTour, badgesVersion);
            }

            @Override public void handleSuccess () {
                reportRequestProcessed(_result);
            }

            protected Object _result;
        });
    }

    @Override // from interface WorldProvider
    public void getHomeId (ClientObject caller, final byte ownerType, final int ownerId,
                           HomeResultListener listener)
        throws InvocationException
    {
        final MemberObject memobj = (MemberObject)caller;
        final boolean tofu = memobj.avatar == null || memobj.avatar.itemId == 0;

        _invoker.postUnit(new PersistingUnit("getHomeId", listener) {
            @Override public void invokePersistent () throws Exception {
                if (tofu) {
                    List<Avatar> gifts = getStartupGiftAvatars(memobj.memberName.getMemberId());
                    if (gifts != null && !gifts.isEmpty()) {
                        _gifts = gifts.toArray(new Avatar[gifts.size()]);
                    }
                }
                _homeId = _memberLogic.getHomeId(ownerType, ownerId);
            }
            @Override public void handleSuccess () {
                if (_gifts != null) {
                    ((HomeResultListener)_listener).selectGift(_gifts, _homeId);
                } else if (_homeId != null) {
                    ((HomeResultListener)_listener).readyToEnter(_homeId);
                } else {
                    handleFailure(new InvocationException("m.no_such_user"));
                }
            }
            protected Integer _homeId;
            protected Avatar[] _gifts;
        });
    }

    @Override
    public void acceptAndProceed (
        ClientObject caller, final int giftCatalogId, ConfirmListener listener)
        throws InvocationException
    {
        _invoker.postUnit(new GiftUnit((MemberObject)caller, giftCatalogId, listener));
    }

    @Override // from interface WorldProvider
    public void setHomeSceneId (final ClientObject caller, final int ownerType, final int ownerId,
                                final int sceneId, final ConfirmListener listener)
        throws InvocationException
    {
        final MemberObject member = (MemberObject) caller;
        _invoker.postUnit(new PersistingUnit("setHomeSceneId", listener, "who", member.who()) {
            @Override public void invokePersistent () throws Exception {
                final int memberId = member.getMemberId();
                final SceneRecord scene = _sceneRepo.loadScene(sceneId);
                if (scene.ownerType == MsoySceneModel.OWNER_TYPE_MEMBER) {
                    if (scene.ownerId == memberId) {
                        _memberRepo.setHomeSceneId(memberId, sceneId);
                    } else {
                        throw new InvocationException("e.not_room_owner");
                    }
                } else if (scene.ownerType == MsoySceneModel.OWNER_TYPE_GROUP) {
                    if (member.isGroupManager(scene.ownerId)) {
                        _groupRepo.setHomeSceneId(scene.ownerId, sceneId);
                    } else {
                        throw new InvocationException("e.not_room_manager");
                    }
                } else {
                    log.warning("Unknown scene model owner type [sceneId=" +
                        scene.sceneId + ", ownerType=" + scene.ownerType + "]");
                    throw new InvocationException(InvocationCodes.INTERNAL_ERROR);
                }
            }
            @Override public void handleSuccess () {
                if (ownerType == MsoySceneModel.OWNER_TYPE_MEMBER) {
                    member.setHomeSceneId(sceneId);
                }
                super.handleSuccess();
            }
        });
    }

    @Override // from interface WorldProvider
    public void inviteToFollow (final ClientObject caller, final int memberId,
                                final InvocationService.InvocationListener listener)
        throws InvocationException
    {
        final MemberObject user = (MemberObject) caller;

        // make sure the target member is online and in the same room as the requester
        final MemberObject target = _locator.lookupMember(memberId);
        if (target == null || !ObjectUtil.equals(user.location, target.location)) {
            throw new InvocationException("e.follow_not_in_room");

        } else if (target.isAway()) {
            throw new InvocationException("e.follow_not_available");
        }

        // issue the follow invitation to the target
        _notifyMan.notifyFollowInvite(target, user.memberName);
    }

    @Override // from interface WorldProvider
    public void followMember (final ClientObject caller, final int memberId,
                              final InvocationService.InvocationListener listener)
        throws InvocationException
    {
        final MemberObject user = (MemberObject) caller;

        // if the caller is requesting to clear their follow, do so
        if (memberId == 0) {
            if (user.following != null) {
                MemberNodeActions.removeFollower(user.following.getMemberId(), user.getMemberId());
                user.setFollowing(null);
            }
            return;
        }

        // Make sure the target isn't bogus
        final MemberObject target = _locator.lookupMember(memberId);
        if (target == null) {
            throw new InvocationException("e.follow_invite_expired");
        }

        // Wire up both the leader and follower
        if (!target.followers.containsKey(user.getMemberId())) {
            log.debug("Adding follower", "follower", user.who(), "target", target.who());
            target.addToFollowers(user.memberName);
        }
        user.setFollowing(target.memberName);
    }

    @Override // from interface WorldProvider
    public void ditchFollower (ClientObject caller, int followerId,
                               InvocationService.InvocationListener listener)
        throws InvocationException
    {
        MemberObject leader = (MemberObject) caller;

        if (followerId == 0) { // Clear all followers
            for (MemberName follower : leader.followers) {
                MemberObject fmo = _locator.lookupMember(follower.getMemberId());
                if (fmo != null) {
                    fmo.setFollowing(null);
                }
            }
            leader.setFollowers(new DSet<MemberName>());

        } else { // Ditch a single follower
            if (leader.followers.containsKey(followerId)) {
                leader.removeFromFollowers(followerId);
            }
            MemberObject follower = _locator.lookupMember(followerId);
            if (follower != null && follower.following != null &&
                follower.following.getMemberId() == leader.getMemberId()) {
                follower.setFollowing(null);
            }
        }
    }

    @Override // from interface WorldProvider
    public void setAvatar (ClientObject caller, int avatarItemId, final ConfirmListener listener)
        throws InvocationException
    {
        setAvatar((MemberObject) caller, avatarItemId, new SetAvatarListener() {
            public void success () {
                listener.requestProcessed();
            }
            public void accessDeniedFailure () throws Exception {
                throw new InvocationException(ItemCodes.E_ACCESS_DENIED);
            }
            public void noSuchItemFailure () throws Exception {
                throw new InvocationException(ItemCodes.E_NO_SUCH_ITEM);
            }
        });
    }

    /**
     * Cause the given player to wear the given avatar, reporting the results on the
     * supplied {@link SetAvatarListener} object. An avatarId of zero may be passed to
     * revert to the default avatar.
     */
    public void setAvatar (final MemberObject user, final int avatarItemId,
        final SetAvatarListener listener)
    {
        if (avatarItemId == ((user.avatar == null) ? 0 : user.avatar.itemId)) {
            listener.success();
            return;
        }
        if (avatarItemId == 0) {
            // a request to return to the default avatar
            finishSetAvatar(user, null, null, listener);
            return;
        }

        // otherwise, make sure it exists and we own it
        final ItemIdent ident = new ItemIdent(Item.AVATAR, avatarItemId);
        _invoker.postUnit(new RepositoryUnit("setAvatar(" + avatarItemId + ")") {
            @Override public void invokePersist () throws Exception {
                _avatar = (Avatar)_itemLogic.loadItem(ident);
                if (_avatar == null) {
                    listener.noSuchItemFailure();
                    return;
                }
                if (user.getMemberId() != _avatar.ownerId) { // ensure that they own it
                    log.warning("Not user's avatar", "user", user.which(),
                                "ownerId", _avatar.ownerId, "avatar.itemId", _avatar.itemId);
                    listener.accessDeniedFailure();
                    return;
                }
                MemoriesRecord memrec = _memoryRepo.loadMemory(_avatar.getType(), _avatar.itemId);
                _memories = (memrec == null) ? null : memrec.toEntry();
            }

            @Override public void handleSuccess () {
                if (_avatar.equals(user.avatar)) {
                    listener.success();
                    return;
                }
                finishSetAvatar(user, _avatar, _memories, listener);
            }

            protected EntityMemories _memories;
            protected Avatar _avatar;
        });
    }

    /**
     * Blocks and gets a list of gift avatars for a given member and the server's configured
     * "startup" pseudo-theme. Returns null if it is not appropriate for the member to receive a
     * gift or the server does not have a startup theme.
     */
    protected List<Avatar> getStartupGiftAvatars (int memberId)
    {
        int themeId = ServerConfig.config.getValue("startup_group_id", 0);
        if (themeId == 0) {
            return null;
        }
        return getGiftAvatars(memberId, themeId);
    }

    /**
     * Blocks and gets a list of gift avatars for a given member and theme. Returns null if it is
     * not appropriate for the member to receive a gift.
     */
    protected List<Avatar> getGiftAvatars (int memberId, int themeId)
    {
        AvatarRepository repo = _itemLogic.getAvatarRepository();

        // check for the presence of a gifted avatar in their inventoty
        // TODO: do we need a flag for this instead in case the gift avatars change?
        List<AvatarRecord> avatars = repo.findItems(memberId, null, themeId);
        if (!avatars.isEmpty()) {
            // they've had a gift and have since changed avatars, just let 'em go
            return null;
        }

        // they have not recieved their gift, instruct the client to show picker
         List<Avatar> lineup = _themeLogic.loadLineup(themeId);
         return lineup.isEmpty() ? null : lineup;
    }

    /**
     * Updates the runtime information for an avatar change then finally commits the change to the
     * database.
     */
    protected void finishSetAvatar (
        final MemberObject user, final Avatar avatar, EntityMemories memories,
        final SetAvatarListener listener)
    {
        final Avatar prev = user.avatar;

        // now we need to make sure that the two avatars have a reasonable touched time
        user.startTransaction();
        try {
            // unset the current avatar to avoid busy-work in avatarUpdatedOnPeer, but
            // we'll set the new avatar at the bottom...
            user.avatar = null;

            // NOTE: we are not updating the used/location fields of the cached avatars,
            // I don't think it's necessary, but it'd be a simple matter here...
            final long now = System.currentTimeMillis();
            if (prev != null) {
                prev.lastTouched = now;
                _itemMan.avatarUpdatedOnPeer(user, prev);
            }
            if (avatar != null) {
                avatar.lastTouched = now + 1; // the new one should be more recently touched
                _itemMan.avatarUpdatedOnPeer(user, avatar);
            }

            // now set the new avatar
            user.setAvatar(avatar);
            user.actorState = null; // clear out their state
            user.getLocal(MemberLocal.class).memories = memories;

            // check if this player is already in a room (should be the case)
            if (memories != null) {
                PlaceManager pmgr = _placeReg.getPlaceManager(user.getPlaceOid());
                if (pmgr != null) {
                    PlaceObject plobj = pmgr.getPlaceObject();
                    if (plobj instanceof RoomObject) {
                        // if so, make absolutely sure the avatar memories are in place in the
                        // room before we update the occupant info (which triggers the avatar
                        // media change on the client).
                        user.getLocal(MemberLocal.class).putAvatarMemoriesIntoRoom(
                            (RoomObject)plobj);
                    }
                    // if the player wasn't in a room, the avatar memories will just sit in
                    // MemberLocal storage until they do enter a room, which is proper
                }
            }
            _bodyMan.updateOccupantInfo(user, new MemberInfo.AvatarUpdater(user));

        } finally {
            user.commitTransaction();
        }
        listener.success();

        // this just fires off an invoker unit, we don't need the result, log it
        _itemMan.updateItemUsage(
            user.getMemberId(), prev, avatar, new ResultListener.NOOP<Void>() {
            @Override public void requestFailed (final Exception cause) {
                log.warning("Unable to update usage from an avatar change.");
            }
        });

        // now fire off a unit to update the avatar information in the database
        _invoker.postUnit(new WriteOnlyUnit("updateAvatar") {
            @Override public void invokePersist () throws Exception {
                int avatarId = (avatar == null) ? 0 : avatar.itemId;
                _memberRepo.configureAvatarId(user.getMemberId(), avatarId);
                _themeRepo.noteAvatarWorn(
                    user.getMemberId(), user.theme != null ? user.theme.groupId : 0, avatarId);
            }
            @Override public void handleFailure (Exception pe) {
                log.warning("configureAvatarId failed", "user", user.which(), "avatar", avatar, pe);
            }
        });
    }

    /**
     * Unit for giving a user a startup avatar and then returning the id of their home room so
     * they can go to it.
     * TODO: share code with {@link MsoySceneRegistry.ThemeRepositoryUnit}
     */
    protected class GiftUnit extends PersistingUnit
        implements SetAvatarListener
    {
        public GiftUnit (MemberObject memobj, int giftCatalogId, ConfirmListener listener)
        {
            super("giftAndWearAvatar", listener);
            _mobj = memobj;
            _mname = memobj.memberName;
            _giftCatalogId = giftCatalogId;
        }

        @Override // from PersistingUnit
        public void invokePersistent ()
            throws Exception
        {
            // find the record for the chosen catalog definition
            CatalogRecord catRec = _avaRepo.loadListing(_giftCatalogId, true);
            if (catRec == null) {
                // this should not happen with a legitimate client
                warn("No gift found matching id");
                throw new InvocationException(MsoyCodes.E_INTERNAL_ERROR);
            }

            _newAvatar = giveGift((Avatar)catRec.item.toItem());
        }

        @Override // from PersistingUnit
        public void handleSuccess ()
        {
            // special case, we have just gifted this avatar so it cannot have any memories, don't
            // bother with a miss on the db
            EntityMemories memories = null;

            finishSetAvatar(_mobj, _newAvatar, memories, this);
        }

        protected Avatar giveGift (Avatar avatar)
        {
            AvatarRepository repo = _itemLogic.getAvatarRepository();
            AvatarRecord item = repo.loadItem(avatar.itemId);
            item = repo.insertClone(item, _mname.getMemberId(), Currency.COINS, 0);
            log.info("Gifted startup avatar", "member", _mname, "catalogId", _giftCatalogId,
                "newItemId", item.itemId);
            return (Avatar)item.toItem();
        }

        protected void warn (String message)
        {
            log.warning(message, "member", _mname, "giftCatalogId", _giftCatalogId);
        }

        @Override // from SetAvatarListener
        public void accessDeniedFailure ()
            throws Exception
        {
            throw new InvocationException(ItemCodes.E_ACCESS_DENIED);
        }

        @Override // from SetAvatarListener
        public void noSuchItemFailure ()
            throws Exception
        {
            throw new InvocationException(ItemCodes.E_NO_SUCH_ITEM);
        }

        @Override // from SetAvatarListener
        public void success ()
        {
            ((ConfirmListener)_listener).requestProcessed();
        }

        protected MemberName _mname;
        protected MemberObject _mobj;
        protected int _giftCatalogId;
        protected Avatar _newAvatar;
    }

    // dependencies
    @Inject protected @MainInvoker Invoker _invoker;
    @Inject protected BodyManager _bodyMan;
    @Inject protected GroupRepository _groupRepo;
    @Inject protected ItemLogic _itemLogic;
    @Inject protected ThemeLogic _themeLogic;
    @Inject protected ItemManager _itemMan;
    @Inject protected MemberLocator _locator;
    @Inject protected MemberLogic _memberLogic;
    @Inject protected MemberRepository _memberRepo;
    @Inject protected MemoryRepository _memoryRepo;
    @Inject protected MsoySceneRepository _sceneRepo;
    @Inject protected AvatarRepository _avaRepo;
    @Inject protected NotificationManager _notifyMan;
    @Inject protected PlaceRegistry _placeReg;
    @Inject protected ThemeRepository _themeRepo;
}
