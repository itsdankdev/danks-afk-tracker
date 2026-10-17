# Dank's AFK Tracker

A lightweight RuneLite plugin for tracking time spent active and AFK during an Old School RuneScape login session.

## Features

- Starts the AFK counter after **five minutes without a RuneLite game-canvas click** by default (configurable).
- Displays current AFK, total AFK, longest AFK, active time, session time, and time since last click.
- Resets session statistics on logout.
- Translucent, movable, black-and-lime-green overlay.
- Optional setting to treat mouse movement as activity.

## How the timer works

The current AFK timer begins after the configured idle threshold, not immediately on the last click. Session and active/AFK totals measure logged-in time; values are local to the current plugin session. The plugin tracks interactions with the game canvas rather than all desktop activity.

## Development

Import the Gradle project into IntelliJ IDEA, use a Java 11 JDK, and run the Gradle `build` task. For local development launch, configure a RuneLite development launcher and enable assertions (`-ea`).

## Plugin Hub submission

This is a source repository for submission to the RuneLite Plugin Hub, not a standalone RuneLite installer. Before submitting, verify compilation against the current RuneLite API and test logout/reset and overlay behavior. Push the source to a public GitHub repository, then open a pull request against `runelite/plugin-hub` containing a plugin marker file with the repository URL and exact commit SHA.

## Attribution

Author: Dank.
