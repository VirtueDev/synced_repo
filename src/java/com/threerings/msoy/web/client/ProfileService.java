//
// $Id$

package com.threerings.msoy.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;

import com.threerings.msoy.data.all.MemberName;

import com.threerings.msoy.person.data.Profile;

import com.threerings.msoy.web.data.ServiceException;
import com.threerings.msoy.web.data.MemberCard;
import com.threerings.msoy.web.data.WebIdent;

/**
 * Defines profile-related services available to the GWT/AJAX web client.
 */
public interface ProfileService extends RemoteService
{
    /** Provides results for {@link #loadProfile}. */
    public static class ProfileResult implements IsSerializable
    {
        /** This user's name and member id. */
        public MemberName name;

        /** This user's total friend count. */
        public int totalFriendCount;

        /** Whether or not the requesting member is a friend of this member. */
        public boolean isOurFriend;

        /** This user's basic profile information. */
        public Profile profile;

        /**
         * This user's featured friends.
         *
         * @gwt.typeArgs <com.threerings.msoy.person.data.Interest>
         */
        public List interests;

        /**
         * This user's featured friends.
         *
         * @gwt.typeArgs <com.threerings.msoy.web.data.MemberCard>
         */
        public List friends;

        /**
         * This user's groups.
         *
         * @gwt.typeArgs <com.threerings.msoy.web.data.GroupCard>
         */
        public List groups;

        /**
         * This user's game ratings.
         *
         * @gwt.typeArgs <com.threerings.msoy.web.data.GameRating>
         */
        public List ratings;

        /**
         * This user's recently earned trophies.
         *
         * @gwt.typeArgs <com.threerings.msoy.game.data.all.Trophy>
         */
        public List trophies;

        /** 
        * This member's recent self feed messages.
        *
        * @gwt.typeArgs <com.threerings.msoy.person.data.FeedMessage>
        */
        public List feed;
    }

    /** Provides results for {@link #loadFriends}. */
    public static class FriendsResult implements IsSerializable
    {
        /** This user's name and member id. */
        public MemberName name;

        /**
         * This user's friends.
         *
         * @gwt.typeArgs <com.threerings.msoy.web.data.MemberCard>
         */
        public List friends;
    }

    /**
     * Loads the specified member's profile information.
     */
    public ProfileResult loadProfile (WebIdent ident, int memberId)
        throws ServiceException;

    /**
     * Requests that this user's profile be updated.
     */
    public void updateProfile (WebIdent ident, String displayName, Profile profile)
        throws ServiceException;

    /**
     * Updates the calling user's interests.
     *
     * @gwt.typeArgs interests <com.threerings.msoy.person.data.Interest>
     */
    public void updateInterests (WebIdent ident, List interests)
        throws ServiceException;

    /**
     * Looks for profiles that match the specified search term. We'll aim to be smart about what we
     * search. Returns a (possibly empty) list of {@link MemberCard} records.
     *
     * @gwt.typeArgs <com.threerings.msoy.web.data.MemberCard>
     */
    public List findProfiles (WebIdent ident, String search)
        throws ServiceException;

    /**
     * Loads up all friends for the specified member.
     */
    public FriendsResult loadFriends (WebIdent ident, int memberId)
        throws ServiceException;

    /**
     * Loads up e-mail addresses from a user's webmail account.
     *
     * @gwt.typeArgs <com.threerings.msoy.web.data.EmailContact>
     */
    public List getWebMailAddresses (WebIdent ident, String email, String password)
        throws ServiceException;

    /**
     * Loads the self feed for the specified member
     *
     * @gwt.typeArgs <com.threerings.msoy.person.data.FeedMessage>
     */
    public List loadSelfFeed (int profileMemberId, int cutoffDays)
        throws ServiceException;
}
