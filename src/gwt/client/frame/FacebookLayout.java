//
// $Id$

package client.frame;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import com.threerings.msoy.data.all.DeploymentConfig;
import com.threerings.msoy.web.gwt.Pages;

/**
 * A frame layout that adds an extra iframe below the main gwt page showing facebook information
 * such as friend scores etc.
 */
public class FacebookLayout extends FramedLayout
{
    @Override
    protected void init (FrameHeader header, ClickHandler onGoHome)
    {
        super.init(header, onGoHome);

        // add our container and iframe
        SimplePanel bar = new SimplePanel();
        RootPanel.get(PAGE).add(bar);

        // create our iframe
        String src = "/gwt/" + DeploymentConfig.version + "/" + Pages.FACEBOOK.getPath() + "/";
        _fbframe = new Frame(src);
        _fbframe.setStyleName("fbIFrame");
        _fbframe.setWidth("100%");
        _fbframe.setHeight(FB_FRAME_HEIGHT + "px");
        _fbframe.getElement().setAttribute("scrolling", "no");
        bar.setWidget(_fbframe);
    }

    @Override
    protected int getReservedHeight ()
    {
        return FB_FRAME_HEIGHT;
    }

    protected Frame _fbframe;
    protected static final int FB_FRAME_HEIGHT = 150;
}