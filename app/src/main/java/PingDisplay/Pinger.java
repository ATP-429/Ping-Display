package PingDisplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Pinger {
    private Process process = null;
    private BufferedReader in = null;
    private Runtime runtime;
    private long startTime;
    private int bufferSize;
    private ArrayList<Packet> pings = new ArrayList<Packet>();
    private String host;
    private String command;

    public Pinger(int bufferSize, String host) {
        this(bufferSize, host, "");
        try {
            this.command = getResourceFileAsString("pingSGP.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //bufferSize - Maximum of pings that'll be stored at a given point of time
    public Pinger(int bufferSize, String host, String command) {
        this.bufferSize = bufferSize;
        this.host = host;
        this.runtime = Runtime.getRuntime();
        this.command = command;
    }

    public void start() {
        startTime = System.currentTimeMillis();
		try {
            // https://stackoverflow.com/a/31776547/14686793
            process = runtime.exec(new String[] {"bash", "-c", command.replace("HOSTNAME", host)});
		}
		catch (IOException e) {
			e.printStackTrace();
        } 

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
    
    public void readInputs() {
        try {
            while (in.ready())
                System.out.println(readOneInput());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readOneInput() {
        try {
            String line = in.readLine();
            readLine(line);
            return line;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void readLine(String line) {
        long time = this.getTime();
        int value = -1;
        try {
            value = Integer.parseInt(line);
        }
        catch(NumberFormatException e) {
            //Must mean input is some ping failed error, like "Host not found" or "Packet dropped", etc. Ignore it, let value be -1
        }
        pings.add(new Packet(time, value));
        if(pings.size() > bufferSize) {
            pings.remove(0);
        }
    }

    public void readLines(String[] lines) {
        for(String line : lines)
            readLine(line);
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
    public static String getResourceFileAsString(String fileName) throws IOException {
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
