package hu.unimiskolc.iit.distsys;


import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.FillInAllPMs;

public class PMFiller implements FillInAllPMs 
{

	@Override
	public void filler(IaaSService iaas, int vmCount)
	{

		VirtualAppliance va = new VirtualAppliance("va",1,1);
		
		ResourceConstraints rc = new AlterableResourceConstraints(10, 10, 16);
		
		for(int i=0;i<iaas.machines.size();i++)
		{
			int vmPerIteration = vmCount/iaas.machines.size();
			try{
				ResourceConstraints asdf = iaas.machines.get(i).getCapacities();
				System.out.println(iaas.machines.get(i).getState().toString());
				iaas.machines.get(i).turnon();
				Timed.simulateUntilLastEvent();
				System.out.println(iaas.machines.get(i).getState().toString());
				System.out.println(asdf.getRequiredCPUs()+" "+asdf.getRequiredProcessingPower()+" "+asdf.getRequiredMemory());
				
				ResourceConstraints rc2 = new AlterableResourceConstraints(
						asdf.getRequiredCPUs()/vmPerIteration,
						asdf.getRequiredProcessingPower()/vmPerIteration,
						asdf.getRequiredMemory()/vmPerIteration);
			VirtualMachine[] vmm = iaas.requestVM(va, rc2, iaas.machines.get(i).localDisk, vmPerIteration);
			for(int j = 0;j<vmm.length;j++)
			{
				System.out.println(vmm[j].getState().toString());
				System.out.println(vmm[j].toString());
			}
			}
			catch(Exception e){System.out.println(e);}
		}
		
		Timed.simulateUntilLastEvent();
		
	}
	

}
