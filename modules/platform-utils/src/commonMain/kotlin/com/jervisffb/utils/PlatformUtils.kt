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
