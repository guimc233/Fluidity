package me.liuli.fluidity.gui.compose

import me.liuli.fluidity.util.client.logWarn
import me.liuli.fluidity.util.mc
import net.minecraftforge.fml.common.Loader
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.concurrent.thread

object DependencyDownloader {

    val os = System.getProperty("os.name").let {
        if (it.contains("win", true)) {
            "windows"
        } else if (it.contains("mac", true) || it.contains("darwin", true)) {
            "macos"
        } else {
            "linux"
        }
    }
    val arch = when (System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: ${System.getProperty("os.arch")}")
    }

    private val dependencyDir = File("${System.getProperty("user.home")}/.cache/fluidity/deps").also { it.mkdirs() }
    private val dependencyList = mutableListOf<Dependency>()
    var loaded = false
        private set
    var totalSize = 0
        private set
    var downloadedSize = 0
        private set

    init {
        val skikoVer = "0.7.32"
        val baseUrl = "https://ayanoyuugiri.github.io/resources/skiko/$skikoVer"
        dependencyList.add(Dependency(System.mapLibraryName("skiko-$os-$arch"), "$baseUrl/${System.mapLibraryName("skiko-$os-$arch")}", skikoVer))
        if (os == "windows") {
            dependencyList.add(Dependency("icudtl.dat", "$baseUrl/icudtl.dat", skikoVer))
        }
        System.setProperty("skiko.library.path", File(dependencyDir, skikoVer).absolutePath)
    }

    fun asyncLoad() {
        loaded = false
        thread {
            try {
                for (dep in dependencyList) {
                    val file = File(if (dep.subdir.isEmpty()) dependencyDir else File(dependencyDir, dep.subdir).apply { mkdirs() }, dep.name)
                    if (!file.exists()) {
                        logWarn("Dependency 「${dep.name}」 not found, attempt download.")
                        val conn = URL(dep.url).openConnection() as HttpURLConnection
                        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0")
                        conn.addRequestProperty("Connection", "close")
                        val size = conn.contentLength
                        if (size != -1) {
                            totalSize += size
                        }
                        if (conn.responseCode != 200) throw IllegalStateException("status code ${conn.responseCode}")
                        val ips = conn.inputStream
                        val data = ByteArray(1024)
                        var count: Int
                        val out = ByteArrayOutputStream() // crash in downloading progress won't mess up
                        while (ips.read(data).also { count = it } != -1) {
                            out.write(data, 0, count)
                            downloadedSize += count
                            if (size == 1) {
                                totalSize += count
                            }
                        }
                        ips.close()
                        out.close()
                        file.writeBytes(out.toByteArray())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dependencyDir.deleteRecursively() // this may solve the crash while loading
                mc.addScheduledTask { throw e }
            } finally {
                loaded = true
            }
        }
    }

    fun awaitLoad() {
        if (loaded) return

        val frame = JFrame("Please wait...")
        frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE

        val label = JLabel("Downloading...")
        label.preferredSize = Dimension(300, 100)
        frame.contentPane.add(label, BorderLayout.CENTER)

        frame.setLocationRelativeTo(null)
        frame.pack()
        frame.isVisible = true

        while (!loaded) {
            label.text = "Downloading: ${((downloadedSize / totalSize.toFloat()) * 100).toInt()}% ($downloadedSize/$totalSize)"
            Thread.sleep(50)
        }

        frame.isVisible = false
    }

    data class Dependency(val name: String, val url: String, val subdir: String = "")
}