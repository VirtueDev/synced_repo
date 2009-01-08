//
// $Id$

package com.threerings.msoy.server.persist;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.*; // for Depot annotations
import com.samskivert.depot.expression.ColumnExp;

/**
 * Represents a friendship between two members.
 */
@Entity(uniqueConstraints={
    @UniqueConstraint(name="inviterInvitee", fields={ "inviterId", "inviteeId" })
})
public class FriendRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<FriendRecord> _R = FriendRecord.class;
    public static final ColumnExp INVITER_ID = colexp(_R, "inviterId");
    public static final ColumnExp INVITEE_ID = colexp(_R, "inviteeId");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 3;

    /** The member id of the inviter. */
    @Id @Index(name="ixInviterId")
    public int inviterId;

    /** The member id of the invitee. */
    @Id @Index(name="ixInviteeId")
    public int inviteeId;

    /**
     * Returns the member of this friendship that was not passed in as an argument.
     */
    public int getFriendId (int memberId)
    {
        return (inviterId == memberId) ? inviteeId : inviterId;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link FriendRecord}
     * with the supplied key values.
     */
    public static Key<FriendRecord> getKey (int inviterId, int inviteeId)
    {
        return new Key<FriendRecord>(
                FriendRecord.class,
                new ColumnExp[] { INVITER_ID, INVITEE_ID },
                new Comparable[] { inviterId, inviteeId });
    }
    // AUTO-GENERATED: METHODS END
}
