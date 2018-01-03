/*
 
    JConsole management extension to view live Tomcat requests
    
    Copyright (C) 2018  @author Jim Joyce

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.jj.plugin.jconsole;



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import com.sun.tools.jconsole.JConsolePlugin;

/**
* RequestWatcherPlugin is a subclass to com.sun.tools.jconsole.JConsolePlugin
*
* This code is derived from JTop management plugin.
* 
*/
public class RequestWatcherPlugin extends JConsolePlugin implements PropertyChangeListener
{
   private RequestWatcher requestWatcher = null;
   private Map<String, JPanel> tabs = null;

   public RequestWatcherPlugin() {
       // register itself as a listener
       addContextPropertyChangeListener(this);
   }

   /*
    * Returns a RequestWatcher tab to be added in JConsole.
    */
   @Override
   public synchronized Map<String, JPanel> getTabs() {
       if (tabs == null) {
           requestWatcher = new RequestWatcher();
           requestWatcher.setMBeanServerConnection(
               getContext().getMBeanServerConnection());
           // use LinkedHashMap if you want a predictable order
           // of the tabs to be added in JConsole
           tabs = new LinkedHashMap<String, JPanel>();
           tabs.put("RequestWatcher", requestWatcher);
       }
       return tabs;
   }

   /*
    * Returns a SwingWorker which is responsible for updating the RequestWatcher tab.
    */
   @Override
   public SwingWorker<?,?> newSwingWorker() {
       return requestWatcher.newSwingWorker();
   }



   /*
    * Property listener to reset the MBeanServerConnection
    * at reconnection time.
    */
   @Override
   public void propertyChange(PropertyChangeEvent ev) {
       String prop = ev.getPropertyName();
       if (prop == JConsoleContext.CONNECTION_STATE_PROPERTY) {
           ConnectionState newState = (ConnectionState)ev.getNewValue();
           // JConsole supports disconnection and reconnection
           // The MBeanServerConnection will become invalid when
           // disconnected. Need to use the new MBeanServerConnection object
           // created at reconnection time.
           if (newState == ConnectionState.CONNECTED && requestWatcher != null) {
               requestWatcher.setMBeanServerConnection(
                   getContext().getMBeanServerConnection());
           }
       }
   }
}