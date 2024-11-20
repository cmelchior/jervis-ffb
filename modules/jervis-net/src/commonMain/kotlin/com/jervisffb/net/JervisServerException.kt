package com.jervisffb.net

import com.jervisffb.net.messages.JervisErrorCode

class JervisServerException(errorCode: JervisErrorCode, message: String): Exception("[$errorCode] $message")
