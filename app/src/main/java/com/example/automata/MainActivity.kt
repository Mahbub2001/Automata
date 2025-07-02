package com.example.automata

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.automata.auth.AuthManager
import com.example.automata.auth.UserSession
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var logoutButton: Button

    private lateinit var inputEditText: EditText
    private lateinit var outputTextView: TextView
    private lateinit var sendButton: ImageButton
    private lateinit var chatContainer: LinearLayout

    private val llmw = LLMW()
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private var firstMessageSent = false
    private var responseBuilder = StringBuilder()
    private var lastBotBubble: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Init layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        logoutButton = findViewById(R.id.logoutButton)
        chatContainer = findViewById(R.id.chat_container)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_dashboard -> showToast("Dashboard Selected")
                R.id.nav_events -> showToast("Events Selected")
                R.id.nav_settings -> showToast("Settings Selected")
                R.id.nav_help -> showToast("Help Selected")
            }
            drawerLayout.closeDrawers()
            true
        }

        AuthManager.init(this)
        UserSession.init(this)

        logoutButton.setOnClickListener {
            UserSession.clear()
            AuthManager.logout(this) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Chat views
        val chatBoxRoot = findViewById<LinearLayout>(R.id.chat_box_root)
        inputEditText = chatBoxRoot.findViewById(R.id.chatInput)
        outputTextView = chatBoxRoot.findViewById(R.id.outputTextView)
        sendButton = chatBoxRoot.findViewById(R.id.sendButton)

        inputEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                handlePrompt()
                true
            } else false
        }

        sendButton.setOnClickListener {
            handlePrompt()
        }

        // Disable chat initially
        enableChatInput(false)

        // Async model load
        mainScope.launch {
            try {
                val modelPath = withContext(Dispatchers.IO) { preparePath() }
                withContext(Dispatchers.IO) { llmw.load(modelPath) }

                showToast("Model Loaded Successfully")
                enableChatInput(true)
            } catch (e: Exception) {
                showToast("Model Load Error: ${e.message}")
            }
        }

        // Request permissions after small delay
        mainScope.launch {
            delay(500)
            PermissionHelper.requestAllPermissions(this@MainActivity)
        }
    }

    private fun handlePrompt() {
        val prompt = inputEditText.text.toString().trim()
        inputEditText.text.clear()

        if (prompt.isNotBlank()) {
            // Always open drawer now
            drawerLayout.openDrawer(GravityCompat.END)

            addChatMessage("You: $prompt", true)

            responseBuilder.clear()
            lastBotBubble = null

            llmw.send(prompt, object : LLMW.MessageHandler {
                override fun h(msg: String) {
                    runOnUiThread {
                        responseBuilder.append(msg)
                        outputTextView.text = responseBuilder.toString()

                        if (lastBotBubble == null) {
                            lastBotBubble = TextView(this@MainActivity).apply {
                                textSize = 16f
                                setPadding(24, 16, 24, 16)
                                setBackgroundResource(R.drawable.bubble_bot)
                            }
                            chatContainer.addView(lastBotBubble)
                        }
                        lastBotBubble?.text = "Bot: ${responseBuilder}"
                    }
                }
            })
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun addChatMessage(message: String, isUser: Boolean) {
        val textView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setBackgroundResource(if (isUser) R.drawable.bubble_user else R.drawable.bubble_bot)
        }
        chatContainer.addView(textView)
    }

    @Throws(IOException::class)
    private fun preparePath(): String {
        val inputStream = resources.openRawResource(R.raw.doa)
        val tempFile = File.createTempFile("model", ".gguf", cacheDir)
        val out: OutputStream = FileOutputStream(tempFile)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }

        inputStream.close()
        out.close()
        return tempFile.absolutePath
    }

    private fun enableChatInput(enabled: Boolean) {
        inputEditText.isEnabled = enabled
        sendButton.isEnabled = enabled
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}
