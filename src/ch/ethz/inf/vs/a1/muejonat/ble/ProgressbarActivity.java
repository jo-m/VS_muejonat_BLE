package ch.ethz.inf.vs.a1.muejonat.ble;

import com.android.mail.ui.ButteryProgressBar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

public class ProgressbarActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupProgressBar();
	}
	
	private ButteryProgressBar mProgress;
	
	public void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
	}
	
	public void hideProgress() {
		mProgress.setVisibility(View.INVISIBLE);
	}
	
	private void setupProgressBar() {
        // create new ProgressBar and style it
        mProgress = new ButteryProgressBar(this);
        mProgress.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
        mProgress.setVisibility(View.INVISIBLE);

        // retrieve the top view of our application
        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(mProgress);

        // Here we try to position the ProgressBar to the correct position by looking
        // at the position where content area starts. But during creating time, sizes 
        // of the components are not set yet, so we have to wait until the components
        // has been laid out
        // Also note that doing progressBar.setY(136) will not work, because of different
        // screen densities and different sizes of actionBar
        ViewTreeObserver observer = mProgress.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                mProgress.setY(contentView.getY() - 10);

                ViewTreeObserver observer = mProgress.getViewTreeObserver();
                observer.removeGlobalOnLayoutListener(this);
            }
        });
    }
}