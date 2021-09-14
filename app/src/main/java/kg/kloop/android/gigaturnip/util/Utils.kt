package kg.kloop.android.gigaturnip.util

import android.net.Uri
import java.io.File

fun getFileName(uri: Uri): String = File(uri.path!!).name
