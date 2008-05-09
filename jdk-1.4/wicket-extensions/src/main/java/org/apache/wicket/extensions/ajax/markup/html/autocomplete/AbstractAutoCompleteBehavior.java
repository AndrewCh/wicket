/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;


import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * @since 1.2
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AbstractAutoCompleteBehavior extends AbstractDefaultAjaxBehavior
{
	private static final ResourceReference AUTOCOMPLETE_JS = new JavascriptResourceReference(
		AutoCompleteBehavior.class, "wicket-autocomplete.js");

	private static final long serialVersionUID = 1L;

	protected boolean preselect = false;

	protected AutoCompleteSettings settings = new AutoCompleteSettings();

	/**
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */

	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderJavascriptReference(AUTOCOMPLETE_JS);
		final String id = getComponent().getMarkupId();
		String initJS = constructInitJS();
		response.renderOnDomReadyJavascript(initJS);
	}

	protected final String constructInitJS()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("new Wicket.AutoComplete('")
			.append(getComponent().getMarkupId())
			.append("','")
			.append(getCallbackUrl())
			.append("',")
			.append(constructSettingsJS())
			.append(");");
		return sb.toString();
	}

	protected final String constructSettingsJS()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{preselect:").append(settings.getPreselect()).append(",maxHeight:").append(
			settings.getMaxHeightInPx()).append(",showListOnEmptyInput:").append(
			settings.getShowListOnEmptyInput()).append("}");
		return sb.toString();
	}

	/**
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#onBind()
	 */

	protected void onBind()
	{
		// add empty AbstractDefaultAjaxBehavior to the component, to force
		// rendering wicket-ajax.js reference if no other ajax behavior is on
		// page
		getComponent().add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;


			protected void respond(AjaxRequestTarget target)
			{
			}
		});
	}

	/**
	 * Callback for the ajax event generated by the javascript. This is where we need to generate
	 * our response.
	 * 
	 * @param input
	 *            the input entered so far
	 * @param requestCycle
	 *            current request cycle
	 */
	protected abstract void onRequest(String input, RequestCycle requestCycle);

	/**
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
	 */

	protected void respond(AjaxRequestTarget target)
	{
		final RequestCycle requestCycle = RequestCycle.get();
		final String val = requestCycle.getRequest().getParameter("q");
		onRequest(val, requestCycle);
	}
}
