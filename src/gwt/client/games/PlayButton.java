//
// $Id$

package client.games;

import com.google.gwt.user.client.ui.PushButton;

import client.shell.Args;
import client.shell.Pages;
import client.util.Link;

/**
 * Displays a single play button.  If multiplayer exists, show a Play with friends button,
 * otherwise show a Play just me button.
 */
public class PlayButton extends PushButton
{
    public PlayButton (final int gameId, int minPlayers, int maxPlayers)
    {
        setStyleName("playButton");
        String args = (minPlayers == 1 && maxPlayers == 1) ?
            Args.compose("game", "s", "" + gameId) : Args.compose("game", "l", "" + gameId);
        addClickListener(Link.createListener(Pages.WORLD, args));
    }
}
