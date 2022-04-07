package PingDisplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FunctionTest  {
    @Test
    public void testParsing() {
        System.out.println("Running tests...");
        Pinger pinger = new Pinger(100, null);
        String[] lines = new String[] {
            "65",
            "Request timed out.",
            "90",
            "120",
            "Request timed out.",
            "40",
            "Request timed out.",
            "Time limit exceeded.",
            "Any other string."
        };
        List<Packet> pings = Arrays.asList(new Packet[] {
            new Packet(0L, 65),
            new Packet(0L, -1),
            new Packet(0L, 90),
            new Packet(0L, 120),
            new Packet(0L, -1),
            new Packet(0L, 40),
            new Packet(0L, -1),
            new Packet(0L, -1),
            new Packet(0L, -1)
        });
        
        pinger.readLines(lines);
        
        List<Packet> pings2 = pinger.getPings();

        assert pings.size() == pings2.size() : "NUMBER OF PINGS READ INCORRECT";

        for(int i = 0; i < pings.size(); i++)
            assert pings.get(i).value == pings2.get(i).value : "VALUE OF READ PING INCORRECT";
    }

    @Test
    public void testBufferCapacity() {
        int bufferSize = 1000;
        Pinger pinger = new Pinger(bufferSize, "", "");
        ArrayList<String> lines = new ArrayList<String>();
        for(int i = 0; i < bufferSize*1000; i++) {
            lines.add("54");
        }
        for(String line : lines) {
            pinger.readLine(line);
            assert pinger.getPings().size() <= bufferSize;            
        }
    }

    @Test
    //Tests the amount of lines that the shell can take
    public void testShellMemory() {
        Pinger pinger = null;
        try {
            pinger = new Pinger(10, "", Pinger.getResourceFileAsString("memtest.sh"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        pinger.start();
        for(int i = 1; i <= 1000000; i++) {
            String line = pinger.readOneInput();
            assert line.equals(""+i): "SHELL DID NOT STORE ALL VALUES; NOT ENOUGH SHELL MEMORY";
        }
    }
}
