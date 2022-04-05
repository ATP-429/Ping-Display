package PingDisplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Pinger {
    private Process process = null;
    private BufferedReader in = null;
    private Runtime runtime = Runtime.getRuntime();
    private long startTime;
    private int bufferSize;
    private ArrayList<Packet> pings = new ArrayList<Packet>();

    //bufferSize - Maximum of pings that'll be stored at a given point of time
    public Pinger(int bufferSize) {
        this.bufferSize = bufferSize;
        startTime = System.currentTimeMillis();
		try {
            // https://stackoverflow.com/a/31776547/14686793
            process = runtime.exec(new String[] {"bash", "-c", getResourceFileAsString("pingSGP.sh")});
		}
		catch (IOException e) {
			e.printStackTrace();
        } 
        //catch (URISyntaxException e) {
          //  e.printStackTrace();
        //}

		try {
			in = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
		
		runtime.addShutdownHook(new Thread(new Runnable() {
			public void run() {
				kill(process.toHandle());
			}
		}));
    }
    
    public void readAll() {
        try {
            if (in.ready())
            {
                String line = in.readLine(); //Reads all remaining bytes from input stream, so only the new lines will be read
                pings.add(new Packet(this.getTime(), Integer.parseInt(line)));
                if(pings.size() > bufferSize) {
                    pings.remove(0);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Packet> getPings() {
        return pings;
    }

    public void removeFirst() {
        pings.remove(0);
    }

    public long getTime() {
        return System.currentTimeMillis()-startTime;
    }

    //Code stolen from a guy on stackoverflow
    public static void kill(ProcessHandle handle)
    {
        handle.descendants().forEach((child) -> kill(child));
        handle.destroy();
    }

    //https://stackoverflow.com/a/46613809
    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
