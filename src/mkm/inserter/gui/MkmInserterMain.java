/**
 * 
 */
package mkm.inserter.gui;

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mkm.XmlConstants;
import mkm.cache.MkmCache;
import mkm.cache.builder.MkmCacheBuilder;
import mkm.config.MkmConfig;
import mkm.connect.MkmConnector;
import mkm.connect.MkmServiceHandler;
import mkm.data.Article;
import mkm.data.Condition;
import mkm.data.MtgCard;
import mkm.data.Rarity;
import mkm.data.multilingual.Language;
import mkm.events.MkmEventHandler;
import mkm.events.MkmEventType;
import mkm.exporter.MkmExporter;
import mkm.inserter.MkmInserter;
import mkm.inserter.gui.component.DoubleField;
import mkm.inserter.gui.component.IntegerField;
import mkm.inserter.gui.component.PriceField;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import util.GuiHelper;

/**
 *
 *
 * @author Kenny
 * @since 08.01.2016
 */
public class MkmInserterMain extends JPanel
{

	public static final String CARD_ID_SEPERATOR = "; ";

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "MkmInserterMain".hashCode();

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public MkmInserterMain(final boolean verbouse, final MkmConnector con, final JMenuBar menu, final boolean isStandalone) throws HeadlessException
	{
		this(verbouse, con, new MkmInserter(con), menu, isStandalone);
	}

	public MkmInserterMain(final boolean verbouse, final MkmConnector con, final MkmInserter inserter, final JMenuBar menu, final boolean isStandalone)
	{
		this.inserter = inserter;
		String timeLogInfo = null;
		if (this.inserter.nameBufferNeedsRebuild())
		{
			long time = System.currentTimeMillis();
			int createdItems = this.inserter.buildCardBuffers();
			timeLogInfo = LoggingHelper.concat("Needed: ", Long.toString(System.currentTimeMillis() - time), "ms for building the cache. Contains ",
					Integer.toString(this.inserter.getCardBuffer().size()), " entries, constructed from: ", Integer.toString(createdItems), " xml files.");
		}

		LogLevel previous = LoggingHelper.SYSTEM_LEVEL;
		if (verbouse)
			LoggingHelper.SYSTEM_LEVEL = LogLevel.Info;

		if (timeLogInfo != null)
		{
			LoggingHelper.logForLevel(LogLevel.None, timeLogInfo);
		}

		this.setup(menu, isStandalone);

		LoggingHelper.SYSTEM_LEVEL = previous;
	}

	public static JFrame createMkmInserterFrame(final boolean verbouse, final MkmConnector con, final MkmInserter inserter)
	{
		return createMkmInserterFrame("MKM-Inserter", verbouse, con, inserter);
	}

	public static JFrame createMkmInserterFrame(final String title, final boolean verbouse, final MkmConnector con, final MkmInserter inserter)
	{
		mainFrame = new JFrame(title);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar mainBar = new JMenuBar();
		mainFrame.setJMenuBar(mainBar);

		MkmInserterMain main = new MkmInserterMain(verbouse, con, inserter, mainBar, true);
		mainFrame.add(main);

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		mainFrame.setSize(1024, 750);
		mainFrame.setVisible(true);

		return mainFrame;
	}

	protected static JFrame mainFrame = null;

	protected MkmInserter inserter;

	protected JList<MtgCard> cardList;

	protected JTextField nameField = new JTextField();

	// info controls
	protected JTextField infoCardName = new JTextField();

	protected JTextField infoEdition = new JTextField();

	protected IntegerField infoCollectorsNumber = new IntegerField();

	protected JPanel infoImagePanel = new JPanel();

	protected JComboBox<String> infoRarity = new JComboBox<>(Rarity.getAllAbbreviations());

	protected PriceField trend = new PriceField(), avg = new PriceField(), minExBetter = new PriceField();

	// submit controls
	protected PriceField sellPrice = new PriceField();

	protected IntegerField sellQuantity = new IntegerField(1);

	protected JTextField sellItemId = new JTextField();

	protected JCheckBox sellFoil = new JCheckBox(), sellSigned = new JCheckBox();

	protected JList<String> sellLanguages = new JList<>(Language.getAllLanguages());

	protected JList<String> sellConditions = new JList<>(Condition.getAllConditions());

	protected JButton upload = null;

	protected FocusListener selectAllFocusListener = new FocusListener()
	{

		@Override
		public void focusLost(final FocusEvent e)
		{
		}

		@Override
		public void focusGained(final FocusEvent e)
		{
			if (e.getSource() instanceof PriceField)
			{
				// select only the nubmer, not the currency sign
				PriceField tmp = (PriceField) e.getSource();
				String doubleText = DoubleField.FORMAT.format(tmp.getValue());
				if (tmp.getText() != null && doubleText != null)
				{
					int start = tmp.getText().indexOf(doubleText);
					if (start > -1)
					{
						tmp.select(start, start + doubleText.length());
						return;
					}
				}
			}
			// if either not a currenyField or the formatting failed, select all
			if (e.getSource() instanceof JTextField)
			{
				((JTextField) e.getSource()).selectAll();
				;
			}
		}
	};

	// menu control
	JTextField menuExportName = new JTextField(MkmConfig.getConfig("mkmUserToExtractCollectionFor"));

	/**
	 * Sets up the GUI
	 */
	public void setup(final JMenuBar frameMenuBar, final boolean isStandalone)
	{
		this.setLayout(null);

		if (frameMenuBar != null)
		{
			initMenu(frameMenuBar, isStandalone);
		}

		initSearchField();
		initResultList();
		initCardInfos();
		initSubmitControls();

	}

	private void initMenu(final JMenuBar bar, final boolean isStandalone)
	{
		JMenu exportMenu = getOrCreateMenu("Export", bar);
		exportMenu.add(new JLabel("User for Export: "));
		exportMenu.add(menuExportName);
		menuExportName.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					startExport();
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}
		});
		JMenuItem startExport = new JMenuItem("Start Export");
		exportMenu.addSeparator();
		exportMenu.add(startExport);

		startExport.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				startExport();
			}
		});

		JMenu actionMenu = getOrCreateMenu("Action", bar);

		JMenuItem startCachebuilder = new JMenuItem("Start cacheBuilder");
		startCachebuilder.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				startCacheBuilder();
			}
		});
		actionMenu.add(startCachebuilder);

		if (isStandalone)
		{
			JMenuItem rebuildGui = new JMenuItem("Rebuild GUI");
			rebuildGui.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					restartGui();
				}

			});
			actionMenu.add(rebuildGui);
		}

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				System.out.println("Bye Bye");
				System.exit(0);
			}
		});
		actionMenu.addSeparator();
		actionMenu.add(exit);

		JMenu helpMenu = getOrCreateMenu("Help", bar);
		JMenuItem showHelp = new JMenuItem("Show Help");
		showHelp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				showHelpDialogue();
			}
		});
		helpMenu.add(showHelp);

		JMenuItem showInfo = new JMenuItem("Info");
		showInfo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				showInfoDialogue();
			}
		});
		helpMenu.add(showInfo);

		if (MkmConnector.IS_SANDBOX_MODE)
		{
			JMenu sandboxMenu = new JMenu(" -- USING SANDBOX SERVER! -- ");
			sandboxMenu.setEnabled(false);
			bar.add(sandboxMenu);
		}

		// this.setJMenuBar(bar);
	}

	private JMenu getOrCreateMenu(final String name, final JMenuBar bar)
	{
		if (name == null)
			return null;

		for (int i = 0; i < bar.getMenuCount(); i++)
		{
			if (name.equals(bar.getMenu(i).getText()))
			{
				return bar.getMenu(i);
			}
		}

		JMenu retVal = new JMenu(name);
		bar.add(retVal);
		return retVal;
	}

	/**
	 * Starts {@link MkmExporter#exportToCvs()}
	 */
	protected void startExport()
	{
		StringBuilder message = new StringBuilder("CVS Export finished.");
		try
		{
			File writtenFile = new MkmExporter(menuExportName.getText(), inserter.getConnector()).exportToCvs();

			if (writtenFile != null)
			{
				message.append("\nWritten to file: ");
				message.append(writtenFile.getAbsolutePath());
				// write to console for copy + paste
				LoggingHelper.info("Result written to: ", writtenFile.getAbsolutePath());
			}
			else
			{
				message.append(" But without any results written.");
			}
		}
		catch (Exception e)
		{
			message.append("\nUnable to export. An Exception occured: ").append(e.getMessage());
		}

		JOptionPane.showMessageDialog(this, message.toString(), "Export finished", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Starts {@link MkmCacheBuilder#buildCardCache(boolean)}
	 */
	protected void startCacheBuilder()
	{
		int entriesToAdd = Integer.parseInt(MkmConnector.mkm_config.getString("cache_entries_to_add"));
		new MkmCacheBuilder(this.inserter.getConnector(), entriesToAdd).buildCardCache(true);
		JOptionPane.showMessageDialog(this, "Added " + entriesToAdd + " cache entries.", "Cache building finished", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showHelpDialogue()
	{
		JOptionPane.showMessageDialog(this,
				LoggingHelper.concat(
						"Type the name of the card into the search field. If the card is not found in the cache: press Enter to contact the server with the exact card name. The card list will be updated\n",
						"Once you found your card, select it in the list and fill out the missing information in the sell card section and press the 'Upload to server' button.\n",
						"If you type two words (seperated with a space) all cards will be searched which contain both words. (Only works for local cache, not for the server)"),
				"Help", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showInfoDialogue()
	{
		JOptionPane.showMessageDialog(this, LoggingHelper.concat("MkmHelper\n", "A tool to speed up your work while selling on MKM\n", "Author: Kenny Krainer\n",
				"Version: ", MkmConnector.VERSION, "\nVersion Info: ", XmlConstants.getVersionHistory()), "Info", JOptionPane.INFORMATION_MESSAGE);
	}

	private void restartGui()
	{
		if (mainFrame != null)
		{
			mainFrame.getContentPane().remove(0);
			mainFrame.add(new MkmInserterMain(false, this.inserter.getConnector(), this.inserter, null, true));
			mainFrame.repaint();
		}
	}

	/**
	 * Creates and inits the label and name for the search field
	 */
	private void initSearchField()
	{
		JPanel nameContainer = createDisabledLabelAndComponentInPanel("CardName: ", nameField);
		nameField.setEnabled(true);
		nameField.setToolTipText("Press enter to contact server.");
		// search field
		nameField.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				// perform search + updates on enter
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					LoggingHelper.logException(LogLevel.Info, "Updating ", nameField.getText());

					performSearchForName(nameField.getText());
				}
				else
				{
					String text = ((JTextField) e.getSource()).getText();

					if (text.length() > 2)
					{
						filterCardList(text);
					}
				}
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}
		});
		nameField.addFocusListener(selectAllFocusListener);

		this.add(nameContainer);
		nameContainer.setBounds(0, 40, 250, 30);

		JLabel searchLabel = new JLabel("Card Search: ");
		searchLabel.setFont(new Font(searchLabel.getFont().getName(), Font.BOLD, 18));
		this.add(searchLabel);
		searchLabel.setBounds(0, 0, 250, 30);

	}

	/**
	 * Creates and inits the {@link JList} responsible for holding the search info
	 */
	private void initResultList()
	{
		MtgCard dummyCard = null;
		try
		{
			String dummyParams = MkmConnector.getPersonalizedParams(MkmConnector.GET_PRODUCT_FOR_ID, new String[] { ":id" }, new String[] { "1" });
			dummyCard = new MtgCard(MkmCache.getCacheContent(dummyParams), false, 1);
		}
		catch (Exception e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Error during reading dummy card");
		}
		// search result list
		cardList = new JList<>(new MtgCard[] { dummyCard == null ? new MtgCard(-1, "edition", "CardName", 11111, Rarity.None) : dummyCard });

		cardList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		cardList.addListSelectionListener(new ListSelectionListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(final ListSelectionEvent e)
			{
				// this method is called twice per selection, only one has getValueIsAdjusting
				if (e.getValueIsAdjusting())
				{
					List<MtgCard> selectedList = ((JList<MtgCard>) e.getSource()).getSelectedValuesList();
					if (selectedList != null && selectedList.size() > 0)
					{
						MtgCard selected = selectedList.get(0);
						// System.out.println("Selected: " + selected);

						selected = refreshCard(selected);

						updateCardInfos(selected);
					}
					updateSellFields(selectedList);
				}
			}
		});

		// wrap into scroll pane
		JScrollPane listWrapper = new JScrollPane(cardList);

		this.add(listWrapper);
		listWrapper.setBounds(265, 0, 400, 680);
	}

	/**
	 * Refreshes the given card, rebuilds the card buffer
	 * 
	 * @param toRefresh
	 *          the card to be reloaded from the server
	 * @return the refreshed card
	 */
	public MtgCard refreshCard(final MtgCard toRefresh)
	{
		MtgCard refreshedCard = MkmCache.refreshCacheContent(toRefresh, this.inserter.getConnector());
		this.inserter.addMtgCardToBuffer(refreshedCard);
		this.inserter.rebuildNameBuffer();

		// reset the name and language in order to select them probably
		refreshedCard.setName(toRefresh.getName());
		refreshedCard.setLanguage(toRefresh.getLanguage());

		return refreshedCard;
	}

	private void initCardInfos()
	{
		JPanel name = createDisabledLabelAndComponentInPanel("Card Name: ", this.infoCardName);
		JPanel edition = createDisabledLabelAndComponentInPanel("Edition: ", this.infoEdition);
		JPanel rarity = createDisabledLabelAndComponentInPanel("Rarity: ", this.infoRarity);
		JPanel collectorsNo = createDisabledLabelAndComponentInPanel("Collectors Number: ", this.infoCollectorsNumber);

		int height = 130;

		JLabel infoLabel = new JLabel("Card Info: ");
		infoLabel.setFont(new Font(infoLabel.getFont().getName(), Font.BOLD, 18));
		this.add(infoLabel);
		infoLabel.setBounds(0, 90, 250, 30);

		this.add(name);
		name.setBounds(0, height, 250, 30);
		height += 35;

		this.add(rarity);
		rarity.setBounds(0, height, 250, 30);
		height += 35;

		this.add(edition);
		edition.setBounds(0, height, 250, 30);
		height += 35;

		this.add(collectorsNo);
		collectorsNo.setBounds(0, height, 250, 30);
		height += 35;

		FocusListener takeOverPrice = new FocusListener()
		{
			@Override
			public void focusLost(final FocusEvent e)
			{
			}

			@Override
			public void focusGained(final FocusEvent e)
			{
				sellPrice.setText(((PriceField) e.getSource()).getText());
			}
		};
		this.trend.addFocusListener(takeOverPrice);
		this.avg.addFocusListener(takeOverPrice);
		this.minExBetter.addFocusListener(takeOverPrice);

		// price info panel consists of min, avg and max
		JPanel minimum = createLabelAndComponentInPanel("Trend: ", this.trend, 42, 80, 30);
		JPanel average = createLabelAndComponentInPanel("Avg: ", this.avg, 42, 80, 30);
		JPanel maximum = createLabelAndComponentInPanel("LowEx+: ", this.minExBetter, 42, 80, 30);

		JPanel pricePanel = new JPanel();
		pricePanel.setLayout(null);

		pricePanel.add(minimum);
		minimum.setBounds(0, 0, 80, 30);

		pricePanel.add(average);
		average.setBounds(85, 0, 80, 30);

		pricePanel.add(maximum);
		maximum.setBounds(170, 0, 80, 30);

		this.add(pricePanel);
		pricePanel.setBounds(0, height, 250, 30);
		height += 40;

		// card picture
		this.add(infoImagePanel);
		this.infoImagePanel.setBounds(0, height, 250, 600);
		height += 500;

	}

	private void initSubmitControls()
	{
		JPanel idPanel = createLabelAndComponentInPanel("CardId: ", this.sellItemId);
		JPanel foilPanel = createLabelAndComponentInPanel("Foil : ", this.sellFoil);
		JPanel signedPanel = createLabelAndComponentInPanel("Signed : ", this.sellSigned);
		JPanel languages = createLabelAndComponentInPanel("Language : ", new JScrollPane(this.sellLanguages), 150, 250, 120);
		JPanel conditions = createLabelAndComponentInPanel("Condtion : ", new JScrollPane(this.sellConditions), 150, 250, 130);
		JPanel quantityPanel = createLabelAndComponentInPanel("Quantity : ", this.sellQuantity);
		this.sellQuantity.addFocusListener(selectAllFocusListener);
		JPanel pricePanel = createLabelAndComponentInPanel("Price : ", this.sellPrice);
		this.sellPrice.setEnabled(true);
		this.sellPrice.addKeyListener(new KeyListener()
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
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					performPostArticle();
				}
			}
		});
		sellPrice.addFocusListener(selectAllFocusListener);

		int currentY = 10;
		int currentX = 700;

		JLabel sellLabel = new JLabel("Selling Card: ");
		sellLabel.setFont(new Font(sellLabel.getFont().getName(), Font.BOLD, 18));
		this.add(sellLabel);
		sellLabel.setBounds(currentX, currentY, 250, 30);
		currentY += 50;

		this.add(idPanel);
		idPanel.setBounds(currentX, currentY, 250, 30);
		currentY += 35;

		this.add(foilPanel);
		foilPanel.setBounds(currentX, currentY, 250, 30);
		currentY += 35;

		this.add(signedPanel);
		signedPanel.setBounds(currentX, currentY, 250, 30);
		currentY += 35;

		this.add(languages);
		languages.setBounds(currentX, currentY, 250, 120);
		currentY += 125;

		this.add(conditions);
		conditions.setBounds(currentX, currentY, 250, 130);
		currentY += 135;

		this.add(quantityPanel);
		quantityPanel.setBounds(currentX, currentY, 250, 30);
		currentY += 30;

		this.add(pricePanel);
		pricePanel.setBounds(currentX, currentY, 250, 30);
		currentY += 35;

		// set default values
		this.sellLanguages.setSelectedValue(Language.English.getLanguageName(), true);
		this.sellConditions.setSelectedValue(Condition.NearMint.getAbbreviation(), true);
		this.sellQuantity.setText("1");

		this.sellQuantity.addKeyListener(new KeyListener()
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
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							sellPrice.requestFocus();
						}
					});
				}
			}
		});

		upload = new JButton("Upload to server");
		upload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				performPostArticle();
			}
		});

		upload.addKeyListener(new KeyListener()
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
				performPostArticle();
			}
		});

		this.add(upload);
		upload.setBounds(currentX + 50, currentY + 10, 125, 30);
		currentY += 40;
	}

	private static boolean isUploadRunning = false;

	private static long lastRunStart = 0;

	/**
	 * Collects entered data, builds an {@link Article} and posts it to the server
	 */
	public synchronized void performPostArticle()
	{
		if (isUploadRunning)
		{
			isUploadRunning = true;
			lastRunStart = System.currentTimeMillis();
		}
		else if (System.currentTimeMillis() - lastRunStart < 1500)
		{
			showError("Not processing upload spamming.");
			return;
		}
		else
		{
			// fallback for strange cases where the user spams the upload button and the isUploadRunning is set to true during another threads-run
			// basically never happens but needed to avoid dead application
			lastRunStart = System.currentTimeMillis();
		}

		try
		{
			String[] selectedIds = sellItemId.getText().split(CARD_ID_SEPERATOR);
			for (String id : selectedIds)
			{
				// int itemID = getIntFromTextField(sellItemId);
				int itemID;
				try
				{
					itemID = Integer.parseInt(id);
				}
				catch (NumberFormatException ex)
				{
					LoggingHelper.logException(LogLevel.Critical, "Invalid integer: ", id);
					continue;
				}

				Article toSell = new Article();

				toSell.setProductId(itemID);

				toSell.setLanguageId(Language.getLanguageForName(sellLanguages.getSelectedValue()).getIntValue());
				toSell.setAmount(getIntFromTextField(sellQuantity));
				toSell.setPrice(sellPrice.getValue());
				toSell.setCondition(Condition.getConditionForAbbreviation(sellConditions.getSelectedValue()));
				toSell.setFoil(sellFoil.isSelected());
				toSell.setSigned(sellSigned.isSelected());

				StringBuilder errorMsg = new StringBuilder();
				if (toSell.getProductId() < 1)
				{
					errorMsg.append("Invalid product Id: '").append(Integer.toString(toSell.getProductId())).append("'. It must be a positive integer!\n");
				}
				if (toSell.getAmount() < 1)
				{
					errorMsg.append("You cannot insert 0 items, you must at least offer 1.\n");
				}
				if (toSell.getPrice() <= 0.0)
				{
					errorMsg.append("This program does not allow you to sell items for free.");
				}

				String error = errorMsg.toString();
				if (error == null || "".equals(error))
				{
					Throwable postError = this.inserter.addArticleToStock(toSell);
					if (postError != null)
					{
						showError("Unable to post your data:<br>Check your log for further details.<br>" + postError.getMessage());
					}
					else
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								nameField.requestFocus();
							}
						});
						JOptionPane.showMessageDialog(this, "Successfully uploaded " + selectedIds.length + " cards", "Info", JOptionPane.INFORMATION_MESSAGE);

						MkmEventHandler.triggerEvent(MkmEventType.ArticleInserted, toSell);
					}
				}
				else
				{
					showError(error);
				}
			}
		}
		finally
		{
			isUploadRunning = false;
		}
	}

	/**
	 * Retrieves an integer value from a text field
	 * 
	 * @param toGetFrom
	 *          the text field to get from
	 * @return 0 for errors, value otherwise
	 */
	private int getIntFromTextField(final JTextField toGetFrom)
	{
		if (toGetFrom == null || toGetFrom.getText() == null || "".equals(toGetFrom.getText()))
			return 0;
		else
		{
			try
			{
				return Integer.parseInt(toGetFrom.getText());
			}
			catch (NumberFormatException ex)
			{
				LoggingHelper.logForLevel(LogLevel.Error, "Unable to parse: ", toGetFrom.getText());
				return 0;
			}
		}
	}

	/**
	 * shows an error popup displaying the message. Linbreaks work with \\n
	 */
	public void showError(final String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Updates the info constrols with the data from the toDisplay param card
	 * 
	 * @param toDisplay
	 *          the card with the infos to be displayed
	 */
	public void updateCardInfos(final MtgCard toDisplay)
	{
		this.infoCollectorsNumber.setText("" + toDisplay.getCollectorsNumber());
		this.infoEdition.setText(toDisplay.getEdition());
		this.infoCardName.setText(toDisplay.getName());
		this.infoRarity.setSelectedItem(toDisplay.getRarity().getAbbreviation());

		BufferedImage img = MkmCache.getImage(toDisplay);
		if (img != null)
		{
			infoImagePanel.getGraphics().drawImage(img.getScaledInstance(250, 350, Image.SCALE_SMOOTH), 0, 0, null);
		}

		this.minExBetter.setValue(Double.toString(toDisplay.getPriceInfo().getLowEx()));
		this.avg.setValue(Double.toString(toDisplay.getPriceInfo().getAvg()));
		this.trend.setValue(Double.toString(toDisplay.getPriceInfo().getTrend()));
	}

	/**
	 * Performs updates to the Sell fields according to the passed card
	 * 
	 * @param toDisplay
	 *          the card with the values to be displayed in the sell portion
	 */
	public void updateSellFields(final List<MtgCard> selectedCards)
	{
		if (selectedCards != null && !selectedCards.isEmpty())
		{
			MtgCard toDisplay = selectedCards.get(0);
			this.sellLanguages.setSelectedValue(toDisplay.getLanguage().getLanguageName(), true);

			StringBuilder selectedIds = new StringBuilder();
			for (int i = 0; i < selectedCards.size(); i++)
			{
				if (i > 0)
				{
					selectedIds.append(CARD_ID_SEPERATOR);
				}

				selectedIds.append(selectedCards.get(i).getCardId());
			}

			this.sellItemId.setText(selectedIds.toString());
		}
		else
		{
			this.sellItemId.setText("");
		}
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	private JPanel createDisabledLabelAndComponentInPanel(final String labelText, final JComponent toAdd)
	{
		return createDisabledLabelAndComponentInPanel(labelText, toAdd, 125, 250, 30);
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	private JPanel createDisabledLabelAndComponentInPanel(final String labelText, final JComponent toAdd, final int componentWidth, final int totalWidth,
			final int height)
	{
		// toAdd.setEnabled(false);
		return createLabelAndComponentInPanel(labelText, toAdd, componentWidth, totalWidth, height);
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	private JPanel createLabelAndComponentInPanel(final String labelText, final JComponent toAdd)
	{
		return createDisabledLabelAndComponentInPanel(labelText, toAdd, 125, 250, 30);
	}

	/**
	 * Wraps the label and text into a Panel for layout purposes
	 * 
	 * @param label
	 *          the text describing the component
	 * @param toAdd
	 *          the component to be added
	 * @return the panel holding both
	 */
	private JPanel createLabelAndComponentInPanel(final String labelText, final JComponent toAdd, final int componentWidth, final int totalWidth,
			final int height)
	{
		// moved due to reuse in mkmmanager
		return GuiHelper.createLabelAndComponentInPanel(labelText, toAdd, componentWidth, totalWidth, height);
	}

	/**
	 * Performs the search for the given item name, delegates to {@link MkmServiceHandler#performNameSearch(String, Language, boolean, MkmInserter)} and
	 * {@link #filterCardList(String)} after
	 * 
	 * @param itemName
	 *          the query string for the search
	 */
	public void performSearchForName(final String itemName)
	{
		try
		{
			MkmServiceHandler serviceHandler = new MkmServiceHandler(this.inserter.getConnector());
			for (String langName : MkmInserter.SEARCH_LANGUAGES)
			{
				serviceHandler.performNameSearch(itemName, Language.getLanguageForName(langName), MkmInserter.PERFORM_EXACT_SEARCH, this.inserter);
			}
			this.filterCardList(itemName);
		}
		catch (IOException ex)
		{
			showError("Unable to contact the server or unable to interpret the server's response. See logFile for more details.\nError Message: " + ex.getMessage());
		}

	}

	/**
	 * Filters the currently cached list for the given text.<br>
	 * The matching cards are determined by {@link MkmInserter#getMatchingCards(String)} as it controls the cache.<br>
	 * Each matching Card will be displayed in the language of the found entry
	 * 
	 * @param text
	 */
	public void filterCardList(final String text)
	{
		long time = System.currentTimeMillis();
		List<String> matches = inserter.getMatchingCards(text);
		LoggingHelper.info("Time needed for match: " + (System.currentTimeMillis() - time) + " ms. Found: ", Integer.toString(matches.size()), " entries.");
		List<MtgCard> matchingCards = new ArrayList<>(matches.size());
		for (String s : matches)
		{
			MtgCard card = inserter.getCardBuffer().get(s);
			// format: NAMEINLANGUAGE_EDITION_LANGUAGENAME_RARITY
			String lang = s.split("_")[2];
			// rebuild lang string with first char upper case
			lang = lang.charAt(0) + lang.toLowerCase().substring(1);
			// set native language for displaying
			card.setName(card.getAllNames().getNameForLang(lang));
			card.setLanguage(Language.getLanguageForName(lang));
			matchingCards.add(card);
		}

		cardList.setModel(new MtgListModel(matchingCards));

	}
}

/**
 * Model to handle updates for the List of MTG Cards
 *
 *
 * @author Kenny
 * @since 09.01.2016
 */
class MtgListModel implements ListModel<MtgCard>
{
	protected List<MtgCard> data;

	/**
	 * 
	 */
	public MtgListModel(final List<MtgCard> data)
	{
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize()
	{
		return this.data.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public MtgCard getElementAt(final int index)
	{
		return this.data.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(final ListDataListener l)
	{
		// not needed?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(final ListDataListener l)
	{
		// not needed?
	}

}