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
    // data[width * -north + -west] = TileType.ZERO


    var i = data.size - width
    sb.append("<tr class='coords'><td><div></div></td>")
    for (x in west..east) {
        sb.append("<td>${x}</td>")
    }
    sb.append("<td><div></div></td></tr>")
    for (y in north downTo south) {
        sb.append("<tr><td class='coords'>${y}</td>")
        for (x in west..east) {
            sb.append("<td coords='${x}#${y}' class='")
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
                if (y == 0)
                    sb.append("' id='")
            }
            if (data[i] != null) {
                sb.append(data[i]!!.cssClass)
            }
            sb.append("'>")
            sb.append("</td>")
            i++
        }
        i -= width * 2
        sb.append("<td class='coords'>${y}</td></tr>")
    }
    sb.append("<tr class='coords'><td><div></div></td>")
    for (x in west..east) {
        sb.append("<td>${x}</td>")
    }
    sb.append("<td><div></div></td></tr>")
    document.write(sb.toString())
}