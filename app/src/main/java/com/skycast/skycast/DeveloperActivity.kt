package com.skycast.skycast

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.skycast.skycast.databinding.ActivityDeveloperBinding
import com.squareup.picasso.Picasso
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DeveloperActivity : AppCompatActivity() {

    // Enabling the Activity Binding
    private val binding: ActivityDeveloperBinding by lazy {
        ActivityDeveloperBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val webView: WebView = binding.aboutDeveloperContent

        // Disable scrolling in WebView
        webView.settings.apply {
            builtInZoomControls = false
            displayZoomControls = false
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }

        // Set padding and margin to zero to remove unwanted scrolling space
        webView.setPadding(0, 0, 0, 0)

        // Replace 'YOUR_TEXT_FILE_URL' with the actual URL of your text file
        val textFileUrl = "https://docs.google.com/document/d/1Zt-21OsvCH3CNAloEVBnVnrd4XRsqETaHWAPzSx-BIU/edit?usp=sharing"
        Picasso.get()
            .load("https://static.vecteezy.com/system/resources/previews/000/693/934/original/dark-blue-technology-and-high-tech-abstract-background-vector.jpg")
            .into(binding.backgroundImg)

        Picasso.get()
            .load("https://lh3.googleusercontent.com/pw/ABLVV87OS3zJnqmKn9TRdTybZqTTA-3otGBj3kav2GAmmzhcLV6UHvPmv51rC-145iz64OpiedPKEQevHFH0fib8s7X5mhDIPAIfjCk8CVZLiqMF_uZYdEDpmy16qTd6oyfCGfomZ9IBoadd8nPKTju7xeO7z292g_ynDMGBJZj9aWWhhVhh4SlHAxeNNNF0NWP0lqIWQVP2LuMiJclqtTj8t03gsapmgBNWWeqLjXGqzJU-GZoCqSYOzu0fhBNqwC9BNVkEhSG6LR3Sh-9xC4510dn1mHR6VAzU8XH_67BR78xmtl5rWMSvfMgNJ8-29GRO2HU6cRAGthxsNex46swjDgerEgrIJEZVT53LJU_tJKnmaykG16V1POA04NjO7LwUPVwlm1Y6Sq4I4jaL0jnRMzrg4jU1OFDVEWEvfNU3l-Rgh1dcXL_OaTM10XUhee-7slBQoJFJ2TDQToeJZAVs7QgS5sG8ih0RfpeCVkMQe_gIssDIkkgmnG7E4TO5LW7oXVzVGCPa3F5ZtEbevHuy_ScL2q9NyI86tt-4cqxP4YYx-G7FpSdmVCwySdrYQ5qfa-ezRPucsNZKssNywYHilqRRvVqGxPkkMZm6l7DdsLv688Th1MeVS-ObU_E0CoOcKPVXJE_2KXsqTBKbQi3Fk28CBjHEoo_ReV3ydz5k6HphXhmyuzdAaWaiHKawSOvWATLOmociQeNPPQlc_46_Ix5Onbc-ywh3ZrrdPDu3QkfkOb17h0I_Cziy1UQogc9AZjvzApdGCjFsq3Tj73InArlZbmE7ElK412jIMTRgo4pidx1VITVz_NMTTLgXFfWVqR0lrLDDKVNxzPBbn2AeYL40X3Kc07j1rnva8UAP5HjISXvgZR8GCdNJO6xIS1b9gA=w719-h958-s-no?authuser=0")
            .into(binding.developerImg)

        // Execute AsyncTask to fetch and display text content
        FetchTextFileTask(webView).execute(textFileUrl)

        binding.instagram.setOnClickListener{
            openInstagramProfile()
        }

        binding.github.setOnClickListener{
            openGithubProfile()
        }

        binding.linkedIn.setOnClickListener{
            openLinkedInProfile()
        }

        binding.emailBtn.setOnClickListener{
            composeEmail(arrayOf("vikash15112000@gmail.com"))
        }
    }
    // method to handle the working of email button
    @SuppressLint("QueryPermissionsNeeded")
    private fun composeEmail(addresses: Array<String>) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)


        // Set the intent to show the chooser only for email apps
        val chooserIntent = Intent.createChooser(intent, "Select Email App")
        if (chooserIntent.resolveActivity(packageManager) != null) {
            startActivity(chooserIntent)
        }
    }


    // Method to handle the working of the GitHub button
    @SuppressLint("QueryPermissionsNeeded")
    private fun openGithubProfile() {
        val githubUrl = "https://github.com/tiwarivk1511?tab=repositories"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))

        // Check if there is an app capable of handling the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    // Method to handle opening the LinkedIn profile in the default web browser
    @SuppressLint("QueryPermissionsNeeded")
    private fun openLinkedInProfile() {
        val linkedinUrl = "https://www.linkedin.com/in/your-linkedin-profile"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))

        // Check if there's an app capable of handling the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    // Method to handle opening the Instagram profile in the default web browser
    @SuppressLint("QueryPermissionsNeeded")
    private fun openInstagramProfile() {
        val instaUrl = "https://www.instagram.com/tiwari_vk1511/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instaUrl))

        // Check if there's an app capable of handling the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private class FetchTextFileTask(private val webView: WebView) :
        AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val url = URL(params[0])
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }

            bufferedReader.close()
            inputStream.close()
            connection.disconnect()

            return stringBuilder.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            // Load HTML content into WebView
            if (result != null) {
                webView.loadDataWithBaseURL(null, result, "text/html", "UTF-8", null)
            }
        }
    }
}
