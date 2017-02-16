/**
 * 
 */
package mkm.manager.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import mkm.connect.MkmConnector;
import mkm.data.Article;
import mkm.data.Rarity;
import mkm.events.MkmEventHandler;
import mkm.events.MkmEventHandlingState;
import mkm.events.MkmEventType;
import mkm.exception.CertRefreshNeededException;
import mkm.exception.EmptyResponseException;
import mkm.exception.ServerCommunicationException;
import mkm.exporter.MkmExporter;
import mkm.localization.Localization;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import util.GuiHelper;

/**
 * Class to represent the functionality of presenting the current user collection and the ability to edit it.
 * 
 * depends on {@link MkmManagerTableComponents}
 *
 * @author Kenny
 * @since 15.02.2016
 */
public class MkmManagerPanel extends JPanel implements mkm.events.IMkmEventConsumer
{
	private MkmExporter exporter;

	/**
	 * 
	 */
	private static final long serialVersionUID = "MkmManager_2016.02.15".hashCode();

	protected JTable dataTable;

	protected List<Article> modifiedArticles = new ArrayList<>();

	protected JTextField filterName = new JTextField(), filterEditon = new JTextField();

	protected JComboBox<String> filterRarity = new JComboBox<>(Rarity.getAllAbbreviations());

	protected final MkmConnector connector;

	private MkmManager parent;

	public MkmManagerPanel(final MkmManager parent, final String userToManage, final MkmConnector con) throws EmptyResponseException
	{
		this.parent = parent;
		this.connector = con;
		this.exporter = new MkmExporter(userToManage, con);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.initFilter();
		this.refreshDisplayedData(false);
		this.initButtons();
		this.initMenu();

		MkmEventHandler.addEventListener(this, MkmEventType.ArticleInserted);
	}

	/**
	 * Forces a refresh on the displayed data
	 * 
	 * @param con
	 *          the
	 * @throws EmptyResponseException
	 */
	private void refreshDisplayedData(final boolean ignoreCache) throws EmptyResponseException
	{
		try
		{
			Article[] insertedProducts = this.exporter.parseUserXml(this.exporter.getCollectionXmlForUser(ignoreCache));

			if (insertedProducts == null)
			{
				throw new EmptyResponseException("No products for the current user (connection is working).");
			}

			if (this.dataTable == null)
			{
				this.initTable(insertedProducts, this.connector);
			}
			else
			{
				this.dataTable.setModel(new MkmManagerTableModel(insertedProducts, this.connector));
			}
		}
		catch (CertRefreshNeededException ex)
		{
			throw new EmptyResponseException("Certificate not valid anymore, no Products found.", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mkm.events.IMkmEventConsumer#handleEvent(mkm.events.MkmEventType, java.lang.Object)
	 */
	@Override
	public MkmEventHandlingState handleEvent(final MkmEventType type, final Object source)
	{
		MkmEventHandlingState retVal = MkmEventHandlingState.unhandled;

		// merge insertedArticles into current list
		if (MkmEventType.ArticleInserted == type)
		{
			if (source == null)
			{
				LoggingHelper.logForLevel(LogLevel.Error, "ArticleInserted was called without an Article. (param == null)");
				retVal = MkmEventHandlingState.error;
			}
			else if (!(source instanceof Article))
			{
				LoggingHelper.logForLevel(LogLevel.Error, "ArticleInserted was called with something other than an Article: ", source.toString());
				retVal = MkmEventHandlingState.error;
			}
			else
			{
				// the posted article does not contain all information (eg. article id) so we have to re-read from server
				// TODO: delay the re-reading until we are actually finished with inserting
				try
				{
					this.refreshDisplayedData(true);
				}
				catch (EmptyResponseException e)
				{
					// this should not happen as it must have worked before
					LoggingHelper.logException(LogLevel.Error, e, "Certificate must have expired.");
				}
				retVal = MkmEventHandlingState.continue_handling;
			}
		}

		return retVal;
	}

	private void initMenu()
	{
		JMenuBar bar = parent.getFrame().getJMenuBar();
		if (bar == null)
		{
			bar = new JMenuBar();
			parent.getFrame().setJMenuBar(bar);
		}

		JMenu actionMenu = bar.getMenu(0);
		if (actionMenu == null)
		{
			actionMenu = new JMenu("Action");
			bar.add(actionMenu);
		}
		else
		{
			actionMenu.addSeparator();
		}

		JMenuItem rebuildMenu = new JMenuItem("Rebuild GUI");
		rebuildMenu.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					parent.rebuildGui();
				}
				catch (EmptyResponseException e1)
				{
					LoggingHelper.logException(LogLevel.Critical, e1, "Unable to retrieve products from MKM.");
				}
			}
		});

		actionMenu.add(rebuildMenu);
	}

	/**
	 * Create filters for edition, name, rarity
	 */
	private void initFilter()
	{
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));

		filterPanel.add(GuiHelper.createLabelAndComponentInPanel("Name:", this.filterName));
		filterPanel.add(GuiHelper.createLabelAndComponentInPanel("Edition:", this.filterEditon));
		filterPanel.add(GuiHelper.createLabelAndComponentInPanel("Rarity:", this.filterRarity));

		this.filterRarity.setSelectedItem(Rarity.None.getAbbreviation());

		this.filterRarity.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					((MkmManagerTableModel) getTable().getModel()).applyFilterRarity((String) e.getItem());
					getTable().repaint();
				}
			}
		});

		this.filterEditon.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				String text = ((JTextField) e.getComponent()).getText();
				((MkmManagerTableModel) getTable().getModel()).applyFilterEdition(text);
				getTable().repaint();
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}
		});
		this.filterName.addKeyListener(new KeyListener()
		{

			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				((MkmManagerTableModel) getTable().getModel()).applyFilterName(((JTextField) e.getSource()).getText());
				getTable().repaint();
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}
		});

		// filterPanel.add(this.filterEditon);

		this.add(filterPanel);
	}

	private void initTable(final Article[] insertedProducts, final MkmConnector con)
	{
		dataTable = new JTable(new MkmManagerTableModel(insertedProducts, con));
		MkmManagerTableCellRenderer renderer = new MkmManagerTableCellRenderer();
		dataTable.setDefaultRenderer(Double.class, renderer);
		dataTable.setDefaultRenderer(Integer.class, renderer);
		dataTable.setDefaultRenderer(String.class, renderer);

		// dataTable.setRowSorter(new MkmManagerTableRowSorter());

		dataTable.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					if (dataTable.getSelectedRow() >= 0)
					{
						((MkmManagerTableModel) dataTable.getModel()).markRowAsDeleted(dataTable.getSelectedRow());
					}
				}
			}
		});

		dataTable.getColumnModel().getColumn(0).setPreferredWidth(350); // name
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(250); // edition
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(50); // collector number
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(50); // quantity
		dataTable.getColumnModel().getColumn(4).setPreferredWidth(50); // price
		dataTable.getColumnModel().getColumn(5).setPreferredWidth(50); // average
		dataTable.getColumnModel().getColumn(6).setPreferredWidth(50); // diff to average

		JScrollPane toAdd = new JScrollPane(dataTable);
		toAdd.setLocation(0, 150);

		this.add(toAdd);
	}

	private void initButtons()
	{
		JButton synchronize = new JButton(Localization.getLocalizedString("Apply Changes"));
		JButton revert = new JButton(Localization.getLocalizedString("Discard Changes"));
		JButton refresh = new JButton(Localization.getLocalizedString("Refresh displayed items"));

		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));

		synchronize.setActionCommand(ButtonActionListener.COMMAND_SYNCH);
		revert.setActionCommand(ButtonActionListener.COMMAND_REVERT);
		refresh.setActionCommand(ButtonActionListener.COMMAND_REFRESH);

		buttonContainer.add(revert);
		buttonContainer.add(refresh);
		buttonContainer.add(synchronize);

		ButtonActionListener listener = new ButtonActionListener(dataTable, (MkmManagerTableModel) dataTable.getModel());
		synchronize.addActionListener(listener);
		refresh.addActionListener(listener);
		revert.addActionListener(listener);

		this.add(buttonContainer);

	}

	public JTable getTable()
	{
		return dataTable;
	}

}

/**
 * Action listener for the buttons of {@link MkmManagerPanel}
 *
 *
 * @author Kenny
 * @since 14.12.2016
 */
class ButtonActionListener implements ActionListener
{
	protected static final String COMMAND_SYNCH = "synch_with_mkm";

	protected static final String COMMAND_REVERT = "revert_to_read";

	protected static final String COMMAND_REFRESH = "refresh_displayed_data";

	private final MkmManagerTableModel model;

	private final JTable toRepaint;

	protected ButtonActionListener(final JTable toRepaint, final MkmManagerTableModel model)
	{
		this.toRepaint = toRepaint;
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if (COMMAND_SYNCH.equals(e.getActionCommand()))
		{
			try
			{
				model.applyChanges();
			}
			catch (ServerCommunicationException ex)
			{
				JOptionPane.showMessageDialog(this.toRepaint, "Error during server communication- See logs for details");
			}
		}
		else if (COMMAND_REVERT.equals(e.getActionCommand()))
		{
			model.revertChanges();
		}
		else if (COMMAND_REFRESH.equals(e.getActionCommand()))
		{
			model.refreshCardInfos();
		}

		toRepaint.repaint();
	}
}
