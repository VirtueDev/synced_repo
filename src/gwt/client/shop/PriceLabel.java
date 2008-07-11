//
// $Id$

package client.shop;

import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.gwt.ui.InlineLabel;

import client.util.MsoyUI;

/**
 * Displays the cost of an item.
 */
public class PriceLabel extends FlowPanel
{
    public PriceLabel (int flowCost, int goldCost)
    {
        setStyleName("Price");
        updatePrice(flowCost, goldCost);
    }

    public void updatePrice (int flowCost, int goldCost)
    {
        clear();
        add(new InlineLabel(CShop.msgs.price(), false, false, true));
        if (goldCost > 0) {
            add(MsoyUI.createInlineImage("/images/ui/gold.png"));
            add(new InlineLabel(""+goldCost, false, false, true));
        }
        if (flowCost > 0 || (goldCost == 0)) {
            add(MsoyUI.createInlineImage("/images/ui/coins.png"));
            add(new InlineLabel(""+flowCost, false, false, true));
        }
    }
}
