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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

      

        // Initialize the API interface using Retrofit.
        val apiInterface = ApiClient.client.create(ApiInterface::class.java)

        // Define the call which will fetch the data.
        val call: Call<JsonObject> = apiInterface.getData()



        // Asynchronous call to fetch data.
        call.enqueue(object : Callback<JsonObject> {
            // This function is triggered when the API call gets a response.
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(
                    "Log3",
                    "Data is coming with response code: ${response.code()} and message: ${response.message()}"
                )

                // Check if the API call was successful.
                if (response.isSuccessful) {
                    // Convert the Retrofit response to a JSONObject.
                    val jsonResponse = JSONObject(response.body().toString())

                    // Check if the response contains the 'results' key.
                    if (jsonResponse.has("results")) {
                        // Fetch the array associated with the 'results' key.
                        val resultsArray = jsonResponse.getJSONArray("results")

                        // Iterate through each object in the array.
                        for (i in 0 until resultsArray.length()) {
                            // Initialize an instance of HomeViewModel to store the parsed data.
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

                        // After parsing all data, set it to the RecyclerView.
                        buildRecycler()
                    } else {
                        Log.e("MainActivity", "The key 'results' is missing in the response.")
                    }
                }
            }


            // This function is triggered when the API call fails.
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })
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
