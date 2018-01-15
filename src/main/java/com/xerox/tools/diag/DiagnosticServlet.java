package com.xerox.tools.diag;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
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
import org.w3c.dom.Element;

/**
 * Servlet to write all environmental info out as XML
 * 
 * @TODO MVC: Refactor data into object graph, render as XML, JSON, or JSP
 */
@WebServlet(name = "Diagnostic", urlPatterns = { "/diag" })
public class DiagnosticServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public DiagnosticServlet() {
		super();
	}

	private Document renderDOM(HttpServletRequest request) {
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		Document d = null;
		HttpSession sess;

		sess = request.getSession();

		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
			d = db.newDocument();

			Element rootEl = null;
			Element rqstEl = null;
			Element sessEl = null;
			Element applEl = null;
			Element jvmEl = null;

			rootEl = d.createElement("results");
			d.appendChild(rootEl);
			applEl = d.createElement("application");
			sessEl = d.createElement("session");
			rqstEl = d.createElement("request");
			jvmEl = d.createElement("jvm");

			rootEl.appendChild(applEl);
			rootEl.appendChild(sessEl);
			rootEl.appendChild(rqstEl);
			rootEl.appendChild(jvmEl);

			getSession(request, d, sessEl);
			getJvm(d, jvmEl);
			getRequest(request, d, rqstEl);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return d;
	}

	/**
	 * Support all operations
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fmt = null;

		response.setContentType("text/xml");
		Document doc = null;

		fmt = request.getParameter("fmt");
		if (fmt == null)
			fmt = "xml";

		doc = renderDOM(request);

		if ("xml".equalsIgnoreCase(fmt)) {
			try {
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				transformer.transform(new DOMSource(doc), new StreamResult(response.getWriter()));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void getRequest(HttpServletRequest rqst, Document doc, Element top) {
		appendValueNode(doc, top, "authType", rqst.getAuthType());
		appendValueNode(doc, top, "characterEncoding", rqst.getCharacterEncoding());
		appendValueNode(doc, top, "contentLength", Integer.toString(rqst.getContentLength()));
		appendValueNode(doc, top, "contentLengthLong", Long.toString(rqst.getContentLengthLong()));
		appendValueNode(doc, top, "contentType", rqst.getContentType());
		appendValueNode(doc, top, "contextPath", rqst.getContextPath());
		appendValueNode(doc, top, "localAddr", rqst.getLocalAddr());
		appendValueNode(doc, top, "localName", rqst.getLocalName());
		appendValueNode(doc, top, "localPort", Integer.toString(rqst.getLocalPort()));
		appendValueNode(doc, top, "method", rqst.getMethod());
		appendValueNode(doc, top, "pathInfo", rqst.getPathInfo());
		appendValueNode(doc, top, "pathTranslated", rqst.getPathTranslated());
		appendValueNode(doc, top, "protocol", rqst.getProtocol());
		appendValueNode(doc, top, "queryString", rqst.getQueryString());
		appendValueNode(doc, top, "remoteAddr", rqst.getRemoteAddr());
		appendValueNode(doc, top, "remoteHost", rqst.getRemoteHost());
		appendValueNode(doc, top, "remotePort", Integer.toString(rqst.getRemotePort()));
		appendValueNode(doc, top, "remoteUser", rqst.getRemoteUser());
		appendValueNode(doc, top, "requestedSessionId", rqst.getRequestedSessionId());
		appendValueNode(doc, top, "requestURI", rqst.getRequestURI());
		appendValueNode(doc, top, "requestURL", rqst.getRequestURL().toString());

		/*
		 * Locale
		 */
		top.appendChild(mapLocaleElement(doc, rqst.getLocale()));

		/*
		 * Locales
		 */
		Element elLocales = doc.createElement("locales");
		top.appendChild(elLocales);
		Enumeration<Locale> locales = rqst.getLocales();
		while (locales.hasMoreElements()) {
			elLocales.appendChild(mapLocaleElement(doc, locales.nextElement()));
		}

		/*
		 * Cookies
		 */
		Element elCookies = doc.createElement("cookies");
		top.appendChild(elCookies);
		Cookie[] cookies = rqst.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				Element elCookie = doc.createElement("cookie");
				elCookies.appendChild(elCookie);
				appendValueNode(doc, elCookie, "name", cookie.getName());
				appendValueNode(doc, elCookie, "simpleName", cookie.getClass().getSimpleName());
				appendValueNode(doc, elCookie, "value", cookie.getValue());
				appendValueNode(doc, elCookie, "comment", cookie.getComment());
				appendValueNode(doc, elCookie, "domain", cookie.getDomain());
				appendValueNode(doc, elCookie, "path", cookie.getPath());
				appendValueNode(doc, elCookie, "maxAge", Integer.toString(cookie.getMaxAge()));
				appendValueNode(doc, elCookie, "secure", Boolean.toString(cookie.getSecure()));
				appendValueNode(doc, elCookie, "version", Integer.toString(cookie.getVersion()));
			}
		}

		/*
		 * Headers
		 */
		Element elHeaders = doc.createElement("headers");
		top.appendChild(elHeaders);
		Enumeration<String> headerNames = rqst.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			Element elHeader = doc.createElement("header");
			elHeaders.appendChild(elHeader);
			String headerName = headerNames.nextElement();
			String headerValue = rqst.getHeader(headerName);

			appendValueNode(doc, elHeader, "name", headerName);
			appendValueNode(doc, elHeader, "value", headerValue);
		}

		/*
		 * Attributes
		 */
		Element elAttributes = doc.createElement("attributes");
		top.appendChild(elAttributes);
		Enumeration<String> attrNames = rqst.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			Element elAttr = doc.createElement("attribute");
			elAttributes.appendChild(elAttr);
			String attrName = attrNames.nextElement();
			Object attrVal = rqst.getAttribute(attrName);
			appendValueNode(doc, elAttr, "name", attrName);
			appendValueNode(doc, elAttr, "type", attrVal.getClass().getSimpleName());
			try {
				appendValueNode(doc, elAttr, "value", (String) attrVal);
			} catch (ClassCastException cce) {
				appendValueNode(doc, elAttr, "value", attrVal.toString());
			}
		}

		/*
		 * Parameters
		 */
		Element elParms = doc.createElement("parameters");
		top.appendChild(elParms);
		Enumeration<String> parmNames = rqst.getParameterNames();
		while (parmNames.hasMoreElements()) {
			Element elParm = doc.createElement("parameter");
			elParms.appendChild(elParm);
			String parmName = parmNames.nextElement();
			String[] parmValues = rqst.getParameterValues(parmName);
			StringBuffer parmValueBuff = new StringBuffer();
			appendValueNode(doc, elParm, "name", parmName);
			for (String parmValue : parmValues) {
				appendValueNode(doc, elParm, "value", parmValue);
			}
		}

		/*
		 * Parts
		 */
		Element elParts = doc.createElement("parts");
		top.appendChild(elParts);
		try {
			Collection<Part> parts = rqst.getParts();

			/*
			 * If we got this far, multipart is enabled on the servlet
			 */
			elParts.setAttribute("multiPartConfigurationEnabled", "true");

			Iterator<Part> partsIt = parts.iterator();
			while (partsIt.hasNext()) {
				Element elPart = doc.createElement("part");
				elParts.appendChild(elPart);
				Part part = partsIt.next();
				appendValueNode(doc, elPart, "contentType", part.getContentType());
				appendValueNode(doc, elPart, "name", part.getName());
				appendValueNode(doc, elPart, "submittedFileName", part.getSubmittedFileName());
				appendValueNode(doc, elPart, "size", Long.toString(part.getSize()));

				Element elPartHeaders = doc.createElement("headers");
				elPart.appendChild(elPartHeaders);
				Collection<String> partHeaderNames = part.getHeaderNames();
				Iterator<String> partHeaderNamesIt = partHeaderNames.iterator();
				while (partHeaderNamesIt.hasNext()) {
					String partHeaderName = partHeaderNamesIt.next();
					Element elPartHeader = doc.createElement("header");
					String partHeaderValue = part.getHeader(partHeaderName);
					appendValueNode(doc, elPartHeader, "name", partHeaderName);
					appendValueNode(doc, elPartHeader, "value", partHeaderValue);
					elPartHeaders.appendChild(elPartHeader);
				}
			}
		} catch (IOException | ServletException e) {
			// Squelch
		} catch (IllegalStateException ise) {
			elParts.setAttribute("multiPartConfigurationEnabled", "false");
		} 

		/*
		 * rqst.getLocales() rqst.getRequestURL()
		 */
	}

	private Element mapLocaleElement(Document doc, Locale locale) {
		/*
		 * Locale
		 */
		Element elLocale = doc.createElement("locale");
		appendValueNode(doc, elLocale, "language", locale.getLanguage());
		appendValueNode(doc, elLocale, "country", locale.getCountry());
		appendValueNode(doc, elLocale, "variant", locale.getVariant());
		appendValueNode(doc, elLocale, "displayCountry", locale.getDisplayCountry());
		appendValueNode(doc, elLocale, "displayLanguage", locale.getDisplayLanguage());
		appendValueNode(doc, elLocale, "displayName", locale.getDisplayName());
		appendValueNode(doc, elLocale, "displayScript", locale.getDisplayScript());
		appendValueNode(doc, elLocale, "displayVariant", locale.getDisplayVariant());
		appendValueNode(doc, elLocale, "iso3Country", locale.getISO3Country());
		appendValueNode(doc, elLocale, "iso3Language", locale.getISO3Language());
		appendValueNode(doc, elLocale, "script", locale.getScript());

		return elLocale;
	}

	private void getJvm(Document doc, Element top) {

		/*
		 * Security Manager
		 */
		SecurityManager secMgr = System.getSecurityManager();
		Element elSecMgr = doc.createElement("securityManager");
		Element elThreadGroup = doc.createElement("threadGroup");

		if (secMgr != null) {
			top.appendChild(elSecMgr);
			ThreadGroup threadGroup = secMgr.getThreadGroup();
			if (threadGroup != null) {
				elSecMgr.appendChild(elThreadGroup);
				appendValueNode(doc, elThreadGroup, "name", secMgr.getThreadGroup().getName());
				appendValueNode(doc, elThreadGroup, "activeCount",
						Integer.toString(secMgr.getThreadGroup().activeCount()));
				appendValueNode(doc, elThreadGroup, "maxPriority",
						Integer.toString(secMgr.getThreadGroup().getMaxPriority()));
				appendValueNode(doc, elThreadGroup, "activeGroupCount",
						Integer.toString(secMgr.getThreadGroup().activeGroupCount()));
				appendValueNode(doc, elThreadGroup, "isDaemon", Boolean.toString(secMgr.getThreadGroup().isDaemon()));
				appendValueNode(doc, elThreadGroup, "isDestroyed",
						Boolean.toString(secMgr.getThreadGroup().isDestroyed()));
			}
		}

		/*
		 * Properties
		 */
		Properties props = System.getProperties();
		Element elProps = doc.createElement("properties");
		top.appendChild(elProps);
		Set<Object> keys = props.keySet();
		for (Object key : keys) {
			Element elProp = doc.createElement("property");
			elProps.appendChild(elProp);
			appendValueNode(doc, elProp, "name", (String) key);
			appendValueNode(doc, elProp, "type", props.get(key).getClass().getSimpleName());
			appendValueNode(doc, elProp, "value", (String) props.get(key));
		}

		/*
		 * Environment
		 */
		Map<String, String> env = System.getenv();
		Set<String> envKeys = env.keySet();
		Element elEnvs = doc.createElement("envEntries");
		top.appendChild(elEnvs);
		for (String envKey : envKeys) {
			Element elEnv = doc.createElement("envEntry");
			elEnvs.appendChild(elEnv);
			Object val = env.get(envKey);
			appendValueNode(doc, elEnv, "name", envKey);
			appendValueNode(doc, elEnv, "value", (String) val);
		}

		/*
		 * Runtime
		 */
		Element elRuntime = doc.createElement("runtime");
		top.appendChild(elRuntime);
		Runtime runtime = Runtime.getRuntime();
		appendValueNode(doc, elRuntime, "freeMemory", Long.toString(runtime.freeMemory()));
		appendValueNode(doc, elRuntime, "maxMemory", Long.toString(runtime.maxMemory()));
		appendValueNode(doc, elRuntime, "totalMemory", Long.toString(runtime.totalMemory()));
		appendValueNode(doc, elRuntime, "availableProcessors", Long.toString(runtime.availableProcessors()));

		appendValueNode(doc, top, "lineSeparator", System.lineSeparator());
		appendValueNode(doc, top, "currentTimeMillis", Long.toString(System.currentTimeMillis()));

	}

	/**
	 * Session info
	 * 
	 * @param rqst
	 * @param doc
	 * @param top
	 */
	private void getSession(HttpServletRequest rqst, Document doc, Element top) {
		HttpSession sess = null;
		Element id = null;
		Element elAtts = null;
		Enumeration<String> names = null;
		Element elServletCtx = null;
		ServletContext servletCtx = null;

		sess = rqst.getSession();

		appendValueNode(doc, top, "id", sess.getId());
		appendValueNode(doc, top, "createTime", Long.toString(sess.getCreationTime()));
		appendValueNode(doc, top, "lastAccessedTime", Long.toString(sess.getCreationTime()));
		appendValueNode(doc, top, "maxInactiveInterval", Long.toString(sess.getMaxInactiveInterval()));

		/*
		 * Session Attributes
		 */
		elAtts = doc.createElement("attributes");
		top.appendChild(elAtts);
		names = sess.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			Object val = sess.getAttribute(name);
			String type = val.getClass().getSimpleName();

			Element elAtt = doc.createElement("attribute");
			elAtts.appendChild(elAtt);

			appendValueNode(doc, elAtt, "name", name);
			appendValueNode(doc, elAtt, "type", type);
			appendValueNode(doc, elAtt, "value", (String) val);
		}

		/*
		 * Servlet Context
		 */
		servletCtx = sess.getServletContext();
		elServletCtx = doc.createElement("servletContext");
		top.appendChild(elServletCtx);
		appendValueNode(doc, elServletCtx, "contextPath", servletCtx.getContextPath());
		appendValueNode(doc, elServletCtx, "serverInfo", servletCtx.getServerInfo());
		appendValueNode(doc, elServletCtx, "servletContextName", servletCtx.getServletContextName());
		appendValueNode(doc, elServletCtx, "virtualServerName", servletCtx.getVirtualServerName());
		appendValueNode(doc, elServletCtx, "effectiveMajorVersion",
				Integer.toString(servletCtx.getEffectiveMajorVersion()));
		appendValueNode(doc, elServletCtx, "effectiveMinorVersion",
				Integer.toString(servletCtx.getEffectiveMinorVersion()));
		appendValueNode(doc, elServletCtx, "majorVersion", Integer.toString(servletCtx.getMajorVersion()));
		appendValueNode(doc, elServletCtx, "minorVersion", Integer.toString(servletCtx.getMinorVersion()));

		/*
		 * Servlet Context - Attributes
		 */
		Element elServletCtxAtts = null;
		elServletCtxAtts = doc.createElement("attributes");
		elServletCtx.appendChild(elServletCtxAtts);
		names = servletCtx.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			Object val = servletCtx.getAttribute(name);
			String type = val.getClass().getSimpleName();

			Element elAtt = doc.createElement("attribute");
			elServletCtxAtts.appendChild(elAtt);

			appendValueNode(doc, elAtt, "name", name);
			appendValueNode(doc, elAtt, "type", type);
			try {
				appendValueNode(doc, elAtt, "value", (String) val);
			} catch (ClassCastException cce) {
				appendValueNode(doc, elAtt, "value", val.toString());
			}
		}

		/*
		 * Servlet Context - Default Session Tracking Modes
		 */
		Element elDefaultSessionTrackingModes = doc.createElement("defaultSessionTrackingModes");
		elServletCtx.appendChild(elDefaultSessionTrackingModes);
		Set<SessionTrackingMode> defaultSessionTrackingMode = servletCtx.getDefaultSessionTrackingModes();
		Iterator<SessionTrackingMode> defaultSessionTrackingModeIt = defaultSessionTrackingMode.iterator();
		while (defaultSessionTrackingModeIt.hasNext()) {
			appendValueNode(doc, elDefaultSessionTrackingModes, "defaultSessionTrackingMode",
					defaultSessionTrackingModeIt.next().name());
		}

		/*
		 * Servlet Context - Effective Session Tracking Modes
		 */
		Element elEffectiveSessionTrackingModes = doc.createElement("effectiveSessionTrackingModes");
		elServletCtx.appendChild(elEffectiveSessionTrackingModes);
		Set<SessionTrackingMode> effectiveSessionTrackingMode = servletCtx.getEffectiveSessionTrackingModes();
		Iterator<SessionTrackingMode> effectiveSessionTrackingModeIt = effectiveSessionTrackingMode.iterator();
		while (effectiveSessionTrackingModeIt.hasNext()) {
			appendValueNode(doc, elEffectiveSessionTrackingModes, "effectiveSessionTrackingMode",
					effectiveSessionTrackingModeIt.next().name());
		}

		/*
		 * Servlet Context - Init Parameter Names
		 */
		Element elInitParameterNames = doc.createElement("initParameterNames");
		elServletCtx.appendChild(elInitParameterNames);
		Enumeration<String> initParameterNames = servletCtx.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String initParamName = initParameterNames.nextElement();
			appendValueNode(doc, top, initParamName, servletCtx.getInitParameter(initParamName));
		}

		/*
		 * Servlet Context - Filter Registrations
		 */
		Map<String, ? extends FilterRegistration> filterRegs = servletCtx.getFilterRegistrations();
		Element elFilterRegs = doc.createElement("filterRegistrations");
		elServletCtx.appendChild(elFilterRegs);
		Set<String> keys = filterRegs.keySet();
		for (String key : keys) {
			FilterRegistration val = filterRegs.get(key);
			Element elFilterReg = doc.createElement("filterRegistration");
			// elFilterRegs.appendChild(elFilterReg);
			// TODO: Escape chars appendValueNode(doc, elFilterRegs, key,
			// val.getName());
		}

		/*
		 * Servlet Context - Servlet Registrations
		 */
		Map<String, ? extends ServletRegistration> servletRegs = servletCtx.getServletRegistrations();
		Element elServletRegs = doc.createElement("servletRegistrations");
		elServletCtx.appendChild(elServletRegs);
		keys = servletRegs.keySet();
		for (String key : keys) {
			ServletRegistration val = servletRegs.get(key);
			Element elServletReg = doc.createElement("servletRegistration");
			elServletRegs.appendChild(elServletReg);
			// TODO: Escape chars appendValueNode(doc, elFilterRegs, key,
			// val.getName());
			appendValueNode(doc, elServletReg, key, val.getName());
		}

		/*
		 * Servlet Context - Session Cookie Config
		 */
		Element elSessCookieConfig = doc.createElement("sessionCookieConfig");
		SessionCookieConfig sessCookieConfig = servletCtx.getSessionCookieConfig();
		elServletCtx.appendChild(elSessCookieConfig);
		appendValueNode(doc, elSessCookieConfig, "name", sessCookieConfig.getName());
		appendValueNode(doc, elSessCookieConfig, "comment", sessCookieConfig.getComment());
		appendValueNode(doc, elSessCookieConfig, "domain", sessCookieConfig.getDomain());
		appendValueNode(doc, elSessCookieConfig, "path", sessCookieConfig.getPath());
		appendValueNode(doc, elSessCookieConfig, "maxAge", Integer.toString(sessCookieConfig.getMaxAge()));
		appendValueNode(doc, elSessCookieConfig, "isHttpOnly", Boolean.toString(sessCookieConfig.isHttpOnly()));
		appendValueNode(doc, elSessCookieConfig, "isSecure", Boolean.toString(sessCookieConfig.isSecure()));
	}

	private void appendValueNode(Document doc, Element top, String name, String value) {
		Element el = doc.createElement(name);
		el.setTextContent(value);
		top.appendChild(el);
	}

}
