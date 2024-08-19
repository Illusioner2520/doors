package net.illusioncraft.Doors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GuildData {
	public GuildData() {};
	@JsonProperty("guild_id")
	public String guild_id;
	@JsonProperty("math")
	public Boolean math;
	@JsonProperty("numbers_only")
	public Boolean numbers_only;
	@JsonProperty("number")
	public Integer number;
	@JsonProperty("last_user")
	public String last_user;
	@JsonProperty("channel")
	public String channel;
	@JsonProperty("count_by")
	public Integer count_by;
	@JsonProperty("individual_mode")
	public Boolean individual_mode;
	@JsonProperty("max_number")
	public Integer max_number;
	@JsonProperty("emoji_correct")
	public String emoji_correct;
	@JsonProperty("emoji_incorrect")
	public String emoji_incorrect;
	@JsonProperty("users")
	public List<UserData> users;
	
	@Override
	public String toString() {
		String usersstring = getUserString();
		return "{\"guild_id\": \"" + guild_id.toString() + "\",\"emoji_correct\": \"" + emoji_correct + "\",\"emoji_incorrect\":\"" + emoji_incorrect + "\", \"math\": " + math.toString() + ", \"numbers_only\": " + numbers_only.toString() + ", \"number\": " + number.toString() + ", \"last_user\": \"" + last_user.toString() + "\", \"channel\": \"" + channel.toString() + "\", \"count_by\": " + count_by.toString() + ", \"individual_mode\": " + individual_mode.toString() + ", \"max_number\":" + max_number.toString() + ", \"users\": " + usersstring + "}";
	}
	
	private String getUserString() {
		String str = "[";
		for (int i = 0; i < users.size(); i++) {
			str += users.get(i).toString() + ",";
		}
		if (!str.equals("[")) str = removeLastChar(str);
		return str + "]";
	}

	public GuildData(String guild_id, String a, Object b) {
		this.guild_id = guild_id;
		this.math = true;
		this.numbers_only = false;
		this.number = 0;
		this.last_user = "0";
		this.emoji_correct = "✅";
		this.emoji_incorrect = "❌";
		this.channel = "0";
		this.count_by = 1;
		this.individual_mode = false;
		this.max_number = 0;
		this.users = new ArrayList<>();
		if (a != null) {
			if (a == "guild_id") this.guild_id = (String) b;
			if (a == "math") this.math = (Boolean) b;
			if (a == "numbers_only") this.numbers_only = (Boolean) b;
			if (a == "number") this.number = (Integer) b;
			if (a == "last_user") this.last_user = (String) b;
			if (a == "channel") this.channel = (String) b;
			if (a == "emoji_correct") this.emoji_correct = (String) b;
			if (a == "emoji_incorrect") this.emoji_incorrect = (String) b;
			if (a == "count_by") this.count_by = (Integer) b;
			if (a == "individual_mode") this.individual_mode = (Boolean) b;
			if (a == "max_number") this.max_number = (Integer) b;
			if (a == "users") this.users = (List<UserData>) b;
		}
	}
	public static String removeLastChar(String s) {
	    return (s == null || s.length() == 0)
	      ? null 
	      : (s.substring(0, s.length() - 1));
	}
}
