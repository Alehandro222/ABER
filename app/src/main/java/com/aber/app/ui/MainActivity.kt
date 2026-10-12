package com.aber.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.aber.app.R
import com.aber.app.data.PostRepository
import com.aber.app.data.PostType
import com.aber.app.data.UserStore
import com.aber.app.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout

/** Home screen: LOST / FOUND tabs, a text filter and a button that opens Image Search. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var store: UserStore
    private val adapter = PostAdapter()

    private var currentType = PostType.LOST
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = UserStore(this)

        // Not logged in (e.g. after logging out)? Go back to the login screen.
        val user = store.currentUser()
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = getString(R.string.hi_user, user.firstName)

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter

        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.lost))
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.found))
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = if (tab.position == 0) PostType.LOST else PostType.FOUND
                refresh()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty()
            refresh()
        }

        binding.fabImageSearch.setOnClickListener {
            startActivity(Intent(this, ImageSearchActivity::class.java))
        }

        refresh()
    }

    private fun refresh() {
        val shown = PostRepository.posts.filter { it.type == currentType && it.matches(query) }
        adapter.submit(shown)
        binding.tvEmpty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            store.logout()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
