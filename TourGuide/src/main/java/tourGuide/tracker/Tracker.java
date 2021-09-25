package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.*;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private final GpsMicroserviceProxy gpsMicroserviceProxy;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService, GpsMicroserviceProxy gpsMicroserviceProxy) {
		this.tourGuideService = tourGuideService;
		this.gpsMicroserviceProxy = gpsMicroserviceProxy;
		
		executorService.submit(this);
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}
	
	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				LOGGER.debug("Tracker stopping");
				break;
			}
			
			List<User> users = tourGuideService.getAllUsers();
			CountDownLatch trackLatch = new CountDownLatch( users.size() );
			LOGGER.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			try {
				users.forEach(u -> tourGuideService.getNewUserLocation(u, trackLatch));
				trackLatch.await();
			} catch (InterruptedException e) {
				LOGGER.error("getLocation - Error during retrieving user location");
			}
			stopWatch.stop();
			LOGGER.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				LOGGER.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}
}
