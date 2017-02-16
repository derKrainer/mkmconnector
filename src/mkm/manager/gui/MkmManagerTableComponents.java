/**
 * 
 */
package mkm.manager.gui;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultRowSorter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import mkm.connect.MkmConnector;
import mkm.data.Article;
import mkm.data.MtgCard;
import mkm.data.Rarity;
import mkm.data.multilingual.Language;
import mkm.events.MkmEventHandler;
import mkm.events.MkmEventType;
import mkm.exception.ServerCommunicationException;
import mkm.localization.Localization;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Provides the functionality of the Table of {@link MkmManagerPanel}
 *
 * @author Kenny
 * @since 11.12.2016
 */
class MkmManagerTableComponents
{

}

/**
 * Represents the status of a single cell of the {@link JTable} of {@link MkmManagerPanel}
 *
 * @author Kenny
 * @since 14.12.2016
 */
enum UpdateStatus
{
	nothing, updated, deleted
}

/**
 * Represents if an inserted item is too cheap, to expensive of just right
 *
 * @author Kenny
 * @since 14.12.2016
 */
enum InsertedPriceStatus
{
	tooCheap, onAverage, tooExpensive
}

/**
 * Class to manange the {@link JTable} of {@link MkmManagerPanel} and handling the functionality which would normally be in a MkmManagerConnector
 *
 *
 * @author Kenny
 * @since 14.12.2016
 */
class MkmManagerTableModel extends AbstractTableModel
{
	protected static final int COL_NAME = 0;

	protected static final int COL_EDITION = 1;

	protected static final int COL_COLLCTTORS_NUMBER = 2;

	protected static final int COL_AMOUNT = 3;

	protected static final int COL_PRICE = 4;

	protected static final int COL_AVERAGE = 5;

	protected static final int COL_DIFF = 6;

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "MkmManagerTableModel_2016-12-11".hashCode();

	/**
	 * Initial set of articles representing the complete collection
	 */
	protected Article[] insertedProducts;

	/**
	 * Subset of {@link #insertedProducts} with applied filters
	 */
	protected Article[] filteredProducts;

	/**
	 * List of articles which have pending modifications
	 */
	protected List<Article> modifiedArticles;

	/**
	 * Map to store row + original object for all objects which are currently modified
	 */
	protected Map<Integer, Article> originalArticles;

	/**
	 * List of articles which are marked as deleted
	 */
	protected List<Article> deletedRows;

	/**
	 * Map containing all changes for single cells (usage with {@link #buildModifyKey(Article, int)} )
	 */
	protected Map<String, UpdateStatus> modifiedCells;

	protected MkmConnector connector;

	/**
	 * Creates a new TableModel with the given connector (for MKM Access) and a set of articles representing the initial data for the table (users collection)
	 * 
	 * @param insertedProducts
	 *          all data to be displayed in the table (users collection)
	 * @param con
	 *          the instance of the {@link MkmConnector} used by callers
	 */
	public MkmManagerTableModel(final Article[] insertedProducts, final MkmConnector con)
	{
		this.insertedProducts = insertedProducts;
		this.filteredProducts = insertedProducts;
		this.modifiedArticles = new ArrayList<>();
		this.originalArticles = new HashMap<>();
		this.deletedRows = new ArrayList<>();
		this.connector = con;

		this.modifiedCells = new HashMap<>();
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
	{
		checkRange(rowIndex, columnIndex);

		Article toSet = filteredProducts[rowIndex];
		Article original = toSet.getClonedArticle();

		boolean modified = false;
		switch (columnIndex)
		{
			case COL_PRICE:
			{
				Double oldValue = toSet.getPrice();
				toSet.setPrice(aValue == null ? null : Double.parseDouble(aValue.toString()));
				if (oldValue != aValue && !oldValue.equals(aValue))
				{
					modified = true;
				}
				break;
			}
			case COL_AMOUNT:
			{
				Integer oldValue = toSet.getAmount();
				toSet.setAmount(aValue == null ? null : Integer.parseInt(aValue.toString()));
				if (oldValue != aValue && !oldValue.equals(aValue))
				{
					modified = true;
				}
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Unsettable index: " + columnIndex + ". Desired new value: " + aValue);
			}
		}

		if (modified)
		{
			// flag as modified, save the change
			this.modifiedCells.put(buildModifyKey(toSet, columnIndex), UpdateStatus.updated);
			this.modifiedArticles.add(toSet);
			this.originalArticles.put(rowIndex, original);

			// inform observers
			this.fireTableDataChanged();
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	private String buildModifyKey(final Article a, final int colIndex)
	{
		return LoggingHelper.concat(Long.toString(a.getArticleId()), "-", Integer.toString(colIndex));
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex)
	{
		switch (columnIndex)
		{
			case COL_PRICE:
			case COL_AMOUNT:
				return getCellUpdateStatus(rowIndex, columnIndex) != UpdateStatus.deleted;
			case COL_COLLCTTORS_NUMBER:
			case COL_EDITION:
			case COL_NAME:
			case COL_AVERAGE:
			case COL_DIFF:
				return false;
			default:
				throw new IllegalArgumentException("Unknown column index: " + columnIndex);
		}
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		checkRange(rowIndex, columnIndex);

		Article rowItem = filteredProducts[rowIndex];
		switch (columnIndex)
		{
			case COL_AMOUNT:
				return rowItem.getAmount();
			case COL_COLLCTTORS_NUMBER:
				return rowItem.getProduct() != null ? rowItem.getProduct().getCollectorsNumber() : 0;
			case COL_EDITION:
				return rowItem.getProduct() != null ? rowItem.getProduct().getEdition() : 0;
			case COL_NAME:
				return rowItem.getProduct() != null ? rowItem.getProduct().getAllNames().getNameForLang(Language.English.getLanguageName()) : 0;
			case COL_PRICE:
				return rowItem.getPrice();
			case COL_AVERAGE:
				return rowItem.getProduct().getPriceInfo().getAvg();
			case COL_DIFF:
				return getPriceDifferenceToAvg(rowIndex);
			default:
				throw new IllegalArgumentException("Unknown column index: " + columnIndex);
		}
	}

	@Override
	public int getRowCount()
	{
		return this.filteredProducts.length;
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		switch (columnIndex)
		{
			case COL_AMOUNT:
				return Localization.getLocalizedString("Qty.");
			case COL_COLLCTTORS_NUMBER:
				return Localization.getLocalizedString("Col. No.");
			case COL_EDITION:
				return Localization.getLocalizedString("Edition");
			case COL_NAME:
				return Localization.getLocalizedString("Name");
			case COL_PRICE:
				return Localization.getLocalizedString("Price");
			case COL_AVERAGE:
				return Localization.getLocalizedString("Average");
			case COL_DIFF:
				return Localization.getLocalizedString("Dif to AVG");
			default:
				throw new IllegalArgumentException("Unknown column index: " + columnIndex);
		}
	}

	@Override
	public int getColumnCount()
	{
		return 7;
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		switch (columnIndex)
		{
			case COL_AMOUNT:
			case COL_COLLCTTORS_NUMBER:
				return Integer.class;
			case COL_EDITION:
			case COL_NAME:
				return String.class;
			case COL_PRICE:
			case COL_AVERAGE:
			case COL_DIFF:
				return Double.class;
			default:
				throw new IllegalArgumentException("Unknown column index: " + columnIndex);
		}
	}

	/**
	 * Checks if a certain cell has been modified
	 * 
	 * @param row
	 *          the row number of the cell
	 * @param col
	 *          the column number of the cell
	 * @return true if the cell has a modified value, false if not
	 */
	public UpdateStatus getCellUpdateStatus(final int row, final int col)
	{
		checkRange(row, col);

		UpdateStatus retVal = this.modifiedCells.get(buildModifyKey(this.filteredProducts[row], col));

		if (retVal == null)
			return UpdateStatus.nothing;
		else
			return retVal;
	}

	/**
	 * Marks a row as deleted
	 * 
	 * @param row
	 *          the number of the row
	 */
	public void markRowAsDeleted(final int row)
	{
		checkRowRange(row);

		if (getCellUpdateStatus(row, 0) == UpdateStatus.deleted)
		{
			// remove deleted flag
			// flag as deleted
			for (int i = 0; i < getColumnCount(); i++)
			{
				this.modifiedCells.remove(buildModifyKey(this.filteredProducts[row], i));
			}

			this.deletedRows.remove(this.filteredProducts[row]);
		}
		else
		{
			// flag as deleted
			for (int i = 0; i < getColumnCount(); i++)
			{
				this.modifiedCells.put(buildModifyKey(this.filteredProducts[row], i), UpdateStatus.deleted);
			}

			this.deletedRows.add(this.filteredProducts[row]);
		}

		fireTableDataChanged();
	}

	/**
	 * Calculates how much the current article differs from the average
	 * 
	 * @param row
	 *          the row number of the object
	 * @return the difference between one item and the average from mkm
	 */
	public double getPriceDifferenceToAvg(final int row)
	{
		checkRowRange(row);

		Article current = this.filteredProducts[row];
		double serverPrice = (double) getValueAt(row, COL_AVERAGE);
		double difference = (current.getPrice() / current.getAmount()) - serverPrice;

		return new BigDecimal(Double.toString(difference)).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Checks the ranges of row and column
	 */
	private void checkRange(final int row, final int col)
	{
		checkRowRange(row);
		if (col > getColumnCount() || col < 0)
			throw new IllegalArgumentException("Column number must be between 0 and 5");
	}

	/**
	 * Checks if the row index is inside the range of this list
	 */
	private void checkRowRange(final int row)
	{
		if (row > getRowCount() || row < 0)
			throw new IllegalArgumentException("Row number must be between 0 and " + insertedProducts.length);
	}

	/**
	 * Filters all insertedProducts for the given edition (contains)
	 * 
	 * @param edition
	 *          a part of the searched card edition
	 */
	public void applyFilterEdition(final String edition)
	{
		List<Article> filteredContent = new ArrayList<>();

		for (Article a : this.insertedProducts)
		{
			if (a.getProduct().getEdition().toUpperCase().contains(edition.toUpperCase()))
				filteredContent.add(a);
		}

		this.filteredProducts = new Article[filteredContent.size()];
		filteredContent.toArray(this.filteredProducts);
	}

	/**
	 * Filters all insertedProducts for the given rarity
	 * 
	 * @param rarity
	 *          the desired rarity for items to be displayed
	 */
	public void applyFilterRarity(final String rarity)
	{
		List<Article> filteredContent = new ArrayList<>();

		for (Article a : this.insertedProducts)
		{
			if (Rarity.None.getAbbreviation().equals(rarity))
				filteredContent.add(a);
			else if (a.getProduct().getRarity().getAbbreviation().equals(rarity))
				filteredContent.add(a);
		}

		this.filteredProducts = new Article[filteredContent.size()];
		filteredContent.toArray(this.filteredProducts);
	}

	/**
	 * Filters all insertedProducts for the given name (contains)
	 * 
	 * @param cardName
	 *          a part of the searched card name
	 */
	public void applyFilterName(final String cardName)
	{
		List<Article> filteredContent = new ArrayList<>();

		for (Article a : this.insertedProducts)
		{
			if (a.getProduct().getName().toUpperCase().contains(cardName.toUpperCase()))
				filteredContent.add(a);
		}

		this.filteredProducts = new Article[filteredContent.size()];
		filteredContent.toArray(this.filteredProducts);
	}

	/**
	 * Refreshes all Price infos of all cards which match the current filter
	 */
	public void refreshCardInfos()
	{
		String xml;
		for (Article a : this.filteredProducts)
		{
			try
			{
				xml = this.connector.performGetRequest(MkmConnector.GET_PRODUCT_FOR_ID, new String[] { ":id" },
						new String[] { Integer.toString(a.getProduct().getCardId()) }, true, false);
			}
			catch (Exception e)
			{
				LoggingHelper.logException(LogLevel.Warning, "Unable to refresh card info of cardId: " + a.getProduct().getCardId());
				continue;
			}

			a.setProduct(new MtgCard(xml));

			// Take over refresh into all data
			for (Article inserted : this.insertedProducts)
			{
				if (inserted.getProduct().getCardId() == a.getProduct().getCardId())
				{
					inserted.setProduct(a.getProduct());
				}
			}
		}
	}

	/**
	 * Clears all currently modified data
	 */
	public void revertChanges()
	{
		this.deletedRows.clear();
		this.modifiedArticles.clear();
		this.modifiedCells.clear();
		this.filteredProducts = this.insertedProducts;

		for (Integer modifiedRow : this.originalArticles.keySet())
		{
			this.insertedProducts[modifiedRow] = this.originalArticles.get(modifiedRow);
		}

		this.originalArticles.clear();
	}

	/**
	 * Applies the local changes to the MKM Collection.<br/>
	 * 
	 * <ol>
	 * <li>Update all articles with modified fields</li>
	 * <li>Delete all articles marked as deleted</li>
	 * <li>Update the local list</li>
	 * </ol>
	 */
	public void applyChanges() throws ServerCommunicationException
	{
		// update all modified objects
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		if (this.modifiedArticles.size() > 0)
		{
			xml.append("<request>").append("\n\t");
			for (Article a : this.modifiedArticles)
			{
				xml.append(a.toSellPostXML(false, true, false));
			}
			xml.append("</request>");

			try
			{
				connector.performPutRequest(MkmConnector.MODIFY_STOCK, xml.toString());

				// inform all listeners about all updated articles
				for (Article a : this.modifiedArticles)
				{
					MkmEventHandler.triggerEvent(MkmEventType.ArticleUpdated, a);
				}
			}
			catch (Exception e)
			{
				handleSynchException(e);
			}
		}

		// delete all deleted articles
		if (this.deletedRows.size() > 0)
		{
			xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			String newLine = "\n\t";
			xml.append("<request>").append(newLine);
			for (Article a : this.deletedRows)
			{
				newLine = "\n\t\t";
				xml.append("<article>").append(newLine);

				xml.append("<idArticle>").append(a.getArticleId()).append("</idArticle>").append(newLine);
				newLine = "\n\t";
				xml.append("<count>").append(a.getAmount()).append("</count>").append(newLine);

				xml.append("</article>").append(newLine);
			}
			xml.append("</request>");

			try
			{
				connector.performDeleteRequest(MkmConnector.DELETE_STOCK, xml.toString());

				// inform all listeners about all deleted articles
				for (Article a : this.modifiedArticles)
				{
					MkmEventHandler.triggerEvent(MkmEventType.ArticleDeleted, a);
				}
			}
			catch (Exception e)
			{
				handleSynchException(e);
			}

			// update the displayed list
			List<Article> insertedArticles = new ArrayList<>(insertedProducts.length);
			for (Article article : insertedProducts)
			{
				insertedArticles.add(article);
			}
			insertedArticles.removeAll(this.deletedRows);
			insertedArticles.toArray(this.insertedProducts);

			this.deletedRows.clear();
		}

		// clean up
		this.modifiedArticles.clear();
		this.modifiedCells.clear();
	}

	/**
	 * Handles a server synchronization exception
	 * 
	 * @param e
	 *          the exception which occured
	 * @throws ServerCommunicationException
	 *           the exception wrapper
	 */
	private void handleSynchException(final Exception e) throws ServerCommunicationException
	{
		JOptionPane.showMessageDialog(null, "Unable to synchronize with server.", "Server Communication Error", JOptionPane.ERROR_MESSAGE);
		LoggingHelper.logException(LogLevel.Error, "Unable to perform synchronization with MkmServer");
		throw new ServerCommunicationException(e.getMessage(), e);
	}
}

class MkmManagerTableCellRenderer extends DefaultTableCellRenderer
{

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = "MkmManagerTableRenderer_2016-12-11".hashCode();

	private Color background = getBackground();

	private Color tooExpensive = new Color(242, 176, 169);

	private Color tooCheap = new Color(196, 242, 169);

	private Color deleted = new Color(255, 141, 89);

	private Color modified = new Color(187, 239, 127);

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row,
			final int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		MkmManagerTableModel model = (MkmManagerTableModel) table.getModel();
		if (model.getCellUpdateStatus(row, column) == UpdateStatus.updated)
		{
			c.setBackground(modified);
		}
		else if (model.getCellUpdateStatus(row, column) == UpdateStatus.deleted)
		{
			c.setBackground(deleted);
		}
		else if (column == MkmManagerTableModel.COL_PRICE)
		{
			double diff = model.getPriceDifferenceToAvg(row);
			if (diff > 0.0)
				c.setBackground(tooExpensive);
			else if (diff < 0.0)
				c.setBackground(tooCheap);
		}
		else if (!isSelected)
		{
			c.setBackground(background);
		}

		return c;
	}

}

class MkmManagerTableRowSorter extends DefaultRowSorter<MkmManagerTableModel, Article>
{

}
