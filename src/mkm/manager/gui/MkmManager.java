/**
 * 
 */
package mkm.manager.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.exception.EmptyResponseException;
import mkm.inserter.gui.MkmInserterMain;
import mkm.manager.gui.obj.Rebuildable;

/**
 *
 *
 * @author Kenny
 * @since 16.02.2016
 */
public class MkmManager implements Rebuildable
{

	protected JFrame mainFrame = null;

	protected JTabbedPane tabs = null;

	protected MkmManagerPanel managerPanel;

	protected MkmInserterMain inserterPanel;

	protected boolean buildManager;

	protected boolean buildInserter;

	protected MkmConnector con;

	/**
	 * Constructs a new MkmManager GUI
	 * 
	 * @throws EmptyResponseException
	 *           if no product has been found the creation is meaningless
	 */
	public MkmManager(final boolean includeManager, final boolean includeInserter, final MkmConnector connector) throws EmptyResponseException
	{
		this.buildInserter = includeInserter;
		this.buildManager = includeManager;
		this.con = connector;

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.rebuildGui();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mkm.manager.gui.Rebuildable#rebuildGui()
	 */
	@Override
	public void rebuildGui() throws EmptyResponseException
	{
		Point currentLocation = new Point(0, 0);
		Dimension currentDimension = new Dimension(1024, 750);
		if (this.mainFrame != null)
		{
			currentLocation = this.mainFrame.getLocation();
			currentDimension = this.mainFrame.getSize();
			System.out.println("Saved Dimension: " + currentDimension);

			this.mainFrame.dispose();
		}

		mainFrame = new JFrame("MkmManagerTest");
		mainFrame.setSize(currentDimension);
		mainFrame.setLocation(currentLocation);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentShown(final ComponentEvent e)
			{
			}

			@Override
			public void componentResized(final ComponentEvent e)
			{
				adjustComponentSizes();
			}

			@Override
			public void componentMoved(final ComponentEvent e)
			{
			}

			@Override
			public void componentHidden(final ComponentEvent e)
			{
			}
		});
		this.tabs = new JTabbedPane();
		this.mainFrame.add(this.tabs);

		initManagerPanel();
		initInserterPanel();

		mainFrame.setVisible(true);

		adjustComponentSizes();

	}

	/**
	 * Creates and adds the manager tab
	 * 
	 * @throws EmptyResponseException
	 */
	protected void initManagerPanel() throws EmptyResponseException
	{
		if (this.buildManager)
		{
			this.managerPanel = new MkmManagerPanel(this, MkmConfig.getConfig("mkmUserName"), this.con);
			this.tabs.addTab("MkmManager", this.managerPanel);
		}
	}

	/**
	 * Creates and adds the inserter Tab
	 */
	protected void initInserterPanel()
	{
		if (this.buildInserter)
		{
			this.inserterPanel = new MkmInserterMain(false, this.con, this.mainFrame.getJMenuBar(), false);
			this.tabs.addTab("MkmInserter", this.inserterPanel);
		}
	}

	public void adjustComponentSizes()
	{
		if (this.managerPanel != null)
		{
			this.managerPanel.setSize(mainFrame.getSize());

			this.managerPanel.getTable().setPreferredScrollableViewportSize(new Dimension(mainFrame.getWidth() - 25, mainFrame.getHeight() - 250));
			this.managerPanel.getTable().setFillsViewportHeight(true);
		}
	}

	public JFrame getFrame()
	{
		return this.mainFrame;
	}

}
