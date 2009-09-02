
package benchmarks.bench3;

import org.apache.qpid.transport.Connection;

import es.upv.dsic.gti_ia.fipa.AgentID;
import es.upv.dsic.gti_ia.magentix2.BridgeAgentInOut;
import es.upv.dsic.gti_ia.magentix2.SingleAgent;

/**
 * This class is responsible for creating receiver's agents like the first one.
 * @author Sergio, Ricard
 * 
 * FALTA PERMITIR INVOCAR A NUESTROS AGENTES CON UN ARGUMENTO !!!
 *
 */
public class ClonadorBenchmark2b extends SingleAgent {
	
	int nagents;
	
	public ClonadorBenchmark2b(AgentID aid, Connection connection, int nagents) {
		super(aid, connection);
		this.nagents = nagents;
	}
	
	public void execute(){
	
/*	if(args.length != 1)
	  {
	      System.out.println("Error, Debe invocar la clase as�: clonador\"(\" nreceptores \")\"");
	      System.exit(1);
	  }
	*/
	String classe = "receptor";
//    int nagents = Integer.parseInt(args[0].toString());

    for(int i=1;i<=nagents;i++)
        {
          try{
        	  ReceptorBenchmark2b agenteReceptor = new ReceptorBenchmark2b(new AgentID(classe + i, this.getAid().protocol, this.getAid().host,this.getAid().port+1),this.getConnection());
             
        	  agenteReceptor.start();

          }
          catch(Exception e){System.out.println("Error, en la clonaci�n de agentes receivers");}

        }
	}


}
