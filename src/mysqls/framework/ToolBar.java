/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016 by the contributors of the JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package mysqls.framework;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import mysqls.graph.ClassNode;
import mysqls.graph.Edge;
import mysqls.graph.Graph;
import mysqls.graph.GraphElement;
import mysqls.graph.Node;

/**
 *  A collapsible tool bar than contains various tools and optional
 *  command shortcut buttons. Only one tool can be selected at the time.
 *  The tool bar also controls a pop-up menu with the same tools as 
 *  the tool bar.
 *  
 *  @author Martin P. Robillard
 */
@SuppressWarnings("serial")
public class ToolBar extends JPanel
{
	private static final int BUTTON_SIZE = 25;
	private static final int OFFSET = 3;
	private static final int H_PADDING = 5;
	private static final int FONT_SIZE = 14;
	private static final String EXPAND = "<<";
	private static final String COLLAPSE = ">>";
	
	private ArrayList<JToggleButton> aButtons = new ArrayList<>();
	private ArrayList<JToggleButton> aButtonsEx = new ArrayList<>();
	private JPanel aToolPanel = new JPanel(new VerticalLayout());
	private JPanel aToolPanelEx = new JPanel(new VerticalLayout());
	private ArrayList<GraphElement> aTools = new ArrayList<>();
	private JPopupMenu aPopupMenu = new JPopupMenu();

	/**
     * Constructs the tool bar.
     * @param pGraph The graph associated with this tool bar.
	 */
	public ToolBar(Graph pGraph)
	{
		ButtonGroup group = new ButtonGroup();
		ButtonGroup groupEx = new ButtonGroup();
		setLayout(new BorderLayout());
		createSelectionTool(group, groupEx);
		createNodesAndEdgesTools(pGraph, group, groupEx);
		addCopyToClipboard();
		createExpandButton();
		freeCtrlTab();
		add(aToolPanel, BorderLayout.CENTER);
	}
	
	private static Icon createSelectionIcon()
	{
		return new Icon()
		{
			public int getIconHeight() 
			{ return BUTTON_SIZE; }
            
			public int getIconWidth() 
			{ return BUTTON_SIZE; }
            
			public void paintIcon(Component pComponent, Graphics pGraphics, int pX, int pY)
            {
				int offset = OFFSET+3;
				Graphics2D g2 = (Graphics2D)pGraphics;
				GraphPanel.drawGrabber(g2, pX + offset, pY + offset);
				GraphPanel.drawGrabber(g2, pX + offset, pY + BUTTON_SIZE - offset);
				GraphPanel.drawGrabber(g2, pX + BUTTON_SIZE - offset, pY + offset);
				GraphPanel.drawGrabber(g2, pX + BUTTON_SIZE - offset, pY + BUTTON_SIZE - offset);
            }
		};
	}
	
	private static Icon createNodeIcon( final Node pNode )
	{
		return new Icon()
		{
            public int getIconHeight() 
            { return BUTTON_SIZE; }
            
            public int getIconWidth() 
            { return BUTTON_SIZE; }
            
            public void paintIcon(Component pComponent, Graphics pGraphic, int pX, int pY)
            {
            	double width = pNode.getBounds().getWidth();
            	double height = pNode.getBounds().getHeight();
               	Graphics2D g2 = (Graphics2D)pGraphic;
               	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               	double scaleX = (BUTTON_SIZE - OFFSET)/ width;
               	double scaleY = (BUTTON_SIZE - OFFSET)/ height;
               	double scale = Math.min(scaleX, scaleY);

               	AffineTransform oldTransform = g2.getTransform();
               	g2.translate(pX, pY);
               	g2.scale(scale, scale);
               	
               	g2.translate(Math.max((height - width) / 2, 0), Math.max((width - height) / 2, 0));
               	g2.setColor(Color.black);
               	pNode.draw(g2);
               	g2.setTransform(oldTransform);
            }
		};
	}

	private static Icon createEdgeIcon( final Edge pEdge )
	{
		return new Icon()
        {
           public int getIconHeight() 
           { return BUTTON_SIZE; }
           
           public int getIconWidth() 
           { return BUTTON_SIZE; }
           
           public void paintIcon(Component pComponent, Graphics pGraphics, int pX, int pY)
           {
           	Graphics2D g2 = (Graphics2D)pGraphics;
           	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
           	
           	ClassNode p = new ClassNode();
           	p.translate(OFFSET, OFFSET);
           	ClassNode q = new ClassNode();
           	q.translate(BUTTON_SIZE - OFFSET, BUTTON_SIZE - OFFSET);
           	pEdge.connect(p, q);
              
           	Rectangle2D bounds = new Rectangle2D.Double();
           	bounds.add(p.getBounds());
           	bounds.add(q.getBounds());
           	bounds.add(pEdge.getBounds());
              
           	double width = bounds.getWidth();
           	double height = bounds.getHeight();
           	double scaleX = (BUTTON_SIZE - OFFSET)/ width;
           	double scaleY = (BUTTON_SIZE - OFFSET)/ height;
           	double scale = Math.min(scaleX, scaleY);

           	AffineTransform oldTransform = g2.getTransform();
           	g2.translate(pX, pY);
           	g2.scale(scale, scale);
           	g2.translate(Math.max((height - width) / 2, 0), Math.max((width - height) / 2, 0));
                             
           	g2.setColor(Color.black);
           	pEdge.draw(g2);
           	g2.setTransform(oldTransform);
           }
        };
	}
	
	private void createSelectionTool(ButtonGroup pGroup, ButtonGroup pGroupEx)
	{
		installTool(createSelectionIcon(), 
				ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings").getString("grabber.tooltip"), 
				null, true, pGroup, pGroupEx);
	}
	
	/*
	 * Adds a tool to the tool bars and menus.
	 * @param pIcon The icon for the tool
	 * @param pToolTip the tool's tool tip
	 * @param pTool the object representing the tool
	 * @param pIsSelected true if the tool is initially selected.
	 */
	private void installTool( Icon pIcon, String pToolTip, GraphElement pTool, boolean pIsSelected, ButtonGroup pCollapsed, ButtonGroup pExpanded )
	{
		final JToggleButton button = new JToggleButton(pIcon);
		button.setToolTipText(pToolTip);
		pCollapsed.add(button);
		aButtons.add(button);
		aToolPanel.add(button);
		button.setSelected(pIsSelected);
		aTools.add(pTool);
		
		final JToggleButton buttonEx = new JToggleButton(pIcon);
		buttonEx.setToolTipText(pToolTip);
		pExpanded.add(buttonEx);
		aButtonsEx.add(buttonEx);
		
		aToolPanelEx.add(createExpandedRowElement(buttonEx, pToolTip));
		buttonEx.setSelected(pIsSelected);
      
		JMenuItem item = new JMenuItem(pToolTip, pIcon);
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent pEvent)
			{
				button.setSelected(true);
				buttonEx.setSelected(true);
			}
		});
		aPopupMenu.add(item);
	}
	
	/*
	 * Return a panel with a button on the left and a label on the right
	 */
	private JPanel createExpandedRowElement(JComponent pButton, String pToolTip)
	{
		JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		linePanel.add(pButton);
		JLabel label = new JLabel(pToolTip);
		Font font = new Font(label.getFont().getFontName(), Font.PLAIN, FONT_SIZE);
		label.setFont(font);
		label.setBorder(BorderFactory.createEmptyBorder(0, H_PADDING, 0, H_PADDING));
		linePanel.add(label);
		return linePanel;
	}
	
	private void createNodesAndEdgesTools(Graph pGraph, ButtonGroup pGroup, ButtonGroup pGroupEx)
	{
		ResourceBundle resources = ResourceBundle.getBundle(pGraph.getClass().getName() + "Strings");

		Node[] nodeTypes = pGraph.getNodePrototypes();
		for(int i = 0; i < nodeTypes.length; i++)
		{
			installTool(createNodeIcon(nodeTypes[i]), resources.getString("node" + (i + 1) + ".tooltip"), 
					nodeTypes[i], false, pGroup, pGroupEx);
		}
		
		Edge[] edgeTypes = pGraph.getEdgePrototypes();
		for(int i = 0; i < edgeTypes.length; i++)
		{
			installTool(createEdgeIcon(edgeTypes[i]), resources.getString("edge" + (i + 1) + ".tooltip"), 
					edgeTypes[i], false, pGroup, pGroupEx);
		}
	}
	
	/*
	 * Free up ctrl TAB for cycling windows
	 */
	private void freeCtrlTab()
	{
		Set<AWTKeyStroke> oldKeys = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
		HashSet<AWTKeyStroke> newKeys = new HashSet<>();
		newKeys.addAll(oldKeys);
		newKeys.remove(KeyStroke.getKeyStroke("ctrl TAB"));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newKeys);
		oldKeys = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
		newKeys = new HashSet<>();
		newKeys.addAll(oldKeys);
		newKeys.remove(KeyStroke.getKeyStroke("ctrl shift TAB"));
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newKeys); 
	}
	
	/**
     * Gets the node or edge prototype that is associated with
     * the currently selected button.
     * @return a Node or Edge prototype
	 */
	public GraphElement getSelectedTool()
	{
		return aTools.get(getSelectedButtonIndex());
	}
	
	/**
	 * Overrides the currently selected tool to be the grabber tool instead.
	 */
	public void setToolToBeSelect()
	{
		for( JToggleButton button : aButtons )
		{
			button.setSelected(false);
		}
		for( JToggleButton button : aButtonsEx )
		{
			button.setSelected(false);
		}
		aButtons.get(0).setSelected(true);
		aButtonsEx.get(0).setSelected(true);
	}

	private void addCopyToClipboard()
	{
		URL imageLocation = getClass().getClassLoader().
				getResource(ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings").
						getString("toolbar.copyToClipBoard"));
		String toolTip = ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings").getString("file.copy_to_clipboard.text");
		
		final JButton button = new JButton(new ImageIcon(imageLocation));
		button.setToolTipText(toolTip);
		if( aButtons.size() > 0 )
		{
			button.setPreferredSize(aButtons.get(0).getPreferredSize());
		}
		aToolPanel.add(button);

		
		final JButton buttonEx = new JButton(new ImageIcon(imageLocation));
		buttonEx.setToolTipText(toolTip);
		aToolPanelEx.add(createExpandedRowElement(buttonEx, toolTip));
		
		if( aButtons.size() > 0 )
		{
			button.setPreferredSize(aButtons.get(0).getPreferredSize());
			buttonEx.setPreferredSize(aButtons.get(0).getPreferredSize());
		}

		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent pEvent)
			{
				copyToClipboard();
			}
		});

		buttonEx.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent pEvent)
			{
				copyToClipboard();
			}
		});
	}
	
	private void copyToClipboard()
	{
		// Obtain the editor frame by going through the component graph
		Container parent = getParent();
		while( parent.getClass() != EditorFrame.class )
		{
			parent = parent.getParent();
		}
		((EditorFrame)parent).copyToClipboard();
	}
		
	private void createExpandButton()
	{
		final JButton expandButton = new JButton(EXPAND);
		expandButton.setAlignmentX(CENTER_ALIGNMENT);
		final String expandString = ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings").getString("toolbar.expand");
		final String collapseString = ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings").getString("toolbar.collapse");
		expandButton.setToolTipText(expandString);
		expandButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		expandButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent pEvent)
			{
				if(expandButton.getText().equals(EXPAND))
				{
					synchronizeToolSelection();
					expandButton.setText(COLLAPSE);
					expandButton.setToolTipText(collapseString);
					aToolPanelEx.setBounds(aToolPanel.getBounds());
					remove(aToolPanel);
					add(aToolPanelEx, BorderLayout.CENTER);
				}
				else
				{
					synchronizeToolSelection();
					expandButton.setText(EXPAND);
					expandButton.setToolTipText(expandString);
					aToolPanel.setBounds(aToolPanelEx.getBounds());
					remove(aToolPanelEx);
					add(aToolPanel, BorderLayout.CENTER);
				}
			}
		});
		add(expandButton, BorderLayout.SOUTH);
	}
	
	private void synchronizeToolSelection()
	{
		int index = getSelectedButtonIndex();
		assert index >= 0;
		aButtons.get(index).setSelected(true);
		aButtonsEx.get(index).setSelected(true);
	}
	
	private int getSelectedButtonIndex()
	{
		ArrayList<JToggleButton> activeButtons = aButtons;
		if( isExpanded() )
		{
			activeButtons = aButtonsEx;
		}
		
		for(int i = 0; i < activeButtons.size(); i++)
		{
			JToggleButton button = activeButtons.get(i);
			if(button.isSelected())
			{
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * The toolbar is expanded iff the main panel contains
	 * the expanded toolbar as one of its components.
	 */
	private boolean isExpanded()
	{
		for( Component component : getComponents() )
		{
			if( component == aToolPanelEx )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Show the pop-up menu corresponding to this toolbar.
	 * @param pPanel The panel associated with this menu.
	 * @param pPoint The point where to show the menu.
	 */
	public void showPopup(GraphPanel pPanel, Point2D pPoint) 
	{
		aPopupMenu.show(pPanel, (int) pPoint.getX(), (int) pPoint.getY());
	}
}