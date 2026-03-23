package org.doubao.like.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "like.hot-list")
public class HotListProperties {
	private boolean enabled = true;
	private boolean preloadOnStartup = true;
	private int refreshIntervalMinutes = 10;
	private int refreshLimit = 200;
	private int allDefaultWindowHours = 72;
	private int dailyWindowHours = 24;
	private int weeklyWindowHours = 168;
	private int monthlyWindowHours = 720;
	private int risingWindowHours = 24;
	private int risingPreviousWindowHours = 24;
	private long risingMinCurrentLikes = 10L;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPreloadOnStartup() {
		return preloadOnStartup;
	}

	public void setPreloadOnStartup(boolean preloadOnStartup) {
		this.preloadOnStartup = preloadOnStartup;
	}

	public int getRefreshIntervalMinutes() {
		return refreshIntervalMinutes;
	}

	public void setRefreshIntervalMinutes(int refreshIntervalMinutes) {
		this.refreshIntervalMinutes = refreshIntervalMinutes;
	}

	public int getRefreshLimit() {
		return refreshLimit;
	}

	public void setRefreshLimit(int refreshLimit) {
		this.refreshLimit = refreshLimit;
	}

	public int getAllDefaultWindowHours() {
		return allDefaultWindowHours;
	}

	public void setAllDefaultWindowHours(int allDefaultWindowHours) {
		this.allDefaultWindowHours = allDefaultWindowHours;
	}

	public int getDailyWindowHours() {
		return dailyWindowHours;
	}

	public void setDailyWindowHours(int dailyWindowHours) {
		this.dailyWindowHours = dailyWindowHours;
	}

	public int getWeeklyWindowHours() {
		return weeklyWindowHours;
	}

	public void setWeeklyWindowHours(int weeklyWindowHours) {
		this.weeklyWindowHours = weeklyWindowHours;
	}

	public int getMonthlyWindowHours() {
		return monthlyWindowHours;
	}

	public void setMonthlyWindowHours(int monthlyWindowHours) {
		this.monthlyWindowHours = monthlyWindowHours;
	}

	public int getRisingWindowHours() {
		return risingWindowHours;
	}

	public void setRisingWindowHours(int risingWindowHours) {
		this.risingWindowHours = risingWindowHours;
	}

	public int getRisingPreviousWindowHours() {
		return risingPreviousWindowHours;
	}

	public void setRisingPreviousWindowHours(int risingPreviousWindowHours) {
		this.risingPreviousWindowHours = risingPreviousWindowHours;
	}

	public long getRisingMinCurrentLikes() {
		return risingMinCurrentLikes;
	}

	public void setRisingMinCurrentLikes(long risingMinCurrentLikes) {
		this.risingMinCurrentLikes = risingMinCurrentLikes;
	}
}
