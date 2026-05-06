package io.github.pideer.pes

import io.github.pideer.pes.ble.parsers.toHeader
import io.github.pideer.pes.data.PayloadType
import io.github.pideer.pes.data.TransferHeaderData
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TransferParserTest {
    @Test
    fun header_test(){
        val header = byteArrayOf(
            1,                  // type
            17, 0, 0, 0,        // length
            0x12.toByte(),      // timestamp
            0x4b.toByte(),
            0xcf.toByte(),
            0x69.toByte(),
            0, 0, 0, 0
        )

        assertEquals(
            header.toHeader(),
            TransferHeaderData(
                PayloadType.DEBUG,
                17.toUInt(),
                1775192850
            )
        )
    }

    @Test
    fun payload_test(){
        val payload = byteArrayOf(
            0x61.toByte(),
            0x62.toByte(),
            0x63.toByte(),
            0x64.toByte(),
            0x65.toByte(),
            0x66.toByte(),
            0x67.toByte(),
            0x68.toByte(),
            0x69.toByte(),
            0x6A.toByte(),
            0x6B.toByte(),
            0x6C.toByte(),
            0x6D.toByte(),
            0x6E.toByte(),
            0x6F.toByte(),
            0x70.toByte()
        )

        assertEquals(payload.toString(Charsets.UTF_8), "abcdefghijklmnop")
    }
}