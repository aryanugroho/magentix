package jasonAgentsConversations.conversationsFactory.initiator;


import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jasonAgentsConversations.agent.ConvMagentixAgArch;
import jasonAgentsConversations.agent.Protocol_Template;
import jasonAgentsConversations.agent.protocolInternalAction;
import es.upv.dsic.gti_ia.core.ACLMessage;



public class ia_fipa_request_Initiator extends protocolInternalAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * It's necessary to define a new protocol Internal Action
	 */
	Jason_Fipa_Request_Initiator fri;

	@Override public int getMinArgs() { return 2; };
	@Override public int getMaxArgs() { return 6; };

	@Override
	public void checkArguments(Term[] args) throws JasonException
	{
		super.checkArguments(args);
		boolean result = false;

		if (((Term)args[args.length-1]).isAtom()){result=true;}

		result = (result && (((Term)args[0]).isString()) );
		
		if ( protocolSteep.compareTo(Protocol_Template.START_STEEP)==0)
		{
			int cont = 0; 
			for (Term t:args){
				switch (cont){
				case 1:result = (result&&t.isAtom());
				break;
				case 2:result = (result&&t.isNumeric());
				break;
				case 3:result = (result&&t.isNumeric());
				break;
				case 4:result = (result&&t.isString());
				break;
				}
				cont++;
			}
		}
		if ((protocolSteep.compareTo(Protocol_Template.REQUEST_STEEP)==0)||
				(protocolSteep.compareTo(Protocol_Template.TASK_DONE_STEEP)==0))
		{
			result = (result && (((Term)args[1]).isAtom()) );
		}
		if (protocolSteep.compareTo(Protocol_Template.REQUEST_STEEP)==0)
		{
			result = (result && (((Term)args[2]).isAtom()) );
		}
		
		if (!result)
		{
			throw JasonException.createWrongArgument(this,"Parameters must be in correct format.");
		}

	}


	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		protocolSteep = getTermAsString(args[0]);

		agentConversationID = getAtomAsString(args[args.length-1]);

		checkArguments(args);

		agName  = ts.getUserAgArch().getAgName();

		ts.getAg().getLogger().info("CALLING INTERNAL ACTION WITH STEEP: '"+protocolSteep+"'");

		/*
		 * When a FR Protocol is taking place*/
		if (protocolSteep.compareTo(Protocol_Template.START_STEEP)==0){

			String participant = getAtomAsString(args[1]);

			conversationTime = getTermAslong(args[2]);

			timeOut = getTermAsInt(args[3]); 			

			String initialInfo = getTermAsString(args[4]);

			//This time allows participants to join the conversation
			int wait = 0;
            while(wait<=100000){
            	wait++;
            }
			
			//building message template
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setProtocol("fipa-request");

			//int participantsNumber = participants.size();

			msg.setContent(agName+","+initialInfo+","+agentConversationID);


			/* The agent creates the CFactory that creates processors that initiate
        		 CONTRACT_NET protocol conversations. In this
        		 example the CFactory gets the name "TALK", we don't add any
        		 additional message acceptance criterion other than the required
        		 by the CONTRACT_NET protocol (null) and we do not limit the number of simultaneous
        		 processors (value 0)*/
			fri = new Jason_Fipa_Request_Initiator(agName, agentConversationID, ts, 
					((ConvMagentixAgArch)ts.getUserAgArch()).getJasonAgent().getAid(), timeOut);
			fri.initialMsg = initialInfo;
			fri.Participant=participant;

			Protocol_Factory = fri.newFactory("Protocol_Factory", null, 
					msg, 1, ((ConvMagentixAgArch)ts.getUserAgArch()).getJasonAgent(),timeOut);

			/* The factory is setup to answer start conversation requests from the agent
        		 using the FIPA_REQUEST protocol.*/

			((ConvMagentixAgArch)ts.getUserAgArch()).getJasonAgent().addFactoryAsInitiator(Protocol_Factory);

			/* finally the new conversation starts an asynchronous conversation.*/
			Protocol_Processor = Protocol_Factory.cProcessorTemplate();

			Protocol_Processor.createAsyncConversation(msg);



		}else
			if(protocolSteep.compareTo(Protocol_Template.REQUEST_STEEP)==0){

				fri.Participant = getAtomAsString(args[2]);
				fri.requestMsg = getTermAsString(args[1]);

				fri.Protocol_Semaphore.release();
			}else
				if(protocolSteep.compareTo(Protocol_Template.TASK_DONE_STEEP)==0){
					
					fri.Protocol_Semaphore.release();
					
				}

		return true;

	}



}
