package station;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Logger {

	private java.util.logging.Logger logger;
	private FileHandler fh;
	private long frame = 0;

	public Logger(String name) {
		int index = 0;
		File file;
		do {
			index++;
			file = new File(name+index+".log");
		} while (file.exists());

		logger = java.util.logging.Logger.getLogger(name);
		try {
			fh = new FileHandler(name+index+".log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printMessages(List<Message> allReceivedMessage, long currentFrame, long correction) {
		String text = ""; 
		long currentTime = System.currentTimeMillis();
		text += "================= F:"+currentFrame+" =:= Time"+currentTime+" (correction:"+correction+"|summe="+(currentTime+correction)+")\n";
		for (Message message : new ArrayList<Message>(allReceivedMessage)) {
			if (!message.isKollision()) 
				text += message.toString()+"\n";
			else
				text += "<<<<< Kollision >>>>> "+message.toString()+"\n";
		}	
		text += "=====================================================================================================\n";

		logger.info(text);
	}
	
	public void printMessage(Message message, long frame) {
		String text = "";
		if (this.frame != frame) {
			this.frame = frame; 
			text += "============================= "+this.frame+" ==============\n";
		}
		
		if (!message.isKollision()) 
			text +=message.toString();
		else
			text += "<<<<< Kollision >>>>> "+message.toString()+")";		

		logger.info(text+"\n");
	}

	public void print(String string) {
		logger.info(string);
	}
		
}
