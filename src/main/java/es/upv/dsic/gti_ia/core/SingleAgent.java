package es.upv.dsic.gti_ia.core;

import java.util.ArrayList;

/**
 * @author Ricard Lopez Fogues
 * @author Sergio Pajares Ferrando
 * @author Joan Bellver Faus
 */

public abstract class SingleAgent extends BaseAgent {

	private ArrayList<ACLMessage> messageList;

	/**
	 * Creates a new SingleAgent
	 * 
	 * @param aid
	 *            Agent Id
	 * @param connection
	 *            Connection the agent will use
	 * @throws Exception
	 *             if agent id already exists on the platform
	 */
	public SingleAgent(AgentID aid) throws Exception {
		super(aid);
		messageList = new ArrayList<ACLMessage>();
	}

	/**
	 * Method to receive a magentix2 AclMessage
	 * Blocker receiver, waiting a message
	 * @return an ACLMessage
	 * @throws Exception
	 */
	public final ACLMessage receiveACLMessage() {

		boolean condition = false;
		ACLMessage msg = new ACLMessage(0);
		int i = 0;
		
		while(!condition) {

			msg = messageList.get(i);

			if (msg != null) {
					condition = true;
			}
		}
		messageList.remove(msg);
		return msg;
//		logger.debug("messageList empty on receiveACLMessage");
//		throw new Exception("messageList empty on receiveACLMessage");

		// MessageTransfer xfr = new MessageTransfer();
		// try {
		// xfr = internalQueue.take();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//        
		// return MessageTransfertoACLMessage(xfr);

	}

	private synchronized void writeQueue(ACLMessage msg) {
		messageList.add(msg);
	}

	public void onMessage(ACLMessage msg) {
		this.writeQueue(msg);
	}
}