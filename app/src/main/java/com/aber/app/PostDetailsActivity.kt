package com.aber.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PostDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        val poster = intent.getStringExtra("poster") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val place = intent.getStringExtra("place") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val details = intent.getStringExtra("details") ?: ""
        val claim = intent.getStringExtra("claim") ?: ""
        val contact = intent.getStringExtra("contact") ?: ""
        val imageRes = intent.getIntExtra("imageRes", 0)

        findViewById<TextView>(R.id.tvPoster).text = "Posted by $poster"
        findViewById<TextView>(R.id.tvType).text = "Type: $type"
        findViewById<TextView>(R.id.tvPlace).text = "Place: $place"
        findViewById<TextView>(R.id.tvDate).text = "Date: $date"
        findViewById<TextView>(R.id.tvDetails).text = "Details: $details"
        findViewById<TextView>(R.id.tvClaim).text = "For claiming: $claim"
        if (imageRes != 0) findViewById<ImageView>(R.id.ivPhoto).setImageResource(imageRes)

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            val text = "ABER Lost & Found\nType: $type\nPlace: $place\nDate: $date\n" +
                    "Details: $details\nFor claiming: $claim"
            val send = Intent(Intent.ACTION_SEND).apply {
                this.type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(send, "Share post via"))
        }

        findViewById<Button>(R.id.btnCall).setOnClickListener {
            if (contact.isBlank()) {
                Toast.makeText(this, "No contact number for this post", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contact")))
            }
        }
    }
}