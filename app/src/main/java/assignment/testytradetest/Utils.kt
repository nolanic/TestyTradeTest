package assignment.testytradetest

import java.io.*

object Utils {

    fun openFileInputStream(fileName:String) : InputStream? {
        return runCatching { App.getInstance().openFileInput(fileName)}.getOrNull()
    }

    fun openFileOutputStream(fileName: String) : OutputStream {
        return App.getInstance().openFileOutput(fileName, 0)
    }

    fun serializeObject(serializable: Serializable, outputStream : OutputStream) {
        val objectStream = ObjectOutputStream(outputStream)
        objectStream.writeObject(serializable)
        objectStream.close()
    }

    fun <T:Serializable>deserializeObject(inputStream: InputStream) : T? {
        val objectStream = ObjectInputStream(inputStream)
        val `object` = runCatching { objectStream.readObject() as T }.getOrNull()
        objectStream.close()
        return `object`
    }

    fun writeObjectToFile(fileName:String, serializable: Serializable) {
        val fileOutputStream = openFileOutputStream(fileName)
        serializeObject(serializable, fileOutputStream)
        fileOutputStream.close()
    }

    fun <T:Serializable>getObjectFromFile(fileName: String) : T? {
        val fileInputStream = openFileInputStream(fileName)
        if (fileInputStream == null) {
            return null
        }
        val `object` = deserializeObject<T>(fileInputStream)
        fileInputStream.close()
        return `object`
    }
}

fun <T>MutableList<T>.removeIfCompat(ifFilter : (T)-> Boolean) {
    val itemsToRemove = this.filter { ifFilter(it) }
    this.removeAll(itemsToRemove)
}