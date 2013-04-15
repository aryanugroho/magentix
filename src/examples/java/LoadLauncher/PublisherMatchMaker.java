package LoadLauncher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import LoadLauncher.Load.*;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
//import es.upv.dsic.gti_ia.core.BaseAgent;

public class PublisherMatchMaker extends BaseAgent {
	private String LOG_FILE_NAME;
	
	private ArrayList<Publication> publications;
	AgentID matchmakerAid;
	private ArrayList<Transmission> transmissions;
	private ArrayList<Long> sequences;
	private long messages_to_send;

	public PublisherMatchMaker(Load load_spec, int index) throws Exception {
		super(load_spec.getPublishers().get(index).getAid());

		this.LOG_FILE_NAME = load_spec.getOutPath() + "/" +
			Load.prefixes[load_spec.getStrategy()] + "_" + this.getName() + "_result_log.txt";
		this.publications = load_spec.getPublishers().get(index).getPublications();
		this.transmissions = new ArrayList<Transmission>();
		this.matchmakerAid=load_spec.getMiddleAgentID();
		this.sequences = new ArrayList<Long>();
		this.messages_to_send = 0;
		
		Publication auxPub;
		
		Iterator<Publication> pubIterator = publications.iterator();
		
		ACLMessage msg;
		while (pubIterator.hasNext()){
			// Publish things
			auxPub=pubIterator.next();
			msg = new ACLMessage();
			msg.setLanguage("ACL");
			msg.setPerformative(ACLMessage.REQUEST);
			msg.setReceiver(matchmakerAid);
			msg.setSender(this.getAid());
			msg.setContent(String.valueOf(System.currentTimeMillis()) + "#PUBLISH#" + auxPub.getChannelName());
			send(msg);
			
			sequences.add(auxPub.getMessagesToSend());
			messages_to_send = messages_to_send + sequences.get(publications.indexOf(auxPub));
		}
	}

	private Publication getPublication(String channelName){
		Iterator<Publication> pubIter = publications.iterator();
		Publication auxPub;
		
		while(pubIter.hasNext()){
			auxPub = pubIter.next();
			
			if (auxPub.getChannelName().equals(channelName)){
				return auxPub;
			}
		}
		
		return null;
	}
	
	public void execute() {
		Iterator<Publication> pubIterator;
		Iterator<AgentID> AidIterator;
		Publication auxPub, nextPub=null;
		ACLMessage msg;
		Long currentTime = System.currentTimeMillis();
		long nextStop=Long.MAX_VALUE, timeout;
		
		Random generator = new Random(System.currentTimeMillis());
		
		// Initial transmissions
		pubIterator=publications.iterator();
		while(pubIterator.hasNext()){
			auxPub = pubIterator.next();
			
			try {
				timeout=generator.nextInt(5000);
				synchronized(this){
					wait(timeout);
				}
				currentTime = System.currentTimeMillis();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			Long auxLong=sequences.get(publications.indexOf(auxPub));
			
			msg=new ACLMessage();
			msg.setLanguage("ACL");
			msg.setPerformative(ACLMessage.INFORM);
			msg.setSender(this.getAid());
			msg.setContent(String.valueOf(System.currentTimeMillis()) + "#" + String.valueOf(auxLong) + "#" + auxPub.getChannelName());
			AidIterator=auxPub.getSubscribers().iterator();
			while(AidIterator.hasNext()){
				msg.setReceiver(AidIterator.next());
				send(msg);
			}
			
			auxLong--;
			
			sequences.set(publications.indexOf(auxPub), auxLong);
			auxPub.setNextPublication(currentTime + auxPub.getPeriod());
			if (nextStop > auxPub.getNextPublication()){
				nextStop = auxPub.getNextPublication();
				nextPub=auxPub;
			}
			
			messages_to_send--;
		}
		
		while (messages_to_send > 0){
			try {
				if ((timeout=(nextStop-currentTime)) > 0){
					synchronized(this){
						wait(timeout);
					}
				}
//				else {
////					System.out.println("Que me cago, que no llego..!");
//				}
				currentTime = System.currentTimeMillis();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			Long auxLong=sequences.get(publications.indexOf(nextPub));
			
			msg=new ACLMessage();
			msg.setLanguage("ACL");
			msg.setPerformative(ACLMessage.INFORM);
			msg.setSender(this.getAid());
			msg.setContent(String.valueOf(System.currentTimeMillis()) + "#" + String.valueOf(auxLong) + "#" + nextPub.getChannelName());
			AidIterator=nextPub.getSubscribers().iterator();
			while(AidIterator.hasNext()){
				msg.setReceiver(AidIterator.next());
				send(msg);
			}
			
			auxLong--;
			sequences.set(publications.indexOf(nextPub), auxLong);
			
			if (auxLong > 0){
				nextPub.setNextPublication(currentTime + nextPub.getPeriod());
			}
			else{
				nextPub.setNextPublication(Long.MAX_VALUE);	
			}
			nextStop=nextPub.getNextPublication();
			
			pubIterator=publications.iterator();
			while(pubIterator.hasNext()){
				auxPub = pubIterator.next();
				if (nextStop > auxPub.getNextPublication()){
					nextStop = auxPub.getNextPublication();
					nextPub=auxPub;
				}
			}
			
			messages_to_send--;
		}
		
		System.out.println(this.getName() + " writing data...");
		
		// Append to file
		BufferedWriter log_file;
		try {
			log_file = new BufferedWriter(new FileWriter(LOG_FILE_NAME, true));
			Transmission auxTrans;
			Iterator<Transmission> transIter;
			transIter=transmissions.iterator();
			while (transIter.hasNext()){
				auxTrans=transIter.next();
				log_file.write(auxTrans.toString()+"\n");
			}
			log_file.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onMessage(ACLMessage msg){
		Transmission trans;
		
		int index=msg.getContent().indexOf('#');
		trans = new Transmission(msg.getSender(),
				this.getAid(), new Date(Long.parseLong(msg.getContent().substring(0, index))),
				new Date(System.currentTimeMillis()), Transmission.SYSTEM, msg.getContent().substring(index+1));
		transmissions.add(trans);
				
		if (msg.getPerformativeInt() == ACLMessage.SUBSCRIBE){
			String channelName=msg.getContent().substring(index+1);
			Publication auxPub;
			if ((auxPub=this.getPublication(channelName)) != null){
				auxPub.getSubscribers().add(msg.getSender());
				ACLMessage replyMsg = new ACLMessage();
				replyMsg.setLanguage("ACL");
				replyMsg.setSender(this.getAid());
				replyMsg.setReceiver(msg.getSender());
				replyMsg.setPerformative(ACLMessage.AGREE);
				replyMsg.setContent(String.valueOf(System.currentTimeMillis()) + "#" + channelName);
				send(replyMsg);
			}
		}
		else if (msg.getPerformativeInt() == ACLMessage.CANCEL){
			String channelName=msg.getContent();
			Publication auxPub;
			if ((auxPub=this.getPublication(channelName)) != null){
				auxPub.getSubscribers().remove(msg.getSender());
				ACLMessage replyMsg = new ACLMessage();
				replyMsg.setLanguage("ACL");
				replyMsg.setSender(this.getAid());
				replyMsg.setReceiver(msg.getSender());
				replyMsg.setPerformative(ACLMessage.AGREE);
				replyMsg.setContent(String.valueOf(System.currentTimeMillis()) + "#" + channelName);
				send(replyMsg);
			}
		}
	}
}
