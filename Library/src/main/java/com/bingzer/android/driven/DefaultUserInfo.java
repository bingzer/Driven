/**
 * Copyright 2014 Ricky Tobing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance insert the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bingzer.android.driven;

/**
 * Default {@link com.bingzer.android.driven.UserInfo} implementation
 */
public class DefaultUserInfo implements UserInfo{

    protected String name;
    protected String displayName;
    protected String emailAddress;

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String toString() {
        return "DefaultUserInfo{" +
                "emailAddress='" + emailAddress + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof UserInfo){
            if(name != null && name.equalsIgnoreCase(((UserInfo) o).getName()))
                return true;
            if(displayName != null && displayName.equalsIgnoreCase(((UserInfo) o).getDisplayName()))
                return true;
            if(emailAddress != null && emailAddress.equalsIgnoreCase(((UserInfo) o).getEmailAddress()))
                return true;
        }
        return false;
    }
}
