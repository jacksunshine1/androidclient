/**
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack;

/**
 * The AbstractConnectionListener class provides an empty implementation for all
 * methods defined by the {@link ConnectionListener} interface. This is a
 * convenience class which should be used in case you do not need to implement
 * all methods.
 *
 * @author Henning Staib
 */
public class AbstractConnectionListener implements ConnectionListener{

    public void connectionClosed(){
        // do nothing
    }

    public void connectionClosedOnError(Exception e){
        // do nothing
    }

    public void reconnectingIn(int seconds){
        // do nothing
    }

    public void reconnectionSuccessful(){
        // do nothing
    }

    public void reconnectionFailed(Exception e){
        // do nothing
    }

}