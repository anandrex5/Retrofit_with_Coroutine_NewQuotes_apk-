package com.example.retrofitwithcoroutine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrofitwithcoroutine.Model.HomeViewModel
import com.example.retrofitwithcoroutine.Network.ApiClient
import com.example.retrofitwithcoroutine.Network.ApiInterface
import com.example.retrofitwithcoroutine.databinding.ActivityMainBinding
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Declaration of an ArrayList to store instances of HomeViewModel.
    lateinit var arrayList: ArrayList<HomeViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the arrayList.
        arrayList = ArrayList()

        // Call the retrofit function to start the API call.
        retrofit()
    }

    // This function is responsible for fetching data using Retrofit.
    private fun retrofit() {


        // Logging for debugging purposes.
        Log.d("Log2", "Making Retrofit call")

        //Launching Coroutines in the main thread
        CoroutineScope(Dispatchers.Main).launch {
            // Initialize the API interface using Retrofit.
            val apiInterface = ApiClient.client.create(ApiInterface::class.java)

            try {
                // Fetch the JSON object directly using await()
                val jsonResponse = apiInterface.getData().await()

                // No need for isSuccessful check since await() will throw an exception on failure.
                processJsonResponse(jsonResponse)

            } catch (e: Exception) {
                Log.e("MainActivity", "Error: ${e.message}")
            }
        }
    }

    // Check if the API call was successful.
    private fun processJsonResponse(jsonResponse: JsonObject) {
        val json = JSONObject(jsonResponse.toString())

        if (json.has("results")) {
            val resultsArray = json.getJSONArray("results")

            for (i in 0 until resultsArray.length()) {
                val model = HomeViewModel()
                val resultObject = resultsArray.getJSONObject(i)

                // Parse each field from the JSON object and store it in the model.
                model._id = resultObject.getString("_id")
                model.author = resultObject.getString("author")
                model.content = resultObject.getString("content")

                // Parsing tags which is an array in the JSON response.
                val tagsList = mutableListOf<String>()
                val tagsArray = resultObject.getJSONArray("tags")
                for (j in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(j))
                }
                model.tags = tagsList

                model.authorSlug = resultObject.getString("authorSlug")
                model.length = resultObject.getInt("length")
                model.dateAdded = resultObject.getString("dateAdded")
                model.dateModified = resultObject.getString("dateModified")

                // Add the populated model to the arrayList.
                arrayList.add(model)
            }

            buildRecycler()
        } else {
            Log.e("MainActivity", "The key 'results' is missing in the response.")
        }
    }

    // This function initializes the RecyclerView.
    private fun buildRecycler() {
        // Using view binding to get the RecyclerView instance.
        val recycler: RecyclerView = binding.recyclerView

        // Set the layout manager and adapter for the RecyclerView.
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = DataAdapter(arrayList)
    }

    // Adapter class for the RecyclerView to display each item.
    private class DataAdapter(var list: ArrayList<HomeViewModel>) :
        RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

        // ViewHolder class that holds the views for each item in the RecyclerView.
        class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView_id: TextView = itemView.findViewById(R.id._id)
            val textViewAuthor: TextView = itemView.findViewById(R.id.author)
            val textViewContent: TextView = itemView.findViewById(R.id.content)
            val textViewTags: TextView = itemView.findViewById(R.id.tags)
            val textViewAuthorSlug: TextView = itemView.findViewById(R.id.authorSlug)
            val textViewLength: TextView = itemView.findViewById(R.id.length)
            val textViewDateAdded: TextView = itemView.findViewById(R.id.dateAdded)
            val textViewDateModified: TextView = itemView.findViewById(R.id.dateModified)
        }

        // Inflate the item layout for each item.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.data_item, parent, false)
            return DataViewHolder(view)
        }

        // Bind the data to the views.
        override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
            holder.textView_id.text = list[position]._id.toString()
            holder.textViewAuthor.text = list[position].author
            holder.textViewContent.text = list[position].content
            holder.textViewTags.text = list[position].tags?.joinToString(", ")
            holder.textViewAuthorSlug.text = list[position].authorSlug
            holder.textViewLength.text = list[position].length.toString()
            holder.textViewDateAdded.text = list[position].dateAdded
            holder.textViewDateModified.text = list[position].dateModified
        }

        // Return the number of items in the list.
        override fun getItemCount(): Int {
            return list.size
        }
    }
}
