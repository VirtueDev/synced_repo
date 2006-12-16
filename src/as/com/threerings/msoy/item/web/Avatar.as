//
// $Id$

package com.threerings.msoy.item.web {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Represents an avatar that's usable in the msoy system.
 */
public class Avatar extends Item
{
    /** The avatar media. */
    public var avatarMedia :MediaDesc;

    /** The headshot media. */
    public var headShotMedia :MediaDesc;

    // from Item
    override public function getType () :int
    {
        return AVATAR;
    }

    /**
     * Returns a media descriptor for the media that should be used
     * to display our headshot representation.
     */
    public function getHeadShotMedia () :MediaDesc
    {
        return (headShotMedia != null) ? headShotMedia :
            new StaticMediaDesc(StaticMediaDesc.HEADSHOT, AVATAR);
    }

    override protected function getDefaultThumbnailMedia () :MediaDesc
    {
        if (avatarMedia != null && avatarMedia.isImage()) {
            return avatarMedia;
        }
        return super.getDefaultThumbnailMedia();
    }

    override protected function getDefaultFurniMedia () :MediaDesc
    {
        return avatarMedia;
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeObject(avatarMedia);
        out.writeObject(headShotMedia);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        avatarMedia = (ins.readObject() as MediaDesc);
        headShotMedia = (ins.readObject() as MediaDesc);
    }
}
}
