//
// $Id$

package com.threerings.msoy.world.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * The base for all furni updates.
 */
public /*abstract*/ class FurniUpdate extends SceneUpdate
{
    /** The furni being operated on by this update. */
    public var data :FurniData;

    // from SceneUpdate
    override public function apply (model :SceneModel) :void
    {
        super.apply(model);
        doUpdate((model as MsoySceneModel));
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(data);
    }

    // documentation inherited
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        data = (ins.readObject() as FurniData);
    }

    protected /*abstract*/ function doUpdate (model :MsoySceneModel) :void
    {
        throw new Error("abstract");
    }
}
}
