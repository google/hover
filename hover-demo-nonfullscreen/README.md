# Non-Fullscreen Hover Menu

Your Hover Menu does not have to take up the full height of the screen.  You can choose to make
your Hover Menu take up only the height needed by the content.  To achieve this, you need to set
the LayoutParams on the View returned by your NavigatorContent to use WRAP_CONTENT for its height.

For example:

```java
@Override
public View getView() {
    if (null == mContent) {
        mContent = LayoutInflater.from(mContext).inflate(R.layout.content_non_fullscreen, null);

        // We present our desire to be non-fullscreen by using WRAP_CONTENT for height.  This
        // preference will be honored by the Hover Menu to make our content only as tall as we
        // want to be.
        mContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    return mContent;
}
```