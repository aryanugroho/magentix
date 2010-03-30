package es.upv.dsic.gti_ia.architecture;

import java.util.Date;
import java.util.logging.*;

import es.upv.dsic.gti_ia.architecture.FIPANames.InteractionProtocol;
import es.upv.dsic.gti_ia.core.ACLMessage;

/**
 * This class implements the FIPA-Request interaction protocol, Role Initiator
 * 
 * @author  Joan Bellver Faus, GTI-IA, DSIC, UPV
 * @version 2009.9.07
 */
public class FIPARequestInitiator {
	private final static int PREPARE_MSG_STATE = 0;
	private final static int SEND_MSG_STATE = 1;
	private final static int RECEIVE_REPLY_STATE = 2;
	private final static int RECEIVE_2ND_REPLY_STATE = 3;
	private final static int ALL_REPLIES_RECEIVED_STATE = 4;
	private final static int ALL_RESULT_NOTIFICATION_RECEIVED_STATE = 5;

	private MessageTemplate template = null;
	private int state = PREPARE_MSG_STATE;
	public QueueAgent myAgent;
	private ACLMessage requestmsg;
	private ACLMessage requestsentmsg;

	private Monitor monitor = null;

	private boolean finish = false;
	String conversationID = null;
	private long timeout = -1;
	private long endingtime = 0;
	private String name;
	private String port;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Create a new FIPA-Request interaction protocol, rol initiator.
	 * 
	 * @param agent
	 *            agent is the reference to the Agent Object
	 * @param msg
	 *            initial message
	 */
	public FIPARequestInitiator(QueueAgent agent, ACLMessage msg) {
		myAgent = agent;
		requestmsg = msg;
		this.monitor = myAgent.addMonitor(this);

	}

	 /**
	  * Return the agent.
	  * @return QueueAgent 
	  */
	 public QueueAgent getQueueAgent()
	 {
		return this.myAgent; 
		 
	 }
	 
	/**
	 * We will be able to know if it has finished the protocol
	 * 
	 * @return value a boolean value is returned, true: the protocol has
	 *         finished, false: the protocol even has not finished
	 */
	public boolean finished() {
		return this.finish;
	}

	/**
	 * returns the id of the message used in communication protocol
	 * @return conversationID
	 */
	public String getIdConversation() {
		return this.conversationID;

	}


	int getState() {
		return this.state;
	}
	
	 /**
	  *  Send a CANCEL Message for  active conversation and and terminates the protocol
	  */
	public void finish()
	 {
		 //Send a CANCEL Message for  active conversation
		 
		 
		 this.requestmsg.setPerformative(ACLMessage.CANCEL);
		 this.myAgent.send(requestmsg);
		 this.monitor.advise();
		 this.state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
		 
	 }
	 
	 

	 /**
	  *  Run the state machine with the communication protocol
	  */
	public void action() {
		switch (state) {
		case PREPARE_MSG_STATE: {

			ACLMessage msg = prepareRequest(this.requestmsg);
			this.requestsentmsg = msg;
			state = SEND_MSG_STATE;
			break;
		}
		case SEND_MSG_STATE: {

			ACLMessage request = this.requestsentmsg;
			if (request == null) {
				// finalización del protocolo
				this.finish = true;
				break;
			} else {
				if (request.getConversationId().equals("")) {
					conversationID = "C" + hashCode() + "_"
							+ System.currentTimeMillis();
					request.setConversationId(conversationID);
				} else {
					conversationID = request.getConversationId();
				}

				// configuramos el template
				template = new MessageTemplate(InteractionProtocol.FIPA_REQUEST);
				template.addConversation(conversationID);
				template.add_receiver(request.getReceiver());

				myAgent.setActiveConversation(conversationID);

				// fijamos el el timeout del mensaje
				Date d = request.getReplyByDate();
				if (d != null)
					timeout = d.getTime() - (new Date()).getTime();
				else
					timeout = -1;
				endingtime = System.currentTimeMillis() + timeout;
				
				// si el mensaje es para un agente Jade
				//lo puedo saber dependiendo de si el agente tiene alguna palabra concreta como /JADE o por ejemplo
				//si es un agente que no existe en la plataforma magentix es de jade.
				if (request.getReceiver() != null) {
					if (request.getReceiver(0).name.contains("/JADE")) {
						//name = response.getReceiver().name_all().substring(0,response.getReceiver().name_all().indexOf("@",response.getReceiver().name_all().indexOf("@") + 1));
						//port = response.getReceiver().port.substring(response.getReceiver().port.indexOf(":") + 1, response.getReceiver().port.indexOf("/", 10));
						
						
						request.getReceiver().host = request.getReceiver().name.substring(request.getReceiver().name.indexOf("@")+1,request.getReceiver().name.indexOf(":"));
						request.getReceiver().port = "7778";
						request.getReceiver().protocol = "http";

					}
				}

				
				
				
				myAgent.send(request);
				state = RECEIVE_REPLY_STATE;

			}
			break;

		}
		case RECEIVE_REPLY_STATE: {

			ACLMessage firstReply = myAgent.receiveACLMessage(template, 0);

			if (firstReply != null) {

				switch (firstReply.getPerformativeInt()) {
				case ACLMessage.AGREE: {
					state = RECEIVE_2ND_REPLY_STATE;
					handleAgree(firstReply);
					break;
				}
				case ACLMessage.REFUSE: {
					state = ALL_REPLIES_RECEIVED_STATE;
					handleRefuse(firstReply);
					break;
				}
				case ACLMessage.NOT_UNDERSTOOD: {
					state = ALL_REPLIES_RECEIVED_STATE;
					handleNotUnderstood(firstReply);
					break;

				}
				case ACLMessage.FAILURE: {
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleFailure(firstReply);
					break;

				}
				case ACLMessage.INFORM: {
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleInform(firstReply);
					break;

				}
				case ACLMessage.CANCEL:{
					
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleOutOfSequence(firstReply);
					break;
				}
				default: {
					// nos llega el segundo mensaje, habido problemas con el
					// primer mensaje
					state = RECEIVE_REPLY_STATE;
					handleOutOfSequence(firstReply);
					break;

				}
				}
				break;
			} else {

				if (timeout > 0) {
					long blocktime = endingtime - System.currentTimeMillis();

					if (blocktime <= 0)
						state = ALL_REPLIES_RECEIVED_STATE;
					else
						this.monitor.waiting(blocktime);
				} else {
					this.monitor.waiting();
					state = RECEIVE_REPLY_STATE;// state =
												// ALL_REPLIES_RECEIVED_STATE;
					break;
				}
			}
		}
		case RECEIVE_2ND_REPLY_STATE: {

			ACLMessage secondReply = myAgent.receiveACLMessage(template, 0);

			if (secondReply != null) {

				switch (secondReply.getPerformativeInt()) {
				case ACLMessage.INFORM: {
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleInform(secondReply);
					break;

				}
				case ACLMessage.FAILURE: {
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleFailure(secondReply);
					break;

				}
				default: {

					// state = RECEIVE_REPLY_STATE;
					state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
					handleOutOfSequence(secondReply);
					break;
				}
				}
				break;
			} else {
				this.monitor.waiting();
				state = RECEIVE_2ND_REPLY_STATE;
				break;
			}
		}
		case ALL_REPLIES_RECEIVED_STATE: {
			state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
			break;
		}
		case ALL_RESULT_NOTIFICATION_RECEIVED_STATE: {

			this.finish = true;
			this.requestmsg = null;
			this.requestsentmsg = null;
			// si somos los ultimos desactivamos el monitor
			this.myAgent.deleteMonitor(this);
			myAgent.deleteActiveConversation(conversationID);
			break;
		}

		}

	}


	/**
	 * This method must return the ACLMessage to be sent. This default
	 * implementation just return the ACLMessage object passed in the
	 * constructor. Programmer might override the method in order to return a
	 * different ACLMessage. Note that for this simple version of protocol, the
	 * message will be just send to the first receiver set.
	 * 
	 * @param msg
	 *            the ACLMessage object passed in the constructor.
	 * @return a ACLMessage.
	 */
	protected ACLMessage prepareRequest(ACLMessage msg) {
		return msg;
	}

	/**
	 * This method is called when a agree message is received.
	 * 
	 * @param msg
	 *            the received agree message.
	 */
	protected void handleAgree(ACLMessage msg) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "in HandleAgree: " + msg.toString());
	}

	/**
	 * This method is called when a refuse message is received.
	 * 
	 * @param msg
	 *            the received refuse message.
	 */
	protected void handleRefuse(ACLMessage msg) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "in HandleRefuse: " + msg.toString());
	}

	/**
	 * This method is called when a NotUnderstood message is received.
	 * 
	 * @param msg
	 *            the received NotUnderstood message.
	 */
	protected void handleNotUnderstood(ACLMessage msg) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "in HandleNotUnderstood: " + msg.toString());
	}

	/**
	 * This method is called when a Inform message is received.
	 * 
	 * @param msg
	 *            the received Inform message.
	 */
	protected void handleInform(ACLMessage msg) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "in HandleInform: " + msg.toString());
	}

	/**
	 * This method is called when a Failure message is received.
	 * 
	 * @param msg
	 *            the received Failure message.
	 */
	protected void handleFailure(ACLMessage msg) {
		if (logger.isLoggable(Level.FINEST))
			logger.log(Level.FINEST, "in HandleFailure: " + msg.toString());
	}

	/**
	 *	This method is called when a unexpected message is received.
	 * 
	 * @param msg
	 *            the received message
	 */
	protected void handleOutOfSequence(ACLMessage msg) {
		if (logger.isLoggable(Level.FINEST))
			logger.log(Level.FINEST, "in HandleOutOfSequence: "
					+ msg.toString());
	}

}
