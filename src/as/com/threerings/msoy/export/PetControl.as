//
// $Id$

package com.threerings.msoy.export {

import flash.display.DisplayObject;

/**
 * Defines actions, accessors and callbacks available to all Pets.
 */
public class PetControl extends MobileControl
{
    /**
     * Creates a controller for a Pet. The display object is the Pet's visualization.
     */
    public function PetControl (disp :DisplayObject)
    {
        super(disp);
    }

    // from MsoyControl
    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        // TODO
    }
}
}
