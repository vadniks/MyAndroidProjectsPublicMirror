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

#include "RootOperations.h"

/*!
 * \brief implementation of the RootOperations
 *
 * \ingroup cpp
 *
 * \sa RootOperations.h
 */

//TODO: note: lauch app from console: 'su -c am start -a <package>.<launchActivity> -n'.

bool RootOperations::isBusyBoxInstalled() {
    return exec("busybox --help").find("not found") != std::string::npos;
}

char * RootOperations::readFile(std::string file)
{
    return exec2("cat " + file);
}

void RootOperations::writeFile(std::string file, std::string text) {
    exec("echo " + text + " >> " + file);
}

bool RootOperations::chmod(std::string file, RootOperations::chb attrs, bool root)
{
    for (int i = 0; i < CHB_ARR_SIZE; i++)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "testo", "native a %i", attrs.permissions[i]);
    }

    int oR = /*(*/attrs.permissions[0];// == 'r') ? 4 : 0;
    int oW = /*(*/attrs.permissions[1];// == 'w') ? 2 : 0;
    int oX = /*(*/attrs.permissions[2];// == 'x') ? 1 : 0;
    int gR = /*(*/attrs.permissions[3];// == 'r') ? 4 : 0;
    int gW = /*(*/attrs.permissions[4];// == 'w') ? 2 : 0;
    int gX = /*(*/attrs.permissions[5];// == 'x') ? 1 : 0;
    int aR = /*(*/attrs.permissions[6];// == 'r') ? 4 : 0;
    int aW = /*(*/attrs.permissions[7];// == 'w') ? 2 : 0;
    int aX = /*(*/attrs.permissions[8];// == 'x') ? 1 : 0;

    int o = oR + oW + oX;
    int g = gR + gW + gX;
    int a = aR + aW + aX;

    //std::string prefix((root) ? "su -c" : "");

    char s[512];
    snprintf(s, 512, "%s chmod %i%i%i %s", (root) ? "su -c" : "", o, g, a, file.c_str());
    //(prefix + " chmod " + std::to_string(o) + std::to_string(g) + std::to_string(a) + " " + file).c_str();

    __android_log_print(ANDROID_LOG_DEBUG, "testo", "native chmod | %s", s);

    //TODO: add native features like create new file with root.

    return system(s) == 0;
}

RootOperations::chb RootOperations::getFilePermissions(std::string file)
{
    struct stat info;
//    if (!stat(file.c_str(), &info))
//        return RootOperations::chb();

    int a = stat(file.c_str(), &info);
    __android_log_print(ANDROID_LOG_DEBUG, "testo", "native gfp %i %d %d %s", a, !a, true, file.c_str());

    struct passwd *own = getpwuid(info.st_uid);
    struct group *grp = getgrgid(info.st_gid);

    RootOperations::chb res;

    res.file = file.c_str();

    res.owner = own->pw_name;
    res.group = grp->gr_name;

    mode_t perm = info.st_mode;
    res.permissions[0] = (perm & S_IRUSR) ? PERM_READ    : PERM_NONE;
    res.permissions[1] = (perm & S_IWUSR) ? PERM_WRITE   : PERM_NONE;
    res.permissions[2] = (perm & S_IXUSR) ? PERM_EXECUTE : PERM_NONE;
    res.permissions[3] = (perm & S_IRGRP) ? PERM_READ    : PERM_NONE;
    res.permissions[4] = (perm & S_IWGRP) ? PERM_WRITE   : PERM_NONE;
    res.permissions[5] = (perm & S_IXGRP) ? PERM_EXECUTE : PERM_NONE;
    res.permissions[6] = (perm & S_IROTH) ? PERM_READ    : PERM_NONE;
    res.permissions[7] = (perm & S_IWOTH) ? PERM_WRITE   : PERM_NONE;
    res.permissions[8] = (perm & S_IXOTH) ? PERM_EXECUTE : PERM_NONE;

    return res;
}

bool RootOperations::fileExists(std::string path)
{
    std::ifstream f(path.c_str());
    return f.good();
}

bool RootOperations::isDirectory(std::string path)
{
    //DIR *dir;
    bool exists;

    struct stat sb;
    exists = stat(path.c_str(), &sb) == 0 && S_ISDIR(sb.st_mode);

//    dir = opendir(path.c_str());
//    exists = dir != nullptr;
//
//    if (exists)
//        closedir(dir);

    //std::string s = "";
    //__android_log_print(ANDROID_LOG_DEBUG, "testo", "%s", (s + "native isd " + (exists ? "true" : "false") + " " + path).c_str());

    return exists; //exec("[ -d " + path + " ] && echo a").compare("a") == 0;
}

std::string RootOperations::exec(std::string command, bool useSu)
{
    if (useSu)
        command = "su -c " + command;

    std::array<char, 128> buf;
    std::string res;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(command.c_str(), "r"), pclose);

    if (!pipe)
        return nullptr;

    while (fgets(buf.data(), buf.size(), pipe.get()))
        res += buf.data();

    std::cout << "testo exec native " << command << " " << res << std::endl;

    return res;
}

int RootOperations::byte_arrSize;

/*RootOperations::byte_arr*/ char * RootOperations::exec2(std::string command)
{
    command = "su -c " + command;

    std::array<unsigned char, 256> buf;
    //std::vector<unsigned char> arr;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(command.c_str(), "r"), pclose);
    //std::string res;

    if (!pipe)
        return nullptr; //Pair<byte_arr, int>({}, 0);

    /*while (*/char *s = fgets((char *) buf.data(), buf.size(), pipe.get());//)
    /*{
        char _buf[256];
        snprintf(_buf, 256, "%c", *buf.data());
        //res += _buf;
        __android_log_print(ANDROID_LOG_DEBUG, "testo", "%c %s %s", *buf.data(), _buf, s);
        arr.push_back(*buf.data());
    }

    RootOperations::byte_arrSize = (int) arr.size();

    byte_arr a = &arr[0];

    */
    return s; //bytesToByteArr(arr); //, (int) arr.size());
}

RootOperations::byte_arr RootOperations::bytesToByteArr(std::vector<__int8_t> src)
{
//    byte_arr o[(int) src.size()];
//    byte_arr out = o;
//
//    int count = 0;
//    for (byte_arr i : src)
//    {
//        out[count] = i;
//        count++;
//    }

    return nullptr;
}

std::string RootOperations::ls(std::string path)
{
    return exec("ls " + path);
}

bool RootOperations::copyFile(std::string file, std::string path)
{
    std::ifstream src(file, std::ios::binary);
    std::ofstream dst(path + "/" + file, std::ios::binary);

    dst << src.rdbuf();
    dst.close();

    return dst.good();
}

bool RootOperations::copyFolder(std::string file, std::string path)
{
    return system(("cp " + file + " " + path + "/" + file).c_str()) == 0;
}

bool RootOperations::move(std::string file, std::string path)
{
    return renameFile(file, path);
}

bool RootOperations::renameFile(std::string file, std::string newName)
{
    return rename(file.c_str(), newName.c_str()) == 0;
}

bool RootOperations::renameFolder(std::string file, std::string newName)
{
    return system(("mv " + file + " " + newName).c_str()) == 0;
}

bool RootOperations::deleteFile(std::string file)
{
    return std::remove(file.c_str()) == 0;
}

bool RootOperations::deleteFolder(std::string folder)
{
    DIR *d = opendir(folder.c_str());
    size_t pathLen = strlen(folder.c_str());
    int r = -1;

    if (d)
    {
        struct dirent *p;
        r = 0;

        while (!r && (p = readdir(d)))
        {
            int r2 = -1;
            char *buf;
            size_t len;

            if (!strcmp(p->d_name, ".") || !strcmp(p->d_name, ".."))
                continue;

            len = pathLen + strlen(p->d_name) + 2;
            buf = static_cast<char *>(malloc(len));

            if (buf)
            {
                struct stat statbuf;

                snprintf(buf, len, "%s/%s", folder.c_str(), p->d_name);

                if (!stat(buf, &statbuf))
                {
                    if (S_ISDIR(statbuf.st_mode))
                        r2 = deleteFolder(buf);
                    else
                        r2 = unlink(buf);
                }

                free(buf);
            }

            r = r2;
        }

        closedir(d);
    }

    if (!r)
        r = rmdir(folder.c_str());

    return r == 0;
}

bool RootOperations::touch(std::string path, std::string name)
{
    std::ofstream newFile(path + "/" + name);
    newFile << "" << std::endl;
    newFile.close();

    std::ifstream f(name);
    return f.good();
}

bool RootOperations::mkdir(std::string path, std::string name)
{
    const int exitCode = system(("su mkdir -p " + path + "/" + name).c_str());
    return exitCode == 0;
}
