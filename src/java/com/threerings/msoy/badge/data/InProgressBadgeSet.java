//
// $Id$

package com.threerings.msoy.badge.data;

import com.threerings.presents.dobj.DSet;

import com.threerings.msoy.badge.data.all.InProgressBadge;

/**
 * A distributed class containing {@link InProgressBadge} objects.
 */
@com.threerings.util.ActionScript(omit=true)
public final class InProgressBadgeSet extends DSet<InProgressBadge>
{
    /** Creates a BadgeSet with the specified contents. */
    public InProgressBadgeSet (Iterable<InProgressBadge> source)
    {
        super(source);
    }

    /** Creates an empty BadgeSet. */
    public InProgressBadgeSet ()
    {
    }

    /** @return true if the set contains a badge of the given type. */
    public boolean containsBadge (BadgeType type)
    {
        return this.containsBadge(type.getCode());
    }

    /** @return true if the set contains a badge of the given type. */
    public boolean containsBadge (int badgeCode)
    {
        InProgressBadge badge = new InProgressBadge();
        badge.badgeCode = badgeCode;
        return this.contains(badge);
    }

    /**
     * @return the InProgressBadge that matches the specified badge code, or null if the
     * badge is not contained in this set.
     */
    public InProgressBadge getBadge (int badgeCode)
    {
        return get(badgeCode);
    }

    /**
     * @return the InProgressBadge that matches the specified badge type, or null if the badge
     * is not contained in this set.
     */
    public InProgressBadge getBadge (BadgeType type)
    {
        return getBadge(type.getCode());
    }

    /**
     * Adds a new badge to the BadgeSet, or updates an existing badge if the specified badge
     * already exists and the specified level is higher than the existing level.
     *
     * @return true if the badge was newly added or updated, false otherwise.
     */
    public boolean addOrUpdateBadge (InProgressBadge badge)
    {
        InProgressBadge existingBadge = this.get(badge.badgeCode);
        if (existingBadge == null) {
            super.add(badge);
            return true;
        } else if (badge.level > existingBadge.level) {
            existingBadge.level = badge.level;
            existingBadge.levelUnits = badge.levelUnits;
            existingBadge.progress = badge.progress;
            return true;
        }

        return false;
    }

    /**
     * Removes a badge from the BadgeSet.
     *
     * @return true if the badge was removed, false if it wasn't in the set.
     */
    public boolean removeBadge (int badgeCode)
    {
        return (super.removeKey(badgeCode) != null);
    }
}
