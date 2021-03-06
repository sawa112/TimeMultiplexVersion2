package station;


import java.io.IOException;
import java.io.InputStreamReader;

public class DataSourceListener extends Thread{
	 

	private final int DATA_SIZE = 24;
	private DataManager dataManager;


	public DataSourceListener(DataManager dataManager ){ 
		this.dataManager = dataManager;
	}
	
	public void run() {		
		InputStreamReader input = new InputStreamReader(System.in);
		while(true){
			char[] dataBuffer = new char[DATA_SIZE];
			try {
				
				input.read(dataBuffer);
				dataManager.setDataBuffer(dataBuffer); 
				 
			} catch (IOException e) { 
				e.printStackTrace();
			} 
		}
	}
	 
}
