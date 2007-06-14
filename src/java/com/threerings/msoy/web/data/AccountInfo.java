//
// $Id$

package com.threerings.msoy.web.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains all account information not already contained in WebCreds.
 */
public class AccountInfo implements IsSerializable
{
    /** The user's real name.  Used for searching only. */
    public String realName = "";
}
