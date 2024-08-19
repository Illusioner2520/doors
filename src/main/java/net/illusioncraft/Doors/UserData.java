package net.illusioncraft.Doors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserData {
	public UserData() {};
	@JsonProperty("correct")
	public Integer correct;
	@JsonProperty("incorrect")
	public Integer incorrect;
	@JsonProperty("user")
	public String user;
	@JsonProperty("name")
	public String name;
	
	@Override
	public String toString() {
		return "{\"correct\":" + correct.toString() + ",\"incorrect\":" + incorrect.toString() + ",\"user\":\"" + user.toString() + "\",\"name\":\"" + name + "\"}";
	}
	
	public UserData(String user_id, String a, Object b, String user_name) {
		this.user = user_id;
		this.correct = 0;
		this.incorrect = 0;
		this.name = user_name;
		if (a != null) {
			if (a == "user") this.user = (String) b;
			if (a == "correct") this.correct = (Integer) b;
			if (a == "incorrect") this.incorrect = (Integer) b;
		}
	}
}
