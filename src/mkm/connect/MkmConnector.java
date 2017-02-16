package mkm.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.ResourceBundle;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpHeaders;

import mkm.cache.MkmCache;
import mkm.data.MtgCard;
import mkm.exception.CertRefreshNeededException;
import mkm.exception.Http400Exception;
import mkm.log.LogLevel;
import mkm.log.LoggingHelper;
import mkm.parser.XmlParser;

/**
 * Basic functionality to connect to the MKM API
 * 
 * @author kenny
 */
public class MkmConnector
{
	public static final String VERSION = "0.2.0";

	public static final ResourceBundle mkm_config = ResourceBundle.getBundle("mkmConnector");

	public static final Boolean IS_SANDBOX_MODE = Boolean.parseBoolean(mkm_config.getString("useSandbox"));

	private static String[] mkmConfigKeys = new String[] { //
			"mkm_app_token", "mkm_app_secret", "mkm_access_token", "mkm_access_token_secret", "mkm_base_url" };

	// check if sandbox values should be used
	static
	{
		if (IS_SANDBOX_MODE)
			mkmConfigKeys = new String[] { "sandbox_mkm_app_token", "sandbox_mkm_app_secret", "sandbox_mkm_access_token", "sandbox_mkm_access_token_secret",
					"sandbox_mkm_base_url" };

	}

	public static final String MKM_APP_TOKEN = mkm_config.getString(mkmConfigKeys[0]);

	public static final String MKM_APP_SECRET = mkm_config.getString(mkmConfigKeys[1]);

	public static final String MKM_ACCESS_TOKEN = mkm_config.getString(mkmConfigKeys[2]);

	public static final String MKM_ACCESS_TOKEN_SECRET = mkm_config.getString(mkmConfigKeys[3]);

	public static final String BASE_URL = mkm_config.getString(mkmConfigKeys[4]);

	public static final String OAUTH_VERSION = mkm_config.getString("oauth_version");

	public static final String OAUTH_SIG_METHOD = mkm_config.getString("oauth_signature_method");

	public static final String RESPONSE_LOG_ROOT_DIR = mkm_config.getString("response_log_root");

	public static final String CACHE_ROOT_DIR = mkm_config.getString("cache_root");

	public static final String OUTPUT_DIR = mkm_config.getString("output_dir");

	public static final String lineSeperator = "\r\n";

	/**
	 * list of all games supported by MKM
	 */
	public static final String GET_GAMES_LIST = WebServiceLocations.getString("WebService.games");

	/**
	 * productId --> list of all articles for this product
	 */
	public static final String GET_ARTICLES_FOR_PRODUCT_ID = WebServiceLocations.getString("WebService.articleForId");

	/**
	 * name --> productID<br>
	 * fullUrl: "https://www.mkmapi.eu/ws/v1.1/metaproduct/:searchString/1/1
	 */
	public static final String GET_METAPRODUCT_BY_NAME = WebServiceLocations.getString("WebService.metaproduct");

	/**
	 * ProductId --> productDescription including Sell, Low and Avaerage costs
	 */
	public static final String GET_PRODUCT_FOR_ID = WebServiceLocations.getString("WebService.productForId");

	/**
	 * ProductName ==> list of products matching (depending on :isExact) <br>
	 * config: /products/:name/1/:idLanguage/:isExact <br>
	 * fullUrl: https://www.mkmapi.eu/ws/v1.1/product/:name/:idGame/:idLanguage/:isExact[/:start]
	 */
	public static final String GET_PRODUCT_FOR_NAME = WebServiceLocations.getString("WebService.productForName");

	/**
	 * Retrieve all expansions for MTG<br>
	 * full url: https://www.mkmapi.eu/ws/v1.1/expansion/:idGame
	 */
	public static final String GET_EXPANSIONS = WebServiceLocations.getString("WebService.allExpansions");

	/**
	 * Retrieve all cards from one expansion<br>
	 * full url: "https://www.mkmapi.eu/ws/v1.1/expansion/:idGame/:name"
	 */
	public static final String GET_EXPANSION_CARDS = WebServiceLocations.getString("WebService.expansionForName");

	/**
	 * Returns the User entity for the user specified by its ID or exact name.<br>
	 * Returns Article entities for available articles from a specific user specified by its ID or name.<br>
	 * full url: https://www.mkmapi.eu/ws/v1.1/user/:idUser
	 */
	public static final String GET_USER_FOR_ID = WebServiceLocations.getString("WebService.userForId");

	/**
	 * Basically the requests returns the complete collection of available articles from that user. By specifying the start parameter, the response can be limited
	 * to 100 entities.<br>
	 * full url: https://www.mkmapi.eu/ws/v1.1/articles/user/:idUser[/:start]
	 */
	public static final String GET_COLLECTION_FOR_USER = WebServiceLocations.getString("WebService.collectionForUserId");

	/**
	 * current user + apiKey --> wants lists
	 */
	public static final String GET_WANTS_LIST_ID = "/wantslist";

	/**
	 * wants list id --> wants list
	 */
	public static final String GET_WANTS_LIST_FOR_ID = WebServiceLocations.getString("WebService.wantslistForId");

	/**
	 * add an article to the current stock<br>
	 * full url: POST https://www.mkmapi.eu/ws/v1.1/stock
	 */
	public static final String POST_NEW_STOCK_ITEM = WebServiceLocations.getString("WebService.addArticle");

	/**
	 * modify an existing article in the current stock<br/>
	 * full url: PUT https://www.mkmapi.eu/ws/v1.1/stock
	 */
	public static final String MODIFY_STOCK = WebServiceLocations.getString("WebService.modifyArticle");

	/**
	 * Deletes an existing article from the users stock<br/>
	 * full url: DELETE https://www.mkmapi.eu/ws/v1.1/stock
	 */
	public static final String DELETE_STOCK = WebServiceLocations.getString("WebService.deleteArticle");

	/**
	 * Calls {@link #performGetRequest(String, String[], String[])} with query, null and null
	 */
	public String performGetRequest(final String query) throws Exception
	{
		return this.performGetRequest(query, (String[]) null, null);
	}

	/**
	 * Wraps the two param strings into arrays and calls {@link #performGetRequest(String, String[], String[])}
	 */
	public String performGetRequest(final String query, final String toReplace, final String replaceWith) throws Exception
	{
		return this.performGetRequest(query, new String[] { toReplace }, new String[] { replaceWith });
	}

	/**
	 * Calls the MkM API with the given query
	 * 
	 * @param query
	 *          the query as described here: https://www.mkmapi.eu/ws/documentation
	 * @param paramNames
	 *          the parts of the query which should be replaced
	 * @param paramValues
	 *          the values the paramNames are replaced with
	 * @return the xml response from the API
	 * @throws IOException
	 *           anything going wrong during access
	 */
	public String performGetRequest(final String query, final String[] paramNames, final String[] paramValues) throws Exception
	{
		return this.performGetRequest(query, paramNames, paramValues, false, false);
	}

	/**
	 * Calls the MkM API with the given query
	 * 
	 * @param query
	 *          the query as described here: https://www.mkmapi.eu/ws/documentation
	 * @param paramNames
	 *          the parts of the query which should be replaced
	 * @param paramValues
	 *          the values the paramNames are replaced with
	 * @param ignoreCache
	 *          true if the cache should not be checked before contacting the server
	 * @param skipWriteToCache
	 *          should the result be written to the cache or skipped
	 * @return the xml response from the API
	 * @throws IOException
	 *           anything going wrong during access
	 */
	public String performGetRequest(final String query, final String[] paramNames, final String[] paramValues, final boolean ignoreCache,
			final boolean skipWriteToCache) throws CertRefreshNeededException, IOException
	{
		// check for parameter failures
		if (query == null)
		{
			throw new IllegalArgumentException("Please provide a valid query for the MKM server");
		}
		if (paramNames != null && paramValues != null && paramValues.length != paramNames.length)
		{
			throw new IllegalArgumentException("Provide the same amount of parameter Names and parameter values");
		}
		if (paramNames != null && paramValues == null || paramNames == null && paramValues != null)
		{
			throw new IllegalArgumentException("Provide either parameterNames AND parameterValue or neither of both");
		}

		HttpURLConnection con = null;
		try
		{
			String adaptedQuery = getPersonalizedString();

			String personalizedParams = getPersonalizedParams(query, paramNames, paramValues);
			adaptedQuery += personalizedParams;

			// log for caching purposes

			if (!ignoreCache)
			{
				// check the cache before contacting server
				String cacheContent = MkmCache.getCacheContent(personalizedParams);
				if (cacheContent != null)
				{
					LoggingHelper.logForLevel(LogLevel.Detailed, "Cache hit, reading from file: ", personalizedParams);
					return cacheContent;
				}
			}

			LoggingHelper.logForLevel(LogLevel.Info, "Missed cache, reading from Server!");

			// create the connection
			try
			{
				con = getConnection(adaptedQuery, "GET");
			}
			catch (Exception ex)
			{
				throw new IOException("Exception during creating a connection", ex);
			}

			// start the connection
			try
			{
				con.connect();
			}
			catch (SSLHandshakeException sslEx)
			{
				// if the ssl handshake fails, throw a special exception marking a need for reinstalling the certificate of MKM
				throw new CertRefreshNeededException("Exception during the SSL Handshake", sslEx);
			}

			LoggingHelper.logForLevel(LogLevel.Info, "Sending the follinging request: ", adaptedQuery);

			BufferedReader stream;
			try
			{
				stream = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			catch (IOException ex)
			{
				if (ex.getMessage().contains("400"))
					throw new Http400Exception(ex);
				else
					throw ex;
			}

			// BufferedReader stream = new BufferedReader(new UTF8Reader(con.getInputStream()));

			LoggingHelper.logForLevel(LogLevel.Detailed, "Resp Code:", Integer.toString(con.getResponseCode()), " - ", con.getResponseMessage());

			StringBuffer xmlStream = new StringBuffer();
			String line = stream.readLine();
			while (line != null)
			{
				xmlStream.append(line).append('\n');
				line = stream.readLine();
			}
			stream.close();

			if (!skipWriteToCache)
			{
				MkmCache.writeToCache(personalizedParams, XmlParser.stripNonValidXMLCharacters(xmlStream.toString()));
			}

			return xmlStream.toString();
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}

	/**
	 * POSTs data to the server
	 * 
	 * @param query
	 *          the location where to write to
	 * @param postContent
	 *          the content to write
	 */
	public void performPostRequest(final String query, final String postContent) throws Exception
	{
		// check for parameter failures
		if (query == null)
		{
			throw new IllegalArgumentException("Please provide a valid query for the MKM server");
		}
		else if (postContent == null || "".equals(postContent))
		{
			throw new IllegalArgumentException("You must post content");
		}

		boolean success = OauthRequest.doPostXML(getPersonalizedString() + query, postContent);

		if (!success)
		{
			System.err.println("Unable to perform post request");
		}
	}

	/**
	 * PUTs data to the server (updates it)
	 * 
	 * @param query
	 *          the location where to write to
	 * @param content
	 *          the content to update
	 */
	public boolean performPutRequest(final String query, final String content) throws Exception
	{
		// check for parameter failures
		if (query == null)
		{
			throw new IllegalArgumentException("Please provide a valid query for the MKM server");
		}
		else if (content == null || "".equals(content))
		{
			throw new IllegalArgumentException("You must put content");
		}

		return OauthRequest.doPutXML(getPersonalizedString() + query, content);
	}

	/**
	 * DELETEs data from the server
	 * 
	 * @param query
	 *          the location where to write to
	 * @param content
	 *          the content to delete
	 */
	public boolean performDeleteRequest(final String query, final String content) throws Exception
	{
		// check for parameter failures
		if (query == null)
		{
			throw new IllegalArgumentException("Please provide a valid query for the MKM server");
		}
		else if (content == null || "".equals(content))
		{
			throw new IllegalArgumentException("You must delete content");
		}

		return OauthRequest.doDeleteXml(getPersonalizedString() + query, content);
	}

	/**
	 * Constructs personalized params for {@link #GET_PRODUCT_FOR_ID} request
	 */
	public static String getPersonalizedParams(final MtgCard card)
	{
		return getPersonalizedParams(GET_PRODUCT_FOR_ID, new String[] { ":id" }, new String[] { Integer.toString(card.getCardId()) });
	}

	/**
	 * Builds the personalized params string for the cache
	 * 
	 * @param query
	 *          the current query
	 * @param paramNames
	 *          the current parameter names
	 * @param paramValues
	 *          the current parameter values
	 * @return the string to use
	 */
	public static String getPersonalizedParams(final String query, final String[] paramNames, final String[] paramValues)
	{
		String personalizedParams = query;
		if (paramNames != null)
		{
			for (int i = 0; i < paramValues.length; i++)
			{
				personalizedParams = personalizedParams.replace(paramNames[i], // replace
						// replace spaces with %20 as the normal UTF-8 conversion is + and MKM expects %20
						(paramValues[i] == null ? "" : paramValues[i].replace(" ", "%20"))); //$NON-NLS-3$
			}
		}
		return personalizedParams;
	}

	/**
	 * Constructs a connection with a Header for a GET connection<br>
	 * 
	 * @param requestUrl
	 *          the target url
	 * @return the created connection with the user data from the mkmConnector.properties
	 * @throws MalformedURLException
	 *           the url was bad
	 * @throws IOException
	 *           something went wrong during reading/writing
	 * @throws InvalidKeyException
	 *           if the build param string is not appropriate to encode with mkmConnector.properties--oauth_signature
	 * @throws NoSuchAlgorithmException
	 *           mkmConnector.properties--oauth_signature_method has a bad value
	 */
	protected HttpURLConnection getConnection(final String requestUrl, final String requestType)
			throws MalformedURLException, IOException, InvalidKeyException, NoSuchAlgorithmException
	{
		String nonce = Long.toString(System.currentTimeMillis() ^ new Random().nextLong());
		String timeStamp = Long.toString(System.currentTimeMillis() / 1000);

		HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();

		String baseString = new StringBuilder(requestType).append('&').append(encodeUTF8(requestUrl)).append('&').toString();

		StringBuilder tmpBuf = new StringBuilder();
		tmpBuf.append("oauth_consumer_key=").append(encodeUTF8(MKM_APP_TOKEN)).append('&');
		tmpBuf.append("oauth_nonce=").append(encodeUTF8(nonce)).append('&');
		tmpBuf.append("oauth_signature_method=").append(encodeUTF8(OAUTH_SIG_METHOD)).append('&');
		tmpBuf.append("oauth_timestamp=").append(encodeUTF8(timeStamp)).append('&');
		tmpBuf.append("oauth_token=").append(encodeUTF8(MKM_ACCESS_TOKEN)).append('&');
		tmpBuf.append("oauth_version=").append(encodeUTF8(OAUTH_VERSION));

		String url = new StringBuilder(baseString).append(encodeUTF8(tmpBuf.toString())).toString();

		connection.addRequestProperty(HttpHeaders.AUTHORIZATION, getSignature(requestUrl, url, nonce, timeStamp));

		connection.setRequestMethod(requestType);

		return connection;
	}

	/**
	 * Retrieves the HmacSHA1 signature for the given url, timestamp, nonce, {@link #MKM_APP_TOKEN}, {@link #OAUTH_SIG_METHOD}, {@link #MKM_ACCESS_TOKEN} and
	 * {@link #OAUTH_VERSION}
	 * 
	 * @param requestUrl
	 *          the basic request
	 * @param parametrizizedUrl
	 *          the url build with params
	 * @param nonce
	 *          the one time random element
	 * @param timeStamp
	 *          the current timestamp
	 * @return the HmacSHA1 hash
	 * @throws NoSuchAlgorithmException
	 *           HmacSHA1 not found on this computer
	 * @throws InvalidKeyException
	 *           if the build param string is not appropriate to encode with HmacSHA1
	 */
	protected static String getSignature(final String requestUrl, final String parametrizizedUrl, final String nonce, final String timeStamp)
			throws NoSuchAlgorithmException, InvalidKeyException
	{
		// debug("Signing '", parametrizizedUrl, "'");

		String signingKey = encodeUTF8(MKM_APP_SECRET) + "&" + encodeUTF8(MKM_ACCESS_TOKEN_SECRET);

		// debug("Signing key: ", signingKey);

		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(), mac.getAlgorithm());
		mac.init(secret);
		byte[] digest = mac.doFinal(parametrizizedUrl.getBytes());
		String signature = DatatypeConverter.printBase64Binary(digest); // Base64.encode(digest) ;

		StringBuffer oauthSig = new StringBuffer();
		oauthSig.append("OAuth realm=\"" + requestUrl + "\", ");
		oauthSig.append("oauth_version=\"" + OAUTH_VERSION + "\", ");
		oauthSig.append("oauth_timestamp=\"" + timeStamp + "\", ");

		oauthSig.append("oauth_nonce=\"" + nonce + "\", ");
		oauthSig.append("oauth_consumer_key=\"" + MKM_APP_TOKEN + "\", ");
		oauthSig.append("oauth_token=\"" + MKM_ACCESS_TOKEN + "\", ");
		oauthSig.append("oauth_signature_method=\"" + OAUTH_SIG_METHOD + "\", ");
		oauthSig.append("oauth_signature=\"" + signature + "\"");

		// debug("Signature: ", oauthSig.toString());

		return oauthSig.toString();
	}

	/**
	 * @see URLEncoder#encode(String, String) with s and UTF-8
	 */
	public static String encodeUTF8(final String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			LoggingHelper.logException(LogLevel.Critical, e, "UTF-8 is unkown");
			return s;
		}
	}

	/**
	 * replaces :user with the current user and :apikey with the curren api key in the {@link #BASE_URL} and appends the withPlaceHolder parameter after it
	 * 
	 * @return the personalized String
	 */
	protected String getPersonalizedString()
	{
		// StringBuffer retVal = new StringBuffer(BASE_URL.length() + withPlaceholder.length() + 10);
		// retVal.append(BASE_URL.replace(":user", USER).replace(":apikey", API_KEY));
		// retVal.append(withPlaceholder);
		// return retVal.toString();

		// V1.1 remove placeholders in connection string... see #getconnection
		return new StringBuilder(BASE_URL).toString();
	}
}
