import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class PetDetective {
    static HashMap<String, Node> nodes = new HashMap();
    static int maximumAllowedSteps;
    static int noOfPets;
    static int carCapacity;
    static int backtrack = 0;
    /**
     * To do file operations. You can see the traversal in traversalSteps.txt
     * @author INCT-RevanthKumar
     *
     */
    static class MyFile{
    	File file;
    	FileWriter fileWriter;
    	MyFile() {
    		file = new File("traversalSteps.txt");
    		try {    			
    			file.createNewFile();
    			fileWriter = new FileWriter("traversalSteps.txt");
    		} catch(IOException e) {
    			System.err.println("Cannot create file");
    		}
    	}
    	public void write(String line) {
    		try {    			
    			fileWriter.write(line+"\n");
    		} catch(IOException e) {
    			System.err.println("Cannot write to file");
    		}
    	}
    	public void finalize() throws Throwable {
    		fileWriter.close();
    	}
    	public void close() {
    		try {    			
    			fileWriter.close();
    		} catch(IOException e) {
    			System.err.println("Cannot close file");
    		
    	}
    }}
    /**
     * Represents a node in graph
     * @author INCT-RevanthKumar
     *
     */
	static class Node{
		String name;
		HashMap<Node, Integer> nearBy;
		boolean isHome = false;
		Node suitableHome, suitablePet;
		Node(String name) {
			this.name = name;
			nearBy = new HashMap();
			nearBy.put(this,0);
			this.isHome = name.contains("home");
		}
		public void connectNode(Node n, int distance) {
			this.nearBy.put(n, distance);
			n.nearBy.put(this, distance);
		}
		public int distanceTo(Node n) {
			return (this.nearBy.containsKey(n))? this.nearBy.get(n) : -1;
		}
		public boolean isConnected(Node n) {
			return this.nearBy.containsKey(n);
		}
		public Node[] getNeighbourNodes() {
			Node[] neighboursAry = this.nearBy.keySet().stream().filter(n -> !this.equals(n))
					.toArray(Node[]::new);
			Arrays.sort(neighboursAry, (n1,n2) -> {
				return this.distanceTo(n1) - this.distanceTo(n2);
			});
			return neighboursAry;
		}
		@Override
		public String toString() {
			return "Node [name=" + name + ", nearBy=" + "]";
		}
	}
	/**
	 * To keep track of progress
	 * @author RevanthKumar
	 *
	 */
	static class Progress {
		int travelledSteps;
		int noOfDeliveredPets;
		List<String>petsInCar = new ArrayList();
		List<String> petsDelivered = new ArrayList();
		List<String> steps = new ArrayList();
		public Progress clone() {
			Progress p = new Progress();
			p.travelledSteps = this.travelledSteps;
			p.noOfDeliveredPets = this.noOfDeliveredPets;
			p.petsInCar = new ArrayList(this.petsInCar);
			p.petsDelivered = new ArrayList(this.petsDelivered);
			p.steps = new ArrayList(this.steps);
			return p;
		}
		public boolean addPetToCar(Node pet) {
			if(petsInCar.size()<4)
			{
				petsInCar.add(pet.name);
				System.out.println("Picked Pet "+pet.name);
				myFile.write("Picked Pet "+pet.name);
				return true;
			}
			System.out.println("Cannot pick "+pet.name+", no space in car");
			myFile.write("Cannot pick "+pet.name+", no space in car");
			return false;
		}
		public void dropPet(Node pet) {
			this.petsInCar.remove(pet.name);
			this.petsDelivered.add(pet.name);
			System.out.println("Dropped Pet "+pet.name);
			myFile.write("Dropped Pet "+pet.name);
		}
		@Override
		public String toString() {
			StringBuilder petsInCar = new StringBuilder();
			this.petsInCar.forEach(pet->petsInCar.append(pet+", "));
			StringBuilder petsDelivered = new StringBuilder();
			this.petsDelivered.forEach(pet->petsDelivered.append(pet+", "));
			return "Pets In Car: "+petsInCar.toString()+"\n"+"Pets Delivered "+petsDelivered.toString()+"\nTravelled Steps: "+this.travelledSteps;
			
		}
	}
	static MyFile myFile = new MyFile();
	public static void main(String args[]) {
		Scanner s = new Scanner(System.in);
		System.out.println("Enter car capacity for pets:");
		carCapacity = s.nextInt();
		System.out.println("Enter the no. of steps");
		maximumAllowedSteps = s.nextInt();
		LinkedList<String> inputList = new LinkedList();
		/* Starting input operations */
		System.out.println("Naming Convention:\nFor Pet: any name representing animal and color"
				+ "\nFor Pet Home <animal_name>_home this is must otherwise algorithm doesn't work"
				+ "\nFor car just car"
				+ "Enter input in the format <name1> <name2> <distance>"
				+ "\nEg: yellow_dog cat 3"
				+ "\nEnter end to stop giving user input");
		s.nextLine();
		while(true) {
			String inpLine = s.nextLine();
			if(inpLine.equals("end"))
				break;
			inputList.add(inpLine);
		}
		/* Input operations done*/
		
		// processing input and creating nodes for graph
		inputList.forEach(inputLine -> {
			String inps[] = inputLine.split(" ");
			Node n1,n2;
			n1 = (nodes.get(inps[0]) == null)?new Node(inps[0]):nodes.get(inps[0]);
			n2 = (nodes.get(inps[1]) == null)?new Node(inps[1]):nodes.get(inps[1]);
			nodes.put(inps[0],n1);
			nodes.put(inps[1],n2);
			n1.connectNode(n2, Integer.parseInt(inps[2]));
		});
		
		// determining home and pet for node
		nodes.values().forEach(node -> {
			if(node.name.contains("home")) {
				String vars[] = node.name.split("_");
				Node pet = nodes.get(vars[0]);
				node.suitablePet = pet;
				pet.suitableHome = node;
			}
		});
		noOfPets = (nodes.size() -1)/2;
		floydWarshell();
		Node car = nodes.get("car");
		Progress progress = new Progress();
		progress.noOfDeliveredPets = 0;
		progress.travelledSteps = 0;
		boolean solutionExists = startSearchingForSolution(car, progress);
		System.out.println("is solution?"+solutionExists);
		myFile.write("is solution?"+solutionExists+"\n");
		myFile.close();
	}
	/**
	 * finds shortest path between all the nodes
	 */
	public static void floydWarshell() {
		Node[] nodesAry = nodes.values().toArray(new Node[0]);
		for(int k = 0;k<nodesAry.length; ++k) {
			Node nodeK = nodesAry[k];
			for( int i=0;i<nodesAry.length;++i) {
				Node nodeI = nodesAry[i];
				for(int j=0;j<nodesAry.length;++j) {
					Node nodeJ = nodesAry[j];
					if(nodeK.isConnected(nodeI) && nodeK.isConnected(nodeJ) && (nodeI.distanceTo(nodeJ)== -1 || nodeI.distanceTo(nodeJ)>nodeI.distanceTo(nodeK) + nodeJ.distanceTo(nodeK))) {
						nodeI.connectNode(nodeJ, nodeI.distanceTo(nodeK) + nodeJ.distanceTo(nodeK));
					}
				}
			}
		}
	}
	
	/**
	 * backtracking algorithm to find solution.
	 * performs a exhaustive recursive search to find solution.
	 * @param currentNode
	 * @param progress
	 * @return
	 */
	public static boolean startSearchingForSolution(Node currentNode,Progress progress) {
		++backtrack;
		int currentBacktrack = backtrack;
		System.out.println("Entered to new level: "+ currentBacktrack+" current node "+ currentNode.name+" travelled steps: "+ progress.travelledSteps);
		myFile.write("Entered to new level: "+ currentBacktrack+" current node "+ currentNode.name+" travelled steps: "+ progress.travelledSteps);
		
		// not a solution if travelled steps exceeds max steps 
		if(progress.travelledSteps > maximumAllowedSteps )
			return false;
		// solution if all pets are delivered
		if(progress.petsDelivered.size() == noOfPets)
		{
			StringBuilder result = new StringBuilder("*******SUCCESS*******\nSolutions Exists\n");
			progress.steps.forEach(step -> result.append(step+"\n"));
			System.out.println(result.toString());
			myFile.write(result.toString());
			return true;
		}
		Node[] neighbours = currentNode.getNeighbourNodes();
		for(Node nextNode: neighbours) {
			if(nextNode.name.equals("car")) continue;
			// executes if a node is home and there is pet in car which has it as home.
			if(nextNode.isHome && progress.petsInCar.contains(nextNode.suitablePet.name)
					&& !progress.petsDelivered.contains(nextNode.suitablePet.name)
					&& progress.travelledSteps + currentNode.distanceTo(nextNode) <= maximumAllowedSteps) {
				Progress progressClone = null;
				try {					
					progressClone = (Progress)progress.clone();
				} catch(Exception e) {
					System.err.println(e.getLocalizedMessage());
				}
				progressClone.travelledSteps += currentNode.distanceTo(nextNode);
				progressClone.steps.add("From " + currentNode.name + " To " + nextNode.name);
				progressClone.dropPet(nextNode.suitablePet);
				if(startSearchingForSolution(nextNode, progressClone))
				{
					return true;
				}
				else {
					System.out.println("BackTracked to level "+ currentBacktrack+ " currentNode: "+ currentNode.name);
					myFile.write("BackTracked to level "+ currentBacktrack+ " currentNode: "+ currentNode.name);
					backtrack = currentBacktrack;
				}
			} else if(!nextNode.isHome && 
					!progress.petsDelivered.contains(nextNode.name) && !progress.petsInCar.contains(nextNode.name)
					&& progress.petsInCar.size() < 4
					&& progress.travelledSteps + currentNode.distanceTo(nextNode) <= maximumAllowedSteps) {
				Progress progressClone = null;
				try {					
					progressClone = (Progress)progress.clone();
				} catch(Exception e) {
					System.err.println(e.getLocalizedMessage());
				}
				progressClone.travelledSteps += currentNode.distanceTo(nextNode);
				progressClone.steps.add("From " + currentNode.name + " To " + nextNode.name);
				progressClone.addPetToCar(nextNode);
				if(startSearchingForSolution(nextNode, progressClone)) {
					return true;
				}
				else {
					System.out.println("BackTracked to level "+ currentBacktrack+ " currentNode: "+ currentNode.name);
					myFile.write("BackTracked to level "+ currentBacktrack+ " currentNode: "+ currentNode.name);
					backtrack = currentBacktrack;
				}
			}
		}
		return false;
	}
}
