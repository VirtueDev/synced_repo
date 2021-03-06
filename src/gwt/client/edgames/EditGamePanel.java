//
// $Id$

package client.edgames;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.SmartTable;

import com.threerings.msoy.edgame.gwt.EditGameService.GameData;
import com.threerings.msoy.edgame.gwt.EditGameService;
import com.threerings.msoy.edgame.gwt.EditGameServiceAsync;
import com.threerings.msoy.item.data.all.GameItem;
import com.threerings.msoy.item.data.all.MsoyItemType;
import com.threerings.msoy.web.gwt.Args;
import com.threerings.msoy.web.gwt.Pages;

import client.shell.DynamicLookup;
import client.ui.MsoyUI;
import client.ui.NaviTabPanel;
import client.util.ClickCallback;
import client.util.InfoCallback;
import client.util.Link;

/**
 * Displays an interface for editing a game.
 */
public class EditGamePanel extends FlowPanel
{
    public EditGamePanel ()
    {
        setStyleName("editGame");
        add(MsoyUI.createNowLoading());
    }

    public void setGame (int gameId, final int tabIdx)
    {
        if (_gameId == gameId) {
            _tabs.activateTab(tabIdx);
            return;
        }

        _gameId = gameId;
        _gamesvc.loadGameData(gameId, new InfoCallback<GameData>() {
            public void onSuccess (GameData data) {
                init(data);
                _tabs.activateTab(tabIdx);
            }
        });
    }

    protected void init (final GameData data)
    {
        clear();

        SmartTable header = new SmartTable("Header", 0, 10);
        header.setText(0, 0, data.info.name, 1, "Title");
        header.setWidget(0, 1, MsoyUI.createHTML(_msgs.egTip(), null), 1, "Tip");
        Button delete = new Button(_msgs.egDelete());
        header.setWidget(0, 2, delete);
        header.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_RIGHT);
        add(header);

        // wire up the delete button
        new ClickCallback<Void>(delete, _msgs.egDeleteConfirm()) {
            @Override protected boolean callService () {
                _gamesvc.deleteGame(data.info.gameId, this);
                return true;
            }
            @Override protected boolean gotResult (Void result) {
                Link.go(Pages.EDGAMES, "m");
                return true;
            }
        };

        // add our giant tab list of doom
        add(_tabs = new NaviTabPanel(Pages.EDGAMES) {
            protected Args getTabArgs (int tabIdx) {
                return Args.compose("e", _gameId, tabIdx);
            }
        });

        _tabs.add(new LazyPanel() {
            protected Widget createWidget () {
                return new InfoEditorPanel(data);
            }
        }, _msgs.egTabInfo());
        _tabs.add(new LazyPanel() {
            protected Widget createWidget () {
                return new CodeEditorPanel(data.info, data.devCode);
            }
        }, _msgs.egTabCode());
        _tabs.add(new LazyPanel() {
            protected Widget createWidget () {
                return new PublishPanel(data);
            }
            @Override public void setVisible (boolean visible) {
                if (!visible) {
                    setWidget(null); // clear out our panel when we change tabs
                }
                super.setVisible(visible);
            }
        }, _msgs.egTabPublish());
        _tabs.add(new LazyPanel() {
            protected Widget createWidget () {
                return new FacebookGameInfoEditorPanel(data.facebook);
            }
        }, _msgs.egTabFacebook());
        // NOTE: if you add a tab here, you have to adjust StuffPage.PRE_ITEM_TABS

        for (final MsoyItemType type : GameItem.TYPES) {
            _tabs.add(new LazyPanel() {
                protected Widget createWidget () {
                    return new GameItemEditorPanel(data.info.gameId, type);
                }
            }, _dmsgs.xlateItemsType(type));
        }
    }

    protected int _gameId;
    protected NaviTabPanel _tabs;

    protected static final EditGamesMessages _msgs = GWT.create(EditGamesMessages.class);
    protected static final DynamicLookup _dmsgs = GWT.create(DynamicLookup.class);

    protected static final EditGameServiceAsync _gamesvc = GWT.create(EditGameService.class);
}
