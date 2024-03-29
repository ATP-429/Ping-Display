package PingDisplay;

public class App {
    public static final int FPS = 60;
    public static void main(String[] args) {
        Window window = new Window();
        Pinger pinger = new Pinger(150, "pingtest-sgp.brawlhalla.com");
        pinger.start();
        //Limiting the fps to 60
        long lastTime = System.currentTimeMillis();
        while (true)
		{
            long currentTime = System.currentTimeMillis();
			if (currentTime - lastTime > 1000 / FPS) //If it's been 1000/60 ms, that is, if it's been the amount of time taken up by one frame, then display another frame
			{
                pinger.readInputs();
                window.render(pinger.getPings());
                lastTime = System.currentTimeMillis();
            }
            else {
                try
				{
					//Wait until it's time to render the next frame
					Thread.sleep(1000 / FPS - (currentTime - lastTime));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        }
    }
}
