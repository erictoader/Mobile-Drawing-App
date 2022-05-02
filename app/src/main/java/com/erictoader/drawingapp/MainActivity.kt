package com.erictoader.drawingapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private var drawingView : DrawingView? = null
    private var ivBackground : ImageView? = null
    private var btnImageSelect : ImageButton? = null
    private var btnBrushSize : ImageButton? = null
    private var btnUndo : ImageButton? = null
    private var btnTrash : ImageButton? = null
    private var btnSave : ImageButton? = null
    private var btnBrushColor : ImageButton? = null

    private var customProgressDialog : Dialog? = null

    val openGalleryLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                ivBackground = findViewById(R.id.iv_background)
                ivBackground?.setImageURI(result.data?.data)
            }
        }

    val requestPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            permissions.entries.forEach {
                val permName = it.key
                val permGranted = it.value

                if(permGranted && permName == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if(permGranted) {
                        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    } else {
                        Toast.makeText(this, "Permission denied for reading storage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeForBrush(20.0F)

        btnImageSelect = findViewById(R.id.ib_background_select)
        btnImageSelect?.setOnClickListener {
            requestStoragePermission()
        }

        btnBrushSize = findViewById(R.id.ib_brush_size_select)
        btnBrushSize?.setOnClickListener {
            showBrushSizeDialogue()
        }

        btnUndo = findViewById(R.id.ib_undo)
        btnUndo?.setOnClickListener {
            drawingView?.undo(this)
        }

        btnTrash = findViewById(R.id.ib_trash)
        btnTrash?.setOnClickListener {
            drawingView?.resetCanvas()
        }

        btnBrushColor = findViewById(R.id.ib_brush_color_select)
        btnBrushColor?.setOnClickListener {
            showBrushColorDialogue()
        }

        btnSave = findViewById(R.id.ib_save)
        btnSave?.setOnClickListener {
            if(haveReadingPermission()) {
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView : FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }
    }

    private fun haveReadingPermission() : Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationaleDialog("Action denied", "Baby Paint needs to access your external storage in order to change the background image")
        } else {
            requestPermission.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun showBrushSizeDialogue() {
        val brushDialogue = Dialog(this)
        brushDialogue.setContentView(R.layout.dialogue_brush_size)
        brushDialogue.setTitle("Select brush size")

        val btnSmall : ImageButton = brushDialogue.findViewById(R.id.ib_small_button)
        btnSmall.setOnClickListener {
            drawingView?.setSizeForBrush(10.0F)
            brushDialogue.dismiss()
        }

        val btnMedium : ImageButton = brushDialogue.findViewById(R.id.ib_medium_button)
        btnMedium.setOnClickListener {
            drawingView?.setSizeForBrush(20.0F)
            brushDialogue.dismiss()
        }

        val btnLarge : ImageButton = brushDialogue.findViewById(R.id.ib_large_button)
        btnLarge.setOnClickListener {
            drawingView?.setSizeForBrush(30.0F)
            brushDialogue.dismiss()
        }

        brushDialogue.show()
    }

    private fun showBrushColorDialogue() {
        val brushDialogue = Dialog(this)
        brushDialogue.setContentView(R.layout.dialogue_brush_color)
        brushDialogue.setTitle("Select brush color")

        val btnRed : ImageButton = brushDialogue.findViewById(R.id.ib_color_red)
        btnRed.setOnClickListener {
            drawingView?.setColorForBrush(Color.RED)
            brushDialogue.dismiss()
        }

        val btnOrange : ImageButton = brushDialogue.findViewById(R.id.ib_color_orange)
        btnOrange.setOnClickListener {
            drawingView?.setColorForBrush(Color.argb(0xFF, 0xFF, 0xA5, 0x00))
            brushDialogue.dismiss()
        }

        val btnYellow : ImageButton = brushDialogue.findViewById(R.id.ib_color_yellow)
        btnYellow.setOnClickListener {
            drawingView?.setColorForBrush(Color.YELLOW)
            brushDialogue.dismiss()
        }

        val btnGreen : ImageButton = brushDialogue.findViewById(R.id.ib_color_green)
        btnGreen.setOnClickListener {
            drawingView?.setColorForBrush(Color.GREEN)
            brushDialogue.dismiss()
        }

        val btnBlue : ImageButton = brushDialogue.findViewById(R.id.ib_color_blue)
        btnBlue.setOnClickListener {
            drawingView?.setColorForBrush(Color.BLUE)
            brushDialogue.dismiss()
        }

        val btnIndigo : ImageButton = brushDialogue.findViewById(R.id.ib_color_indigo)
        btnIndigo.setOnClickListener {
            drawingView?.setColorForBrush(Color.argb(0xFF, 0x9B, 0x00, 0xBB))
            brushDialogue.dismiss()
        }

        brushDialogue.show()
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view : View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val backgroundRes = view.background

        if(backgroundRes != null) {
            backgroundRes.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

    private suspend fun saveBitmapFile(bitmap : Bitmap?) : String {
        var result = ""
        withContext(Dispatchers.IO) {
            if(bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val file = File(externalCacheDir?.absoluteFile.toString() + File.separator +
                            "BabyPaint_" + System.currentTimeMillis()/1000 + ".png")
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(bytes.toByteArray())
                    fileOutputStream.close()

                    result = file.absolutePath

                    runOnUiThread {
                        if(result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "File saved successfully $result", Toast.LENGTH_SHORT).show()
                            shareDrawing(result)
                        }
                        else {
                            Toast.makeText(this@MainActivity, "Error saving file", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e : Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        cancelProgressDialog()
        return result
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

    private fun cancelProgressDialog() {
        if(customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    private fun shareDrawing(result: String) {
        MediaScannerConnection.scanFile(this, arrayOf(result), null) {
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"

            startActivity(Intent.createChooser(shareIntent, "Share your drawing"))
        }
    }
}