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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.riversun.jetty.basicauth.BasicAuth.UserPath;
import org.riversun.jetty.basicauth.BasicAuthLogicCore.SkipBasicAuthCallback;

/**
 * BasicAuthSecurityHandler
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class BasicAuthSecurityHandler extends ConstraintSecurityHandler {

    private BasicAuth mBasicAuth;
    private BasicAuthLogicCore mBasicAuthLogic = new BasicAuthLogicCore();

    /**
     * Set the condition of basic authentication
     * 
     * @param basicAuth
     * @return
     */
    public BasicAuthSecurityHandler setBasicAuth(BasicAuth basicAuth) {

        this.mBasicAuth = basicAuth;
        mBasicAuthLogic.setBasicAuth(basicAuth);
        init();
        return BasicAuthSecurityHandler.this;

    }

    private void init() {

        final HashLoginService loginService = new HashLoginService();
        loginService.setName(mBasicAuth.getRealm());

        this.setAuthenticator(new BasicAuthenticator());
        this.setRealmName("realm");

        this.setLoginService(loginService);

        final UserStore userStore = new UserStore();

        // A target path of BASIC authentication and a map of the role(s) set for that
        // path
        // key:path value:roles

        // The purpose of this design is to make it easy
        // to set which resource the user has access to rather than the original idea of
        // Role
        final Map<String, List<String>> pathRolesMap = new LinkedHashMap<>();

        for (UserPath authPathModel : mBasicAuth.getUserPathList()) {

            // Create role based on user name = 1 user, 1 role (user specific role)
            final String[] roles = new String[] { "role_for_" + authPathModel.userName };

            userStore.addUser(authPathModel.userName, new Password(authPathModel.password), roles);

            // Paths to be BASIC authenticated are splitted by comma separators
            final String[] pathSpecs = authPathModel.pathSpecs.split(",");

            for (String pathSpec : pathSpecs) {

                String key = pathSpec;

                List<String> roleList = null;

                if (pathRolesMap.containsKey(key)) {
                    roleList = pathRolesMap.get(key);
                } else {
                    roleList = new ArrayList<String>();
                    pathRolesMap.put(key, roleList);
                }
                roleList.add(roles[0]);
            }

        }

        for (final String pathSpec : pathRolesMap.keySet()) {
            final List<String> roleList = pathRolesMap.get(pathSpec);
            final ConstraintMapping mapping = createMapping(pathSpec, roleList.toArray(new String[] {}));
            this.addConstraintMapping(mapping);
        }

        loginService.setUserStore(userStore);

    }

    private static ConstraintMapping createMapping(String pathSpec, String[] roles) {

        final Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(roles);
        constraint.setAuthenticate(true);

        final ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(pathSpec);
        return mapping;

    }

    /////
    @Override
    public void handle(String pathInContext, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        final String remoteAddr = getRemoteAddr(request);

        // TODO Determine RemoteAddr and implement processing
        // to not perform BASIC authentication if IP is within a certain range
        boolean needBasicAuth = true;

        if (needBasicAuth) {
            boolean isAuthenticationSuccess = mBasicAuthLogic.handle("", baseRequest, request, response);
            if (isAuthenticationSuccess) {
                // SKIP auth
                getHandler().handle(pathInContext, baseRequest, request, response);
            }
        } else {
            // IP range passed
        }

    }

    /**
     * Add path to ignore #setRetryBasicAuth effect
     * 
     * @param path
     * @return
     */
    public BasicAuthSecurityHandler addRetryBasicAuthExcludedPath(String path) {
        mBasicAuthLogic.addRetryBasicAuthExcludedPath(path);
        return BasicAuthSecurityHandler.this;
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
    public BasicAuthSecurityHandler setRetryBasicAuth(boolean enabled) {
        mBasicAuthLogic.setRetryBasicAuth(enabled);
        return BasicAuthSecurityHandler.this;
    }

    public BasicAuthSecurityHandler setSkipBasicAuthCallback(SkipBasicAuthCallback listener) {
        mBasicAuthLogic.setSkipBasicAuthCallback(listener);
        return BasicAuthSecurityHandler.this;
    }

    /**
     * Returns remote IP address
     * If the access is via AWS ELB, so get IP address from X-Forwarded-For
     * 
     * @param request
     * @return
     */
    private String getRemoteAddr(HttpServletRequest request) {
        // In the case of a request via ELB, it is stored in X-Forwarded-For
        final String xff = request.getHeader("X-Forwarded-For");
        if (xff != null) {
            return xff;
        }
        return request.getRemoteAddr();
    }
}
