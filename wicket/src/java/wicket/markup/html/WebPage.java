/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Page;
import wicket.PageParameters;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.model.IModel;
import wicket.protocol.http.WebRequestCycle;

/**
 * Base class for HTML pages. This subclass of Page simply returns HTML when
 * asked for its markup type. It also has a method which subclasses can use to
 * retrieve a bookmarkable link to the application's home page.
 * <p>
 * WebPages can be constructed with any constructor when they are being used in
 * a Wicket session, but if you wish to link to a Page using a URL that is
 * "bookmarkable" (which implies that the URL will not have any session
 * information encoded in it, and that you can call this page directly without
 * having a session first directly from your browser), you need to implement
 * your Page with a no-arg constructor or with a constructor that accepts a
 * PageParameters argument (which wraps any query string parameters for a
 * request). In case the page has both constructors, the constructor with
 * PageParameters will be used.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Gwyn Evans
 */
public class WebPage extends Page implements IHeaderRenderer
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static Log log = LogFactory.getLog(WebPage.class);

	/** Components contribution to <body onload="..." */
	private String bodyOnLoad;

	/**
	 * Constructor. Having this constructor public means that you page is
	 * 'bookmarkable' and hence can be called/ created from anywhere.
	 */
	protected WebPage()
	{
		super();
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request.
	 * Having this constructor public means that you page is 'bookmarkable' and
	 * hence can be called/ created from anywhere. For bookmarkable pages (as
	 * opposed to when you construct page instances yourself, this constructor
	 * will be used in preference to a no-arg constructor, if both exist. Note
	 * that nothing is done with the page parameters argument. This constructor
	 * is provided so that tools such as IDEs will include it their list of
	 * suggested constructors for derived classes.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected WebPage(final PageParameters parameters)
	{
		this();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected WebPage(final IModel model)
	{
		super(model);
	}

	/**
	 * Gets the markup type for a WebPage, which is "html" by default. Support
	 * for pages in another markup language, such as VXML, would require the
	 * creation of a different Page subclass in an appropriate package under
	 * wicket.markup. To support VXML (voice markup), one might create the
	 * package wicket.markup.vxml and a subclass of Page called VoicePage.
	 * <p>
	 * Note: The markup type must be equal to the extension of the markup file.
	 * In the case of WebPages, it must always be "html".
	 * 
	 * @return Markup type for HTML
	 */
	public String getMarkupType()
	{
		return "html";
	}

	/**
	 * @return The WebRequestCycle for this WebPage.
	 */
	protected final WebRequestCycle getWebRequestCycle()
	{
		return (WebRequestCycle)getRequestCycle();
	}

	/**
	 * Creates and returns a bookmarkable link to this application's home page.
	 * 
	 * @param id
	 *            Name of link
	 * @return Link to home page for this application
	 */
	protected final BookmarkablePageLink homePageLink(final String id)
	{
		return new BookmarkablePageLink(id, getApplicationPages().getHomePage());
	}

	/**
	 * THIS IS NOT PART OF WICKETS PUBLIC API. DO NOT USE IT YOURSELF.
	 * <p>
	 * Invoked by HtmlHeaderContainer it'll ask all child components of the Page
	 * if they have something to contribute to the &lt;head&gt; section of the
	 * HTML output. Every component interested must implement
	 * IHeaderContributor.
	 * <p>
	 * Note: HtmlHeaderContainer will be removed from the component hierachie at
	 * the end of the request (@see #onEndRequest()) and thus can not transport
	 * status from one request to the next. This is true for all components
	 * added to the header.
	 * 
	 * @see IHeaderContributor
	 * @param container
	 *            The header component container
	 */
	public final void renderHeaderSections(final HtmlHeaderContainer container)
	{
		this.bodyOnLoad = null;

		// A components interested in contributing to the header must
		// implement IHeaderContributor.
		visitChildren(IHeaderContributor.class, new IVisitor()
		{
			/**
			 * @see wicket.Component.IVisitor#component(wicket.Component)
			 */
			public Object component(Component component)
			{
				if (component.isVisible())
				{
					if (component instanceof IHeaderContributor)
					{
						boolean previousValue = component.setRendering(true);
						try
						{
							((IHeaderContributor)component).renderHead(container);
						}
						finally
						{
							component.setRendering(previousValue);
						}
					}
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * THIS IS NOT PART OF THE PUBLIC API.
	 * 
	 * Get what will be appended to the page markup's body onload attribute
	 * 
	 * @return The onload attribute
	 */
	public String getBodyOnLoad()
	{
		return this.bodyOnLoad;
	}

	/**
	 * THIS IS NOT PART OF THE PUBLIC API.
	 * 
	 * Append string to body onload attribute
	 * 
	 * @param onLoad
	 *            Attribute value to be appended
	 */
	public final void appendToBodyOnLoad(final String onLoad)
	{
		if (onLoad != null)
		{
			// Tell the page to change the Page's
			// body tags.
			if (this.bodyOnLoad == null)
			{
				this.bodyOnLoad = onLoad;
			}
			else
			{
				this.bodyOnLoad = this.bodyOnLoad + onLoad;
			}
		}
	}

	/**
	 * Remove the header component and all its children from the component
	 * hierachie. Be aware, thus you can not transfer state from one request to
	 * another.
	 * 
	 * @see wicket.Component#onEndRequest()
	 */
	protected void onEndRequest()
	{
		final Component header = get(HtmlHeaderSectionHandler.HEADER_ID);
		if (header != null)
		{
			this.remove(header);
		}

		super.onEndRequest();
	}

	/**
	 * Called just before a component's listener method (the provided method
	 * argument) is called. This method may be used to set up dependencies,
	 * enforce authorization, etc. NOTE: if this method fails, the method will
	 * not be excuted. Method
	 * {@link WebPage#afterCallComponent(Component, Method)} will always be
	 * called.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param method
	 *            the method of that component that is to be called
	 */
	public void beforeCallComponent(final Component component, final Method method)
	{
	}

	/**
	 * Called right after a component's listener method (the provided method
	 * argument) was called. This method may be used to clean up dependencies,
	 * do logging, etc. NOTE: this method will also be called when
	 * {@link WebPage#beforeCallComponent(Component, Method)} or the method
	 * invocation itself failed.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param method
	 *            the method of that component that is to be called
	 */
	public void afterCallComponent(final Component component, final Method method)
	{
	}
}