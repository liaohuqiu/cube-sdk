package in.srain.cube.app.lifecycle;

public interface LifeCycleComponent {

    /**
     * back to UI
     */
    public void onRestart();

    /**
     * the UI is hidden partly
     */
    public void onPause();

    /**
     * back to UI after leave partly
     */
    public void onResume();

    /**
     * the whole UI is invisible
     */
    public void onStop();

    public void onDestroy();
}
