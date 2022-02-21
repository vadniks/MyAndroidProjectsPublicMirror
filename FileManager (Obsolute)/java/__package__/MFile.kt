/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import java.io.File
import java.net.URI

/**
 * @author Vad Nik.
 * @version dated Jan 05, 2019.
 * @link http://github.com/vadniks
 */
internal class MFile : File {

    constructor(path: String) : super(path)

    constructor(parent: String, child: String) : super(parent, child)

    constructor(parent: File, child: String) : super(parent, child)

    constructor(uri: URI) : super(uri)

    constructor(file: File) : super(file.parent, file.name)

    override fun canRead(): Boolean = if (Processing.isRooted.invoke()) true else super.canRead()

    override fun canWrite(): Boolean = if (Processing.isRooted.invoke()) true else super.canWrite()

    override fun listFiles(): Array<File> {
        println("testo ls $path")

        if (super.canRead())
            return super.listFiles()
        else if (!Processing.isRooted.invoke())
            return super.listFiles()

//        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls", super.getPath()))
//        val b = BufferedReader(InputStreamReader(p.inputStream)).readLines()
//        p.destroy()

        val b = ls(super.getPath()).toArray()

        val a = ArrayList<File>()
        for (i in b)
            a.add(MFile(super.getPath(), i))

        return a.toTypedArray()
    }

    private external fun ls(path: String): String

    private fun String.toArray(): Array<String> {
        val arr = ArrayList<String>()
        var tmp = ""

        for (i in this) {
            if (i == '\n') {
                arr.add(tmp)
                tmp = ""
            } else
                tmp += i
        }

        println("testo toArray $this")

        return arr.toTypedArray()
    }

    override fun isDirectory(): Boolean {
        println("testo isd $path")

        if (super.canRead())
            return super.isDirectory()
        else if (!Processing.isRooted.invoke())
            return super.isDirectory()

//        val p = Runtime.getRuntime().exec(
//            arrayOf("su", "-c", "[", "-d", "\"${super.getPath()}\"", "]", "&&", "echo", "a"))
//        val b = BufferedReader(InputStreamReader(p.inputStream)).readLine()
//        p.destroy()
//
//        return b == "a"

        println("testo isd return $path")

        return isDir(super.getPath())
    }

    private external fun isDir(file: String): Boolean

    override fun exists(): Boolean {
        if (super.canRead())
            return super.exists()
        else if (!Processing.isRooted.invoke())
            return super.exists()

        return fileExists(super.getPath()) || isDir(super.getPath())
    }

    private external fun fileExists(path: String): Boolean
}
