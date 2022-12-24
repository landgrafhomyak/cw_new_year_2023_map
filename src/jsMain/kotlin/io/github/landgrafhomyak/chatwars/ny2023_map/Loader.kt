package io.github.landgrafhomyak.chatwars.ny2023_map

import kotlinx.browser.document
import org.w3c.xhr.XMLHttpRequest

public fun main() {
    val xhr = XMLHttpRequest()
    xhr.open("GET", "/data", async = false)
    xhr.send()
    if (xhr.status != 200.toShort()) {
        // todo
        return
    }
    val north = xhr.getResponseHeader("North")!!.toInt()
    val east = xhr.getResponseHeader("East")!!.toInt()
    val south = xhr.getResponseHeader("South")!!.toInt()
    val west = xhr.getResponseHeader("West")!!.toInt()
    val sb = StringBuilder()
    val data = (xhr.response as String).map { b -> TileType.fromSerial(b) }

    val width = east - west + 1

    var i = data.size - width
    for (y in north downTo south) {
        sb.append("<tr>")
        for (x in west..east) {
            sb.append("<td coords='${x}#${y}' class='")
            if (data[i] != null) {
                if (x < 0) {
                    if (y < 0) {
                        sb.append("black ")
                    } else if (y > 0) {
                        sb.append("red ")
                    } else {
                        sb.append("forbidden ")
                    }
                } else if (x > 0) {
                    if (y < 0) {
                        sb.append("blue ")
                    } else if (y > 0) {
                        sb.append("white ")
                    } else {
                        sb.append("forbidden ")
                    }
                } else {
                    sb.append("forbidden ")
                }
                sb.append(data[i]!!.cssClass)
            } else if (x == 0 || y == 0) {
                sb.append("forbidden-hidden")
            }
            if (x == 0 && y == 0)
                sb.append("' id='zero")
            sb.append("'>")
            if (data[i] != null) {
                sb.append("<div></div>")
            }
            sb.append("</td>")
            i++
        }
        i -= width * 2
        sb.append("</tr>")
    }
    document.write(sb.toString())
}