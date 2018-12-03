/*
 * 
 * jetty-basic-auth-helper
 * 
 * Copyright (c) 2006-2018 Tom Misawa, riversun.org@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 */
package org.riversun.jetty.basicauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.riversun.jetty.basicauth.BasicAuthLogicCore.SkipBasicAuthCallback;

/**
 * Resourcehandler supporting basic authentication
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class BasicAuthResourceHandler extends ResourceHandler {

	private BasicAuthLogicCore mBasicAuthLogic = new BasicAuthLogicCore();

	/**
	 * Set the condition of basic authentication
	 * 
	 * @param basicAuth
	 * @return
	 */
	public BasicAuthResourceHandler setBasicAuth(BasicAuth basicAuth) {

		mBasicAuthLogic.setBasicAuth(basicAuth);

		return BasicAuthResourceHandler.this;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		mBasicAuthLogic.setWelcomeFilesAndRelatedPaths(getWelcomeFiles());

		if (mBasicAuthLogic.handle(target, baseRequest, req, resp)) {
			super.handle(target, baseRequest, req, resp);
		}

	}

	/**
	 * Add path to ignore #setRetryBasicAuth effect
	 * 
	 * @param path
	 * @return
	 */
	public BasicAuthResourceHandler addRetryBasicAuthExcludedPath(String path) {
		mBasicAuthLogic.addRetryBasicAuthExcludedPath(path);
		return BasicAuthResourceHandler.this;
	}

	/**
	 * Enabling retry of basic authentication when authorization failed
	 * 
	 * If the user-A who has already passed the BASIC authentication for page-A.
	 * Then the user-A who doesn't have the permission for page-B tries to access
	 * page-B.
	 * 
	 * True:Show BASIC authentication dialog again for the user who has correct
	 * permission for page-B.
	 * 
	 * False:Show error page that shows FORBIDDEN, "You don't have permission to
	 * access."
	 * 
	 * @param enabled
	 * @return
	 */
	public BasicAuthResourceHandler setRetryBasicAuth(boolean enabled) {
		mBasicAuthLogic.setRetryBasicAuth(enabled);
		return BasicAuthResourceHandler.this;
	}

	public BasicAuthResourceHandler setsetSkipBasicAuthCallback(SkipBasicAuthCallback listener) {
		mBasicAuthLogic.setSkipBasicAuthCallback(listener);
		return BasicAuthResourceHandler.this;
	}

}
