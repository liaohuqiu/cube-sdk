package in.srain.cube.diskcache;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

    /**
     * Returns the remainder of 'reader' as a string, closing it when done.
     */
    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * Returns the ASCII characters up to but not including the next "\r\n", or
     * "\n".
     *
     * @throws java.io.EOFException if the stream is exhausted before the next newline
     *                              character.
     */
    public static String readAsciiLine(InputStream in) throws IOException {
        StringBuilder result = new StringBuilder(80);
        while (true) {
            int c = in.read();
            if (c == -1) {
                throw new EOFException();
            } else if (c == '\n') {
                break;
            }

            result.append((char) c);
        }
        int length = result.length();
        if (length > 0 && result.charAt(length - 1) == '\r') {
            result.setLength(length - 1);
        }
        return result.toString();
    }

    /**
     * Closes 'closeable', ignoring any checked exceptions. Does nothing if 'closeable' is null.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Try to delete directory in a fast way.
     */
    public static void deleteDirectoryQuickly(File dir) throws IOException {

        if (!dir.exists()) {
            return;
        }
        final File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(to);
        if (!dir.exists()) {
            // rebuild
            dir.mkdirs();
        }

        // try to run "rm -r" to remove the whole directory
        if (to.exists()) {
            String deleteCmd = "rm -r " + to;
            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec(deleteCmd);
                process.waitFor();
            } catch (IOException e) {

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!to.exists()) {
            return;
        }
        deleteDirectoryRecursively(to);
        if (to.exists()) {
            to.delete();
        }
    }

    public static void chmod(String mode, String path) {
        try {
            String command = "chmod " + mode + " " + path;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * recursively delete
     *
     * @param dir
     * @throws java.io.IOException
     */
    public static void deleteDirectoryRecursively(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("not a directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectoryRecursively(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    public static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    public static boolean writeString(String filePath, String content) {
        File file = new File(filePath);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        FileWriter writer = null;
        try {

            writer = new FileWriter(file);
            writer.write(content);

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {

                    writer.close();
                    return true;
                }
            } catch (IOException e) {
            }
        }
        return false;
    }

    public static String readString(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;

        FileInputStream fileInput = null;
        FileChannel channel = null;
        try {
            fileInput = new FileInputStream(filePath);
            channel = fileInput.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(buffer.array());
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
        } finally {

            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
