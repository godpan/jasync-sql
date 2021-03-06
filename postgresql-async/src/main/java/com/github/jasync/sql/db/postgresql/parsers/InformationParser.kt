package com.github.jasync.sql.db.postgresql.parsers

import com.github.jasync.sql.db.util.ByteBufferUtils
import com.github.jasync.sql.db.postgresql.messages.backend.ServerMessage
import io.netty.buffer.ByteBuf
import java.nio.charset.Charset

abstract class InformationParser(val charset: Charset) : MessageParser {

  override fun parseMessage(b: ByteBuf): ServerMessage {

    val fields = mutableMapOf<Char, String>()
    while (b.isReadable) {
      val kind = b.readByte()
      if (kind.toInt() != 0) {
        fields[kind.toChar()] = ByteBufferUtils.readCString(b, charset)
      }
    }

    return createMessage(fields.toMap())
  }

  abstract fun createMessage(fields: Map<Char, String>): ServerMessage

}
