package PingDisplay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Pinger {
    private Process process = null;
    private BufferedReader in = null;
    private Runtime runtime = Runtime.getRuntime();
    private long startTime;

    public Pinger() {
        startTime = System.currentTimeMillis();
		try {
			process = runtime.exec("res/pingSGP.sh");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			in = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
		
		runtime.addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				kill(process.toHandle());
			}
		}));
    }
    
    public ArrayList<Packet> read() {
        ArrayList<Packet> readings = new ArrayList<Packet>();
        try
        {
            if (in.ready())
            {
                String line = in.readLine(); //Reads all remaining bytes from input stream, so only the new lines will be read
                
                
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

    //Code stolen from a guy on stackoverflow
	public static void kill(ProcessHandle handle)
	{
		handle.descendants().forEach((child) -> kill(child));
		handle.destroy();
	}
}
