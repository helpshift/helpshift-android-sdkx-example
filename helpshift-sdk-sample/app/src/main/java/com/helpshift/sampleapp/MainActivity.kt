package com.helpshift.sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.helpshift.Helpshift
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.show_conversation).setOnClickListener(this)
        findViewById<Button>(R.id.show_faqs).setOnClickListener(this)
        findViewById<Button>(R.id.show_single_faq).setOnClickListener(this)
        findViewById<Button>(R.id.show_faq_section).setOnClickListener(this)

        /*
        You can use other methods like for user login logout
        Helpshift.login(<map of user data>)
        Helpshift.logout()
        More apis are available on developer docs please check -
        https://developers.helpshift.com/sdkx_android/getting-started/
        */
    }


    //Create Helpshift config
    private fun getHelpshiftConfig(): Map<String, Any> {
        /*
        * Please check the available configurations
        * https://developers.helpshift.com/sdkx_android/sdk-configuration/
        * */
        val config = mutableMapOf<String, Any>()

        //Add tags
        config["tags"] = arrayOf("foo", "bar")

        //create cifs
        val userType = mutableMapOf<String, String>()
        userType["type"] = "sl"
        userType["value"] = "S10-conqueror"

        val isPro: MutableMap<String, String> = HashMap()
        isPro["type"] = "b"
        isPro["value"] = "true"


        val cifMap = mutableMapOf<String, Any>()
        cifMap["user_type"] = userType
        cifMap["is_pro"] = isPro
        //Add cifs here
        config["customIssueFields"] = cifMap

        return config
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                // call show conversation
                R.id.show_conversation -> Helpshift.showConversation(
                    this,
                    getHelpshiftConfig()
                )
                // call show faqs
                R.id.show_faqs -> Helpshift.showFAQs(this, getHelpshiftConfig())
                //call single faq with given id
                R.id.show_single_faq -> {
                    val id = findViewById<EditText>(R.id.faq_id_input).text.trim().toString()
                    if (id.isNotEmpty()) {
                        Helpshift.showSingleFAQ(this, id, getHelpshiftConfig())
                    } else {
                        Toast.makeText(this, "Enter faq id", Toast.LENGTH_LONG).show()
                    }
                }
                // call faq section with given id
                R.id.show_faq_section -> {
                    val id = findViewById<EditText>(R.id.section_id_input).text.trim().toString()
                    if (id.isNotEmpty()) {
                        Helpshift.showFAQSection(this, id, getHelpshiftConfig())
                    } else {
                        Toast.makeText(this, "Enter section id", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}