package EjemploQuery;

import org.apache.qpid.transport.Connection;

import EjemploRequest.Principal_Grafico;

import es.upv.dsic.gti_ia.proto.*;
import es.upv.dsic.gti_ia.fipa.AgentID;
import es.upv.dsic.gti_ia.magentix2.QueueAgent;
import es.upv.dsic.gti_ia.proto.MessageTemplate;
import es.upv.dsic.gti_ia.proto.FIPANames.InteractionProtocol;
import es.upv.dsic.gti_ia.fipa.*;

public class Aeropuerto extends QueueAgent {

	
	
    
    public Aeropuerto(AgentID aid, Connection connection)
    {
    	super(aid, connection);   
    }
    protected void execute() {
        System.out.println(this.getName() + ": Abriendo centralita...");
 
        // Filtrado para recibir s�lo mensajes del protocolo FIPA-Query.
    
        
        
        MessageTemplate plantilla = new MessageTemplate(InteractionProtocol.FIPA_QUERY);
        ComprobarResponder responder = new ComprobarResponder(this, plantilla);
		System.out.println("Aeropuerto "+this.getName()+": Esperando avisos...");
    	do{
      		responder.action();
    	}while(true);
    }
 
    class ComprobarResponder extends FIPAQueryResponder {
        public ComprobarResponder(QueueAgent agente, MessageTemplate plantilla) {
            super(agente, plantilla);
        }
 
        protected ACLMessage prepareResponse(ACLMessage request)
                throws NotUnderstoodException, RefuseException {
            System.out.printf("Operadora: Hemos recibido una llamada de %s solicitando informacion sobre su reserva.", request.getSender().getLocalName());
 
            // Si el solicitante es v�lido se acepta su petici�n.
 
            if (true){//comprobarSolicitante(request.getSender().getLocalName())) {
                System.out.println("Operadora: Espere un momento por favor...");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            } else {
                System.out.println(getName() + ": Todas las operadoras estan ocupadas.");
                throw new RefuseException("Por favor intentelo de nuevo mas tarde");
            }
        }
 
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            String retorno = "No dispone de ninguna reserva";
 
            if (comprobarSolicitante(request.getSender().getLocalName()))
                retorno = "Si que ha hecho alguna reserva";
 
            inform.setContent(retorno);
            return inform;
        }
 
        // M�todo simple de aceptaci�n o rechazo de solicitudes.
        private boolean comprobarSolicitante(String nombre) {
            return (nombre.length() > 25);
        }
    }
	
}