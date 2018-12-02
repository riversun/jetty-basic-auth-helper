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

import java.util.ArrayList;
import java.util.List;
/**
 * Condition of basic authentication
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class BasicAuth {

    private BasicAuth() {
    }

    private String realm;
    private List<UserPath> authPathList;

    String getRealm() {
        return realm;
    }

    List<UserPath> getUserPathList() {
        return authPathList;
    }

    public static class Builder {

        private String mRealm;
        private List<UserPath> mAuthPathList;

        public Builder setRealm(String realm) {
            this.mRealm = realm;
            return Builder.this;
        }

        public Builder addUserPath(String userName, String password, String pathSpecs) {

            if (mAuthPathList == null) {
                mAuthPathList = new ArrayList<>();
            }
            mAuthPathList.add(new UserPath(userName, password, pathSpecs));
            return Builder.this;
        }

        public BasicAuth build() {
            return new BasicAuth(this);
        }

    }

    private BasicAuth(Builder builder) {
        this.realm = builder.mRealm;
        this.authPathList = builder.mAuthPathList;
    }

    static final class UserPath {
        public String userName;
        public String password;
        public String pathSpecs;

        public UserPath(String userName, String password, String pathSpecs) {
            super();
            this.userName = userName;
            this.password = password;
            this.pathSpecs = pathSpecs;
        }

    }

}