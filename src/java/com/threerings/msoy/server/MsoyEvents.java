//
// $Id$

package com.threerings.msoy.server;

import java.util.Date;

import com.threerings.panopticon.common.event.annotations.Event;
import com.threerings.panopticon.common.event.annotations.Field;
import com.threerings.panopticon.common.event.annotations.Index;

import com.threerings.msoy.data.all.VisitorInfo;
import com.threerings.msoy.money.data.all.Currency;

/**
 * Logging events generated by the Whirled server.
 *
 * Note: event names should not be changed. If you /really/ want to, you also have to convert
 * the appropriate Panopticon aggregators to consume the new event.
 */
public class MsoyEvents
{
    public interface MsoyEvent
    {
    }

    @Event(name="CurrentMemberStats") // note: do not change this event name
    public static class CurrentMemberStats implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String serverName;
        @Field final public int total;
        @Field final public int active;
        @Field final public int guests;
        @Field final public int viewers;

        public CurrentMemberStats (
                String serverName, int total, int active, int guests, int viewers)
        {
            this.timestamp = new Date();
            this.serverName = toValue(serverName);
            this.total = total;
            this.active = active;
            this.guests = guests;
            this.viewers = viewers;
        }
    }

    @Event(name="Login") // note: do not change this event name
    public static class Login implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public boolean firstLogin;
        @Field final public boolean isGuest;
        @Field final public long createdOn;
        @Field final public String tracker;

        public Login (int memberId, boolean firstLogin, boolean isGuest,
            long createdOn, String tracker)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.firstLogin = firstLogin;
            this.isGuest = isGuest;
            this.createdOn = createdOn;
            this.tracker = toValue(tracker);
        }
    }

    @Event(name="SessionMetrics") // note: do not change this event name
    public static class SessionMetrics implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public boolean isGuest;
        @Field final public int inMyRooms;
        @Field final public int inFriendRooms;
        @Field final public int inStrangerRooms;
        @Field final public int inWhirleds;
        @Field final public int totalActive;
        @Field final public int totalIdle;
        @Field final public String sessionToken;

        public SessionMetrics (int memberId, boolean isGuest, int timeInMyRooms,
            int timeInFriendRooms, int timeInStrangerRooms, int timeInWhirleds,
            int totalTimeActive, int totalTimeIdle, String sessionToken)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.isGuest = isGuest;
            this.inMyRooms = timeInMyRooms;
            this.inFriendRooms = timeInFriendRooms;
            this.inStrangerRooms = timeInStrangerRooms;
            this.inWhirleds = timeInWhirleds;
            this.totalActive = totalTimeActive;
            this.totalIdle = totalTimeIdle;
            this.sessionToken = toValue(sessionToken);
        }
    }

    @Event(name="MailSent") // note: do not change this event name
    public static class MailSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int senderId;
        @Field final public int recipientId;
        @Field final public int payloadType;

        public MailSent (int senderId, int recipientId, int payloadType)
        {
            this.timestamp = new Date();
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.payloadType = payloadType;
        }
    }

    @Event(name="RetentionMailSent") // note: do not change this event name
    public static class RetentionMailSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int recipientId;
        @Field final public String visitorId;
        @Field final public String lapseStatus;
        @Field final public String subjectLine;
        @Field final public String bucket;
        @Field final public int numFriends;
        @Field final public int numPersonalMessages;
        @Field final public boolean validated;

        public RetentionMailSent (int recipientId, String visitorId, String lapseStatus,
            String subjectLine, String bucket, int numFriends, int numPersonalMessages,
            boolean validated)
        {
            this.timestamp = new Date();
            this.recipientId = recipientId;
            this.visitorId = visitorId;
            this.lapseStatus = lapseStatus;
            this.subjectLine = subjectLine;
            this.bucket = bucket;
            this.numFriends = numFriends;
            this.numPersonalMessages = numPersonalMessages;
            this.validated = validated;
        }
    }

    @Event(name="ExchangeRate") // note: do not change this event name
    public static class ExchangeRate implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String serverName;
        @Field final public double rate;

        public ExchangeRate (String serverName, double rate)
        {
            this.timestamp = new Date();
            this.serverName = serverName;
            this.rate = rate;
        }
    }

    @Event(name="FlowTransaction") // note: do not change this event name
    public static class FlowTransaction implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int actionType;
        @Field final public int deltaFlow;
        @Field final public int deltaBars;
        @Field final public int deltaBling;

        public FlowTransaction (int memberId, int actionType, Currency currency, int amountDelta)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.actionType = actionType;
            this.deltaFlow = (currency == Currency.COINS) ? amountDelta : 0;
            this.deltaBars = (currency == Currency.BARS) ? amountDelta : 0;
            this.deltaBling = (currency == Currency.BLING) ? amountDelta : 0;
        }
    }

    @Event(name="ItemPurchase") // note: do not change this event name
    public static class ItemPurchase implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public byte itemType;
        @Field final public int itemId;
        @Field final public int flowCost;
        @Field final public int goldCost;

        public ItemPurchase (
            int memberId, byte itemType, int itemId, Currency currency, int amountPaid)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.itemType = itemType;
            this.itemId = itemId;
            this.flowCost = (currency == Currency.COINS) ? amountPaid : 0;
            this.goldCost = (currency == Currency.BARS) ? amountPaid : 0;
        }
    }

    @Event(name="ItemCatalogListing") // note: do not change this event name
    public static class ItemCatalogListing implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int creatorId;
        @Field final public byte itemType;
        @Field final public int itemId;
        @Field final public int flowCost;
        @Field final public int goldCost;
        @Field final public int pricing;
        @Field final public int salesTarget;

        public ItemCatalogListing (int creatorId, byte itemType,
            int itemId, int flowCost, int goldCost, int pricing, int salesTarget)
        {
            this.timestamp = new Date();
            this.creatorId = creatorId;
            this.itemType = itemType;
            this.itemId = itemId;
            this.flowCost = flowCost;
            this.goldCost = goldCost;
            this.pricing = pricing;
            this.salesTarget = salesTarget;
        }
    }

    @Event(name="FriendshipAction") // note: do not change this event name
    public static class FriendshipAction implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int friendId;
        @Field final public boolean isAdded;

        public FriendshipAction (int memberId, int friendId, boolean isAdded)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.friendId = friendId;
            this.isAdded = isAdded;
        }
    }

    @Event(name="BatchFriendRequestSent")
    public static class BatchFriendRequestSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int count;
        @Field final public int failures;

        public BatchFriendRequestSent (int memberId, int count, int failures)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.count = count;
            this.failures = failures;
        }
    }
    
    @Event(name="GroupMembershipAction") // note: do not change this event name
    public static class GroupMembershipAction implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int groupId;
        @Field final public boolean isJoined;

        public GroupMembershipAction (int memberId, int groupId, boolean isJoined)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.groupId = groupId;
            this.isJoined = isJoined;
        }
    }

    @Event(name="GroupRankModification") // note: do not change this event name
    public static class GroupRankModification implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int groupId;
        @Field final public byte newRank;

        public GroupRankModification (int memberId, int groupId, byte newRank)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.groupId = groupId;
            this.newRank = newRank;
        }
    }

    @Event(name="RoomExit") // note: do not change this event name
    public static class RoomExit implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int playerId;
        @Field final public int sceneId;
        @Field final public boolean isWhirled;
        @Field final public int secondsInRoom;
        @Field final public int occupantsLeft;
        @Field final public String tracker;

        public RoomExit (
            int playerId, int sceneId, boolean isWhirled, int secondsInRoom, int occupantsLeft,
            String tracker)
        {
            this.timestamp = new Date();
            this.playerId = playerId;
            this.sceneId = sceneId;
            this.isWhirled = isWhirled;
            this.secondsInRoom = secondsInRoom;
            this.occupantsLeft = occupantsLeft;
            this.tracker = tracker;
        }
    }

    @Event(name="AVRGExit") // note: do not change this event name
    public static class AVRGExit implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Index @Field final public int gameId;
        @Field final public int playerId;
        @Field final public int secondsInGame;
        @Field final public int playersLeft;
        @Field final public String tracker;

        public AVRGExit (int playerId, int gameId, int seconds, int playersLeft, String tracker)
        {
            this.timestamp = new Date();
            this.playerId = playerId;
            this.gameId = gameId;
            this.secondsInGame = seconds;
            this.playersLeft = playersLeft;
            this.tracker = tracker;
        }
    }

    @Event(name="GameExit") // note: do not change this event name
    public static class GameExit implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Index @Field final public int gameId;
        @Field final public byte gameGenre;
        @Field final public int playerId;
        @Field final public int secondsInGame;
        @Field final public boolean multiplayer;
        @Field final public String tracker;

        public GameExit (
            int playerId, byte gameGenre, int gameId, int seconds, boolean multiplayer,
            String tracker)
        {
            this.timestamp = new Date();
            this.playerId = playerId;
            this.gameGenre = gameGenre;
            this.gameId = gameId;
            this.secondsInGame = seconds;
            this.multiplayer = multiplayer;
            this.tracker = toValue(tracker);
        }
    }

    @Event(name="GamePlayed") // note: do not change this event name
    public static class GamePlayed implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int gameGenre;
        @Field final public int gameId;
        @Field final public int itemId;
        @Field final public int payout;
        @Field final public int secondsPlayed;
        @Field final public int playerId;

        public GamePlayed (
            int gameGenre, int gameId, int itemId, int payout, int secondsPlayed, int playerId)
        {
            this.timestamp = new Date();
            this.gameGenre = gameGenre;
            this.gameId = gameId;
            this.itemId = itemId;
            this.payout = payout;
            this.secondsPlayed = secondsPlayed;
            this.playerId = playerId;
        }
    }

    @Event(name="TrophyEarned") // note: do not change this event name
    public static class TrophyEarned implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int recipientId;
        @Field final public int gameId;
        @Field final public String trophyIdent;

        public TrophyEarned (int recipientId, int gameId, String trophyIdent)
        {
            this.timestamp = new Date();
            this.recipientId = recipientId;
            this.gameId = gameId;
            this.trophyIdent = toValue(trophyIdent);
        }
    }

    @Event(name="PrizeEarned") // note: do not change this event name
    public static class PrizeEarned implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int recipientId;
        @Field final public int gameId;
        @Field final public String prizeIdent;
        @Field final public byte prizeItemType;

        public PrizeEarned (int recipientId, int gameId, String prizeIdent, byte prizeItemType)
        {
            this.timestamp = new Date();
            this.recipientId = recipientId;
            this.gameId = gameId;
            this.prizeIdent = toValue(prizeIdent);
            this.prizeItemType = prizeItemType;
        }
    }

    @Event(name="InviteSent") // note: do not change this event name
    public static class InviteSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String inviteId;
        @Field final public int inviterId;
        @Field final public String recipient;

        public InviteSent (String inviteId, int inviterId, String recipient)
        {
            this.timestamp = new Date();
            this.inviteId = toValue(inviteId);
            this.inviterId = inviterId;
            this.recipient = toValue(recipient);
        }
    }

    @Event(name="GameInviteSent") // note: do not change this event name
    public static class GameInviteSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int gameId;
        @Field final public int inviterId;
        @Field final public String recipient;
        @Field final public String type;

        public GameInviteSent (int gameId, int inviterId, String recipient, String type)
        {
            this.timestamp = new Date();
            this.gameId = gameId;
            this.inviterId = inviterId;
            this.recipient = toValue(recipient);
            this.type = type;
        }
    }

    @Event(name="InviteViewed") // note: do not change this event name
    public static class InviteViewed implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String inviteId;

        public InviteViewed (String inviteId)
        {
            this.timestamp = new Date();
            this.inviteId = toValue(inviteId);
        }
    }

    @Event(name="VisitorInfoCreated") // note: do not change this event name
    public static class VisitorInfoCreated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Index @Field final public String tracker;
        @Field final public boolean web;

        public VisitorInfoCreated (VisitorInfo info, boolean web)
        {
            this.timestamp = new Date();
            this.tracker = toValue(info.id);
            this.web = web;
        }
    }

    @Event(name="VectorAssociated") // note: do not change this event name
    public static class VectorAssociated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Index @Field final public String tracker;
        @Field final public String vector;

        public VectorAssociated (VisitorInfo info, String vector)
        {
            this.timestamp = new Date();
            this.vector = toValue(vector);
            this.tracker = toValue(info.id);
        }
    }

    @Event(name="HttpReferrerAssociated") // note: do not change this event name
    public static class HttpReferrerAssociated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Index @Field final public String tracker;
        @Field final public String referrer;

        public HttpReferrerAssociated (VisitorInfo info, String referrer)
        {
            this.timestamp = new Date();
            this.referrer = toValue(referrer);
            this.tracker = toValue(info.id);
        }
    }

    @Event(name="AccountCreated") // note: do not change this event name
    public static class AccountCreated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int newMemberId;
        @Field final public boolean isGuest;
        @Field final public String inviteId;
        @Field final public int affiliateId;
        @Field final public String tracker;

        public AccountCreated (
            int newMemberId, boolean isGuest, String inviteId, int affiliateId, String tracker)
        {
            this.timestamp = new Date();
            this.newMemberId = newMemberId;
            this.isGuest = isGuest;
            this.inviteId = toValue(inviteId);
            this.affiliateId = affiliateId;
            this.tracker = toValue(tracker);
        }
    }

    @Event(name="RoomUpdated") // note: do not change this event name
    public static class RoomUpdated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int sceneId;

        public RoomUpdated (int memberId, int sceneId)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.sceneId = sceneId;
        }
    }

    @Event(name="ProfileUpdated") // note: do not change this event name
    public static class ProfileUpdated implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;

        public ProfileUpdated (int memberId)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
        }
    }

    @Event(name="ForumMessagePosted") // note: do not change this event name
    public static class ForumMessagePosted implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int threadId;
        /**
         * The number of posts that have been added to the related discussion thread.
         * If this is 1, it indicates that this is the first post of a new thread.
         */
        @Field final public int postNumber;

        public ForumMessagePosted (int memberId, int threadId, int postNumber)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.threadId = threadId;
            this.postNumber = postNumber;
        }
    }

    /**
     * Generic event for an action such as a button click performed on the client.
     */
    @Event(name="ClientAction") // note: do not change this event name
    public static class ClientAction implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String tracker;
        @Index @Field final public String actionName;
        /** Additional information such as which game's button was clicked */
        @Field final public String details;

        public ClientAction (String tracker, String actionName, String details)
        {
            this.timestamp = new Date();
            this.tracker = tracker;
            this.actionName = actionName;
            this.details = toValue(details);
        }
    }

    /**
     * A/B Test-related action such as a button click or hitting an a/b test page.  Used
     * for short term testing.
     */
    @Event(name="TestAction") // note: do not change this event name
    public static class TestAction implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String tracker;
        @Index @Field final public String actionName;
        @Index @Field final public String testName;
        @Field final public int testGroup;

        public TestAction (String tracker, String actionName, String testName, int testGroup)
        {
            this.timestamp = new Date();
            this.tracker = tracker;
            this.actionName = actionName;
            this.testName = testName;
            this.testGroup = testGroup;
        }
    }

    /**
     * Web-side equivalent of a PlayerLogin, except it works for guest visitors as well.
     * Should be fired when someone loads up GWT and authenticates (or fails to authenticate).
     */
    @Event(name="WebSessionStatusChanged") // note: do not change this event name
    public static class WebSessionStatusChanged implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public String tracker;
        @Field final public boolean player;
        @Field final public boolean guest;
        @Field final public boolean other;
        @Field final public boolean newInfo;

        public WebSessionStatusChanged (
            String tracker, boolean player, boolean guest, boolean other, boolean newInfo)
        {
            this.timestamp = new Date();
            this.tracker = tracker;
            this.player = player;
            this.guest = guest;
            this.other = other;
            this.newInfo = newInfo;
        }
    }

    /**
     * Generic one-shot experiences being logged about players.
     */
    @Event(name="Experience") // note: do not change this event name
    public static class Experience implements MsoyEvent
    {
        /**
         * Constants for the various experience events that we log about our users.
         *
         * <p>The enum is package-protected, so that we don't expose experience types directly
         * to client code; changes to this set must be done with understanding of how or whether
         * reports will need to be changed to cope with the data change.
         */
        enum Type {
            // NOTE: do not change existing string tokens
            GAME_SINGLEPLAYER  ("GS"),
            GAME_MULTIPLAYER   ("GM"),
            GAME_AVRG          ("GA"),
            VISIT_WHIRLED      ("VW"),
            VISIT_ROOM         ("VR"),
            FORUMS_READ        ("FR"),
            FORUMS_POSTED      ("FP"),
            SHOP_BROWSED       ("SB"),
            SHOP_DETAILS       ("SD"),
            SHOP_PURCHASED     ("SP"),
            EDIT_PROFILE       ("EP"),
            EDIT_ROOM          ("ER"),
            ITEM_UPLOADED      ("IU"),
            ITEM_LISTED        ("IL"),
            ACCOUNT_CREATED    ("AC"),
            ACCOUNT_LOGIN      ("AL");
            // more events go here. if needed, update aggregators in Panopticon:
            // DailyAllGuestBehavior.properties, DailyExperience*.properties

            /** Package-protected string that gets logged in Panopticon. */
            final String token;

            private Type (String token) {
                this.token = token;
            }
        }

        @Index @Field final public Date timestamp;
        @Index @Field final public String tracker;
        @Field final public int memberId;
        @Field final public String action;

        public Experience (Type action, int memberId, String tracker)
        {
            this.timestamp = new Date();
            this.tracker = toValue(tracker);
            this.memberId = memberId;
            this.action = action.token;
        }
    }

    /**
     * Notes that a user paid for a broadcast message to be sent.
     */
    @Event(name="BroadcastSent") // note: do not change this event name
    public static class BroadcastSent implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public int barsPaid;

        public BroadcastSent (int memberId, int barsPaid)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.barsPaid = barsPaid;
        }
    }

    /**
     * Notes that a mochi game was played on facebook arcade.
     */
    @Event(name="FacebookMochiGameEntered")
    public static class FacebookMochiGameEntered implements MsoyEvent
    {
        @Index @Field final public Date timestamp;
        @Field final public int memberId;
        @Field final public String mochiTag;

        public FacebookMochiGameEntered (int memberId, String mochiTag)
        {
            this.timestamp = new Date();
            this.memberId = memberId;
            this.mochiTag = mochiTag;
        }
    }

    protected static String toValue (String input) {
        return (input != null) ? input : "";
    }
}
