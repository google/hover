# Non-Fullscreen Hover Menu

Your `HoverView` does not have to take up the full height of the screen.  You can choose to make
your `HoverView` take up only the height needed by the content.  To achieve this, you need to set
the LayoutParams on the View returned by your `Content` to use WRAP_CONTENT for its height. You also
need return true from your `Content`'s `isFullscreen()` method.

For example:

```java
// Within your Content implementation...
 
@NonNull
@Override
public View getView() {
    if (null == mContent) {
        mContent = LayoutInflater.from(mContext).inflate(R.layout.content_non_fullscreen, null);
  
        mContent.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }
    return mContent;
}
 
@Override
public boolean isFullscreen() {
    return false;
}
```