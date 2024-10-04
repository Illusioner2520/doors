import discord
import random
from discord import option
from math import *
import ast
import asyncio

discord.MemberCacheFlags.all()

bot = discord.Bot(intents=discord.Intents.all())

f = open("save.txt", "r", encoding='utf-8')
global cache
c = f.read()
globals()['cache'] = [] if c == "" else ast.literal_eval(c)
print("Cache loaded: " + str(globals()['cache']))
f.close()

message_queue = asyncio.Queue()

intents = discord.Intents.default()
intents.members=True
intents.message_content=True

async def create_guild(g,a,b):
    dict = {}
    dict['guild'] = g
    dict["math"] = True
    dict["numbers_only"] = False
    dict["number"] = 0
    dict["last_user"] = 0
    dict["channel"] = 0
    dict['count_by'] = 1
    dict['individual_mode'] = False
    dict['max_number'] = 0
    dict['emoji_correct'] = "✅"
    dict['emoji_incorrect'] = "❌"
    dict['users'] = []
    dict['emoji_numbers'] = {}
    if a is not None:
        dict[a] = b
    globals()['cache'].append(dict)
    return dict

async def create_user(g,u,a,b):
    dict = {}
    dict['user'] = u
    dict['correct'] = 0
    dict['incorrect'] = 0
    dict['name'] = "Unknown User"
    if a is not None:
        dict[a] = b
    (await get_value(g,"users")).append(dict)
    return dict

@bot.slash_command(name="info",description="Read info")
async def info(ctx):
    embed = await new_embed()
    d = await get_entire_data(ctx.guild.id)
    embed.description += "Info for server `" + ctx.guild.name + "`\n"
    embed.description += "Previous Number: " + str(d['number']) + "\n"
    embed.description += "Last User: <@" + str(d['last_user']) + ">\n"
    embed.description += "Counting by: " + str(d['count_by']) + "\n"
    embed.description += "Math enabled: " + str(d['math']) + "\n"
    embed.description += "Numbers Only Mode: " + str(d['numbers_only']) + "\n"
    embed.description += "Individual Mode: " + str(d['individual_mode']) + "\n"
    embed.description += "Counting channel: <#" + str(d['channel']) + ">\n"
    embed.description += "Highest number said: " + str(d['max_number']) + "\n"
    embed.description += "Correct Emoji: " + str(d['emoji_correct']) + "\n"
    embed.description += "Incorrect Emoji: " + str(d['emoji_incorrect']) + "\n"
    await ctx.respond(embed=embed)

@bot.slash_command(name="setchannel",description="Set the counting channel")
@option("channel",discord.TextChannel,description="The channel to set as the counting channel",required=False)
async def set_channel(ctx,channel):
    nchannel = channel if channel is not None else ctx.channel
    await set_value(ctx.guild.id,"channel",nchannel.id)
    embed = await new_embed()
    embed.description = "Counting channel set to " + nchannel.mention
    await ctx.respond(embed=embed)

@bot.slash_command(name="countby",description="Set how much you are counting by")
@option("count_by",int,description="How much you will be counting by",required=True)
async def count_by(ctx,count_by):
    await set_value(ctx.guild.id,"count_by",count_by)
    embed = await new_embed()
    embed.description = "Now counting by " + str(count_by)
    await ctx.respond(embed=embed)

@bot.slash_command(name="calc",description="Calculate")
@option("equation",str,description="Equation")
async def calc(ctx,equation):
    embed = await new_embed()
    embed.description = "```" + equation + " = " + str(await process_math(equation)) + "```"
    await ctx.respond(embed=embed)
        
@bot.slash_command(name="debug",description="Debug the bot")
async def debug(ctx):
    await ctx.respond(str(globals()['cache']))

@bot.slash_command(name="toggle",description="Toggle features of the bot")
@option("feature", description="The feature you want to enable/disable", choices=["Math", "Numbers Only", "Individual Mode"])
async def toggle(ctx,feature):
    embed = await new_embed()
    if feature == "Math":
        n = await toggle_boolean_value(ctx.guild.id,"math")
        embed.description = "Math toggled on." if n else "Math toggled off."
    elif feature == "Numbers Only":
        n = await toggle_boolean_value(ctx.guild.id,"numbers_only")
        embed.description = "Numbers Only mode toggled on." if n else "Numbers Only mode toggled off."
    elif feature == "Individual Mode":
        n = await toggle_boolean_value(ctx.guild.id,"individual_mode")
        embed.description = "Individual Mode toggled on." if n else "Individual Mode toggled off."
    await ctx.respond(embed=embed)

@bot.slash_command(name="setemoji",description="Set the emoji")
@option("setfor",description="Correct or Incorrect?",choices=["Correct","Incorrect"])
@option("emoji",str,description="The emoji")
async def setemoji(ctx,setfor,emoji):
    embed = await new_embed()
    if setfor == "Correct":
        await set_value(ctx.guild.id,"emoji_correct",emoji)
        embed.description = "The correct emoji is now " + emoji
    elif setfor == "Incorrect":
        await set_value(ctx.guild.id,"emoji_incorrect",emoji)
        embed.description = "The incorrect emoji is now " + emoji
    await ctx.respond(embed=embed)

@bot.slash_command(name="setemojifornumber",description="Set the emoji for a specific number")
@option("number",int,description="The number to set the emoji for")
@option("emoji",str,description="The emoji")
async def setemojifornumber(ctx,number,emoji):
    embed = await new_embed()
    v = await get_value(ctx.guild.id,"emoji_numbers")
    v[str(number)] = emoji
    await set_value(ctx.guild.id,"emoji_numbers",v)
    embed.description = "The emoji for the number " + str(number) + " is now " + emoji
    await ctx.respond(embed=embed)

@bot.slash_command(name="clearemojifornumber",description="Clear the emoji for a number")
@option("number",int,description="Number to clear")
async def clearemojifornumber(ctx,number):
    embed = await new_embed()
    v = await get_value(ctx.guild.id,"emoji_numbers")
    previous_emoji = v[str(number)]
    if str(number) in v:
        del v[str(number)]
    await set_value(ctx.guild.id,"emoji_numbers",v)
    embed.description = "The emoji for the number " + str(number) + " is now cleared. (Previously " + previous_emoji + ")" if previous_emoji is not None else "Nothing changed. That number is already cleared."
    await ctx.respond(embed=embed)

@bot.slash_command(name="leaderboard",description="Display a leaderboard")
@option("leaderboard",description="Leaderboard type",choices=["Correct","Incorrect","Percentage"])
async def leaderboard(ctx,leaderboard):
    embed = await new_embed()
    embed.description = "Leaderboard for `" + ctx.guild.name + "` (" + leaderboard + ")"
    us = await get_value(ctx.guild.id,"users")
    if leaderboard == "Correct":
        us.sort(key=sc)
    elif leaderboard == "Incorrect":
        us.sort(key=si)
    elif leaderboard == "Percentage":
        us.sort(key=sp)
    for v in range(0,len(us)):
        if us[v]["name"] == "Unknown User":
            continue
        t = str(us[v]["correct"] if leaderboard == "Correct" else us[v]["incorrect"])
        if leaderboard == "Percentage":
            t = str(round(us[v]["correct"] / (us[v]["correct"] + us[v]["incorrect"]) * 100,2)) + "%"
        embed.description += "\n**" + str(v + 1) + ".** " + us[v]["name"] + ": **" + t + "**"
    await ctx.respond(embed=embed)

@bot.slash_command(name="user",description="Display info about a user")
@option("user",discord.User,description="The user",required=False)
async def user(ctx,user):
    embed = await new_embed()
    u = user if user is not None else ctx.author
    i = await get_user_value(ctx.guild.id,u.id,"incorrect")
    c = await get_user_value(ctx.guild.id,u.id,"correct")
    try:
        p = round(c / (i + c) * 100,2)
    except Exception as e:
        p = None
    embed.description = "**User data for " + u.name + ":**\nCorrect " + (await get_value(ctx.guild.id,"emoji_correct")) + ": **" + str(c) + "**\nIncorrect " + (await get_value(ctx.guild.id,"emoji_incorrect")) + ": **" + str(i) + "**\nPercentage: **" + str(p) + "%**"
    await ctx.respond(embed=embed)

def sc(a):
  return -(a["correct"])
def si(a):
  return -(a["incorrect"])
def sp(a):
  if (a["name"] == "Unknown User"):
      return 0
  return -(a["correct"] / (a["correct"] + a["incorrect"]))

async def get_value(g,a):
    for s in globals()['cache']:
        if s['guild'] == g:
            return s[a]
    new_guild = await create_guild(g,None,None)
    return new_guild[a]

async def toggle_boolean_value(g,a):
    for s in globals()['cache']:
        if s['guild'] == g:
            s[a] = False if s[a] else True
            return s[a]
    new_guild = await create_guild(g,None,None)
    v = await set_value(g,a,False if new_guild[a] else True)
    return v

async def get_entire_data(g):
    for s in globals()['cache']:
        if s['guild'] == g:
            return s
    new_guild = await create_guild(g,None,None)
    return new_guild

async def set_value(g,a,b):
    for s in globals()['cache']:
        if s['guild'] == g:
            s[a] = b
            return b
    new_guild = await create_guild(g,a,b)
    return b

async def new_embed():
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    return embed

async def save():
    with open("save.txt", "w", encoding='utf-8') as file:
        v = str(globals()['cache'])
        file.write(v)

async def process_math(string):
    try:
        val = int(eval(string.split()[0].replace("^", "**").replace("\\","")))
        return val
    except Exception as e:
        return None

async def reset_progress(message):
    m = await get_value(message.guild.id,"max_number")
    n = await get_value(message.guild.id,"number")
    if m < n:
        await set_value(message.guild.id,"max_number",n)
    await set_value(message.guild.id,"number",0)
    c = await get_user_value(message.guild.id,message.author.id,"incorrect")
    await set_user_value(message.guild.id,message.author.id,"incorrect",c+1)
    await set_user_value(message.guild.id,message.author.id,"name",message.author.name)
    return

async def turn_into_int(v):
    try:
        return int(v)
    except Exception as e:
        return None

async def get_user_value(g,u,a):
    for s in globals()['cache']:
        if s['guild'] == g:
            for t in s['users']:
                if t['user'] == u:
                    return t[a]
            new_user = await create_user(g,u,None,None)
            return new_user[a]
    await create_guild(g,None,None)
    new_user = await create_user(g,u,None,None)
    return new_user[a]

async def set_user_value(g,u,a,b):
    for s in globals()['cache']:
        if s['guild'] == g:
            for t in s['users']:
                if t['user'] == u:
                    t[a] = b
                    return t[a]
            new_user = await create_user(g,u,a,b)
            return new_user[a]
    await create_guild(g,None,None)
    new_user = await create_user(g,u,a,b)
    return new_user[a]

async def process_message(message):
    d = await get_entire_data(message.guild.id)
    n = d['number']
    l = d['last_user']
    m = await process_math(message.content) if d['math'] else await turn_into_int(message.content.split()[0])
    if m is None:
        if d['numbers_only'] and not message.author.bot:
            await message.add_reaction(d['emoji_incorrect'])
            await message.channel.send(f"**{message.author.mention} ruined it at {str(n)}!** You can only say numbers! You can toggle off Numbers Only mode to disable this with </toggle:1269760558194884741>. (Next number is {d['count_by']})", reference=message)
            await reset_progress(message)
        return
    if message.author.bot and len(message.content) >= 1:
        await message.channel.send(f"Bots cannot count", reference=message)
        return
    if l == message.author.id and not d['individual_mode'] and d['number'] != 0:
        await message.add_reaction(d['emoji_incorrect'])
        await message.channel.send(f"**{message.author.mention} ruined it at {str(n)}!** You can't count twice in a row! You can toggle on Individual Mode to enable this with </toggle:1269760558194884741>. (Next number is {d['count_by']})", reference=message)
        await reset_progress(message)
        return
    if m == n + d['count_by']:
        e = d['emoji_numbers'][str(m)] if str(m) in d['emoji_numbers'] is not None else d['emoji_correct']
        c = await get_user_value(message.guild.id,message.author.id,"correct")
        await set_user_value(message.guild.id,message.author.id,"correct",c+1)
        await set_user_value(message.guild.id,message.author.id,"name",message.author.name)
        await message.add_reaction(e)
        d['number'] = m
        d['last_user'] = message.author.id
        return
    elif n != 0:
        await message.add_reaction(d['emoji_incorrect'])
        await message.channel.send(f"**{message.author.mention} ruined it at {str(n)}!** Wrong number! (Next number is {d['count_by']})", reference=message)
        await reset_progress(message)
        return
    

async def process_messages():
    while True:
        message = await message_queue.get()
        await process_message(message)
        message_queue.task_done()
    
async def execute_periodically(interval):
    while True:
        await asyncio.sleep(interval)
        await save()

@bot.listen()
async def on_message(message):
    c = await get_value(message.guild.id,"channel")
    if c == message.channel.id:
        await message_queue.put(message)
    elif c == 0 and not message.author.bot and await process_math(message.content) is not None:
        await message.channel.send("Please set a counting channel with </setchannel:1269760558194884740>", reference=message)

@bot.listen()
async def on_connect():
    bot.loop.create_task(process_messages())
    bot.loop.create_task(execute_periodically(60))

bot.run("[TOKEN]")
