//
// $Id$

package client.shop;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Image;

import com.threerings.gwt.ui.SmartTable;

import com.threerings.msoy.item.gwt.ListingCard;

import client.item.ItemBox;
import client.item.ItemMessages;
import client.shell.Args;
import client.shell.Pages;
import client.ui.MsoyUI;
import client.ui.Stars;

/**
 * Displays a catalog listing.
 */
public class ListingBox extends ItemBox
{
    public ListingBox (ListingCard listing)
    {
        super(listing.thumbMedia, listing.name, Pages.SHOP,
              Args.compose("l", "" + listing.itemType, "" + listing.catalogId), listing.remixable);

        String cname = _imsgs.itemBy(listing.creator.toString());
        addLabel(MsoyUI.createLabel(cname, "Creator"));

        int row = getRowCount();
        setWidget(row, 0, new Stars(listing.rating, true, true, null), 1, "Rating");

        SmartTable cost = new SmartTable(0, 0);
        setWidget(row, 1, cost);

        cost.setWidget(0, 0, new Image("/images/ui/coins.png"));
        cost.getFlexCellFormatter().setWidth(0, 0, "15px");
        cost.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
        cost.setText(0, 1, String.valueOf(listing.flowCost), 1, "Cost");

        if (listing.goldCost > 0) {
            cost.setWidget(1, 0, new Image("/images/ui/gold.png"));
            cost.getFlexCellFormatter().setWidth(1, 0, "15px");
            cost.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasAlignment.ALIGN_RIGHT);
            cost.setText(1, 1, String.valueOf(listing.goldCost), 1, "Cost");
        }
    }

    @Override // from ItemBox
    protected int getColumns ()
    {
        return 2;
    }

    protected static final ItemMessages _imsgs = GWT.create(ItemMessages.class);
}
