package name.qd.ws.service.solana;

import org.p2p.solanaj.ws.listeners.NotificationEventListener;

public class SolanaEventListener implements NotificationEventListener {

	@Override
	public void onNotifiacationEvent(Object data) {
		System.out.println(data);
	}
}
