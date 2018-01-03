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

/**
 * POJO class to contain request information
 *
 */
public class RequestInfo implements Comparable {
	String requestProcessorName=null;
	protected String getRequestProcessorName() {
		return requestProcessorName;
	}
	protected void setRequestProcessorName(String requestProcessorName) {
		this.requestProcessorName = requestProcessorName;
	}
	long requestProcessingTime=0L; 
	String currentURI=null;
	String workerThread=null;
	long requestBytesReceived=0L;
	long requestBytesSent=0L; //response?
	protected long getRequestProcessingTime() {
		return requestProcessingTime;
	}
	protected void setRequestProcessingTime(long requestProcessingTime) {
		this.requestProcessingTime = requestProcessingTime;
	}
	protected String getCurrentURI() {
		return currentURI;
	}
	protected void setCurrentURI(String currentURI) {
		this.currentURI = currentURI;
		if( currentURI == null){
			this.currentURI="<defunct>";
		}
	}
	protected String getWorkerThread() {
		return workerThread;
	}
	protected void setWorkerThread(String workerThread) {
		this.workerThread = workerThread;
		if( workerThread == null){
			this.workerThread="<defunct>";
		}
	}
	protected long getRequestBytesReceived() {
		return requestBytesReceived;
	}
	protected void setRequestBytesReceived(long requestBytesRecieved) {
		this.requestBytesReceived = requestBytesRecieved;
	}
	protected long getRequestBytesSent() {
		return requestBytesSent;
	}
	protected void setRequestBytesSent(long requestBytesSent) {
		this.requestBytesSent = requestBytesSent;
	}
	@Override
	public int compareTo(Object arg0) {
		RequestInfo other=(RequestInfo)arg0;
		return (int)other.requestProcessingTime - (int)this.requestProcessingTime;
	}
	

}
