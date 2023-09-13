package online.soumya.contactsync.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import online.soumya.contactsync.ProfileActivity
import online.soumya.contactsync.databinding.ContactOneRowBinding
import online.soumya.contactsync.model.MainActivityModel

class MailActivityRecViewAdapter(var contactList:ArrayList<MainActivityModel>):RecyclerView.Adapter<MailActivityRecViewAdapter.ViewHolder>() {
    class ViewHolder(var binding:ContactOneRowBinding):RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = ContactOneRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       // TODO("Not yet implemented")
        Glide.with(holder.itemView.context).load(contactList[position].img).apply(
            RequestOptions.bitmapTransform(
                CircleCrop()
            )).into(holder.binding.imgProfileHome)
        holder.binding.txtName.text = contactList[position].name
        holder.binding.txtMobileNo.text = contactList[position].mobileNo

        holder.binding.cardOneRow.setOnClickListener {
            val intent = Intent(holder.itemView.context,ProfileActivity::class.java)
            intent.putExtra("mobileNo",contactList[position].mobileNo)
            holder.itemView.context.startActivity(intent)

        }
    }
}