package de.e_nexus.jabber.handler.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.modules.Module;
import org.apache.vysper.xmpp.modules.ServerRuntimeContextService;
import org.apache.vysper.xmpp.protocol.HandlerDictionary;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;

public final class JabberChatModul implements Module {
	private Map<String, EntityImpl> users;

	public JabberChatModul(Map<String, EntityImpl> users) {
		this.users = users;
	}

	public void initialize(ServerRuntimeContext serverRuntimeContext) {

	}

	public String getVersion() {
		return "1.0";
	}

	public List<ServerRuntimeContextService> getServerServices() {
		return Collections.EMPTY_LIST;
	}

	public String getName() {
		return "XEP-0045 Multi-User Chat";
	}

	public List<HandlerDictionary> getHandlerDictionaries() {
		List<HandlerDictionary> dd = new ArrayList<HandlerDictionary>();
		dd.add(new JabberChatHandler(getUsers()));
		return dd;
	}

	public Map<String, EntityImpl> getUsers() {
		return users;
	}
}