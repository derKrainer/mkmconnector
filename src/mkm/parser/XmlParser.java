package mkm.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import mkm.log.LogLevel;
import mkm.log.LoggingHelper;

/**
 * Simple class for handling xml stringss
 * 
 * @author kenny
 */

public class XmlParser
{
	private Map<String, List<Node>> nodeCatalog = new HashMap<>();

	private final String xmlStream;

	private final File toRead;

	public XmlParser(final File xmlFile)
	{
		this.toRead = xmlFile;
		this.xmlStream = null;
	}

	public XmlParser(final String xmlStream)
	{
		this.xmlStream = stripNonValidXMLCharacters(xmlStream);
		this.toRead = null;
	}

	/**
	 * Builds the xml catalog from the passed xml stream (the one in the constructor)
	 */
	public void catalogNodes() throws IOException
	{
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		Document parsedDoc = null;
		try
		{
			DocumentBuilder parser = fac.newDocumentBuilder();
			InputStream is = null;
			if (this.xmlStream != null)
			{
				// is = new StringBufferInputStream(stripNonValidXMLCharacters(xmlStream));
				is = new ByteArrayInputStream(stripNonValidXMLCharacters(xmlStream).getBytes());
			}
			else if (this.toRead != null)
			{
				is = new FileInputStream(toRead);
			}
			else
			{
				throw new IllegalStateException("XmlStream and File are both null. Unable to parse anything");
			}
			InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));

			parsedDoc = parser.parse(new org.xml.sax.InputSource(reader));
		}
		catch (ParserConfigurationException ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Unable to create DocumentBuilder. Unable to parse the document without it. Exiting.");
			System.exit(0);
		}
		catch (SAXException ex)
		{
			// this exception is logged too often, let caller log
			// LoggingHelper.logException(LogLevel.Error, ex, "Unable to parse the xml file. Possibly invalid?");
			throw new IOException("Error during parsing xml", ex);
		}
		catch (IOException ex)
		{
			LoggingHelper.logException(LogLevel.Critical, ex, "Error reading the xml file");
			throw ex;
		}

		analyze(parsedDoc.getFirstChild());
	}

	/**
	 * This method ensures that the output String has only valid XML unicode characters as specified by the XML 1.0 standard. For reference, please see
	 * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the standard</a>. This method will return an empty String if the input is null or empty.
	 *
	 * @param in
	 *          The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String stripNonValidXMLCharacters(final String in)
	{
		if (in == null || ("".equals(in)))
			return ""; // vacancy test.

		StringBuffer out = new StringBuffer(in.length()); // Used to hold the output.
		char current; // Used to reference the current character.

		for (int i = 0; i < in.length(); i++)
		{
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
			if (Arrays.binarySearch(invalidSpecialChars, current) < 0 && // check special chars then check ranges
			// !(0x10 < current && current < 0x1f) && // mostly tabs & new lines
					(((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF))))
				out.append(current);
			else
				LoggingHelper.logForLevel(LogLevel.Detailed, "removed char: " + current, " from xml.");
		}
		return out.toString();
	}

	/**
	 * Characters which cannot be parsed by the UTF-8 Parser
	 */
	private static char[] invalidSpecialChars = new char[] { 0x9, //
			0x19, // tab
			0x13, // 
			0xc3, // 
			0x1e, // �
			0xA, //
			0xD, // newline
			0x1e, // return carriage
			0x1c, //
			0x1a, //
	};

	/**
	 * Adds the current node to the catalog and goes recursive for each child
	 */
	protected void analyze(final Node n)
	{
		// add node to catalog
		if (this.nodeCatalog.get(n.getNodeName()) == null)
		{
			this.nodeCatalog.put(n.getNodeName(), new ArrayList<Node>());
		}
		this.nodeCatalog.get(n.getNodeName()).add(n);

		// recursive for al children
		if (n.hasChildNodes())
		{
			for (int i = 0; i < n.getChildNodes().getLength(); i++)
			{
				analyze(n.getChildNodes().item(i));
			}
		}
	}

	/**
	 * Retrieves the catalog of all xml attribute names with all corresponding nodes
	 */
	public Map<String, List<Node>> getNodeCatalog() throws IOException
	{
		if (this.nodeCatalog == null || this.nodeCatalog.isEmpty())
		{
			this.catalogNodes();
		}

		return this.nodeCatalog;
	}

	/**
	 * Searches all nodes matching the given path, elements are seperated by '/'
	 * 
	 * @param path
	 *          the path to the wanted nodes
	 * @return all nodes matching the given path, an empty list if nothing was found
	 * @throws IOException
	 */
	public List<Node> getNodesForPath(final String path) throws IOException
	{
		List<Node> retVal = new ArrayList<>();

		String[] elementNames = path.split("/");
		// avoid unncessesary calls, throw an exception here, should help when searching for errors
		if (elementNames.length < 2)
		{
			throw new IllegalArgumentException("If there is no delimiter in the path, use getNodeCatalog().get('String')");
		}
		if (elementNames.length == 2)
		{
			// special case for only 2 elements, just check the parent node
			List<Node> shortList = getNodeCatalog().get(elementNames[1]);
			for (Node n : shortList)
			{
				if (n.getParentNode() != null && elementNames[0].equals(n.getParentNode().getNodeName()))
				{
					retVal.add(n);
				}
			}
		}
		else
		{
			// general case
			List<Node> initialList = getNodeCatalog().get(elementNames[elementNames.length - 1]);
			if (initialList != null)
			{
				for (Node n : initialList)
				{
					boolean failed = false;
					Node currentParent = n.getParentNode();
					for (int i = elementNames.length - 2; i >= 0; i--)
					{
						if (currentParent == null || !elementNames[i].equals(currentParent.getNodeName()))
						{
							failed = true;
							break;
						}
						else
						{
							currentParent = currentParent.getParentNode();
						}
					}
					if (!failed)
					{
						retVal.add(n);
					}
				}
			}
		}

		return retVal;
	}

	/**
	 * Retrieves the text value fo a node<br>
	 * eg: {@literal <}foobar>1337{@literal <}/foobar> would return 1337
	 * 
	 * @return
	 */
	public static String getNodeTextValue(final Node n)
	{
		for (int i = 0; i < n.getChildNodes().getLength(); i++)
		{
			if (n.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE)
			{
				return n.getChildNodes().item(i).getTextContent();
			}
		}
		return null;
	}

	/**
	 * Prints nodes and their inner text value
	 * 
	 * @return the printet content
	 * 
	 * @see #getNodeTextValue(Node)
	 */
	public static String printCurrentNodes(final LogLevel logLevel, final List<Node> toPrint)
	{
		if (toPrint == null)
			return "";

		StringBuilder sb = new StringBuilder();

		for (Node n : toPrint)
		{
			sb.append(getNodeAsString(n));
			sb.append("\n\n");
		}

		LoggingHelper.logForLevel(logLevel, sb.toString());

		return sb.toString();
	}

	/**
	 * Prints a valid xml with the given root
	 * 
	 * @param root
	 * @return
	 */
	public static String getNodeAsString(final Node root)
	{
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(root);

			transformer.transform(source, result);

			return result.getWriter().toString();
		}
		catch (TransformerException e)
		{
			LoggingHelper.logException(LogLevel.Error, e, "Unable to print xml");
			return null;
		}
	}

}
