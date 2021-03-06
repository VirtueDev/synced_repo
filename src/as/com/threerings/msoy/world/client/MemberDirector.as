//
// $Id$

package com.threerings.msoy.world.client {

import com.threerings.util.Log;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.msoy.client.MemberService;
import com.threerings.msoy.data.MemberMarshaller;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.MsoyCodes;
import com.threerings.msoy.world.client.WorldContext;

public class MemberDirector extends BasicDirector
{
    public const log :Log = Log.getLog(MemberDirector);

    // statically reference classes we require
    MemberMarshaller;

    public function MemberDirector (ctx :WorldContext)
    {
        super(ctx);
        _wctx = ctx;
    }

    /**
     * Request to make the user our friend.
     */
    public function inviteToBeFriend (friendId :int) :void
    {
        _msvc.inviteToBeFriend(friendId, _wctx.resultListener(handleInviteResult));
    }

    /**
     * Request to change our display name.
     */
    public function setDisplayName (newName :String) :void
    {
        _msvc.setDisplayName(newName,
            _wctx.confirmListener(_wctx.getWorldController().refreshDisplayName));
    }

    protected function handleInviteResult (automatic :Boolean) : void
    {
        if (!automatic) {
            _wctx.displayFeedback(MsoyCodes.GENERAL_MSGS, "m.friend_invited")
        }
    }

    // from BasicDirector
    override protected function registerServices (client :Client) :void
    {
        client.addServiceGroup(MsoyCodes.MSOY_GROUP);
    }

    // from BasicDirector
    override protected function fetchServices (client :Client) :void
    {
        super.fetchServices(client);

        _msvc = (client.requireService(MemberService) as MemberService);
    }

    protected var _wctx :WorldContext;
    protected var _msvc :MemberService;
    protected var _mobj :MemberObject;
}
}
