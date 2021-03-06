package station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MessageManager {

	private Logger logger;
	private ClockManager clockManager;
	private Set<Byte> freeSlots;
	private List<Byte> allSlots = Arrays.asList(new Byte[] { 1, 2, 3, 4, 5, 6,
			7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
			24, 25 });
	private List<Message> allReceivedMessage;
	private Random random;
	private Message ownMessage;
	private byte reservedSlot = 0;

	public MessageManager(Logger logger, ClockManager clockManager) {
		this.logger = logger;
		this.clockManager = clockManager;
		this.allReceivedMessage = new ArrayList<Message>();
		this.freeSlots = new HashSet<Byte>(allSlots);
		this.random = new Random();
	}

	public void resetFrame() {
		this.freeSlots = new HashSet<Byte>(allSlots);
		resetAllMessageFromOldFrame();
		this.ownMessage = null;
	}

	private void resetAllMessageFromOldFrame() {
		ArrayList<Message> messagesToPrint = new ArrayList<Message>();
		for (Message m : new ArrayList<Message>(allReceivedMessage)) {
			if (m.isOldFrame()) {
				messagesToPrint.add(m);
				freeSlots.remove((Byte) m.getReservedSlot());
			}
		}
		// logger.printMessages(messagesToPrint, clockManager.getCurrentFrame(),
		// clockManager.getCorrectionInMS());
		allReceivedMessage.removeAll(messagesToPrint);
	}
 

	/**
	 * 
	 * @param currentMessage
	 */
	public void receivedMessage(Message currentMessage) {
		freeSlots.remove((Byte) currentMessage.getReservedSlot());
		currentMessage.setReceivedTimeInMS(clockManager.currentTimeMillis());
		currentMessage.setCurrentCorrection(clockManager.getCorrectionInMS());
		checkOwnMessage(currentMessage);
		currentMessage.setOldFrame(false);
		allReceivedMessage.add(currentMessage);
	}

	private void checkOwnMessage(Message m) {
		if (this.ownMessage != null)
			if (this.clockManager.getCurrentSendingSlot(m) == this.clockManager
					.getCurrentSendingSlot(ownMessage)) {
				m.setOwnMessage(true);
			} else {
				m.setOwnMessage(false);
			}
	}

	/**
	 * Kollisionbehandlung
	 * 
	 * @param currentMessage
	 */
	private void handleKollision() {
		if (allReceivedMessage.size() > 1)
			for (int i = 0; i < allReceivedMessage.size() - 1; i++) {
				Message m1 = allReceivedMessage.get(i);
				Message m2 = allReceivedMessage.get(i + 1);
				if (this.clockManager.getCurrentSendingSlot(m1) == this.clockManager
						.getCurrentSendingSlot(m2)) {
					m1.setKollision(true);
					m2.setKollision(true);
//					freeSlots.add((Byte)m1.getReservedSlot());
//					freeSlots.add((Byte)m2.getReservedSlot());
				} else {
					m1.setKollision(false);
					m2.setKollision(false);
//					freeSlots.remove((Byte)m1.getReservedSlot());
//					freeSlots.remove((Byte)m2.getReservedSlot());
					if (i > 0
							&& this.clockManager
									.getCurrentSendingSlot(allReceivedMessage
											.get(i - 1)) == this.clockManager
									.getCurrentSendingSlot(m1)) {
						allReceivedMessage.get(i - 1).setKollision(true);
						m1.setKollision(true);
//						freeSlots.add((Byte)m1.getReservedSlot());
					}
				}
			}
	}

	public void syncMessagesReceivedTime() {
		handleKollision();
		clockManager.sync(allReceivedMessage);
		for (Message m : new ArrayList<Message>(allReceivedMessage)) {
			m.setCurrentCorrection(clockManager.getCorrectionInMS());
			if (clockManager.getCurrentFrame() == clockManager.getFrame(m
					.getReceivedTimeInMS())) {
				m.setOldFrame(false);
			} else {
				m.setOldFrame(true);
			}
			checkOwnMessage(m);
		}
		handleKollision();
	}

	/**
	 * prüft ob eigene Nachricht kollidiert
	 * 
	 * @param currentMessage
	 * @return
	 */
	public boolean isOwnKollision() {
		boolean isOwnMessageKollision = false;
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage() && m.isKollision())
				isOwnMessageKollision = true;
		return isOwnMessageKollision;
	}

	public boolean isOwnMessageSended() {
		boolean OwnMessageSended = false;
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage())
				OwnMessageSended = true;
		return OwnMessageSended;
	}

	/**
	 * @param ownMessage
	 *            the ownMessage to set
	 */
	public void setOwnMessage(Message ownMessage) {
		this.ownMessage = ownMessage;
	}

	/**
	 * @return the freeSlots
	 */
	public Set<Byte> getFreeSlots() {
		return freeSlots;
	}

	/**
	 * @param freeSlots
	 *            the freeSlots to set
	 */
	public void setFreeSlots(Set<Byte> freeSlots) {
		this.freeSlots = freeSlots;
	}

	/**
	 * @return the allReceivedMessage
	 */
	public List<Message> getAllReceivedMessage() {
		return allReceivedMessage;
	}

	/**
	 * @param allReceivedMessage
	 *            the allReceivedMessage to set
	 */
	public void setAllReceivedMessage(List<Message> allReceivedMessage) {
		this.allReceivedMessage = allReceivedMessage;
	}

	/**
	 * @return the ownMessage
	 */
	public Message getOwnMessage() {
		return ownMessage;
	}

	/**
	 * prüft ob es noch es noch freie Slots gibt
	 * 
	 * @return
	 */
	public boolean isFreeSlotNextFrame() {
		return freeSlots.size() > 0;
	}

	/**
	 * Waehlt einen freien zufaelligen Slot aus oder gibt reservierte Slot
	 * zurück, wenn es existiert
	 * 
	 * @return
	 */
	public byte getFreeSlot() {
		if (this.reservedSlot > 0) {
			return reservedSlot;
		} else {
			byte cS = calcNewSlot();
			return cS;
		}
	}

	public byte calcNewSlot() {
		Set<Byte> fs = new HashSet<Byte>(freeSlots);
		int size = fs.size();
//		System.out.println("> "+clockManager.getCorrectedTimeInMS()+" ::: "+Arrays.toString(fs.toArray()));
		if (size != 0) {
			int rand = random.nextInt(size);
			int i = 0;
			for(byte obj : fs)
			{
			    if (i == rand)
			        return obj;
			    i = i + 1;
			}
			return fs.iterator().next();//get(rand);
		} else {
			return 0;
		}

	}

	/**
	 * @return the reservedSlot
	 */
	public byte getReservedSlot() {
		return reservedSlot;
	}

	/**
	 * @param rs
	 *            the reservedSlot to set
	 */
	public void setReservedSlot(byte rs) {
		if (rs == 0 && reservedSlot != 0) {
			freeSlots.add((Byte) this.reservedSlot);
		}
		this.reservedSlot = rs;
	}

}
