/* 
 * 
 *  jetty-basic-auth-helper
 * 
 *  Copyright (c) 2006-2018 Tom Misawa, riversun.org@gmail.com
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 *  
 */
package org.riversun.jetty.basicauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.riversun.jetty.basicauth.BasicAuth.UserPath;

/**
 * Core logic of basic authentication processing
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class BasicAuthLogicCore {

    private BasicAuth mBasicAuthCondition;
    private Map<String, List<UserPath>> mPathSpecUserMap = new LinkedHashMap<>();
    private boolean mForceShowDialogWhenNotAuthed = true;

    public BasicAuthLogicCore setforceShowDialogWhenNotAuthed(boolean enabled) {
        mForceShowDialogWhenNotAuthed = enabled;
        return BasicAuthLogicCore.this;
    }

    /**
     * Set the condition of basic authentication
     * 
     * @param basicAuth
     * @return
     */
    public BasicAuthLogicCore setBasicAuth(BasicAuth basicAuth) {

        this.mBasicAuthCondition = basicAuth;

        final List<UserPath> userPathList = basicAuth.getUserPathList();

        for (UserPath userPath : userPathList) {

            final String[] pathSpecs = userPath.pathSpecs.split(",");

            for (String pathSpec : pathSpecs) {

                List<UserPath> storedUserPathList = mPathSpecUserMap.get(pathSpec);
                if (storedUserPathList == null) {
                    storedUserPathList = new ArrayList<UserPath>();
                    mPathSpecUserMap.put(pathSpec, storedUserPathList);
                }
                storedUserPathList.add(userPath);
            }
        }
        return BasicAuthLogicCore.this;
    }

    /**
     * Returns if basic authentication needed on this request
     * 
     * @param req
     * @return
     */
    private List<UserPath> isBasicAuthNeeded(HttpServletRequest req) {

        String pathInfo = req.getRequestURI();

        final Set<String> pathSpecSet = mPathSpecUserMap.keySet();

        for (String pathSpec : pathSpecSet) {

            final int asterPos = pathSpec.indexOf("*");
            if (asterPos >= 0) {
                String path = pathSpec.substring(0, asterPos);

                if (pathInfo.startsWith(path)) {
                    return mPathSpecUserMap.get(pathSpec);
                }

            } else {
                String path = pathSpec;
                if (pathInfo.equals(path)) {
                    return mPathSpecUserMap.get(pathSpec);
                }
            }

        }

        return null;
    }

    public boolean handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        // Example request header of basic authentication
        // Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ{{=}}{{=}}
        String authHeader = req.getHeader("Authorization");

        List<UserPath> permitUserList = isBasicAuthNeeded(req);

        if (permitUserList != null) {

            // - When there is target-user-list for accessing this path
            // (so need to authenticate)

            if (authHeader != null && authHeader.startsWith("Basic ")) {
                // - If there is authentication information for BASIC authentication

                // Get the right hand of "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ{{=}}{{=}}"
                // u know,
                // that is the base64 data part.
                String base64encodedUserColonPassPart = authHeader.substring(authHeader.indexOf(" ") + 1);

                _User user = parseAuthUserFromBase64(base64encodedUserColonPassPart);

                boolean isAuthenticated = false;

                for (UserPath allowedUser : permitUserList) {
                    if (allowedUser.userName.equals(user.userName) && allowedUser.password.equals(user.password)) {
                        // - User is matched
                        isAuthenticated = true;
                        break;
                    }
                }

                if (isAuthenticated) {
                    // super.handle(target, baseRequest, req, resp);
                    return true;
                } else {

                    if (mForceShowDialogWhenNotAuthed) {
                        // - Is this option is true,
                        // force to show basic authentication dialog again!

                        // Even if the authentication information has been sent
                        // BUT is not the authentication information for this path.

                        // Show basic authentication dialog again!
                        final String realm = mBasicAuthCondition.getRealm();

                        // Below is the server response to tell that authentication is required
                        resp.setHeader("WWW-Authenticate", "BASIC realm=\"" + realm + "\"");
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
                    } else {

                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access.");
                    }
                    return false;
                }

            } else {

                // - If there is no authentication information for BASIC authentication
                // That means that user need to authenticate now
                final String realm = mBasicAuthCondition.getRealm();

                // Below is the server response to tell that authentication is required
                resp.setHeader("WWW-Authenticate", "BASIC realm=\"" + realm + "\"");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
            }
        } else {
            // - If the user list that allowed to access this path , does not exist
            // (So it's possible to access thie page without authentication)
            // super.handle(target, baseRequest, req, resp);
            return true;
        }

        return false;
    }

    private String decodeBase64Str(String base64encodedStr) {
        final byte[] decodedBytes = Base64.getDecoder().decode(base64encodedStr.getBytes());
        final String decodedStr = new String(decodedBytes);
        return decodedStr;
    }

    private static class _User {
        public String userName;
        public String password;
    }

    /**
     * Parsing user name and password from Base64 string
     * 
     * @param enbase64encodedUserColonPassPart
     * @return
     */
    private _User parseAuthUserFromBase64(String enbase64encodedUserColonPassPart) {

        final String userColonPassStr = decodeBase64Str(enbase64encodedUserColonPassPart);
        final int firstColonPos = decodeBase64Str(enbase64encodedUserColonPassPart).indexOf(":");

        final _User user = new _User();

        if (firstColonPos >= 0) {
            user.userName = userColonPassStr.substring(0, firstColonPos);
            user.password = userColonPassStr.substring(firstColonPos + 1);
        } else {
            user.userName = userColonPassStr;
        }
        return user;
    }
}