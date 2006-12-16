//
// $Id$

package com.threerings.msoy.item.web {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Represents a piece of furniture (any prop really) that a user can place into
 * a virtual world scene and potentially interact with.
 */
public class Furniture extends Item
{
    /** An action associated with this furniture which is dispatched to the
     * virtual world client when the furniture is clicked on (max length 255
     * characters). */
    public var action :String = "";

    // from Item
    override public function getType () :int
    {
        return FURNITURE;
    }

    override protected function getDefaultThumbnailMedia () :MediaDesc
    {
        if (furniMedia != null && furniMedia.isImage()) {
            return furniMedia;
        }
        return super.getDefaultThumbnailMedia();
    }

    override protected function getDefaultFurniMedia () :MediaDesc
    {
        return null; // there is no default
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeField(action);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        action = (ins.readField(String) as String);
    }
}
}
