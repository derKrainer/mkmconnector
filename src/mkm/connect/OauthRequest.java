/**
 * 
 */
package mkm.connect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Random;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Implementation of OAuth Requests with data
 *
 * @author Kenny
 * @since 26.01.2016
 */
public class OauthRequest
{

	public static final String METHOD_POST = "POST";

	public static final String METHOD_PUT = "PUT";

	public static final String METHOD_DELETE = "DELETE";

	/**
	 * Performs a post to the given url, posting the given xml
	 */
	public static boolean doPostXML(final String url, final String xmlContent) throws IOException, Exception
	{
		return performRequest(url, xmlContent, METHOD_POST);
	}

	/**
	 * Performs a PUT request (update)
	 * 
	 * @param url
	 *          the server url to connect to
	 * @param xmlContent
	 *          the content to be put to the server
	 * @return true if the operation was successful
	 * @throws IOException
	 *           any communication error
	 * @throws Exception
	 *           any general error
	 */
	public static boolean doPutXML(final String url, final String xmlContent) throws IOException, Exception
	{
		return performRequest(url, xmlContent, METHOD_PUT);
	}

	/**
	 * Performs a DELETe request (deletes something from the server)
	 * 
	 * @param url
	 *          the server url to connect to
	 * @param xmlContent
	 *          the content to be put to the server
	 * @return true if the operation was successful
	 * @throws IOException
	 *           any communication error
	 * @throws Exception
	 *           any general error
	 */
	public static boolean doDeleteXml(final String url, final String xmlContent) throws IOException, Exception
	{
		return performRequest(url, xmlContent, METHOD_DELETE);
	}

	/**
	 * General method for performing requests (PUT, POST, DELETE)
	 * 
	 * @param url
	 *          the server url
	 * @param xmlContent
	 *          the content to post
	 * @param requestMethod
	 *          {@link #METHOD_DELETE} | {@link #METHOD_POST} | {@link #METHOD_PUT}
	 * @return true if successful
	 * @throws IOException
	 *           any communication error
	 * @throws Exception
	 *           any general error
	 */
	private static boolean performRequest(final String url, final String xmlContent, final String requestMethod) throws IOException, Exception
	{
		String nonce = Long.toString(System.currentTimeMillis() ^ new Random().nextLong());
		String timeStamp = Long.toString(System.currentTimeMillis() / 1000);

		String baseString = new StringBuilder(requestMethod).append('&').append(MkmConnector.encodeUTF8(url)).append('&').toString();

		StringBuilder tmpBuf = new StringBuilder();
		tmpBuf.append("oauth_consumer_key=").append(MkmConnector.encodeUTF8(MkmConnector.MKM_APP_TOKEN)).append('&');
		tmpBuf.append("oauth_nonce=").append(MkmConnector.encodeUTF8(nonce)).append('&');
		tmpBuf.append("oauth_signature_method=").append(MkmConnector.encodeUTF8(MkmConnector.OAUTH_SIG_METHOD)).append('&');
		tmpBuf.append("oauth_timestamp=").append(MkmConnector.encodeUTF8(timeStamp)).append('&');
		tmpBuf.append("oauth_token=").append(MkmConnector.encodeUTF8(MkmConnector.MKM_ACCESS_TOKEN)).append('&');
		tmpBuf.append("oauth_version=").append(MkmConnector.encodeUTF8(MkmConnector.OAUTH_VERSION));

		String adaptedUrl = new StringBuilder(baseString).append(MkmConnector.encodeUTF8(tmpBuf.toString())).toString();

		String signature = MkmConnector.getSignature(url, adaptedUrl, nonce, timeStamp);

		if (METHOD_PUT.equals(requestMethod) || METHOD_POST.equalsIgnoreCase(requestMethod) || METHOD_DELETE.equalsIgnoreCase(requestMethod))
			return httpPost(url, signature, xmlContent, ContentType.APPLICATION_XML, requestMethod);
		else
			throw new IllegalArgumentException("Unknown request method: " + requestMethod);
	}

	/**
	 * Do a HTTT POST request and return the status code.
	 * 
	 * @param uri
	 *          URI for HTTP POST request
	 * @param headerAuthorization
	 *          Empty or authorization header
	 * @param stringEntity
	 *          String entity which will be posted
	 * @param entityConentType
	 *          Empty or entity content type
	 * @return True, when request was successful
	 * @throws RuntimeException
	 *           When status code is not HTTP_CREATED
	 */
	public static boolean httpPost(final String uri, final String headerAuthorization, final String stringEntity, final ContentType entityConentType,
			final String requestMethod) throws RuntimeException, IOException
	{
		HttpClient httpClient = HttpClientBuilder.create().build();

		try
		{
			StringEntity ent = new StringEntity(stringEntity);
			if (entityConentType != null)
			{
				ent.setContentType(entityConentType.toString());
			}

			HttpUriRequest request = null;
			if (METHOD_PUT.equals(requestMethod))
			{
				request = new HttpPut(uri);
				((HttpPut) request).setEntity(ent);
			}
			else if (METHOD_POST.equals(requestMethod))
			{
				request = new HttpPost(uri);
				((HttpPost) request).setEntity(ent);
			}
			else if (METHOD_DELETE.equals(requestMethod))
			{
				request = new HttpDeleteWithBody(uri);
				((HttpDeleteWithBody) request).setEntity(ent);
			}
			else
				throw new IllegalArgumentException("Unknown request method: " + requestMethod);

			if (!headerAuthorization.isEmpty())
			{
				request.addHeader(HttpHeaders.AUTHORIZATION, headerAuthorization);
			}

			HttpResponse response = httpClient.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpURLConnection.HTTP_CREATED && statusCode != HttpURLConnection.HTTP_OK)
			{
				throw new RuntimeException("ERROR: HTTP code: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
			}

			return true;

		}
		catch (IOException e)
		{
			throw e;
		}
	}
}

/**
 * Implementation of a delete request with content
 *
 * @author Kenny
 * @since 15.12.2016
 */
class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "DELETE";

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}

	public HttpDeleteWithBody(final String uri)
	{
		super();
		setURI(URI.create(uri));
	}

	public HttpDeleteWithBody(final URI uri)
	{
		super();
		setURI(uri);
	}

	public HttpDeleteWithBody()
	{
		super();
	}
}
