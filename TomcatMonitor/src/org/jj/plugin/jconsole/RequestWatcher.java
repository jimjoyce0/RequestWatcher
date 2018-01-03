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

import java.lang.management.*;
import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.text.NumberFormat;
import java.net.MalformedURLException;
import static java.lang.management.ManagementFactory.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
* RequestWatcher is a JPanel to extend JConsole with Tomcat request info in a separate tab
* in a table.
* This code is derived from JTop example from Oravle.
*/
/**
 * @author dad
 *
 */
public class RequestWatcher extends JPanel {

   private static class StatusBar extends JPanel {

       private final JLabel statusText;

       public StatusBar(boolean defaultVisible) {
           super(new GridLayout(1, 1));
           statusText = new JLabel();
           statusText.setVisible(defaultVisible);
           add(statusText);
       }

       @Override
       public Dimension getMaximumSize() {
           Dimension maximum = super.getMaximumSize();
           Dimension minimum = getMinimumSize();
           return new Dimension(maximum.width, minimum.height);
       }

       public void setMessage(String text) {
           statusText.setText(text);
           statusText.setVisible(true);
       }
   }

   private MBeanServerConnection server;

   private RequestTableModel rmodel;
   private final StatusBar statusBar;
   public RequestWatcher() {
       super(new GridBagLayout());

       rmodel = new RequestTableModel();
       JTable table = new JTable(rmodel);
       table.setPreferredScrollableViewportSize(new Dimension(500, 300));

       // Set the renderer to format Double
       table.setDefaultRenderer(Double.class, new DoubleRenderer());
       // Add some space
       table.setIntercellSpacing(new Dimension(6,3));
       table.setRowHeight(table.getRowHeight() + 4);

       // Create the scroll pane and add the table to it.
       JScrollPane scrollPane = new JScrollPane(table);

       // Add the scroll pane to this panel.
       GridBagConstraints c1 = new GridBagConstraints();
       c1.fill = GridBagConstraints.BOTH;
       c1.gridy = 0;
       c1.gridx = 0;
       c1.weightx = 1;
       c1.weighty = 1;
       add(scrollPane, c1);

       statusBar = new StatusBar(false);
       GridBagConstraints c2 = new GridBagConstraints();
       c2.fill = GridBagConstraints.HORIZONTAL;
       c2.gridy = 1;
       c2.gridx = 0;
       c2.weightx = 1.0;
       c2.weighty = 0.0;
       add(statusBar, c2);
   }

   // Set the MBeanServerConnection object for communicating
   // with the target VM
   public void setMBeanServerConnection(MBeanServerConnection mbs) {
	   //	     MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
       this.server = mbs;
//       try {
//           this.tmbean = newPlatformMXBeanProxy(server,
//                                                THREAD_MXBEAN_NAME,
//                                                ThreadMXBean.class);
//       } catch (IOException e) {
//           e.printStackTrace();
//       }
//       if (!tmbean.isThreadCpuTimeSupported()) {
//           statusBar.setMessage("Monitored VM does not support thread CPU time measurement");
//       } else {
//           try {
//               tmbean.setThreadCpuTimeEnabled(true);
//           } catch (SecurityException e) {
//               statusBar.setMessage("Monitored VM does not have permission for enabling thread cpu time measurement");
//           }
//       }
      
   }

   //todo add connector info
   class RequestTableModel extends AbstractTableModel {
 
       private String[] columnNames = {"Resource",
                                       "RequestProcessingTime(sec)",
                                       "Worker Thread"};
       
       private List<RequestInfo> threadList =
           Collections.emptyList();

       public RequestTableModel() {
       }

       @Override
       public int getColumnCount() {
           return columnNames.length;
       }

       @Override
       public int getRowCount() {
           return threadList.size();
       }

       @Override
       public String getColumnName(int col) {
           return columnNames[col];
       }

       @Override
       public Object getValueAt(int row, int col) {
           RequestInfo me = threadList.get(row);
           switch (col) {
               case 0 :
                   // Column 1 shows the requested resource
                   return me.getCurrentURI();
               case 1 :
                   // Column 2 shows the time the request has taken so far
                   long ns = me.getRequestProcessingTime();
                   double sec = (double)ns / 1000; 
                   return new Double(sec);
               case 2 :
                   // Column 3 shows the worker thread name
                   return me.getWorkerThread();
               default:
                   return null;
           }
       }

       @Override
       public Class<?> getColumnClass(int c) {
           return getValueAt(0, c).getClass();
       }

       void setDataList(List<RequestInfo> list) {
           threadList = list;
       }
   }


   
   private List<RequestInfo> getProcessorInfo() {
	  //name="type=RequestProcessor"
	   //name="worker=\"http-bio-48343\""
	   ArrayList<RequestInfo> ret = null;
	   try{
	   ObjectName rq = new ObjectName("Catalina:name=*,type=RequestProcessor,worker=*");
	   
	   Set<ObjectName> queryNames = server.queryNames(rq, null);
	   ret = new ArrayList<RequestInfo>(queryNames.size());
	   
		   for (ObjectName objectName : queryNames) {
			    //System.err.println(objectName.getCanonicalName() );
			    RequestInfo ri = new RequestInfo();
			    Object wtName = server.getAttribute(objectName, "workerThreadName");
			    if( wtName != null ){
				    ri.setCurrentURI((String)server.getAttribute(objectName, "currentUri"));
				    ri.setRequestBytesReceived(Long.parseLong(server.getAttribute(objectName, "requestBytesReceived").toString()));
				    ri.setRequestBytesSent(Long.parseLong(server.getAttribute(objectName, "requestBytesSent").toString()));
				    ri.setRequestProcessingTime(Long.parseLong(server.getAttribute(objectName, "requestProcessingTime").toString()));
				    ri.setWorkerThread((String)wtName);
				    ri.setRequestProcessorName(objectName.getCanonicalName());
				    ret.add(ri);
			    }
			    // use attribute you can be interested in
			    //System.out.println(server.getAttribute(objectName, "maxTime"));
			}
		   Collections.sort(ret);
		   
	   }
	   catch (Exception e){
		   e.printStackTrace(System.err);
	   }
	   
	   return ret;
   }


   /**
    * Format Double with 4 fraction digits
    */
   class DoubleRenderer extends DefaultTableCellRenderer {

       NumberFormat formatter;
       public DoubleRenderer() {
           super();
           setHorizontalAlignment(JLabel.RIGHT);
       }

       @Override
       public void setValue(Object value) {
           if (formatter==null) {
               formatter = NumberFormat.getInstance();
               formatter.setMinimumFractionDigits(3);
           }
           setText((value == null) ? "" : formatter.format(value));
       }
   }


   class Worker extends SwingWorker<List<RequestInfo>,Object> {
       private RequestTableModel tmodel;
       Worker(RequestTableModel tmodel) {
           this.tmodel = tmodel;
       }

 
       @Override
       public List<RequestInfo> doInBackground() {
          // return getThreadList();
    	   return getProcessorInfo();
       }
       

       @Override
       protected void done() {
           try {
               // Set table model with the new thread list
               tmodel.setDataList(get());
               // refresh the table model
               tmodel.fireTableDataChanged();
           } catch (InterruptedException e) {
           } catch (ExecutionException e) {
           }
       }
   }

   // Return a new SwingWorker for UI update
   public SwingWorker<?,?> newSwingWorker() {
       return new Worker(rmodel);
   }

   public static void main(String[] args) throws Exception {
       // Validate the input arguments
       if (args.length != 1) {
           usage();
       }

       String[] arg2 = args[0].split(":");
       if (arg2.length != 2) {
           usage();
       }
       String hostname = arg2[0];
       int port = -1;
       try {
           port = Integer.parseInt(arg2[1]);
       } catch (NumberFormatException x) {
           usage();
       }
       if (port < 0) {
           usage();
       }

       // Create the RequestWatcher Panel
       final RequestWatcher requestWatcher = new RequestWatcher();
       // Set up the MBeanServerConnection to the target VM
       MBeanServerConnection server = connect(hostname, port);
       requestWatcher.setMBeanServerConnection(server);

       // A timer task to update GUI per each interval
       TimerTask timerTask = new TimerTask() {
           @Override
           public void run() {
               // Schedule the SwingWorker to update the GUI
               requestWatcher.newSwingWorker().execute();
           }
       };

       // Create the standalone window with RequestWatcher panel
       // by the event dispatcher thread
       SwingUtilities.invokeAndWait(new Runnable() {
           @Override
           public void run() {
               createAndShowGUI(requestWatcher);
           }
       });

       // refresh every 2 seconds
       Timer timer = new Timer("RequestWatcher Sampling thread");
       timer.schedule(timerTask, 0, 2000);

   }


   private static MBeanServerConnection connect(String hostname, int port) {
       // Create an RMI connector client and connect it to
       // the RMI connector server
       String urlPath = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
       MBeanServerConnection server = null;
       try {
           JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
           JMXConnector jmxc = JMXConnectorFactory.connect(url);
           server = jmxc.getMBeanServerConnection();
           
       } catch (MalformedURLException e) {
           // should not reach here
       } catch (IOException e) {
           System.err.println("\nCommunication error: " + e.getMessage());
           System.exit(1);
       }
       return server;
   }

   private static void usage() {
       System.err.println("Usage: java RequestWatcher <hostname>:<port>");
       System.exit(1);
   }
   /**
    * Create the GUI and show it.  For thread safety,
    * this method should be invoked from the
    * event-dispatching thread.
    */
   private static void createAndShowGUI(JPanel requestWatcher) {
       // Create and set up the window.
       JFrame frame = new JFrame("RequestWatcher");
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       // Create and set up the content pane.
       JComponent contentPane = (JComponent) frame.getContentPane();
       contentPane.add(requestWatcher, BorderLayout.CENTER);
       contentPane.setOpaque(true); //content panes must be opaque
       contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
       frame.setContentPane(contentPane);

       // Display the window.
       frame.pack();
       frame.setVisible(true);
   }

}