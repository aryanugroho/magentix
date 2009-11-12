package _thomas_Example;

import java.util.ArrayList;

import es.upv.dsic.gti_ia.architecture.QueueAgent;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.organization.OMSProxy;
import es.upv.dsic.gti_ia.organization.SFProxy;

public class ClientAgent extends QueueAgent {

	public ClientAgent(AgentID aid) throws Exception {

		super(aid);

	}

	OMSProxy serviciosOMS = new OMSProxy();
	SFProxy sfservice = new SFProxy();
	public String result;

	public void execute() {

		ArrayList<String> results = new ArrayList<String>();
		ArrayList<AgentID> agents = new ArrayList<AgentID>();

		try{
		
		result = serviciosOMS.AcquireRole(this, "member", "virtual");

		result = serviciosOMS.AcquireRole(this,"customer", "travelagency");
		
		es.upv.dsic.gti_ia.architecture.Monitor mon = new es.upv.dsic.gti_ia.architecture.Monitor();
		mon.waiting(20 * 1000);
	
		
		int i=0;
		
		do{
			i++;
			results = sfservice.searchService(this, "SearchCheapHotel");
			
		}while(results.get(0).equals("null"));

		
		
		
		agents = sfservice.getProcess(this, results.get(0));

		for (AgentID agent : agents)
			System.out
					.println(this.getAid().name+" agents who have the service SearchCheapHotel: "
							+ agent.name);

		String res = sfservice.getProfile(this,results.get(0));
		
		
		
		ArrayList<String> arg = new ArrayList<String>();
		
		arg.add("One");
		arg.add("Two");
		arg.add("Three");
		
		
		sfservice.genericService(this,agents.get(0),res,"http://localhost:8080/sfservices/THservices/owl/owls/SearchCheapHotelProcess.owl", arg);
		
		
		}catch(Exception e){
			
			System.out.println("Error:"+ e.getMessage());
		}
		
		

	}

}
