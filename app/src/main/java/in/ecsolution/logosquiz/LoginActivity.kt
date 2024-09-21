package `in`.ecsolution.logosquiz

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var username: TextInputEditText
    private lateinit var mobile: TextInputEditText
    private lateinit var listDiocese: AutoCompleteTextView
    private lateinit var listCategory: AutoCompleteTextView
    private var usernameText: String = ""
    private var mobileText: String = ""
    private var diocese: String = ""
    private var category: String = ""
    private lateinit var btnExit: ImageButton
    private lateinit var overlayLogin: RelativeLayout
    private lateinit var quizDbHelper: QuizDbHelper
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //check if user is already logged in
        val sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        val loginStatus = sharedPreferences.getString("login_status", "false")
        if (loginStatus == "true") {
            login()
        }
        //else show login screen
        else{
            enableEdgeToEdge()
            setContentView(R.layout.activity_login)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            // Initialize views
            btnLogin = findViewById(R.id.loginBtn)
            username = findViewById(R.id.userNameTxt)
            mobile = findViewById(R.id.userMobileTxt)
            listDiocese = findViewById(R.id.listDioceseTxt)
            listCategory = findViewById(R.id.listCategoryTxt)
            btnExit = findViewById(R.id.icon_exit)

            username.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(username.windowToken, 0)
                    listDiocese.requestFocus() // Move to the next field
                    true
                } else {
                    false
                }
            }

            listDiocese.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    listCategory.requestFocus() // Move to the next field
                    true
                } else {
                    false
                }
            }

            listCategory.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mobile.requestFocus() // Move to the next field
                    true
                } else {
                    false
                }
            }

            mobile.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(username.windowToken, 0)
                    btnLogin.performClick() // Trigger the login button
                    true
                } else {
                    false
                }
            }
            listDiocese.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (v as AutoCompleteTextView).showDropDown()
                }
            }

            listCategory.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (v as AutoCompleteTextView).showDropDown()
                }
            }



            // Set click listener for login button
            btnLogin.setOnClickListener {
                getLoginData()
                if (validateLogin()) {
                    overlayLogin = findViewById(R.id.overlay)
                    overlayLogin.visibility = View.VISIBLE
                    if (this.isInternetAvailable()) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val dataUpdater = DataUpdater(this@LoginActivity)

                            // Execute updates sequentially
                            val questionsUpdateSuccess = dataUpdater.updateQuestions()
                            val booksUpdateSuccess = dataUpdater.updateBooks()
                            val syllabusUpdateSuccess = dataUpdater.updateSyllabus()

                            val allSuccessful =questionsUpdateSuccess && booksUpdateSuccess && syllabusUpdateSuccess

                            withContext(Dispatchers.Main) {
                                if (allSuccessful) {
                                    // Run the one-time code only after all updates have succeeded
                                    if (!dataUpdater.isOneTimeCodeExecuted()) {
                                        dataUpdater.runOneTimeCode()
                                        dataUpdater.setOneTimeCodeExecuted()
                                    }

                                    // Proceed with the login
                                    saveLoginData()
                                    login()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Download Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    overlayLogin.visibility = View.GONE
                                    quizDbHelper = QuizDbHelper.getInstance(this@LoginActivity)
                                    quizDbHelper.resetTables()
                                }
                            }
                        }
                    }
                    else{
                        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            btnExit.setOnClickListener {
                val builder = AlertDialog.Builder(this@LoginActivity)
                builder.setMessage("Are you sure you want to exit the App?")
                    .setTitle("Confirmation")
                    .setPositiveButton("Yes") { _, _ ->
                        MusicManager.stopMusic()//stopMusic()
                        finish()
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Do nothing here if the user cancels
                    }
                builder.create().show()
            }
        }
    }
    private fun login() {
        val intent = Intent(this, HomePageActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun getLoginData() {
        usernameText = username.text.toString().trim()
        mobileText = mobile.text.toString().trim()
        diocese = listDiocese.text.toString().trim()
        category = listCategory.text.toString().trim()
    }
    private fun validateLogin():Boolean {
        getLoginData()
        if (usernameText.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (mobileText.isEmpty()||mobileText.length!=10) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (diocese.isEmpty()) {
            Toast.makeText(this, "Please select your Diocese", Toast.LENGTH_SHORT).show()
            return false
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select your category", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun saveLoginData() {
        val sharedPreferences = getSharedPreferences("login_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", usernameText)
        editor.putString("mobile", mobileText)
        editor.putString("diocese", diocese)
        editor.putString("category", category)
        editor.putString("dailyQuizPlayedDate","")
        editor.putString("dailyQuizDownloadDate",SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        editor.putString("login_status", "true")
        editor.apply()
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
                it.clearFocus()
            }
        }
        return super.onTouchEvent(event)
    }
}