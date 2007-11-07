//
// $Id$

package client.game;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import com.threerings.gwt.ui.WidgetUtil;

import com.threerings.msoy.web.client.DeploymentConfig;
import com.threerings.msoy.web.client.GameService;
import com.threerings.msoy.web.client.GameServiceAsync;
import com.threerings.msoy.web.data.LaunchConfig;

import client.shell.Args;
import client.shell.Frame;
import client.shell.Page;
import client.shell.WorldClient;
import client.util.MsoyUI;

/**
 * Displays a page that allows a player to play a particular game. If it's single player the game
 * is shown, if it's multiplayer the lobby is first shown where the player can find opponents
 * against which to play.
 */
public class index extends Page
{
    /** Required to map this entry point to a page. */
    public static Creator getCreator ()
    {
        return new Creator() {
            public Page createPage () {
                return new index();
            }
        };
    }

    // @Override from Page
    public void onHistoryChanged (Args args)
    {
        // if we're not a dev deployment, disallow guests
        if (!DeploymentConfig.devDeployment && CGame.ident == null) {
            setContent(MsoyUI.createLabel(CGame.cmsgs.noGuests(), "infoLabel"));
            return;
        }

        // if we have d-NNN then we want to see game detail
        String action = args.get(0, "");
        if (action.equals("d")) {
            GameDetailPanel panel;
            if (getContent() instanceof GameDetailPanel) {
                panel = (GameDetailPanel)getContent();
            } else {
                setContent(panel = new GameDetailPanel(this));
            }
            panel.setGame(args.get(1, 0), args.get(2, ""));

        } else if (action.equals("t")) {
            setContent(new TrophyCasePanel(this, args.get(1, 0)));

        } else if (action.equals("s")) {
            // head straight into this game (single player or first available party game)
            loadLaunchConfig(args.get(1, 0), -1, true);

        } else {
            // otherwise our args are 'gameId-gameOid' or just 'gameId'
            loadLaunchConfig(args.get(0, 0), args.get(1, -1), false);
        }
    }

    // @Override // from Page
    public void onPageLoad ()
    {
        super.onPageLoad();
        configureCallbacks(this);
    }

    // @Override // from Page
    public void onPageUnload ()
    {
        super.onPageUnload();
        clearCallbacks();
    }

    // @Override // from Page
    protected String getPageId ()
    {
        return GAME;
    }

    // @Override // from Page
    protected void initContext ()
    {
        super.initContext();

        // wire up our remote services
        CGame.gamesvc = (GameServiceAsync)GWT.create(GameService.class);
        ((ServiceDefTarget)CGame.gamesvc).setServiceEntryPoint("/gamesvc");

        // load up our translation dictionaries
        CGame.msgs = (GameMessages)GWT.create(GameMessages.class);
    }

    protected void loadLaunchConfig (int gameId, final int gameOid, final boolean playNow)
    {
        // load up the information needed to launch the game
        CGame.gamesvc.loadLaunchConfig(CGame.ident, gameId, new AsyncCallback() {
            public void onSuccess (Object result) {
                launchGame((LaunchConfig)result, gameOid, playNow);
            }
            public void onFailure (Throwable cause) {
                MsoyUI.error(CGame.serverError(cause));
            }
        });
    }

    protected void launchGame (final LaunchConfig config, final int gameOid, boolean playNow)
    {
        switch (config.type) {
        case LaunchConfig.FLASH_IN_WORLD:
            WorldClient.displayFlash("worldGame=" + config.gameId);
            break;

        case LaunchConfig.FLASH_LOBBIED:
            if (gameOid <= 0) {
                if (playNow) {
                    WorldClient.displayFlash("playNow=" + config.gameId);
                } else {
                    WorldClient.displayFlash("gameLobby=" + config.gameId);
                }
            } else {
                WorldClient.displayFlash("gameLocation=" + gameOid);
            }
            break;

        case LaunchConfig.JAVA_FLASH_LOBBIED:
        case LaunchConfig.JAVA_SELF_LOBBIED:
            if (config.type == LaunchConfig.JAVA_FLASH_LOBBIED && gameOid <= 0) {
                WorldClient.displayFlash("gameLobby=" + config.gameId);

            } else {
                // clear out the client as we're going into Java land
                Frame.closeClient(false);

                // prepare a command to be invoked once we know Java is loaded
                _javaReadyCommand = new Command() {
                    public void execute () {
                        displayJava(config, gameOid);
                    }
                };

                // stick up a loading message and the HowdyPardner Java applet
                FlowPanel bits = new FlowPanel();
                bits.setStyleName("javaLoading");
                bits.add(new Label("Loading game..."));

                String gameServer = "http://" + config.server + ":" + config.httpPort;
                String howdyJar =
                    gameServer + "/clients/" + DeploymentConfig.version + "/howdy.jar";
                bits.add(WidgetUtil.createApplet("game", howdyJar,
                                                 "com.threerings.msoy.client.HowdyPardner",
                                                 "100", "10", true, new String[0]));
                setContent(bits);
            }
            break;

        case LaunchConfig.FLASH_SOLO:
            setPageTitle(config.name);
            setFlashContent(WidgetUtil.createFlashObjectDefinition(
                                "game", config.gameMediaPath, 800, 600, null));
            break;

        case LaunchConfig.JAVA_SOLO:
            setPageTitle(config.name);
            setContent(new Label("Not yet supported"));
            break;

        default:
            setPageTitle(config.name);
            setContent(new Label(CGame.msgs.errUnknownGameType("" + config.type)));
            break;
        }
    }

    protected void displayJava (LaunchConfig config, int gameOid)
    {
        String[] args = new String[] {
            "game_id", "" + config.gameId, "game_oid", "" + gameOid,
            "server", config.server, "port", "" + config.port,
            "authtoken", (CGame.ident == null) ? "" : CGame.ident.token };

        // we have to serve game-client.jar from the server to which it will connect back due to
        // security restrictions and proxy the game jar through there as well
        String gameServer = "http://" + config.server + ":" + config.httpPort;
        String gameJar = gameServer + "/clients/" +
            DeploymentConfig.version + "/" + (config.lwjgl ? "lwjgl-" : "") +
            "game-client.jar";

        WorldClient.displayJava(
            WidgetUtil.createApplet(
                "game", gameJar + "," + gameServer + config.gameMediaPath,
                // TODO: allow games to specify their dimensions in their config
                "com.threerings.msoy.game.client." +
                (config.lwjgl ? "LWJGL" : "") + "GameApplet",
                "100%", "600", false, args));
    }

    protected void javaReady ()
    {
        if (_javaReadyCommand != null) {
            DeferredCommand.addCommand(_javaReadyCommand);
            _javaReadyCommand = null;
        }
    }

    protected Command _javaReadyCommand;

    protected static native void configureCallbacks (index page) /*-{
       $wnd.howdyPardner = function () {
            page.@client.game.index::javaReady()();
       }
    }-*/;

    protected static native void clearCallbacks () /*-{
       $wnd.howdyPardner = null;
    }-*/;
}
