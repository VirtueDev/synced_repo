//
// $Id$

package com.threerings.msoy.swiftly.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.threerings.msoy.swiftly.data.DocumentElement;
import com.threerings.msoy.swiftly.data.PathElement;
import com.threerings.msoy.swiftly.data.PathElementTreeNode;
import com.threerings.msoy.swiftly.data.ProjectRoomObject;
import com.threerings.msoy.swiftly.data.ProjectTreeModel;
import com.threerings.msoy.swiftly.util.SwiftlyContext;

public class ProjectPanel extends JPanel
    implements TreeSelectionListener, TreeModelListener
{
    public ProjectPanel (SwiftlyContext ctx, SwiftlyEditor editor)
    {
        super(new BorderLayout());
        _ctx = ctx;
        _editor = editor;
        add(_scrollPane, BorderLayout.CENTER);
        setupToolbar();
        add(_toolbar, BorderLayout.PAGE_END);
    }

    /**
     * Initializes and adds the {@link ProjectTreeModel} to the panel.
     * @param roomObj the {@link ProjectRoomObject} used as the root node.
     */
    public void setProject (ProjectRoomObject roomObj)
    {
        _roomObj = roomObj;
        _treeModel = new ProjectTreeModel(roomObj);
        _treeModel.addTreeModelListener(this);

        _tree = new JTree(_treeModel);
        _tree.setDragEnabled(true);
        _tree.setEditable(true);
        _tree.setShowsRootHandles(true);
        _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeSelectionListener(this);

        _scrollPane.getViewport().setView(_tree);
        disableToolbar();
    }

    // from interface TreeSelectionListener
    public void valueChanged (TreeSelectionEvent e)
    {
        PathElementTreeNode node = (PathElementTreeNode) _tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        setSelectedNode(node);

        PathElement element = getSelectedPathElement();
        if (element instanceof DocumentElement) {
            _editor.addEditorTab((DocumentElement)element);
        }
    }

    // from interface TreeModelListener
    public void treeNodesChanged (TreeModelEvent e)
    {
        // TODO we are in single selection mode so only one node will ever change [right?]
        PathElementTreeNode node = (PathElementTreeNode)e.getChildren()[0];
        if (node.getElement() instanceof DocumentElement) {
            _editor.updateTabTitleAt((DocumentElement)node.getElement());
            // TODO: replace this with super sophisticated fine grained editing model
            _editor.updateTabDocument((DocumentElement)node.getElement());
        }
    }

    // from interface TreeModelListener
    public void treeNodesInserted (TreeModelEvent e)
    {
        // TODO is it clear this is what we want to happen?
        PathElementTreeNode node = (PathElementTreeNode)e.getChildren()[0];
        _tree.scrollPathToVisible(new TreePath(node.getPath()));
    }

    // from interface TreeModelListener
    public void treeNodesRemoved (TreeModelEvent e)
    {
        // TODO iterate over every child and close any open tabs.
    }

    // from interface TreeModelListener
    public void treeStructureChanged (TreeModelEvent e)
    {
        // nada
    }

    protected Action createPlusButtonAction ()
    {
        // TODO need icon
        return new AbstractAction("+") {
            // from AbstractAction
            public void actionPerformed (ActionEvent e) {
                addPathElement(PathElement.Type.FILE);
            }
        };
    }

    protected Action createAddDirectoryAction ()
    {
        // TODO need icon
        return new AbstractAction("+Dir") {
            // from AbstractAction
            public void actionPerformed (ActionEvent e) {
                addPathElement(PathElement.Type.DIRECTORY);
            }
        };
    }

    protected Action createMinusButtonAction ()
    {
        // TODO need icon
        return new AbstractAction("-") {
            // from AbstractAction
            public void actionPerformed (ActionEvent e) {
                deletePathElement();
            }
        };
    }

    /**
     * Adds a {@link PathElement} to the tree and broadcasts that fact to the server.
     */
    protected void addPathElement (PathElement.Type type)
    {
        // prompt the user for the directory name
        String name = _editor.showSelectPathElementNameDialog(type);
        if (name == null) {
            return; // if the user hit cancel do no more
        }

        // the parent element is the directory or project the selected element is in, or if
        // a project or directory is selected, that is the parent element
        PathElement parentElement = getSelectedPathElement();
        int parentId = (parentElement.getType() == PathElement.Type.FILE) ?
            parentElement.getParentId() : parentElement.elementId;

        PathElement element = null;
        if (type == PathElement.Type.DIRECTORY) {
            element = PathElement.createDirectory(name, parentId);
        } else if (type == PathElement.Type.FILE) {
            element = new DocumentElement(name, parentId, "");
        } else {
            // other types not implemented
        }
        if (element != null) {
            _roomObj.service.addPathElement(_ctx.getClient(), element);
        }
    }

    /**
     * Removes a {@link PathElement} and broadcasts that fact to the server.
     */
    protected void deletePathElement ()
    {
        // TODO throw up a Are you sure yes/no dialog
        PathElement element = getSelectedPathElement();

        // XXX we know the tab was selected in order for delete to work. This might be dangerous.
        // we also know the tab was open.. hmmm
        if (element instanceof DocumentElement) {
            _editor.closeCurrentTab();
        } else if (element.getType() == PathElement.Type.DIRECTORY) {
            // TODO oh god we have to remove all the tabs associated with this directory
            // soo.. every tab that has a common getParentId() ?
        } else {
            // TODO you're trying to remove the project itself? Does Homey play that?
            return;
        }
        _roomObj.service.deletePathElement(_ctx.getClient(), element.elementId);
    }

    protected void setupToolbar ()
    {
        _plusButton = new JButton(createPlusButtonAction());
        _toolbar.add(_plusButton);

        _minusButton = new JButton(createMinusButtonAction());
        _toolbar.add(_minusButton);

        _addDirectoryButton = new JButton(createAddDirectoryAction());
        _toolbar.add(_addDirectoryButton);

        _toolbar.setFloatable(false);
        disableToolbar();
    }

    protected void enableToolbar ()
    {
        setToolbarEnabled(true);
    }

    protected void disableToolbar ()
    {
        setToolbarEnabled(false);
    }

    protected void setToolbarEnabled (boolean value)
    {
        _plusButton.setEnabled(value);
        _minusButton.setEnabled(value);
        _addDirectoryButton.setEnabled(value);
    }

    protected PathElementTreeNode getSelectedNode ()
    {
        return _selectedNode;
    }

    protected PathElement getSelectedPathElement ()
    {
        return _selectedNode == null ? null : (PathElement)_selectedNode.getUserObject();
    }

    protected void setSelectedNode (PathElementTreeNode node)
    {
        // if this is the first selection enable the buttons
        if (_selectedNode == null) {
            enableToolbar();
        }
        _selectedNode = node;
    }

    protected SwiftlyContext _ctx;
    protected SwiftlyEditor _editor;
    protected ProjectRoomObject _roomObj;
    protected ProjectTreeModel _treeModel;
    protected PathElementTreeNode _selectedNode;

    protected JTree _tree;
    protected JToolBar _toolbar = new JToolBar();
    protected JButton _plusButton;
    protected JButton _minusButton;
    protected JButton _addDirectoryButton;
    protected JScrollPane _scrollPane = new JScrollPane();
}
