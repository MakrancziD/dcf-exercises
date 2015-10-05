package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

public class ExerciseNrTwo extends ExercisesBase
{

	public void CreateVMs() throws Exception
	{
		PhysicalMachine[] phMach = new PhysicalMachine[10];
		
		IaaSService iaasSvc = getNewIaaSService();
		VirtualAppliance va = new VirtualAppliance("va",1,0);
		
		for(int i=0;i<phMach.length;i++)
		{
			phMach[i]= getNewPhysicalMachine();
			phMach[i].turnon();
			
			iaasSvc.registerHost(phMach[i]);
			iaasSvc.registerRepository(phMach[i].localDisk);
			phMach[i].localDisk.registerObject(va);
			
			Timed.simulateUntilLastEvent();
		}

		ResourceConstraints rc = new AlterableResourceConstraints(10, 10, 16);
		
		for(int i=0;i<10;i++)
		{
			iaasSvc.requestVM(va, rc, phMach[i].localDisk, 10);
		}
		
		Timed.simulateUntilLastEvent();
	}
}
