/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.view.ActionMode
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author Vad Nik.
 * @version dated Dec 13, 2018.
 * @link http://github.com/vadniks
 */
internal class Processing(var currentPath: String, private val view: MainView) {
    private val items = ArrayList<FileView>()
    private var recycler: RecyclerView
    private lateinit var adapterGrid: MainGridRecyclerAdapter
    private val chosenItems = ArrayList<FileView>()
    private var isChoosingPath = false
    private var chMode = -1
    var isChoosingItems = false
    private lateinit var srl: SwipeRefreshLayout
    private lateinit var search: SearchView
    private val searchResults = ArrayList<FileView>()
    private var foundFilesCount = 0
    private var endSearch = false
    private var forbidItemChoosing = false
    var dontHideActionMode = false
    private var isSearching = false
    var dirCounter = 0
    private val chosenViews = ArrayList<View>()
    private var hasUserChoosenSmth = false

    private var onLongClickListener: (v: View, f: FileView) -> Unit = { v, f ->
        (v.context as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {
            private val ID_COPY               = 0
            private val ID_MOVE               = 1
            private val ID_DELETE             = 2
            private val ID_ENTER              = 3
            private val ID_RENAME             = 4
            private val ID_SHOW_FULL_NAME     = 5
            private val ID_CHANGE_PERMISSIONS = 6
            private val ID_ARCHIVATE          = 7

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean = false

            override fun onCreateActionMode(p0: ActionMode?, menu: Menu): Boolean {
                view.setVisibleActionBar(false)
                view.setEnableMenu(false)
                dontHideActionMode = true

                if (isChoosingPath)
                    menu.add(0, ID_ENTER, 0, view._getString(R.string.copy)).setIcon(R.drawable.enter)
                else {
                    isChoosingItems = true

                    menu.add(0, ID_COPY, 0, view._getString(R.string.copy)).setIcon(R.drawable.copy)
                    menu.add(0, ID_MOVE, 1, view._getString(R.string.move)).setIcon(R.drawable.move)
                    menu.add(0, ID_DELETE, 2, view._getString(R.string.delete)).setIcon(R.drawable.delete)
                    menu.add(0, ID_RENAME, 3, view._getString(R.string.rename)).setIcon(R.drawable.rename)
                    menu.add(0, ID_SHOW_FULL_NAME, 4, view._getString(R.string.showFullName))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                    menu.add(0, ID_CHANGE_PERMISSIONS, 5, view._getString(R.string.chmodTip))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
//                    menu.add(0, ID_EXTRACT_ZIP, 6, view._getString(R.string.extractArchive))
//                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                }
                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when(item.itemId) {
                    ID_COPY -> {
                        hasUserChoosenSmth = true
                        forbidItemChoosing = true
                        mode.finish()
                        configurePath(view._getString(R.string.confPath), MODE_COPY)
                    }
                    ID_MOVE -> {
                        hasUserChoosenSmth = true
                        forbidItemChoosing = true
                        mode.finish()
                        configurePath(view._getString(R.string.confPath), MODE_MOVE)
                    }
                    ID_DELETE -> action({ f -> delete(f) }, mode, R.string.errDelete)
                    ID_ENTER -> {
                        for (i in chosenItems) {
                            if (doubles(File(currentPath))) {
                                view.showToast(view._getString(R.string.errCloneAlrd))
                                continue
                            }

                            println("testo configure path ${i.name} $currentPath") //TODO: debug.

                            var successful = true

                            //TODO: test coping and moving files.

                            if (chMode == MODE_COPY) {
                                successful = copy(i.file, currentPath)
                            } else if (chMode == MODE_MOVE) {
                                successful =
                                        if (move(i.file, currentPath)) {
                                            items.remove(i)
                                            true
                                        } else
                                            false
                            }
                            if (!successful)
                                view.showToast(view._getString(R.string.errCopyMove))
                        }
                        chosenItems.clear()
                        isChoosingPath = false
                        mode.finish()
                    }
                    ID_RENAME -> {
                        hasUserChoosenSmth = true
                        forbidItemChoosing = true
                        mode.finish()
                        configureName()
                    }
                    ID_SHOW_FULL_NAME -> {
                        hasUserChoosenSmth = true
                        forbidItemChoosing = true
                        mode.finish()
                        copyToClipboard(f.name)
                        view.showSnackbar(f.name)
                        view.showToast(view._getString(R.string.clipboardTip))
                    }
                    ID_CHANGE_PERMISSIONS -> {
                        hasUserChoosenSmth = true
                        forbidItemChoosing = true
                        mode.finish()
                        prepareForChmod(f)
                    }
//                    ID_EXTRACT_ZIP -> {
//                        hasUserChoosenSmth = true
//                        forbidItemChoosing = true
//                        mode.finish()
//
//                    }
                }
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                isChoosingItems = false
                adapterGrid.notifyDataSetChanged()
                view.setEnableMenu(true)
                forbidItemChoosing = false
                view.setVisibleActionBar(true)
                dontHideActionMode = false

                for (i in chosenViews)
                    i.setBackgroundColor(Color.WHITE)

                if (!hasUserChoosenSmth)
                    chosenItems.clear()

                chosenViews.clear()

                hasUserChoosenSmth = false

                println("testo action mode destroy") //TODO: debug.
            }

            private fun copyToClipboard(msg: String) {
                lateinit var c: Context
                view.doWithContext { context -> c = context }
                val cm = c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.primaryClip = ClipData.newPlainText(view._getString(R.string.copiedText), msg)
            }

            private fun action(block: (f: File) -> Boolean, mode: ActionMode, @StringRes stringId: Int) {
                hasUserChoosenSmth = true
                for (i in chosenItems) {
                    items.remove(i)
                    if (!block.invoke(i.file))
                        view.showToast(view._getString(stringId))
                }
                mode.finish()
                chosenItems.clear()
            }

            private fun configurePath(msg: String, _mode: Int) {
                isChoosingPath = true
                chMode = _mode
                (v.context as AppCompatActivity).startSupportActionMode(this)
                //chosenItems.clear()
                refresh()
            }

            private fun configureName() {
                view.askForAString(
                    view._getString(R.string.newNameTip),
                    arrayOf({ s -> rename(chosenItems[0].file, s) }),
                    arrayOf(view._getString(R.string.ok)))
            }
        })
    }

    companion object {
        private var       COLUMN_COUNT = 3 //TODO: add possibility to change this for user.
        private const val MODE_COPY    = 0
        private const val MODE_MOVE    = 1
        private const val NEW_FILE     = 0
        private const val NEW_FOLDER   = 1

        @Deprecated("")
        const val REQUEST_APP_CHOOSER_CALLBACK     = 1034
        @Deprecated("")
        const val REQUEST_APP_CHOOSER_CALLBACK_KEY = "REQUEST_APP_CHOOSER_CALLBACK_KEY"

        lateinit var isRooted: () -> Boolean
    }

    private fun prepareForChmod(file: FileView) {
        val p = getFilePermissions(file.file.path)
        lateinit var c: Context
        view.doWithContext { co -> c = co }

        println("testo pfc ${p.permissions[4]}")

        ChmodDialog(ChBTranslated(p.owner, p.group, p.permissions, file.file.path), c) { res ->
            println("testo chmod ${Arrays.toString(res.permissions)}")
            chmod(file.file.path, res, view.useRoot())
        }.show()
    }

    //TODO: add chown feature i.e. change owner and group.

    init {
        isRooted = view::useRoot

        val ni = FileView.toThese(echoAll(currentPath))
        items.addAll(if (ni.contains(FileView.DOUBLE_DOT_DIR)) ni else addDoubleDot(ni))
        //items.addAll( FileView.toThese(echoAll("/")))
        view.showPath(currentPath)

        recycler = view._findViewById(R.id.recycler) as RecyclerView //findViewById(R.id.recycler)
        recycler.layoutManager = view.initGridManager(COLUMN_COUNT) //GridLayoutManager(this, COLUMN_COUNT)

        recycler.setHasFixedSize(true)
        recycler.setItemViewCacheSize(20)
        recycler.isDrawingCacheEnabled = true
        recycler.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        adapterGrid = MainGridRecyclerAdapter(items, { _, file, item ->
            //TODO: add the '..' folder to go up, if it's not the root folder.

            if (file.isDirectory && file.isDoubleDot) {
                goUp()
                return@MainGridRecyclerAdapter
            }

            println("testo access ${file.name} ${file.file.path} ${file.file.canRead()}") //TODO: debug.

            if (isChoosingItems && !forbidItemChoosing) {
                if (!file.file.canRead() && !view.useRoot()) {
                    view.showToast(view._getString(R.string.access_tip))
                    return@MainGridRecyclerAdapter
                }

                if (chosenItems.contains(file)) {
                    chosenViews.remove(item)
                    chosenItems.remove(file)
                    item.setBackgroundColor(Color.WHITE)
                } else {
                    chosenViews.add(item)
                    chosenItems.add(file)
                    item.setBackgroundColor(Color.CYAN)
                }
                return@MainGridRecyclerAdapter
            }

            if (!file.isDirectory) {
                if (!file.file.canRead() && !view.useRoot()) {
                    view.showToast(view._getString(R.string.access_tip))
                    return@MainGridRecyclerAdapter
                }

                if (file.file.isArchive()) {
                    view.askForAString(
                        view._getString(R.string.archivePathTip),
                        arrayOf({ inp ->
                            extractArchive(file.file, File(inp))
                        }),
                        arrayOf(view._getString(R.string.ok))
                    )
                    return@MainGridRecyclerAdapter
                }

                openFile(file.file) //openAs(file.file)
                return@MainGridRecyclerAdapter
            }

            if (!file.file.canRead() && !view.useRoot()) {
                view.showToast(view._getString(R.string.access_tip))
                return@MainGridRecyclerAdapter
            }

            println("testo ${file.file.path} ${file.file.absolutePath} ${file.file.canonicalPath}") //TODO: debug.

            dirCounter++

            //TODO: add root alternatives for adding, editing and viewing files/folders (like with canRead()).

            if (!file.file.isSymlink())
                updateList(FileView.toThese(echoAll(file.file.path)), file.file.path)
            else
                updateList(FileView.toThese(echoAll(file.file.absolutePath)), file.file.absolutePath)
        }, { v, f, vg ->
            if (forbidItemChoosing)
                return@MainGridRecyclerAdapter

            if (!f.file.canRead() && !view.useRoot()) {
                view.showToast(view._getString(R.string.access_tip))
                return@MainGridRecyclerAdapter
            }

            if (chosenItems.contains(f)) {
                chosenViews.remove(v)
                chosenItems.remove(f)
                v.setBackgroundColor(Color.WHITE)
            } else {
                chosenViews.add(v)
                chosenItems.add(f)
                v.setBackgroundColor(Color.CYAN)
            }

            onLongClickListener.invoke(v, f)
        })

        recycler.adapter = adapterGrid
        recycler.addItemDecoration(view.initItemDivider(GridLayoutManager.VERTICAL))
        recycler.addItemDecoration(view.initItemDivider(GridLayoutManager.HORIZONTAL))
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                srl.isEnabled = (if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(0).top) >= 0
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int): Unit =
                super.onScrollStateChanged(recyclerView, newState)
        })

        srl = view._findViewById(R.id.srl) as SwipeRefreshLayout
        srl.setOnRefreshListener(this::refresh)
    }

    //TODO: add 'open file as' feature, i.e. onLongClick on an item -> show menuItem 'open as' -> as text -> send chosen app file as text.

    fun initSearch(s: SearchView) {
        search = s

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            /**
             * In this context: searches in whole file system.
             */
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null && !p0.isBlank()) {
                    val cp = currentPath

                    //view.startLoading()
                    lateinit var c: Job
                    view.showInfSnackbar("", {
                        endSearch = true
                        println("testo cancel ${c.cancel()}") //TODO: debug.
                        view.showSearchPD(false)
                    }, view._getString(R.string.stop_searching))

                    //TODO: add progress indicator while in recursivily search mode.

                    c = launch(CommonPool) {
                        view.showSearchPD(true)

                        launch(CommonPool) {
                            while (true) {
                                if (!c.isActive) {
                                    endSearch = false
                                    println("testo watch") //TODO: debug.
                                    break
                                }
                            }
                        }

                        search(File(cp), p0, 1)
                    }
                }
                return false
            }

            /**
             * In this context: searches in the current folder,
             * without recursive opening child folders.
             */
            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 == null || p0.isBlank())
                    updateList(ArrayList(), currentPath)
                else
                    updateList(search2(File(currentPath), p0) ?: ArrayList(), currentPath)
                return false
            }
        })
        search.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewDetachedFromWindow(v: View?) {
                isSearching = false
                refresh()
            }

            override fun onViewAttachedToWindow(v: View?) {
                isSearching = true
            }
        })
    }

    private fun openFile(file: File, mime: String? = null) { //TODO: add 'execute as program' feature.
        view._startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(file), mime ?: (if (file.getMimeType() == "") "*/*" else file.getMimeType()).apply {
                println("testo mime ${this}") //TODO: debug.
            })
        }, view._getString(R.string.choose_app)))
    }

    @Deprecated("")
    @RequiresApi(22)
    private fun openFile(file: File, mime: String, sender: IntentSender) {
        view._startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(file), mime)
        }, view._getString(R.string.choose_app), sender))
    }

    @Deprecated("")
    private fun openAs(file: File) {
        if (file.getMimeType() != MIME_TEXT || file.getMimeType() != MIME_ANY) {
            openFile(file)
            return
        }

        val actions = arrayOf<() -> Unit>(
            { openFile(file, MIME_TEXT) },
            {},
            {},
            {},
            {},
            { openFile(file, MIME_ANY) }
        )

        view.doWithContext { context ->
            OpenAsDialog(
                context,
                actions
            )
        }
    }

    @RequiresApi(22)
    private fun makeForOpenAs(file: File, mime: String) = openFile(
        file,
        mime,
        view.createSender(
            REQUEST_APP_CHOOSER_CALLBACK,
            view.createIntent(Any::class.java, Pair(
            REQUEST_APP_CHOOSER_CALLBACK_KEY, true)
        ),
        0)
    )

    private fun addDoubleDot(src: ArrayList<FileView>): ArrayList<FileView> {
        println("testo add '..'")
        val arr = ArrayList<FileView>()

        if (src.isEmpty()) {
            arr.add(FileView.DOUBLE_DOT_DIR)
            return arr
        }

        for ((j, i) in src.withIndex()) {
            if (j == 0) {
                arr.add(FileView.DOUBLE_DOT_DIR)
                arr.add(i.apply {
                    id++
                })
            } else
                arr.add(i.apply {
                    id++
                })
        }
        return arr
    }

    //TODO: test copy and paste operations.

    /**
     * Re-adds elements to list (with clearing).
     */
    private fun updateList(newItems: ArrayList<FileView>, newPath: String) {
        currentPath = newPath

        view.setEmptyTextVisibility(if (newItems.isEmpty()) View.VISIBLE else View.GONE) //empty_text.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE

        items.clear()
        items.addAll(if (newItems.contains(FileView.DOUBLE_DOT_DIR) || isSearching) newItems else addDoubleDot(newItems))

        view.showPath(newPath)
        adapterGrid.notifyDataSetChanged()
    }

    /**
     * Just adds a new element into list and notifies the adapter.
     */
    private fun updateList(newItem: FileView) {
        items.add(newItem)

        if (!items.contains(FileView.DOUBLE_DOT_DIR) && !isSearching) {
            items.clear()
            items.addAll(addDoubleDot(items))
        }

        view.doPost {
            view.setEmptyTextVisibility(if (items.isEmpty()) View.VISIBLE else View.GONE)
            //view.showPath(newItem.file.path)
            view.showPath(currentPath)
            adapterGrid.notifyDataSetChanged()
        }
    }

    private fun refresh() {
        srl.isRefreshing = true

        items.clear()

//        for ((j, i) in File(currentPath).listFiles().withIndex())
//            items.add(FileView(j, i.name, i.isDirectory, i))

        val ni = FileView.toThese(echoAll(currentPath))
        items.addAll(if (ni.contains(FileView.DOUBLE_DOT_DIR)) ni else addDoubleDot(ni))

        adapterGrid.notifyDataSetChanged()

        view.setEmptyTextVisibility(if (items.isEmpty()) View.VISIBLE else View.GONE)

        println("testo refresh $currentPath ${items.isEmpty()}") //TODO: debug.

        view.showPath(currentPath)

        srl.isRefreshing = false
    }

    fun onNewFile(): Unit = view.askForAString(
        view._getString(R.string.fname),
        arrayOf({ s -> onNewFile(s, NEW_FILE) }, { s -> onNewFile(s, NEW_FOLDER) }),
        arrayOf(view._getString(R.string.file), view._getString(R.string.folder)))

    private fun onNewFile(s: String, mode: Int) {
        val f = File(currentPath)

        if (!f.canRead() && !f.canWrite() && !view.useRoot()) {
            view.showToast(view._getString(R.string.access_denied))
            return
        }

        if (!touch(f, s, mode)) {
            view.showToast(view._getString(R.string.errNwFile))
            return
        }
        updateList(FileView(items.size, s, true, File(f, s)))
    }

    fun onGoTo(): Unit = view.askForAString(
        view._getString(R.string.enter_path),
        arrayOf({ s -> onGoTo(s) }),
        arrayOf(view._getString(R.string.ok)))

    private fun onGoTo(s: String) {
        val f = MFile(s)
        if (!f.exists() || !f.isDirectory || !view.useRoot()) {
            view.showToast(view._getString(R.string.folderExistsTip))
            return
        }

        updateList(FileView.toThese(echoAll(s)), s)
    }

    fun onOrientationChanged(conf: Configuration) {
        COLUMN_COUNT =
                if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    5
                else
                    3
        recycler.layoutManager = view.initGridManager(COLUMN_COUNT)
    }

    private fun extractArchive(file: File, dest: File) {
        if (!dest.exists())
            dest.mkdir()

        unzip(file.path, dest.path)
        refresh()
    }

    private fun archivate(folder: File) {

    }

    private fun unzip(src: String, dest: String) {
        val folder = File(dest)

        if (!folder.exists())
            folder.mkdir()

        val fis = FileInputStream(src)
        val zis = ZipInputStream(fis)
        var ze: ZipEntry? = zis.nextEntry
        val buf = ByteArray(1024)

        while (ze != null) {
            val fileName = ze.name
            val newFile = File("$dest/$fileName")
            File(newFile.parent).mkdir()

            val fos = FileOutputStream(newFile)
            var len = 0

            while ({len = zis.read(buf); len}() > 0)
                fos.write(buf, 0, len)

            fos.close()
            zis.closeEntry()
            ze = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()
        fis.close()
    }

    private fun zip(src: String, dest: String) {

    }

    /**
     * Searches recursively.
     * Requires clearing [searchResults] before starting.
     *
     * @param path folder to search in (to search in whole FS, type '/').
     * @param fileName name of file that user wants to find.
     * @param mode 0 - contains, 1 - equals.
     * @return [ArrayList] of [FileView]s, containing searchResults,
     *         null if no results have been found.
     */
    private fun search(path: File, fileName: String, mode: Int) {
        if (!path.isDirectory)
            return
        if (!path.canRead())
            return

        if (endSearch)
            return

        for (i in path.listFiles()) {
            if (endSearch)
                return

            if (i.isDirectory) {
                if (!endSearch)
                    search(i, fileName, mode) //TODO: recursion.
                else {
                    println("testo search add end") //TODO: debug.
                    return
                }
            } else {
                if (if (mode == 0) i.name.contains(fileName) else i.name == fileName) {
                    //searchResults.add(FileView(foundFilesCount, i.name, false, i))

                    updateList(FileView(foundFilesCount, i.name, false, i))

                    println("testo search add ${i.name} ${i.path}") //TODO: debug.

                    foundFilesCount++
                }
            }
        }
        println("testo return search add") //TODO: debug.
    }

    /**
     * Like [search] function, but non-recursively.
     */
    private fun search2(path: File, fileName: String): ArrayList<FileView>? {
        if (!path.isDirectory || !path.canRead()) //TODO: add rooted options for searches.
            return null

        val res = ArrayList<FileView>()

        for ((j, i) in path.listFiles().withIndex()) {
            if (i.name.contains(fileName))
                res.add(FileView(j, i.name, i.isDirectory, i))
        }

        return res
    }

    /**
     * Like [search] function, but using linux pre-built tools.
     * @param type if type equals 0 searches file, folder otherwise.
     */
    @Deprecated("throws IOException with message: 'permission denied''")
    private fun search(path: String, fileName: String, type: Int): ArrayList<FileView>? {
        val proc = Runtime.getRuntime().exec("find $path ${if (type == 0) "-type f " else ""}-iname $fileName*")
        println("testo find ${proc.inputStream.readBytes().toString()}") //TODO: debug.
        return null
    }

    /**
     * Equivalent for clicking on the '..' folder (goes one level up to parent folder).
     */
    fun goUp() {
        if (currentPath == "/")
            return

        val path = File(currentPath).parentFile.path

        println("testo up $path") //TODO: debug.

        if (!File(path).canRead() && !view.useRoot()) {
            view.showToast(view._getString(R.string.access_tip))
            return
        }

        updateList(FileView.toThese(echoAll(path)), path)
    }

    //TODO: add 'rename' feature.

    /**
     * @return true if operation successful, false otherwise.
     */
    private fun copy(file: File, path: String): Boolean {
        if (!file.canRead() && view.useRoot()) {
            //else if (canRead(file) || view.useRoot())

            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "cp", file.path, path))
            //val b = BufferedReader(InputStreamReader(p.inputStream)).readLines()
            val exitVal = p.waitFor()
            p.destroy()

            refresh()

            return exitVal == 0
        } else if (file.canRead()) {

            val f = File(path)
            if (!f.canRead() || !f.canWrite())
                return false

            val new = File(path, file.name)

            if (!file.isDirectory) {
                return if (new.createNewFile()) {
                    new.writeBytes(file.readBytes())

                    refresh()

                    true
                } else
                    false
            } else {
                println("testo !su copy folder")

                val copiedFolder = File(f, file.name)

                //TODO: make TextEditorActivity's label shows file name.

                if (copiedFolder.mkdir())
                    FileUtils.copyDirectory(file, copiedFolder)
                else
                    return false

                refresh()

                return copiedFolder.exists()

//                if (!new.mkdir())
//                    return false
//
//                for (i in file.listFiles()) {
//                    val copied = File(new, i.name)
//
//                    if (!copied.isDirectory) {
//                        if (!copied.createNewFile())
//                            return false
//                    } else
//                        if (!copied.mkdir())
//                            return false
//
//
//                }
            }
        } else
            return false
    }

    /**
     * @return true if operation successful, false otherwise.
     */
    private fun move(file: File, path: String, rename: Boolean = false): Boolean {
        if ((!file.canRead() && view.useRoot()) || (rename && view.useRoot())) {
            //else if (canRead(file) || view.useRoot())

            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "mv", file.path, path))
            //val b = BufferedReader(InputStreamReader(p.inputStream)).readLines()
            val exitVal = p.waitFor()
            p.destroy()

            refresh()

            return exitVal == 0
        } else if (file.canRead() && file.canWrite()) {

            val f = File(path)
            if (!f.canRead() || !f.canWrite() || !f.isDirectory)
                return false

            if (!file.isDirectory) {
                //TODO: if folder add one more branch here and in copy().

                val name = file.name
                val buf = file.readBytes()

                return if (file.delete()) {
                    val moved = File(path, name)

                    if (moved.createNewFile()) {
                        moved.writeBytes(buf)

                        refresh()

                        true
                    } else
                        false
                } else
                    false
            } else {
                val copied = File(f, file.name)

                println("testo !su move dir ${f.name} ${f.path} ${file.name}")

                //if (copied.mkdir())
                    FileUtils.moveDirectory(file, copied)
                //else
                    //return false

                //TODO: make listing files be in alphabetic order.

                refresh()

                return copied.exists()

//                val new = File(path, file.name)
//
//                if (!new.mkdir())
//                    return false
//
//                for (i in file.listFiles()) {
//                    val copied = File(new.path, i.name)
//
//                    if (!i.canRead() || !i.canWrite())
//                        return false
//
//                    if (copied.createNewFile()) { //TODO: add if folder!!!
//                        copied.writeBytes(i.readBytes())
//
//                        if (!i.delete())
//                            return false
//                    } else
//                        return false
//                }
//
//                refresh()
//
//                return file.delete()
            }
        } else
            return false
    }

    /**
     * Renames folders too.
     */
    private fun rename(file: File, newName: String): Boolean {
        chosenItems.clear()
        return if (!file.canRead() && view.useRoot()) {
            val b = move(file, "${file.parentFile.path}/$newName", true)
            refresh()
            b
        } else {
            val b = file.renameTo(File(file.parent, newName))
            refresh()
            b
        }
    }

    /**
     * @return true if operation successful, false otherwise.
     */
    private fun delete(file: File): Boolean { //TODO: test does it remove folder and folder with content.
        if (!file.canRead() && view.useRoot()) {
            //else if (canRead(file) || view.useRoot())

            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "rm", if (file.isDirectory) "-r" else "", file.path))
            //val b = BufferedReader(InputStreamReader(p.inputStream)).readLines()
            val exitVal = p.waitFor()
            p.destroy()

            return exitVal == 0
        } else if (file.canRead() && file.canWrite()) {
            return if (!file.isDirectory)
                file.delete()
            else {
                FileUtils.deleteDirectory(file)
                file.exists()
            }
        } else
            return false
    }

    /**
     * @return true if clone of the given file has been found, false otherwise.
     */
    private fun doubles(file: File): Boolean {
        println("testo doubles ${file.path} ${file.name}")

        var found = false
        for (i in file.listFiles()) {
            found = i.name == file.name
        }
        return found
    }

    /**
     * @param folder in which new empty file will be created.
     * @param mode, if it equals '0' creates file, creates folder otherwise.
     * @return true if operation successful, false otherwise.
     */
    private fun touch(folder: File, fileName: String, mode: Int): Boolean {
        if (!folder.canRead() && view.useRoot()) {

            val p =
                if (mode == 0)
                    Runtime.getRuntime().exec(
                        arrayOf(
                            "su",
                            "-c",
                            "echo",
                            "\"\"",
                            ">",
                            "${folder.path}/$fileName"
                        )
                    ) //TODO: if !useRoot remove 'su -c'.
                else
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "mkdir", "${folder.path}/$fileName"))
            //val b = BufferedReader(InputStreamReader(p.inputStream)).readLines()
            val exitVal = p.waitFor()
            p.destroy()

            return exitVal == 0
        } else if (folder.canRead() && folder.canWrite()) {

            val f = File(folder, fileName)

            return if (mode == NEW_FILE) f.createNewFile() else f.mkdir()
        } else
            return false
    }

    @Deprecated("use File.canRead() || view.useRoot() instead")
    private fun canRead(file: File): Boolean = if (view.getPreference(MainActivity.ENABLE_ROOT)) true else file.canRead()

    @Deprecated("use File.canWrite() || view.useRoot() instead")
    private fun canWrite(file: File): Boolean = if (view.getPreference(MainActivity.ENABLE_ROOT)) true else file.canWrite()

    @Deprecated("useless")
    private external fun rootCopy(file: String, path: String): Boolean

    @Deprecated("useless")
    private external fun rootMove(file: String, path: String): Boolean

    @Deprecated("useless")
    private external fun rootRename(file: String, newName: String): Boolean

    @Deprecated("useless")
    private external fun rootDelete(file: String): Boolean

    @Deprecated("useless")
    private external fun rootDeleteFolder(folder: String): Boolean

    @Deprecated("useless")
    private external fun rootTouch(path: String, name: String): Boolean

    @Deprecated("useless")
    private external fun rootMkDir(path: String, name: String): Boolean

    @Deprecated("useless")
    private external fun rootFind(file: String): String

    private external fun chmod(file: String, attrs: ChBTranslated, root: Boolean = false): Boolean

    private external fun getFilePermissions(file: String): ChBTranslated

    //TODO: make contextual icons be white like copy, rename etc.
}
