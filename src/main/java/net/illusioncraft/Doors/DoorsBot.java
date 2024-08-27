package net.illusioncraft.Doors;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;

public class DoorsBot extends ListenerAdapter implements EventListener {
	public JDA jda;
	public DoorsBot() {
		startBot();
	}
	private List<GuildData> guildcache;
	public void saveData() {
		Path fileName = Path.of("counting.json");
		try {
			Files.writeString(fileName, getSaveData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String getSaveData() {
		String str = "{\"data\":[";
		for (int i = 0; i < guildcache.size(); i++) {
			str += guildcache.get(i).toString() + ",";
		}
		str = removeLastChar(str);
		return str + "]}";
	}
	public static String removeLastChar(String s) {
	    return (s == null || s.length() == 0)
	      ? null 
	      : (s.substring(0, s.length() - 1));
	}
	private void startBot() {
		ObjectMapper mapper = new ObjectMapper();
		JsonUtils json = new JsonUtils();
        File file = new File("counting.json");
        JsonNode jn = json.readJson(file);
        try {
			CountingData countingdata = mapper.treeToValue(jn, CountingData.class);
	        this.guildcache = countingdata.data;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		jda = JDABuilder.createLight("[INSERT TOKEN HERE]", GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MODERATION)
                .setActivity(Activity.playing("Minecraft"))
                .addEventListeners(this)
                .build();
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
        		Commands.slash("setchannel", "Set the counting channel").addOption(OptionType.CHANNEL, "channel", "The channel to set as the counting channel",false)
        );
        commands.addCommands(
        		Commands.slash("info", "Read info")
        );
        commands.addCommands(
        		Commands.slash("calc", "Calculate").addOption(OptionType.STRING, "equation", "Equation",true)
        );
        commands.addCommands(
        		Commands.slash("countby", "Set how much you are counting by").addOption(OptionType.INTEGER, "count_by", "How much you are counting by",true)
        );
        commands.addCommands(
        		Commands.slash("debug", "Debug the bot")
        );
        commands.addCommands(
        		Commands.slash("leaderboard", "Server leaderboard")
        			.addOptions(
                        new OptionData(OptionType.STRING, "leaderboard", "The leaderboard you want", true)
                            .addChoice("Correct", "Correct")
                            .addChoice("Incorrect", "Incorrect")
                            .addChoice("Total", "Total")
                            .addChoice("Percentage", "Percentage")
                    )
        );
        commands.addCommands(
        		Commands.slash("toggle", "Enable/Disable features of the bot")
        			.addOptions(
                        new OptionData(OptionType.STRING, "feature", "The feature you want to toggle", true)
                            .addChoice("Math", "Math")
                            .addChoice("Numbers Only", "Numbers Only")
                            .addChoice("Individual Mode", "Individual Mode")
                    )
        );
        commands.addCommands(
        		Commands.slash("say", "Say something").addOption(OptionType.STRING, "words", "Things to say",true)
        );
        commands.addCommands(
        		Commands.slash("setemoji", "Set Emoji")
        			.addOptions(
                        new OptionData(OptionType.STRING, "setfor", "What event you are setting the emoji for", true)
                            .addChoice("Correct", "Correct")
                            .addChoice("Incorrect", "Incorrect")
                    ).addOption(OptionType.STRING, "emoji", "Emoji to Use",true)
        );
        commands.addCommands(
        		Commands.slash("setemojifornumber", "Set Emoji").addOption(OptionType.INTEGER, "number", "Number to set the emoji for",true).addOption(OptionType.STRING, "emoji", "Emoji to Use",true)
        );
        commands.addCommands(
        		Commands.slash("user", "Get data about a user").addOption(OptionType.USER, "user", "User to get info from",false)
        );
        
        commands.queue();
	}
	@Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null)
            return;
        switch (event.getName()) {
        	case "setchannel":
        		EmbedBuilder eb = getEmbed();
        		GuildChannelUnion nchannel = null;
        		if (event.getOption("channel") != null) {
        			nchannel = event.getOption("channel").getAsChannel();
        		}
        		MessageChannelUnion mchannel = event.getChannel();
        		TextChannel tchannel;
        		if (nchannel != null && nchannel.getType() != ChannelType.TEXT) {
        			eb.setDescription("Please select a valid Text Channel");
        			event.replyEmbeds(eb.build()).queue();
        			return;
        		}
        		if (nchannel == null && mchannel.getType() != ChannelType.TEXT) {
        			eb.setDescription("Please run this command in a valid Text Channel or specify a valid text channel.");
        			event.replyEmbeds(eb.build()).queue();
        			return;
        		}
        		if (nchannel == null) {
        			tchannel = mchannel.asTextChannel();
        		} else {
        			tchannel = nchannel.asTextChannel();
        		}
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				GuildData data = guildcache.get(i);
        				data.channel = tchannel.getId();
        				guildcache.set(i, data);
        				eb.setDescription("Counting channel set to " + tchannel.getAsMention());
            			event.replyEmbeds(eb.build()).queue();
            			return;
        			}
        		}
        		GuildData guild_data = new GuildData(event.getGuild().getId(),"channel",tchannel.getId());
        		guildcache.add(guild_data);
				eb.setDescription("Counting channel set to " + tchannel.getAsMention());
    			event.replyEmbeds(eb.build()).queue();
    			return;
        	case "info":
        		EmbedBuilder eb2 = getEmbed();
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				GuildData data = guildcache.get(i);
        				eb2.setDescription("Info for server `" + event.getGuild().getName() + "`\nPrevious Number: " + data.number.toString() + "\nLast User: <@" + data.last_user.toString() + ">\nCounting by: " + data.count_by.toString() + "\nMath enabled: " + data.math.toString() + "\nNumbers Only Mode: " + data.numbers_only.toString() + "\nIndividual Mode: " + data.individual_mode.toString() + "\nCounting channel: <#" + data.channel.toString() + ">\nHighest number said: " + data.max_number.toString());
            			event.replyEmbeds(eb2.build()).queue();
            			return;
        			}
        		}
        		eb2.setDescription("Unable to find server info");
        		event.replyEmbeds(eb2.build()).queue();
    			return;
        	case "countby":
        		EmbedBuilder eb3 = getEmbed();
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				GuildData data = guildcache.get(i);
        				data.count_by = event.getOption("count_by").getAsInt();
        				guildcache.set(i, data);
        				eb3.setDescription("You are now counting by " +  event.getOption("count_by").getAsString());
            			event.replyEmbeds(eb3.build()).queue();
            			return;
        			}
        		}
        		GuildData guild_data1 = new GuildData(event.getGuild().getId(),"count_by",event.getOption("count_by").getAsInt());
        		guildcache.add(guild_data1);
				eb3.setDescription("You are now counting by " +  event.getOption("count_by").getAsString());
    			event.replyEmbeds(eb3.build()).queue();
    			return;
        	case "setemojifornumber":
        		EmbedBuilder eb10 = getEmbed();
        		Integer number = event.getOption("number").getAsInt();
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				GuildData data = guildcache.get(i);
        				if (data.emoji_numbers == null) data.emoji_numbers = new HashMap<>();
        				data.emoji_numbers.put(number.toString(), event.getOption("emoji").getAsString());
        				eb10.setDescription("Your emoji for the number " + number.toString() + " is now " + event.getOption("emoji").getAsString());
        				event.replyEmbeds(eb10.build()).queue();
            			return;
        			}
        		}
        		eb10.setDescription("An error has occurred.");
				event.replyEmbeds(eb10.build()).queue();
        		return;
        	case "setemoji":
        		EmbedBuilder eb8 = getEmbed();
        		
    			String setfor = event.getOption("setfor").getAsString();
        		if (setfor.equals("Correct")) {
            		for (int i = 0; i < guildcache.size(); i++) {
            			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
            				GuildData data = guildcache.get(i);
            				data.emoji_correct = event.getOption("emoji").getAsString();
            				eb8.setDescription("Your correct emoji is now " + event.getOption("emoji").getAsString());
            				guildcache.set(i, data);
                			event.replyEmbeds(eb8.build()).queue();
                			return;
            			}
            		}
            		GuildData guild_data2 = new GuildData(event.getGuild().getId(),"emoji_correct",event.getOption("emoji").getAsString());
            		guildcache.add(guild_data2);
    				eb8.setDescription("Your correct emoji is now " + event.getOption("emoji").getAsString());
        			event.replyEmbeds(eb8.build()).queue();
        			return;
        		} else if (setfor.equals("Incorrect")) {
        			for (int i = 0; i < guildcache.size(); i++) {
            			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
            				GuildData data = guildcache.get(i);
            				data.emoji_incorrect = event.getOption("emoji").getAsString();
            				eb8.setDescription("Your incorrect emoji is now " + event.getOption("emoji").getAsString());
            				guildcache.set(i, data);
                			event.replyEmbeds(eb8.build()).queue();
                			return;
            			}
            		}
            		GuildData guild_data2 = new GuildData(event.getGuild().getId(),"emoji_incorrect",event.getOption("emoji").getAsString());
            		guildcache.add(guild_data2);
    				eb8.setDescription("Your incorrect emoji is now " + event.getOption("emoji").getAsString());
        			event.replyEmbeds(eb8.build()).queue();
        			return;
        		}
        	case "toggle":
        		EmbedBuilder eb4 = getEmbed();
        		String feature = event.getOption("feature").getAsString();
        		if (feature.equals("Math")) {
            		for (int i = 0; i < guildcache.size(); i++) {
            			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
            				GuildData data = guildcache.get(i);
            				if (data.math) {
            					data.math = false;
            					eb4.setDescription("Math toggled off.");
            				} else {
            					data.math = true;
            					eb4.setDescription("Math toggled on.");
            				}
            				guildcache.set(i, data);
                			event.replyEmbeds(eb4.build()).queue();
                			return;
            			}
            		}
            		GuildData guild_data2 = new GuildData(event.getGuild().getId(),"math",false);
            		guildcache.add(guild_data2);
    				eb4.setDescription("Math toggled off.");
        			event.replyEmbeds(eb4.build()).queue();
        			return;
        		} else if (feature.equals("Numbers Only")) {
            		for (int i = 0; i < guildcache.size(); i++) {
            			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
            				GuildData data = guildcache.get(i);
            				if (data.numbers_only) {
            					data.numbers_only = false;
            					eb4.setDescription("Numbers Only mode toggled off.");
            				} else {
            					data.numbers_only = true;
            					eb4.setDescription("Numbers Only mode toggled on.");
            				}
            				guildcache.set(i, data);
                			event.replyEmbeds(eb4.build()).queue();
                			return;
            			}
            		}
            		GuildData guild_data2 = new GuildData(event.getGuild().getId(),"numbers_only",true);
            		guildcache.add(guild_data2);
    				eb4.setDescription("Numbers Only mode toggled on.");
        			event.replyEmbeds(eb4.build()).queue();
        			return;
        		} else if (feature.equals("Individual Mode")) {
            		for (int i = 0; i < guildcache.size(); i++) {
            			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
            				GuildData data = guildcache.get(i);
            				if (data.individual_mode) {
            					data.individual_mode = false;
            					eb4.setDescription("Individual Mode toggled off.");
            				} else {
            					data.individual_mode = true;
            					eb4.setDescription("Individual Mode toggled on.");
            				}
            				guildcache.set(i, data);
                			event.replyEmbeds(eb4.build()).queue();
                			return;
            			}
            		}
            		GuildData guild_data2 = new GuildData(event.getGuild().getId(),"individual_mode",true);
            		guildcache.add(guild_data2);
    				eb4.setDescription("Individual Mode toggled on.");
        			event.replyEmbeds(eb4.build()).queue();
        			return;
        		}
        		return;
        	case "debug":
        		EmbedBuilder eb5 = getEmbed();
        		eb5.setDescription(getSaveData());
    			event.replyEmbeds(eb5.build()).queue();
    			return;
        	case "leaderboard":
        		EmbedBuilder eb7 = getEmbed();
        		String lbtype = event.getOption("leaderboard").getAsString();
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				GuildData data = guildcache.get(i);
        				List<UserData> people = data.users;
        				String str = "Leaderboard for `" + event.getGuild().getName() + "` (" + lbtype + ")";
        				if (lbtype.equals("Total")) {
            				Collections.sort(people, new TotalComparator());
        				} else if (lbtype.equals("Correct")) {
            				Collections.sort(people, new CorrectComparator());
        				} else if (lbtype.equals("Incorrect")) {
            				Collections.sort(people, new IncorrectComparator());
        				} else if (lbtype.equals("Percentage")) {
            				Collections.sort(people, new PercentageComparator());
        				}
        				for (int j = 0; j < people.size(); j++) {
        					Float value = null;
        					Integer value1 = null;
            				if (lbtype.equals("Total")) {
            					value1 = (people.get(j).correct + people.get(j).incorrect);
            				} else if (lbtype.equals("Correct")) {
            					value1 = (people.get(j).correct);
            				} else if (lbtype.equals("Incorrect")) {
            					value1 = (people.get(j).incorrect);
            				} else if (lbtype.equals("Percentage")) {
            					value = (float) (((float) people.get(j).correct / (float) (people.get(j).correct + people.get(j).incorrect)) * 100);
            				}
            				Integer k = j + 1;
            				if (lbtype.equals("Percentage")) {
            					DecimalFormat format = new DecimalFormat("###.00");
            					str += "\n**" + k + ".** " + people.get(j).name + ": **" + format.format(value) + "%**";
            				} else {
            					str += "\n**" + k + ".** " + people.get(j).name + ": **" + value1.toString() + "**";
            				}
        				}
                		eb7.setDescription(str);
            			event.replyEmbeds(eb7.build()).queue();
            			return;
        			}
        		}
        		eb7.setDescription("An error occured?");
    			event.replyEmbeds(eb7.build()).queue();
        		return;
        	case "calc":
        		EmbedBuilder eb6 = getEmbed();
        		Integer math = process_math(event.getOption("equation").getAsString());
        		if (math != null) {
        			eb6.setDescription("```" + event.getOption("equation").getAsString() + " = " + math.toString() + "```");
        		} else {
        			eb6.setDescription("```" + event.getOption("equation").getAsString() + " = Unknown```");
        		}
    			event.replyEmbeds(eb6.build()).queue();
    			return;
        	case "user":
        		EmbedBuilder eb9 = getEmbed();
        		Member user = event.getMember();
        		if (event.getOption("user") != null) {
        			user = event.getOption("user").getAsMember();
        		}
        		String user_name = user.getUser().getName();
        		String user_id = user.getId();
        		for (int i = 0; i < guildcache.size(); i++) {
        			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
        				for (int j = 0; j < guildcache.get(i).users.size(); j++) {
        					GuildData gdata = guildcache.get(i);
        					if (gdata.users.get(j).user.equals(user_id)) {
        						UserData data = gdata.users.get(j);
        						Float percentage = (float) (((float) data.correct / (float) (data.correct + data.incorrect)) * 100);
        						eb9.setDescription("**User data for " + user_name + ":**\nCorrect " + gdata.emoji_correct + " : **" + data.correct.toString() + "**\nIncorrect " + gdata.emoji_incorrect + " : **" + data.incorrect.toString() + "**\nPercentage Correct: **" + percentage.toString() + "%**");
        						if (user_name != data.name) {
        							data.name = user_name;
        							gdata.users.set(j, data);
        							guildcache.set(i, gdata);
        						}
        		    			event.replyEmbeds(eb9.build()).queue();
        		    			return;
        					}
        				}
        			}
        		}
            	eb9.setDescription("Unable to find that user.");
            	event.replyEmbeds(eb9.build()).queue();
        		return;
        	case "say":
        		event.reply(event.getOption("words").getAsString()).queue();
        		return;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
	}
	
	private EmbedBuilder getEmbed() {
    	Random rand = new Random();
		EmbedBuilder eb = new EmbedBuilder();
    	eb.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
    	return eb;
	}
	private Integer process_math(String exp) {
		if (exp.equals("")) {
			return null;
		}
		try {
			double result = new ExpressionBuilder(exp.replaceAll("\\\\", "")).build().evaluate();
			return (int) Math.round(result);
		} catch (UnknownFunctionOrVariableException e){
			return null;
		} catch (UnsupportedOperationException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ArithmeticException e) {
			return null;
		}
	}
	private void process_message(MessageReceivedEvent event) {
		for (int i = 0; i < guildcache.size(); i++) {
			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
				GuildData data = guildcache.get(i);
				Integer number = data.number;
				String last_user = data.last_user;
				Integer next_number = null;
				if (event.getAuthor().isBot()) {
					if (event.getMessage().getContentRaw().length() >= 1 && process_math(event.getMessage().getContentRaw()) != null) {
						event.getMessage().reply("Bots cannot count.").queue();
						return;
					}
				}
				if (data.math) {
					next_number = process_math(event.getMessage().getContentRaw().split(" ")[0]);
				} else {
					try {
						next_number = Integer.valueOf(event.getMessage().getContentRaw().split(" ")[0]);
					} catch (NumberFormatException e) {
						next_number = null;
					}
				}
				if (last_user.equals(event.getAuthor().getId()) && !data.individual_mode && number != 0 && next_number != null) {
					if (data.emoji_incorrect != null) {
						event.getMessage().addReaction(Emoji.fromFormatted(data.emoji_incorrect)).queue();
					} else {
						event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
					}
					event.getMessage().reply("**" + event.getAuthor().getAsMention() + " ruined it at " + number.toString() + "!** You can't count twice in a row! You can toggle on Individual Mode to enable this with </toggle:1269760558194884741>. (Next number is " + data.count_by.toString() + ")").queue();
					reset_progress(event);
					return;
				}
				if (data.numbers_only && !event.getAuthor().isBot() && (event.getMessage().getContentRaw().split(" ").length > 1 || next_number == null)) {
					if (data.emoji_incorrect != null) {
						event.getMessage().addReaction(Emoji.fromFormatted(data.emoji_incorrect)).queue();
					} else {
						event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
					}
					event.getMessage().reply("**" + event.getAuthor().getAsMention() + " ruined it at " + number.toString() + "!** You can only say numbers! You can toggle off Numbers Only mode to disable this with </toggle:1269760558194884741>. (Next number is " + data.count_by.toString() + ")").queue();
					reset_progress(event);
					return;
				}
				if (next_number != null) {
					if (next_number.equals(number + data.count_by)) {
						if (data.emoji_numbers != null && data.emoji_numbers.get(next_number.toString()) != null) {
							event.getMessage().addReaction(Emoji.fromFormatted(data.emoji_numbers.get(next_number.toString()))).queue();
						} else if (data.emoji_correct != null) {
							event.getMessage().addReaction(Emoji.fromFormatted(data.emoji_correct)).queue();
						} else {
							event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
						}
						data.number = next_number;
						data.last_user = event.getAuthor().getId();
						Boolean found = false;
						for (int j = 0; j < data.users.size(); j++) {
							if (data.users.get(j).user.equals(event.getAuthor().getId())) {
								found = true;
								UserData user = data.users.get(j);
								user.correct += 1;
								if (user.name != event.getAuthor().getName()) user.name = event.getAuthor().getName();
								data.users.set(j, user);
							}
						}
						if (!found) {
							UserData user = new UserData(event.getAuthor().getId(),"correct",1,event.getAuthor().getName());
							data.users.add(user);
						}
					} else if (number != 0) {
						if (data.emoji_incorrect != null) {
							event.getMessage().addReaction(Emoji.fromFormatted(data.emoji_incorrect)).queue();
						} else {
							event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
						}
						event.getMessage().reply("**" + event.getAuthor().getAsMention() + " ruined it at " + number.toString() + "!** Wrong number! (Next number is " + data.count_by.toString() + ")").queue();
						reset_progress(event);
						return;
					}
				}
				guildcache.set(i, data);
			}
		}
	}
	private void reset_progress(MessageReceivedEvent event) {
		for (int i = 0; i < guildcache.size(); i++) {
			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
				GuildData data = guildcache.get(i);
				if (data.max_number < data.number) {
					data.max_number = data.number;
				}
				data.number = 0;
				Boolean found = false;
				for (int j = 0; j < data.users.size(); j++) {
					if (data.users.get(j).user.equals(event.getAuthor().getId())) {
						found = true;
						UserData user = data.users.get(j);
						user.incorrect += 1;
						if (user.name != event.getAuthor().getName()) user.name = event.getAuthor().getName();
						data.users.set(j, user);
					}
				}
				if (!found) {
					UserData user = new UserData(event.getAuthor().getId(),"incorrect",1,event.getAuthor().getName());
					data.users.add(user);
				}
				guildcache.set(i, data);
				return;
			}
		}
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		for (int i = 0; i < guildcache.size(); i++) {
			if (guildcache.get(i).guild_id.equals(event.getGuild().getId())) {
				if (guildcache.get(i).channel.equals(event.getChannel().getId())) {
					process_message(event);
				} else if (guildcache.get(i).channel.equals("0") && !event.getAuthor().isBot() && process_math(event.getMessage().getContentRaw()) != null) {
					event.getMessage().reply("Please set a counting channel with </setchannel:1269760558194884740>").queue();
				}
				return;
			}
		}
		if (process_math(event.getMessage().getContentRaw()) != null) {
			event.getMessage().reply("Please set a counting channel with </setchannel:1269760558194884740>").queue();
		}
	}
}
