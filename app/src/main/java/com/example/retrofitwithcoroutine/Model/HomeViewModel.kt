package com.example.retrofitwithcoroutine.Model

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {


    var _id: String? = null
    var author: String? = null
    var content: String? = null
    // Using a List to store multiple tags for a quote
    var tags: List<String>? = null
    var authorSlug: String? = null
    var length: Int? = null
    var dateAdded: String? = null
    var dateModified: String? = null
}