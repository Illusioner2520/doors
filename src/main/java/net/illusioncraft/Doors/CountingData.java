package net.illusioncraft.Doors;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CountingData {
	public CountingData() {};
	@JsonProperty("data")
	public List<GuildData> data;
}
