/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code in source and/or binary form,
 * without author's written permission, are strongly prohibited. 
 */

/*************************************
 * Created on 07.01.2019. *
 *************************************/

#ifndef FILEMANAGER_ROOTOPERATIONS_H
#define FILEMANAGER_ROOTOPERATIONS_H

#include <string>
#include <iostream>
#include <fstream>
#include <cstdlib>
#include <dirent.h>
#include <linux/stat.h>
#include <sys/stat.h>
#include <zconf.h>
#include <memory>
#include <stdexcept>
#include <array>
#include <cstdio>
#include <vector>
#include <android/log.h>
#include <stdint.h>
#include <pwd.h>
#include <grp.h>

#define PERM_READ    4
#define PERM_WRITE   2
#define PERM_EXECUTE 1
#define PERM_NONE    0

#define CHB_ARR_SIZE 9

/*!
 * \class RootOperations
 *
 * \brief The RootOperations class provides
 *        the standard file operations
 *        functions but only for those
 *        files that require the 'root'
 *        access, for others use the Java's
 *        methods.
 *        This class is used for JNI.
 *
 * \ingroup cpp
 *
 * \sa RootOperations.cpp,
 *     native-lib.cpp
 */
class RootOperations {

public:

    [[deprecated("unused")]]
    typedef unsigned char * byte_arr;

    typedef struct ChmodBundle
    {
        std::string owner;
        std::string group;
        int permissions[9];
        std::string file;
    } chb;

    /*!
     * \brief contains byte_arr size after
     * readFile function would finished.
     */
    [[deprecated("unused")]]
    static int byte_arrSize;

private:

    [[deprecated("unused")]]
    static byte_arr bytesToByteArr(std::vector<__int8_t > src);

public:
    static bool isDirectory(std::string path);
    static std::string exec(std::string command, bool useSu = true);
    static bool fileExists(std::string path);
    static bool isBusyBoxInstalled();
    static char * readFile(std::string file); //TODO: make it read and return byte array.
    static void writeFile(std::string file, std::string text);
    static chb getFilePermissions(std::string file);
    static bool chmod(std::string file, chb attrs, bool root);

    //TODO: add 'get file permissions' feature.

    //[[deprecated("does stuff just like the original function")]]
    static char * exec2(std::string command);

    /*!
     * \brief equivalent for console
     *        command 'ls'.
     */
    static std::string ls(std::string path);

    /*!
     * \param file the copied one.
     * \param path path where the
     *        file will be copied in.
     */
    static bool copyFile(std::string file, std::string path);

    /*!
     * \param folder the copied one.
     * \param path path where the
     *        file will be copied in.
     */
    static bool copyFolder(std::string file, std::string path);

    /*!
     * \param file (folder) the moved one.
     * \param path path where the
     *        file will be moved in.
     */
    static bool move(std::string file, std::string path);

    /*!
     * \param file one that will be renamed.
     * \param newName what the file will
     *        be renamed to.
     */
    static bool renameFile(std::string file, std::string newName);

    /*!
     * \param folder one that will be renamed.
     * \param newName what the file will
     *        be renamed to.
     */
    static bool renameFolder(std::string folder, std::string newName);

    /*!
     * \param file to be deleted.
     */
    static bool deleteFile(std::string file);

    /*!
     * \param folder to be deleted.
     */
    static bool deleteFolder(std::string folder);

    /*!
     * \brief creates file.
     *
     * \param path where new file will be created in.
     * \param name the file's new name.
     */
    static bool touch(std::string path, std::string name);

    /*!
     * \brief creates folder.
     *
     * \param path where new folder will be created in.
     * \param name the folder's new name.
     */
    static bool mkdir(std::string path, std::string name);
};


#endif //FILEMANAGER_ROOTOPERATIONS_H
