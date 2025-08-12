
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.R // R.drawable.image_placeholder 등을 위함
import com.example.onenthapp.databinding.ItemImageSliderBinding // 아이템 레이아웃 바인딩

class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size

    inner class ImageViewHolder(private val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Glide.with(binding.ivSliderImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder_bg) // 적절한 플레이스홀더 이미지로 교체
                .error(R.drawable.image_placeholder_bg) // 적절한 에러 이미지로 교체
                .into(binding.ivSliderImage)
        }
    }
}