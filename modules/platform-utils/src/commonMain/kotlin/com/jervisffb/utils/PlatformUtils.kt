package com.jervisffb.utils

/**
 * Return the current thread id.
 */
public expect fun threadId(): ULong

/**
 * Returns the public IP address of this machine
 */
public expect fun getPublicIp(): String

/**
 * Returns the IP address of this machine on the local network
 */
public expect fun getLocalIpAddress(): String

/**
 * Open a URL in a new tab in a System browser.
 * Returns `true` if it succeeded, `false` if not.
 * In that case, any error is logged.
 */
public expect fun openUrlInBrowser(url: String): Boolean

/**
 * Returns whether this platform is able to be a P2P Host, i.e., can set up
 * a web server.
 */
public expect fun canBeHost(): Boolean

/**
 * A short string describing the build target.
 */
public expect fun getBuildType(): String

/**
 * Returns a string containing information about the current platform and runtime.
 */
public expect fun getPlatformDescription(): String
