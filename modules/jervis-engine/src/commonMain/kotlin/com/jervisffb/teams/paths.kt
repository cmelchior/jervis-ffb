package com.jervisffb.teams

/**
 * TODO Figure out where it is best to place team/player data.
 *  It would be nice to separate it from the rules engine, but at the same time,
 *  it also require the API from the engine as well as some knowledge about the UI.
 *  While icons are a purely an UI concern, trying to support multiple UIs is also
 *  causing a lot of architectural issues for reasons that is propably too idealistic.
 *  Maybe we should just bake in support for the "default" UI, but just keep the APIs
 *  generic enough that it isn't impossible to replace icons.
 *  It especially becomes problematic once you start considering server/client scenarios :/
 */
const val iconRootPath = "icons/cached/players/iconsets"
const val portraitRootPath = "icons/cached/players/portraits"
