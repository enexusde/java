package de.e_nexus.eclipse.plugins.eclipselink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class EclipseLinkThread extends Thread {
	private boolean running = true;

	private ServerSocket sock;
	public ArrayList<EclipseLinkURLListener> listeners = new ArrayList<EclipseLinkURLListener>();

	public EclipseLinkThread() {
		super();
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		try {
			try {
				this.sock = new ServerSocket(63551);
				this.sock.setSoTimeout(10);
			} catch (IOException e) {
				running = false;
				e.printStackTrace();
			}
			
			while (running) {
				try {
					sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Socket accept = sock.accept();
					InputStream inputStream = accept.getInputStream();
					int available = inputStream.available();
					byte[] lst = new byte[available];
					inputStream.read(lst);
					OutputStream outputStream = accept.getOutputStream();
					outputStream
							.write(("HTTP/1.1 200 OK\n"
									+ "Cache-Control: no-cache\n\n<html><h1>OK</h1></html>")
									.getBytes());
					outputStream.flush();
					outputStream.close();
					accept.close();
					String url = new String(lst);
					if (url.length() > 0)
						for (EclipseLinkURLListener listener : listeners) {
							try {
								listener.notifyURL(url);
							} catch (Exception e) {

							}
						}

				} catch (SocketTimeoutException e) {
				}

			}
		} catch (IOException e) {
		}
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
