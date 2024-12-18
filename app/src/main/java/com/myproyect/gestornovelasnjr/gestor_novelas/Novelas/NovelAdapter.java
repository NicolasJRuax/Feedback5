package com.myproyect.gestornovelasnjr.gestor_novelas.Novelas;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myproyect.gestornovelasnjr.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelHolder> {

    private List<Novel> novels = new ArrayList<>();
    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;
    private WeakReference<Context> contextRef;
    private FirebaseFirestore db;

    public interface OnItemClickListener {
        void onItemClick(Novel novel);
    }

    public NovelAdapter(Context context, OnDeleteClickListener deleteListener, OnItemClickListener itemClickListener) {
        this.contextRef = new WeakReference<>(context);
        this.deleteListener = deleteListener;
        this.itemClickListener = itemClickListener;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NovelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_novel, parent, false);
        return new NovelHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelHolder holder, int position) {
        Novel currentNovel = novels.get(position);
        holder.textViewTitle.setText(currentNovel.getTitle());
        holder.textViewAuthor.setText(currentNovel.getAuthor());

        // Actualizar icono de favorito
        holder.buttonFavorite.setImageResource(currentNovel.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentNovel);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            deleteListener.onDeleteClick(currentNovel);
        });

        holder.buttonFavorite.setOnClickListener(v -> {
            if (currentNovel.getId() == null) {
                Log.e("Firestore", "ID de la novela es nulo; no se puede actualizar el estado de favorito.");
                Context context1 = contextRef.get();
                if (context1 != null) {
                    Toast.makeText(context1, "Error: ID de novela es nulo", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            boolean isFavorite = !currentNovel.isFavorite();
            currentNovel.setFavorite(isFavorite);
            updateFavoriteStatus(currentNovel);

            Context context1 = contextRef.get();
            if (context1 != null) {
                Toast.makeText(context1, currentNovel.getTitle() + (isFavorite ? " añadido a favoritos" : " eliminado de favoritos"), Toast.LENGTH_SHORT).show();
            }
            holder.buttonFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        });

        holder.buttonDetails.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentNovel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return novels.size();
    }

    public void setNovels(List<Novel> novels) {
        this.novels = novels;
        notifyDataSetChanged();
    }

    // Agrega este método para obtener la lista de novelas
    public List<Novel> getNovels() {
        return novels;
    }

    private void updateFavoriteStatus(Novel novel) {
        if (novel.getId() != null) {
            db.collection("novels").document(novel.getId())
                    .update("favorite", novel.isFavorite())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado de favorito actualizado correctamente en Firestore."))
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error al actualizar el estado de favorito", e);
                        Context context = contextRef.get();
                        if (context != null) {
                            Toast.makeText(context, "Error al actualizar favorito", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("Firestore", "No se puede actualizar el estado de favorito: ID de novela es nulo");
            Context context = contextRef.get();
            if (context != null) {
                Toast.makeText(context, "No se pudo actualizar el estado de favorito", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class NovelHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewAuthor;
        private ImageButton buttonDelete;
        private ImageButton buttonFavorite;
        private ImageButton buttonDetails;

        public NovelHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonFavorite = itemView.findViewById(R.id.buttonFavorite);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);

            buttonDelete.setImageResource(android.R.drawable.ic_menu_delete);
            buttonDetails.setImageResource(android.R.drawable.ic_menu_info_details);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Novel novel);
    }
}
