package org.waxberry.zscnews.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by GJH-08 on 2014/12/11.
 */
public class FileService {

    private Context mContext;

    public FileService(Context pContext)
    {
        mContext = pContext;
    }

    /**
     * 判断储存卡是否可用。
     */
    public boolean isExternalStorage()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        else
        {
            Log.i("deleteFileInExternalStorage", "SD card is unmounted.");
            return false;
        }
    }

    /**
     * 返回程序的files目录。
     * @param pPath 指定files目录的子目录。
     */
    public String getApplicationFilesPathInExternalStorage(String pPath)
    {
        if (isExternalStorage())
        {
            if(mContext.getExternalFilesDir(pPath) != null)
            {
                return mContext.getExternalFilesDir(pPath).getPath();
            }
            else
            {
                // Android/data/com.gjh.cantonfair/files
                if(pPath == null)
                {
                    pPath = "";
                }
                return Environment.getExternalStorageDirectory().getPath()
                        + "/Android/data/org.waxberry.zscnews/files" + pPath;
            }
        }
        return null;
    }

    /**
     * 返回程序的cache目录。
     */
    public String getApplicationCachePathInExternalStorage()
    {
        if (isExternalStorage())
        {
            if(mContext.getExternalCacheDir() != null)
            {
                return mContext.getExternalCacheDir().getPath();
            }
            else
            {
                // Android/data/com.gjh.cantonfair/cache
                return Environment.getExternalStorageDirectory().getPath()
                        + "/Android/data/org.waxberry.zscnews/cache";
            }
        }
        return null;
    }

    /**
     * 计算Cache目录大小。
     */
    public long getCacheSize()
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return 0;
        }
        // 获取程序的完整目录。
        String Cache_External = getApplicationCachePathInExternalStorage();
        File file = new File(Cache_External);

        return getFolderSize(file);
    }

    /**
     * 递归计算目录大小。
     */
    public long getFolderSize(File file)
    {
        long size = 0;
        try
        {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    size = size + getFolderSize(fileList[i]);

                }else{
                    size = size + fileList[i].length();

                }
            }
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }

        return size;
    }

    /**
     * 删除文件或目录。
     */
    public void deleteCache()
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return;
        }
        // 获取程序的完整目录。
        String Cache_External = getApplicationCachePathInExternalStorage();
        File file = new File(Cache_External);
        // 删除文件。
        deleteFile(file);
    }

    /**
     * 删除文件或目录。
     */
    public void deleteCache(String PathName)
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return;
        }
        // 获取程序的完整目录。
        String Cache_External = getApplicationCachePathInExternalStorage() + "/" + PathName;
        File file = new File(Cache_External);
        if(!file.exists())
        {
            return;
        }
        // 删除文件。
        deleteFile(file);
    }

    /**
     * 递归删除文件或目录。
     * @param file 需要删除的文件或目录。
     */
    public void deleteFile(File file)
    {
        try
        {
            if(file.isDirectory())
            {
                File[] fileList = file.listFiles();
                for (int i = 0; i < fileList.length; i++)
                {
                    if (fileList[i].isDirectory())
                    {
                        deleteFile(fileList[i]);

                    }
                    fileList[i].delete();
                }
            }
            else
            {
                file.delete();
            }
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    /**
     * 储存缓存文件到内存卡。
     * @param PathName 需要储存的缓存目录名。
     * @param FileName 需要储存的缓存文件名。
     */
    public void saveCacheFileToExternalStorage(String PathName, String FileName, String Content)
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return;
        }
        // 获取程序的完整目录。
        String Path_External = getApplicationCachePathInExternalStorage();
        // String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        //Toast.makeText(mContext, FileName, Toast.LENGTH_SHORT).show();
        // 保存文件的输出流。
        FileOutputStream fos = null;
        // 设置文件绝对路径。
        String FullFileName = Path_External + "/" + PathName + "/" + FileName;
        // Log.i("savePhotoToExternalStorage", FullFileName);

        File file = new File(Path_External + "/" + PathName);
        try
        {
            if(!file.exists())
            {
                file.mkdirs();
            }
            // Log.d("Name", FullFileName);
            // 把数据写入文件。
            fos = new FileOutputStream(FullFileName);
            fos.write(Content.getBytes());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(fos != null)
                {
                    fos.flush();
                    fos.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isFileExists(String PathName, String FileName)
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return false;
        }
        // 获取程序的完整目录。
        String Path_External = getApplicationCachePathInExternalStorage();
        String FullFileName = Path_External + "/" + PathName + "/" + FileName;

        File file = new File(FullFileName);

        return file.exists();
    }

    /**
     * 从内存卡读取缓存文件。
     * @param PathName 读取的缓存目录名。
     * @param FileName 读取的缓存文件名。
     */
    public String loadCacheFileFromExternalStorage(String PathName, String FileName)
    {
        // 检测sd是否可用。
        if (!isExternalStorage())
        {
            return null;
        }
        // 获取程序的完整目录。
        String Path_External = getApplicationCachePathInExternalStorage();
        // String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        // Toast.makeText(mContext, FileName, Toast.LENGTH_SHORT).show();
        // 读取文件的输入流。
        FileInputStream fis = null;
        String FullFileName = Path_External + "/" + PathName + "/" + FileName;
        String strResult = null;

        try
        {
            fis = new FileInputStream(FullFileName);
            // 把读取到的文件写入String。
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            strResult = new String(buffer);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(fis != null)
                {
                    fis.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return strResult;
    }

}
