package filesave;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class FileUtils {

    // 这个用于设置是否写入外部文件，0表示不是外部文件，1表示是外部文件
    private int writtenExternalFile = 0;

    public FileUtils() {
    }

    public FileUtils(int writtenExternalFile) {
        this.writtenExternalFile = writtenExternalFile;
    }

    /**
     * 写入文件内容
     *
     * @param context  Android环境下的Context对象，非Android环境下传入null
     * @param path     文件路径，不传入则为""
     * @param filename 文件名
     * @param content  写入的内容
     */
    public void saveFile(Object context, String path, String filename, String content) {
        // 如果当前环境是不是Android，则调用Java的文件保存方法，如果是Android，则调用Android的文件保存方法
        if (isAndroid()) {
            if (this.writtenExternalFile == 0)
                AndroidFileUtils.saveFile(context, path, filename, content);
            else
                AndroidFileUtils.saveFileExternal(context, path, filename, content);
        } else {
            JavaFileUtils.saveFile(path, filename, content);
        }
    }

    // 判断是否在Android环境下运行
    private boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;  // 如果找到了android.os.Build类，说明是Android平台
        } catch (ClassNotFoundException e) {
            return false; // 如果没有找到该类，说明是PC平台
        }
    }

    public int getWrittenExternalFile() {
        return writtenExternalFile;
    }

    public void setWrittenExternalFile(int writtenExternalFile) {
        this.writtenExternalFile = writtenExternalFile;
    }


    static class JavaFileUtils {
        // PC特有的文件保存方法

        /**
         * 在非Android中保存文件
         *
         * @param filepath 文件路径，不传入则为""，表示当前项目根目录
         * @param fileName 文件名
         * @param content  文件内容
         */
        public static void saveFile(String filepath, String fileName, String content) {
            // 在PC端，完全不引用android相关类，直接使用标准Java代码
            try {
                // 在PC端直接使用FileOutputStream保存文件
                if (!filepath.startsWith(File.separator)) {
                    filepath = System.getProperty("user.dir") + File.separator + filepath;
                }// 检查path是否为相对路径，如果是则转换为当前项目的绝对路径

                File file = new File(filepath, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content.getBytes());
//                    System.out.println("PC environment detected, write data to PC successfully!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class AndroidFileUtils {

        /**
         * 在Android中保存文件（内部目录）
         *
         * @param context  Android环境下的Context对象
         * @param path     文件路径（非必要）
         * @param filename 文件名
         * @param content  文件内容
         */
        public static void saveFile(Object context, String path, String filename, String content) {
            // Android的文件保存方法
            // 只有在Android环境下才会执行Android相关的代码
            try {
                Class<?> contextClass = context.getClass();
                {
                    // 仅限于在应用的内部存储空间中创建文件
                    // 使用反射获取getFilesDir方法
                    Method getFilesDirMethod = contextClass.getMethod("getFilesDir");
                    Object filesDir = getFilesDirMethod.invoke(context);
                    File appFilesDir = (File) filesDir;

                    writeData(path, filename, content, appFilesDir);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 在Android中保存文件（外部目录）
         *
         * @param context  Android环境下的Context对象
         * @param path     文件路径（非必要）
         * @param filename 文件名
         * @param content  文件内容
         */
        public static void saveFileExternal(Object context, String path, String filename, String content) {
            if (isAndroid10OrAbove()) {
                // Android 10及以上版本使用新的存储方式
                saveFileAndroid10(context, path, filename, content);
            } else {
                // Android 10以下版本使用旧的存储方式
                saveFileAndroid9(context, path, filename, content);
            }
        }

        /**
         * 判断是否为Android 10及以上版本
         *
         * @return
         */
        private static boolean isAndroid10OrAbove() {
            // 判断是否为Android 10（API 29）或更高版本
            try {
                // 如果运行在Android环境且版本大于等于10（API 29），返回true
                Class<?> versionClass = Class.forName("android.os.Build$VERSION");
                int sdkInt = (int) versionClass.getField("SDK_INT").get(null);
                return sdkInt >= 29;  // 判断是否为 Android 10 或更高版本
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Android 9及以下版本保存文件（外部目录）
         *
         * @param context  Android环境下的Context对象
         * @param path     文件路径
         * @param filename 文件名
         * @param content  文件内容
         */
        private static void saveFileAndroid9(Object context, String path, String filename, String content) {
            try {
                Class<?> environmentClass = Class.forName("android.os.Environment");
                File externalStorageDirectory = (File) environmentClass.getMethod("getExternalStorageDirectory").invoke(null);
                writeData(path, filename, content, externalStorageDirectory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 写入数据到文件
         *
         * @param path       文件路径
         * @param filename   文件名
         * @param content    文件内容
         * @param appFileDir 获取到的文件目录
         */
        private static void writeData(String path, String filename, String content, File appFileDir) {
            File newDirectory = new File(appFileDir, path);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }

            File dataFile = new File(newDirectory, filename);
            try (FileOutputStream fos = new FileOutputStream(dataFile)) {
                fos.write(content.getBytes());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Android 10及以上版本保存文件（外部目录）
         *
         * @param context  Android环境下的Context对象
         * @param path     文件路径
         * @param filename 文件名
         * @param content  文件内容
         */
        private static void saveFileAndroid10(Object context, String path, String filename, String content) {
            try {
                Class<?> contextClass = context.getClass();

                Class<?> contentValuesClass = Class.forName("android.content.ContentValues");

                Class<?> mediaColumnsClass = Class.forName("android.provider.MediaStore$MediaColumns");

                Class<?> environmentClass = Class.forName("android.os.Environment");

                String displayName = (String) mediaColumnsClass.getDeclaredField("DISPLAY_NAME").get(null);
                String mimeType = (String) mediaColumnsClass.getDeclaredField("MIME_TYPE").get(null);
                String relativePath = (String) mediaColumnsClass.getDeclaredField("RELATIVE_PATH").get(null);
                String environmentDirectoryDownloads = (String) environmentClass.getDeclaredField("DIRECTORY_DOWNLOADS").get(null); // TODO 这里的目录可以改为文档或者下载
//            String environmentDirectoryDocuments = (String) environmentClass.getDeclaredField("DIRECTORY_DOCUMENTS").get(null); // TODO 文档目录

                // 创建 ContentValues 对象
                Object contentValuesObject = contentValuesClass.getDeclaredConstructor().newInstance();

                Method putMethod = contentValuesClass.getMethod("put", String.class, String.class); // 获取 put 方法，参数为 String, Object

                putMethod.invoke(contentValuesObject, displayName, filename);
                putMethod.invoke(contentValuesObject, mimeType, "text/plain");
                putMethod.invoke(contentValuesObject, relativePath, environmentDirectoryDownloads + "/" + path);

                {
                    // 通过反射进行文件写入
                    Method getContentResolverMethod = contextClass.getMethod("getContentResolver");

                    Object contentResolver = getContentResolverMethod.invoke(context);

                    Class<?> mediaStoreClass = Class.forName("android.provider.MediaStore$Files");

                    Method getContentUriMethod = mediaStoreClass.getMethod("getContentUri", String.class);

                    Object contentUri = getContentUriMethod.invoke(null, "external");

                    Class<?> uriClass = Class.forName("android.net.Uri");

                    Method insertMethod = contentResolver.getClass().getMethod("insert", uriClass, contentValuesClass);

                    Object uriResult = insertMethod.invoke(contentResolver, contentUri, contentValuesObject);

                    Method openOutputStreamMethod = contentResolver.getClass().getMethod("openOutputStream", uriClass);
//                OutputStream fos = (OutputStream) openOutputStreamMethod.invoke(contentResolver, uriResult);// 这一行是优化后的代码，如果出现问题，就注释掉下面，使用这一行

                    // 通过反射调用 openOutputStream 获取 OutputStream
                    try (OutputStream fos = (OutputStream) openOutputStreamMethod.invoke(contentResolver, uriResult)) {
                        if (fos != null) {
                            fos.write(content.getBytes());
                            fos.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
