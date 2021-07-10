package com.example.orderfoodapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.models.NewsItem
import kotlinx.android.synthetic.main.news_item.view.*

class NewsAdapter(
    private val newsList: MutableList<NewsItem>
): RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_news,
                parent,
                false
            )
        )
    }

    fun addNews(news: NewsItem) {
        newsList.add(news)
        notifyItemInserted(newsList.size - 1)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val curNews = newsList[position]
        holder.itemView.apply {
            img_imageView.setImageResource(curNews.image)
            title_textView.text = curNews.title
            content_textView.text = curNews.content
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

}