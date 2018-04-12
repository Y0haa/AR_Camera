package com.midea.ar_camera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


/**
 * Created by yoha on 3/5/18.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {


    ArrayList<PickerTile> pickerTiles;
    Context context;
    BottomItemPicker.Builder builder;
    OnItemClickListener onItemClickListener;
    ArrayList<Uri> selectedUriList;
    ArrayList<Integer> array_image = new ArrayList<Integer>();
    ArrayList<Boolean> selectedOrNot = new ArrayList<Boolean>();
    public static Camera camera = null;

    public GalleryAdapter(Context context, BottomItemPicker.Builder builder) {

        this.context = context;
        this.builder = builder;

        array_image.add(R.drawable.kitchen_view_min1t);
        array_image.add(R.drawable.rice_cooker_02);
        array_image.add(R.drawable.robot_vacuum_03);
        array_image.add(R.drawable.rice_cooker3d_04);


        selectedOrNot.add(false);
        selectedOrNot.add(false);
        selectedOrNot.add(false);
        selectedOrNot.add(false);


        pickerTiles = new ArrayList<>();
        selectedUriList = new ArrayList<>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }

        Cursor cursor = null;
        try {
            String[] columns;
            String orderBy;
            Uri uri;
            if (builder.mediaType == BottomItemPicker.Builder.MediaType.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Images.Media.DATA};
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
            } else {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Video.VideoColumns.DATA};
                orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC";
            }




            cursor = context.getApplicationContext().getContentResolver().query(uri, columns, null, null, orderBy);
            //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


            if (cursor != null) {

                int count = 0;
                while (cursor.moveToNext() && count < builder.previewMaxCount) {

                    String dataIndex;
                    if (builder.mediaType == BottomItemPicker.Builder.MediaType.IMAGE) {
                        dataIndex = MediaStore.Images.Media.DATA;
                    }else{
                        dataIndex = MediaStore.Video.VideoColumns.DATA;
                    }
                    String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    File imageFile = new File(imageLocation);
                    pickerTiles.add(new PickerTile(Uri.fromFile(imageFile)));
                    count++;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    public void setSelectedUriList(ArrayList<Uri> selectedUriList, Uri uri) {
        this.selectedUriList = selectedUriList;

        int position = -1;


        PickerTile pickerTile;
        for (int i = 0; i < pickerTiles.size(); i++) {
            pickerTile = pickerTiles.get(i);
            if (pickerTile.isImageTile() && pickerTile.getImageUri().equals(uri)) {
                position = i;
                break;
            }

        }


        if (position > 0) {
            notifyItemChanged(position);
        }


    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.bottomitempicker_grid_item, null);
        final GalleryViewHolder holder = new GalleryViewHolder(view);


        return holder;
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, final int position) {

//        PickerTile pickerTile = getItem(position);


        final boolean[] isSelected = {false};
//        boolean isSelected = false;
//
//        if (pickerTile.isCameraTile()) {
//            holder.iv_thumbnail.setImageResource( (Integer)array_image .get(position));
//            holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
//            holder.iv_thumbnail.setImageDrawable(builder.cameraTileDrawable);
//        } else if (pickerTile.isGalleryTile()) {
//
//            holder.iv_thumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
//            holder.iv_thumbnail.setImageDrawable(builder.galleryTileDrawable);
//            holder.iv_thumbnail.setImageResource( (Integer)array_image .get(position));
//
//        } else {
//            Uri uri = pickerTile.getImageUri();
//            if (builder.imageProvider == null) {
//                Glide.with(context)
//                        .load(uri)
//                        .thumbnail(0.1f)
//                        .dontAnimate()
//                        .centerCrop()
//                        .placeholder(R.drawable.ic_gallery)
//                        .error(R.drawable.img_error)
//                        .into(holder.iv_thumbnail);
//            } else {
//                builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
//            }
//
//
//            isSelected = selectedUriList.contains(uri);
//
//
//        }
//
//

        holder.iv_thumbnail.setImageResource(array_image.get(position));


        holder.itemView.setTag(position+"");

//        boolean value = selectedOrNot.get(position);
//
//        if(value){
//            selectedOrNot.set(position, false);
//            ((FrameLayout) holder.root).setBackgroundResource(R.drawable.gallery_photo_selected);
//
//
//        }else{
//            selectedOrNot.set(position, true);
//            ((FrameLayout) holder.root).setBackground(null);
//
//        }


        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    onItemClickListener.onItemClick(holder.itemView, array_image.get(position));


                    int pos  = Integer.parseInt(view.getTag().toString());

                    boolean value = selectedOrNot.get(pos);

                    if(value){
                        selectedOrNot.set(pos, false);
                        ((FrameLayout) holder.root).setBackgroundResource(R.color.background);

                    }else{
                        selectedOrNot.set(pos, true);
                        ((FrameLayout) holder.root).setBackgroundResource(R.drawable.gallery_photo_selected);

                    }



//                    selectedOrNot.set()
//
//
//                    if(isSelected[0] != true){
//                        final boolean[] isSelected = {true};
//                    if (holder.root instanceof FrameLayout) {
//
//                        Drawable foregroundDrawable;
//
//                        if (builder.selectedForegroundDrawable != null) {
//
//                            foregroundDrawable = builder.selectedForegroundDrawable;
//
//                        } else {
//
//                            foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected);
//
//                        }
//
//                        ((FrameLayout) holder.root).setForeground(isSelected[0] ? foregroundDrawable : null);
//                    }
//                  }
//                  else{
//                        if (isSelected[0] = true) {
//                            final boolean[] isSelected = {false};
//                            if (holder.root instanceof FrameLayout) {
//
//                                Drawable foregroundDrawable;
//
//                                if (builder.selectedForegroundDrawable != null) {
//
//                                    foregroundDrawable = builder.selectedForegroundDrawable;
//
//                                } else {
//
//                                    foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected);
//
//                                }
//
//                                ((FrameLayout) holder.root).setForeground(isSelected[0] ? foregroundDrawable : null);
//                            }
//                        }
//                        }
                }
            });
        }

    }


//    public PickerTile getItem(int position) {
//        return pickerTiles.get(position);
//    }

    @Override
    public int getItemCount() {
        return array_image.size();
    }

    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        protected final Uri imageUri;
        protected final
        @TileType
        int tileType;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        protected PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isGalleryTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }

        public boolean isImageTile() {
            return tileType == IMAGE;
        }

        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        public boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        @IntDef({IMAGE, CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        SquareFrameLayout root;




        SquareImageView iv_thumbnail;

        public GalleryViewHolder(View view) {
            super(view);
            root = (SquareFrameLayout) view.findViewById(R.id.root);
            iv_thumbnail = (SquareImageView) view.findViewById(R.id.iv_thumbnail);

        }

    }



}
