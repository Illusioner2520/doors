from asyncio import streams
from sys import maxsize
from discord.ext.commands.bot import Bot
import discord
import os
import datetime
import random
from discord import Intents
from discord import option
from discord import Option
import time
import json
import math
from math import *
import sched, time
import ast

discord.MemberCacheFlags.all()

bot = discord.Bot(intents=discord.Intents.all())

# discord.opus.load_opus("libopus")
f = open("save.txt", "r")
global cache
globals()['cache'] = ast.literal_eval(f.read())
print("Cache loaded: " + str(globals()['cache']))

intents = discord.Intents.default()
intents.members=True
intents.message_content=True

async def create_guild(ctx,a,b):
    guild_id = ctx.guild.id
    dict = {}
    dict['guild'] = guild_id
    dict["math"] = True
    dict["numbers_only"] = False
    dict["number"] = 0
    dict["last_user"] = 0
    dict["channel"] = 0
    dict['count_by'] = 1
    dict['individual_mode'] = False
    dict['max_number'] = 0
    if a is not None:
        dict[a] = b
    globals()['cache'].append(dict)

@bot.slash_command(name="info",description="Read info")
async def info(ctx):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    for s in globals()['cache']:
        if s["guild"] == ctx.guild.id:
            embed.description += "Info for server `" + ctx.guild.name + "`\n"
            embed.description += "Previous Number: " + str(s["number"]) + "\n"
            embed.description += "Last User: <@" + str(s["last_user"]) + ">\n"
            embed.description += "Counting by: " + str(s["count_by"]) + "\n"
            embed.description += "Math enabled: " + str(s["math"]) + "\n"
            embed.description += "Numbers Only Mode: " + str(s["numbers_only"]) + "\n"
            embed.description += "Individual Mode: " + str(s["individual_mode"]) + "\n"
            embed.description += "Counting channel: <#" + str(s["channel"]) + ">\n"
            embed.description += "Highest number said: " + str(s["max_number"]) + "\n"
            await ctx.respond(embed=embed)
            return
    embed.description = "Unable to find server info"
    await ctx.respond(embed=embed)

@bot.slash_command(name="calc",description="Calculate")
@option("equation",str,description="Equation")
async def calc(ctx,equation):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    embed.description = "```" + equation + " = " + str(await process_math(equation)) + "```"
    await ctx.respond(embed=embed)

@bot.slash_command(name="setchannel",description="Set the counting channel")
@option("channel",discord.TextChannel,description="The channel to set as the counting channel",required=False)
async def set_channel(ctx, channel):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    nchannel = channel if channel is not None else ctx.channel
    msg = ""
    for s in globals()['cache']:
        if s["guild"] == ctx.guild.id:
            s["channel"] = nchannel.id
            msg = "Counting channel set to " + str(nchannel.mention)
            embed.description = msg
            await ctx.respond(embed=embed)
            return
    await create_guild(ctx, "channel", nchannel.id)
    msg = "Counting channel set to " + str(nchannel.mention)
    embed.description = msg
    await ctx.respond(embed=embed)

@bot.slash_command(name="countby",description="Set how much you are counting by")
@option("count_by",int,description="How much you are counting by")
async def count_by(ctx, count_by):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    msg = ""
    for s in globals()['cache']:
        if s["guild"] == ctx.guild.id:
            s["count_by"] = count_by
            msg = "You are now counting by " + str(count_by)
            embed.description = msg
            await ctx.respond(embed=embed)
            return
    await create_guild(ctx, "count_by", count_by)
    msg = "You are now counting by " + str(count_by)
    embed.description = msg
    await ctx.respond(embed=embed)

def save():
    print("Saving")
    with open("save.txt", "w") as text_file:
        text_file.write(str(globals()['cache']))
def fib(n):
    if n<= 0:
        return None
    elif n == 1:
        return 0
    elif n == 2:
        return 1
    else:
        return fib(n-1)+fib(n-2)
def fibonacci(n):
    return fib(n)
@bot.slash_command(name="debug",description="Debug the bot")
async def debug(ctx):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    embed.description = str(globals()['cache'])
    await ctx.respond(embed=embed)
@bot.slash_command(name="toggle",description="Enable/Disable features of the bot")
@option("feature", description="The feature you want to enable/disable", choices=["Math", "Numbers Only", "Individual Mode"])
async def enable_command(ctx, feature):
    r = random.randint(0,255)
    g = random.randint(0,255)
    b = random.randint(0,255)
    embed = discord.Embed(
        description="",
        color=discord.Colour.from_rgb(r,g,b)
    )
    msg = ""
    if feature == "Math":
        for s in globals()['cache']:
            if s["guild"] == ctx.guild.id:
                if s["math"] == True:
                    s["math"] = False
                    msg = "Math toggled off."
                else:
                    s["math"] = True
                    msg = "Math toggled on."
                embed.description = msg
                await ctx.respond(embed=embed)
                return
        await create_guild(ctx, "math", False)
        msg = "Math toggle off."
    elif feature == "Numbers Only":
        for s in globals()['cache']:
            if s["guild"] == ctx.guild.id:
                if s["numbers_only"] == True:
                    s["numbers_only"] = False
                    msg = "Numbers Only mode toggled off."
                else:
                    s["numbers_only"] = True
                    msg = "Numbers Only mode toggled on."
                embed.description = msg
                await ctx.respond(embed=embed)
                return
        await create_guild(ctx, "numbers_only", True)
        msg = "Numbers Only mode toggle on."
    elif feature == "Individual Mode":
        for s in globals()['cache']:
            if s['guild'] == ctx.guild.id:
                if s['individual_mode'] == True:
                    s['individual_mode'] = False
                    msg = "Individual Mode toggled off."
                else:
                    s['individual_mode'] = True
                    msg = "Individual Mode toggled on."
                embed.description = msg
                await ctx.respond(embed=embed)
                return
        await create_guild(ctx, "individual_mode", True)
        msg = "Individual Mode toggled on."
    embed.description = msg
    await ctx.respond(embed=embed) #coolio

async def process_math(string):
    try:
        val = int(eval(string.split()[0].replace("^", "**").replace("\\","")))
        print(val)
        return val
    except Exception as e:
        return None
async def reset_progress(message):
    for s in globals()['cache']:
        if s['guild'] == message.guild.id:
            if s['max_number'] < s['number']:
                s['max_number'] = s['number']
            s['number'] = 0
            return
async def process_message(message):
    for s in globals()['cache']:
        if s['guild'] == message.guild.id:
            number = s['number']
            last_user = s['last_user']
            if message.author.bot:
                if len(message.content) >= 1 and await process_math(message.content) is not None:
                    await message.channel.send(f"Bots cannot count", reference=message)
                return
            if s['math']:
                next_number = await process_math(message.content)
            else:
                try:
                    next_number = int(message.content.split()[0])
                except Exception as e:
                    return None
            if last_user == message.author.id and not s['individual_mode'] and s['number'] != 0 and next_number is not None:
                await message.add_reaction("❌")
                await message.channel.send(f"**{message.author.mention} ruined it at {str(number)}!** You can't count twice in a row! You can toggle on Individual Mode to enable this with </toggle:1269760558194884741>. (Next number is {s['count_by']})", reference=message)
                await reset_progress(message)
                return

            if s['numbers_only'] and (len(message.content.split()) > 1 or next_number is None):
                await message.add_reaction("❌")
                await message.channel.send(f"**{message.author.mention} ruined it at {str(number)}!** You can only say numbers! You can toggle off Numbers Only mode to disable this with </toggle:1269760558194884741>. (Next number is {s['count_by']})", reference=message)
                await reset_progress(message)
                return

            if next_number is not None:
                if next_number == number + s['count_by']:
                    if next_number == 100:
                        await message.add_reaction("💯")
                    elif next_number == 69:
                        await message.add_reaction("👌")
                    else:
                        await message.add_reaction("✅")
                    s['number'] = next_number
                    s['last_user'] = message.author.id
                elif number != 0:
                    await message.add_reaction("❌")
                    await message.channel.send(f"**{message.author.mention} ruined it at {str(number)}!** Wrong number! (Next number is {s['count_by']})", reference=message)
                    await reset_progress(message)
                else:
                    await message.add_reaction("⚠️")
                    await message.channel.send(f"Next number is {s['count_by']}", reference=message)
            print(next_number)
    return

@bot.listen()
async def on_message(message):
    for s in globals()['cache']:
        if s['guild'] == message.guild.id:
            if s['channel'] == message.channel.id:
                await process_message(message)
            elif s["channel"] == 0 and not message.author.bot and await process_math(message.content) is not None:
                await message.channel.send("Please set a counting channel with </setchannel:1269760558194884740>", reference=message)
            return
    if await process_math(message.content) is not None:
        await message.channel.send("Please set a counting channel with </setchannel:1269760558194884740>", reference=message)

my_scheduler = sched.scheduler(time.time, time.sleep)
my_scheduler.enter(60, 1, save)
my_scheduler.run()

bot.run(os.environ['token'])
