package EjemploContractNet;

import org.apache.qpid.transport.Connection;


import es.upv.dsic.gti_ia.magentix2.QueueAgent;
import es.upv.dsic.gti_ia.proto.Adviser;
import es.upv.dsic.gti_ia.fipa.*;
import es.upv.dsic.gti_ia.proto.FIPANames;
import es.upv.dsic.gti_ia.proto.*;
import java.util.Date;
import java.util.ArrayList;

public class Cliente extends QueueAgent {

	private Adviser adv= new Adviser();
	private int precionMaximo;
	private int numeroDeOfertas=0;
    public Cliente(AgentID aid, Connection connection)
    {

    	super(aid, connection);
        
    
    }
    
    protected void execute() {
        //El precio m�ximo se recibir� como argumento de entrada.
        
 
        
            this.precionMaximo = 20000000;
 
            //B�squeda del servicio de venta de coches en las p�ginas amarillas.
              
                                  //Creamos el mensaje CFP(Call For Proposal) cumplimentando sus par�metros
                    ACLMessage mensajeCFP = new ACLMessage(ACLMessage.CFP);
                    
                    mensajeCFP.add_receiver(new AgentID("Concesionario","qpid","localhost",""));
                    mensajeCFP.add_receiver(new AgentID("Concesionario1","qpid","localhost",""));
                    
            //Protocolo que vamos a utilizar
                    mensajeCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    mensajeCFP.setContent("Busco coche, �proponeis precios?");
                    //mensajeCFP.setSender(getAid());
                    //Indicamos el tiempo que esperaremos por las ofertas.
                    mensajeCFP.setReplyByDate(new Date(System.currentTimeMillis() + 1500000));
 
                    //Se a�ade el comportamiento que manejar� las ofertas.
                    
                    this.setTarea(new ManejoOpciones(this, mensajeCFP));
                 
                    es.upv.dsic.gti_ia.proto.Adviser adv = new Adviser();
                    adv.esperar();
 
             

 
    } // Fin del setup
 
    private class ManejoOpciones extends FIPAContractNetInitiator {
 
        public ManejoOpciones(QueueAgent agente, ACLMessage plantilla) {
            super(agente, plantilla);
        }
 
        //Manejador de proposiciones.
        protected void handlePropose(ACLMessage propuesta, ArrayList aceptadas) {
            System.out.printf("%s: Recibida oferta de autos %s. Ofrece un coche por %s euros.\n",
                this.myAgent.getName(), propuesta.getSender().getLocalName(), propuesta.getContent());
        }
 
        //Manejador de rechazos de proposiciones.
        protected void handleRefuse(ACLMessage rechazo) {
            System.out.printf("%s: Autos %s no tiene coches que ofrecer.\n",
                this.myAgent.getName(), rechazo.getSender().getLocalName());
        }
 
        //Manejador de respuestas de fallo.
        protected void handleFailure(ACLMessage fallo) {
          //  if (fallo.getSender().equals(myAgent.getAMS())) {
 
        //Esta notificacion viene del entorno de ejecuci�n JADE (no existe el receptor)
                System.out.println("AMS: Esta venta de autos no existe o no es accesible");
            //} else {
                System.out.printf("%s: Autos %s ha sufrido un fallo.\n",
                    this.myAgent.getName(), fallo.getSender().getLocalName());
            //}
            //Fall�, por lo tanto, no recibiremos respuesta desde ese agente
            Cliente.this.numeroDeOfertas--;
        }
 
        //M�todo colectivo llamado tras finalizar el tiempo de espera o recibir todas las propuestas.
        protected void handleAllResponses(ArrayList respuestas, ArrayList aceptados) {
 
        //Se comprueba si una venta de autos se pas� del plazo de env�o de ofertas.
            if (respuestas.size() < numeroDeOfertas) {
                System.out.printf("%s: %d ventas de autos llegan tarde.\n",
                    this.myAgent.getName(), Cliente.this.numeroDeOfertas - respuestas.size());
            }
 
            //Escogemos la mejor oferta
            int mejorOferta = Integer.MAX_VALUE;
            AgentID mejorAutos = null;
            ACLMessage aceptado = null;
            for (Object resp:respuestas) {
                ACLMessage mensaje = (ACLMessage) resp;
                if (mensaje.getPerformativeInt() == ACLMessage.PROPOSE) {
                    ACLMessage respuesta = mensaje.createReply();
                    respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    aceptados.add(respuesta);
 
                    //Si la oferta es la mejor (inferior a todas las otras)
                    //Se almacena su precio y el AID de la venta de autos que la hizo.
                    int oferta = Integer.parseInt(mensaje.getContent());
                    if (oferta <= precionMaximo && oferta <= mejorOferta) {
                        mejorOferta = oferta;
                        mejorAutos = mensaje.getSender();
                        aceptado = respuesta;
                    }
                }
            }
 
            //Si hay una oferta aceptada se modifica su performativa.
            if (aceptado != null) {
                System.out.printf("%s: Decidido!!! Compro el coche de Autos %s\n",
                    this.myAgent.getName(), mejorAutos.getLocalName());
                aceptado.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }
 
        //Manejador de los mensajes inform.
        protected void handleInform(ACLMessage inform) {
            System.out.printf("%s: Autos %s te ha enviado el contrato.\n",
                this.myAgent.getName(), inform.getSender().getLocalName());
        }
    }
}