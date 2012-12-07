package ru.vang.songoftheday.util;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

//TODO use based on docs
public final class HttpHelper {
	
	private HttpHelper() {};
	
	private static DefaultHttpClient sClient;
	static {
		final HttpParams params = new BasicHttpParams();
		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		final ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		sClient = new DefaultHttpClient(connectionManager, params);
		sClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		setAcceptAllCookies(sClient);
	}
		
	private static void setAcceptAllCookies(final AbstractHttpClient httpClient) {
		final CookieSpecFactory acceptAllFactory = new CookieSpecFactory() {
			public CookieSpec newInstance(final HttpParams params) {
				return new BrowserCompatSpec() {
					public void validate(final Cookie cookie, final CookieOrigin origin)
							throws MalformedCookieException {
					}
				};
			}
		};
		httpClient.getCookieSpecs().register("accept_all", acceptAllFactory);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, "accept_all");
	}
	
	public static DefaultHttpClient getHttpClient() {		
		return sClient;
	}
}